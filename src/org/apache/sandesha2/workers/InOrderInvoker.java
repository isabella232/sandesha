/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *  
 */

package org.apache.sandesha2.workers;

import java.util.ArrayList;
import java.util.Iterator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.sandesha2.Sandesha2Constants;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.TerminateManager;
import org.apache.sandesha2.Sandesha2Constants.ClientAPI;
import org.apache.sandesha2.storage.StorageManager;
import org.apache.sandesha2.storage.beanmanagers.NextMsgBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.InvokerBeanMgr;
import org.apache.sandesha2.storage.beans.NextMsgBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.storage.beans.InvokerBean;
import org.apache.sandesha2.util.MsgInitializer;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.wsrm.Sequence;

/**
 * This is used when InOrder invocation is required. This is a seperated Thread that keep running
 * all the time. At each iteration it checks the InvokerTable to find weather there are any messages to
 * me invoked.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 */

public class InOrderInvoker extends Thread {
	boolean invokerStarted = false;

	ConfigurationContext context = null;

	public synchronized void stopInvoker() {
		invokerStarted = false;
	}

	public synchronized boolean isInvokerStarted() {
		return invokerStarted;
	}

	public void setConfugurationContext(ConfigurationContext context) {
		this.context = context;
	}

	public void start(ConfigurationContext context) {
		invokerStarted = true;
		this.context = context;
		super.start();
	}

	public void run() {

		while (isInvokerStarted()) {

			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				System.out.println("Invoker was Inturrepted....");
				ex.printStackTrace();
				System.out.println("End printing Interrupt...");
			}

			try {

				StorageManager storageManager = SandeshaUtil
						.getSandeshaStorageManager(context);
				NextMsgBeanMgr nextMsgMgr = storageManager.getNextMsgBeanMgr();

				InvokerBeanMgr storageMapMgr = storageManager
						.getStorageMapBeanMgr();

				SequencePropertyBeanMgr sequencePropMgr = storageManager
						.getSequencePropretyBeanMgr();

				//Getting the incomingSequenceIdList
				SequencePropertyBean sequencePropertyBean = (SequencePropertyBean) sequencePropMgr
						.retrieve(
								Sandesha2Constants.SequenceProperties.ALL_SEQUENCES,
								Sandesha2Constants.SequenceProperties.INCOMING_SEQUENCE_LIST);
				if (sequencePropertyBean == null)
					continue;

				ArrayList seqPropList = (ArrayList) sequencePropertyBean
						.getValue();
				Iterator seqPropIt = seqPropList.iterator();

				currentIteration: while (seqPropIt.hasNext()) {

					String sequenceId = (String) seqPropIt.next();

					NextMsgBean nextMsgBean = nextMsgMgr.retrieve(sequenceId);
					if (nextMsgBean == null)
						throw new SandeshaException(
								"Next message not set correctly");

					long nextMsgno = nextMsgBean.getNextMsgNoToProcess();
					if (nextMsgno <= 0)
						throw new SandeshaException(
								"Invalid messaage number for the nextMsgNo");

					Iterator stMapIt = storageMapMgr.find(
							new InvokerBean(null, nextMsgno, sequenceId))
							.iterator();

					while (stMapIt.hasNext()) {

						InvokerBean stMapBean = (InvokerBean) stMapIt
								.next();
						String key = stMapBean.getKey();

						MessageContext msgToInvoke = SandeshaUtil
								.getStoredMessageContext(key);

						RMMsgContext rmMsg = MsgInitializer
								.initializeMessage(msgToInvoke);
						Sequence seq = (Sequence) rmMsg
								.getMessagePart(Sandesha2Constants.MessageParts.SEQUENCE);
						long msgNo = seq.getMessageNumber().getMessageNumber();

						try {
							//Invoking the message.
							new AxisEngine(msgToInvoke.getSystemContext())
									.receive(msgToInvoke);

							ServiceContext serviceContext = msgToInvoke
									.getServiceContext();
							Object debug = null;
							if (serviceContext != null) {
								debug = serviceContext
										.getProperty(ClientAPI.SANDESHA_DEBUG_MODE);
								if (debug != null && "on".equals(debug)) {
									System.out
											.println("DEBUG: Invoker invoking a '"
													+ SandeshaUtil
															.getMessageTypeString(rmMsg
																	.getMessageType())
													+ "' message.");
								}
							}

							//deleting the message entry.
							storageMapMgr.delete(key);

						} catch (AxisFault e) {
							throw new SandeshaException(e.getMessage());
						}

						//undating the next mst to invoke
						nextMsgno++;
						stMapIt = storageMapMgr
								.find(
										new InvokerBean(null, nextMsgno,
												sequenceId)).iterator();

						//terminate (AfterInvocation)
						if (rmMsg.getMessageType() == Sandesha2Constants.MessageTypes.APPLICATION) {
							Sequence sequence = (Sequence) rmMsg
									.getMessagePart(Sandesha2Constants.MessageParts.SEQUENCE);
							if (sequence.getLastMessage() != null) {
								TerminateManager.terminateAfterInvocation(
										context, sequenceId);
								
								//exit from current iteration. (since an entry was removed)
								break currentIteration;
							}
						}
					}

					nextMsgBean.setNextMsgNoToProcess(nextMsgno);
					nextMsgMgr.update(nextMsgBean);

				}
			} catch (SandeshaException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
}
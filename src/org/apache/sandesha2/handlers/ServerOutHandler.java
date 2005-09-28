/*
 * Copyright  1999-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.sandesha2.handlers;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clientapi.InOutMEPClient;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.log4j.lf5.viewer.configure.MRUFileManager;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.MsgInitializer;
import org.apache.sandesha2.MsgValidator;
import org.apache.sandesha2.SOAPAbstractFactory;
import org.apache.sandesha2.SandeshaException;
import org.apache.sandesha2.RMMsgContext;
import org.apache.sandesha2.RMMsgCreator;
import org.apache.sandesha2.msgreceivers.RMMessageReceiver;
import org.apache.sandesha2.storage.AbstractBeanMgrFactory;
import org.apache.sandesha2.storage.beanmanagers.RetransmitterBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beanmanagers.StorageMapBeanMgr;
import org.apache.sandesha2.storage.beans.RetransmitterBean;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;
import org.apache.sandesha2.storage.beans.StorageMapBean;
import org.apache.sandesha2.util.SandeshaUtil;
import org.apache.sandesha2.wsrm.Sequence;
import org.apache.wsdl.MessageReference;
import org.apache.wsdl.WSDLConstants;

/**
 * @author Sanka
 * @author Chamikara
 * @author Jaliya
 */
public class ServerOutHandler extends AbstractHandler {

	public void invoke(MessageContext msgCtx) throws AxisFault {
		//log
		System.out.println("In server OutHandler");

		//Strating the sender.
		ConfigurationContext ctx = msgCtx.getSystemContext();
		SandeshaUtil.startSenderIfStopped(ctx);

		
		
		
		

		
		//getting rm message
		RMMsgContext rmMsgCtx;
		try {
			rmMsgCtx = MsgInitializer.initializeMessage(msgCtx);
		} catch (SandeshaException ex) {
			throw new AxisFault("Cant initialize the message");
		}

		//getting the request message and rmMessage.
		MessageContext reqMsgCtx = msgCtx.getOperationContext()
				.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN);
		RMMsgContext requestRMMsgCtx;
		try {
			requestRMMsgCtx = MsgInitializer.initializeMessage(reqMsgCtx);
			if (rmMsgCtx.getMessageType() == Constants.MESSAGE_TYPE_UNKNOWN) {

				System.out.println("GOT Possible Response Message");
				AbstractContext context = rmMsgCtx.getContext();
				if (context == null)
					throw new SandeshaException("Context is null");

				Sequence sequence = (Sequence) requestRMMsgCtx
						.getMessagePart(Constants.MESSAGE_PART_SEQUENCE);
				if (sequence == null)
					throw new SandeshaException("Sequence part is null");

				String incomingSeqId = sequence.getIdentifier().getIdentifier();
				if (incomingSeqId == null || incomingSeqId == "")
					throw new SandeshaException("Invalid seqence Id");

				SequencePropertyBeanMgr seqPropMgr = AbstractBeanMgrFactory
						.getInstance(context).getSequencePropretyBeanMgr();
				SequencePropertyBean acksToBean = seqPropMgr.retrieve(
						incomingSeqId, Constants.SEQ_PROPERTY_ACKS_TO_EPR);
				if (acksToBean == null
						|| acksToBean.getValue() == null
						|| !(acksToBean.getValue() instanceof EndpointReference))
					throw new SandeshaException("Acksto is not set correctly");

				EndpointReference acksToEPR = (EndpointReference) acksToBean
						.getValue();
				if (acksToEPR.getAddress() == null
						|| acksToEPR.getAddress() == "")
					throw new SandeshaException("AcksTo not set correctly");

				SOAPEnvelope env = rmMsgCtx.getSOAPEnvelope();
				if (env == null) {
					SOAPEnvelope envelope = SOAPAbstractFactory.getSOAPFactory(
							Constants.DEFAULT_SOAP_VERSION)
							.getDefaultEnvelope();
					rmMsgCtx.setSOAPEnvelop(envelope);
				}
				SOAPBody soapBody = rmMsgCtx.getSOAPEnvelope().getBody();
				if (soapBody == null)
					throw new SandeshaException(
							"Invalid SOAP message. Body is not present");

				boolean validResponse = false;
				if (soapBody.getChildElements().hasNext())
					validResponse = true;

				if (!validResponse) { //TODO either change MsgReceiver or move
					if (Constants.WSA.NS_URI_ANONYMOUS.equals(acksToEPR
							.getAddress())) {
						Sequence reqSequence = (Sequence) requestRMMsgCtx
								.getMessagePart(Constants.MESSAGE_PART_SEQUENCE);
						if (reqSequence == null)
							throw new SandeshaException(
									"Sequence part of application message is null");

						String sequenceId = reqSequence.getIdentifier()
								.getIdentifier();

						RMMsgCreator.addAckMessage(rmMsgCtx, sequenceId);
					}
				} else {
					//valid response

					RMMsgContext ackRMMsgContext = RMMsgCreator
							.createAckMessage(requestRMMsgCtx);
					MessageContext ackMsgContext = ackRMMsgContext
							.getMessageContext();
					ackMsgContext.setServiceGroupContext(msgCtx
							.getServiceGroupContext());
					ackMsgContext.setServiceGroupContextId(msgCtx
							.getServiceGroupContextId());
					ackMsgContext.setServiceContext(msgCtx.getServiceContext());
					ackMsgContext.setServiceContextID(msgCtx
							.getServiceContextID());
					
					//TODO set a suitable operation description
					OperationContext ackOpContext = new OperationContext(
							reqMsgCtx.getOperationDescription());
					
					ackOpContext.addMessageContext(ackMsgContext);
					ackMsgContext.setOperationContext(ackOpContext);
					RMMsgContext newRMMsgCtx = SandeshaUtil.deepCopy(rmMsgCtx);
					MessageContext newMsgCtx = newRMMsgCtx.getMessageContext();
					rmMsgCtx.setSOAPEnvelop(ackRMMsgContext.getSOAPEnvelope());

					//setting contexts
					newMsgCtx.setServiceGroupContext(msgCtx
							.getServiceGroupContext());
					newMsgCtx.setServiceGroupContextId(msgCtx
							.getServiceGroupContextId());
					newMsgCtx.setServiceContext(msgCtx.getServiceContext());
					newMsgCtx.setServiceContextID(msgCtx.getServiceContextID());
					OperationContext newOpContext = new OperationContext(
							newMsgCtx.getOperationDescription());
					newOpContext.addMessageContext(newMsgCtx);
					newMsgCtx.setOperationContext(newOpContext);

					//processing the response
					processResponseMessage(newRMMsgCtx, requestRMMsgCtx);
					//msgCtx.setTo(null);
					msgCtx.setPausedTrue(getName());
					

				}
			}
		} catch (SandeshaException e) {
			throw new AxisFault(e.getMessage());
		}
		
		// serializing
		try {
			SOAPEnvelope env11 = msgCtx.getEnvelope();
			XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
			env11.serialize(writer);
		} catch (XMLStreamException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryConfigurationError e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private void processResponseMessage(RMMsgContext rmMsg, RMMsgContext reqRMMsg)
			throws SandeshaException {
		if (rmMsg == null || reqRMMsg == null)
			throw new SandeshaException("Message or reques message is null");

		Sequence sequence = (Sequence) reqRMMsg
				.getMessagePart(Constants.MESSAGE_PART_SEQUENCE);
		if (sequence == null)
			throw new SandeshaException("Sequence part is null");

		String incomingSeqId = sequence.getIdentifier().getIdentifier();
		if (incomingSeqId == null || incomingSeqId == "")
			throw new SandeshaException("Invalid seqence Id");

		AbstractContext context = rmMsg.getContext();
		if (context == null)
			throw new SandeshaException("Context is null");

		SequencePropertyBeanMgr mgr = AbstractBeanMgrFactory.getInstance(
				context).getSequencePropretyBeanMgr();

		SequencePropertyBean toBean = mgr.retrieve(incomingSeqId,
				Constants.SEQ_PROPERTY_TO_EPR);
		SequencePropertyBean replyToBean = mgr.retrieve(incomingSeqId,
				Constants.SEQ_PROPERTY_REPLY_TO_EPR);
		SequencePropertyBean outSequenceBean = mgr.retrieve(incomingSeqId,
				Constants.SEQ_PROPERTY_OUT_SEQUENCE_ID);

		if (toBean == null)
			throw new SandeshaException("To is null");
		if (replyToBean == null)
			throw new SandeshaException("Replyto is null");

		EndpointReference incomingTo = (EndpointReference) toBean.getValue();
		EndpointReference incomingReplyTo = (EndpointReference) replyToBean
				.getValue();

		if (incomingTo == null || incomingTo.getAddress() == null
				|| incomingTo.getAddress() == "")
			throw new SandeshaException("To Property has an invalid value");
		if (incomingReplyTo == null || incomingReplyTo.getAddress() == null
				|| incomingReplyTo.getAddress() == "")
			throw new SandeshaException("ReplyTo is not set correctly");

		rmMsg.setTo(incomingReplyTo);
		rmMsg.setReplyTo(incomingTo);

		if (outSequenceBean == null || outSequenceBean.getValue() == null) {
			RetransmitterBeanMgr retransmitterMgr = AbstractBeanMgrFactory
					.getInstance(context).getRetransmitterBeanMgr();

			rmMsg.getMessageContext().setPausedTrue(getName());

			RetransmitterBean appMsgEntry = new RetransmitterBean();
			
			RMMsgContext copiedRMMsgCtx = SandeshaUtil.deepCopy(rmMsg);
			MessageContext copiedMsgCtx = copiedRMMsgCtx.getMessageContext();
			MessageContext msg = rmMsg.getMessageContext();
			//msg.setResponseWritten(false);
			//msg.setOutPutWritten(true);
			//msg.getOperationContext().setProperty(org.apache.axis2.Constants.RESPONSE_WRITTEN,org.apache.axis2.Constants.VALUE_TRUE);

			
			Object val = msg.getOperationContext().getProperty (org.apache.axis2.Constants.RESPONSE_WRITTEN);
			copiedMsgCtx.setServiceGroupContext(msg
					.getServiceGroupContext());
			copiedMsgCtx.setServiceGroupContextId(msg
					.getServiceGroupContextId());
			copiedMsgCtx.setServiceContext(msg
					.getServiceContext());
			copiedMsgCtx.setServiceContextID(msg
					.getServiceContextID());
			copiedMsgCtx.setOperationContext(msg.getOperationContext());
			
			//TODO IF - may not work when op context is changed
			try {
				msg.getOperationContext().addMessageContext(copiedMsgCtx);
			} catch (AxisFault e) {
				e.printStackTrace();
			}
			
			String key = SandeshaUtil.storeMessageContext(rmMsg
					.getMessageContext());
			appMsgEntry.setKey(key);
			appMsgEntry.setLastSentTime(0);
			appMsgEntry.setTempSequenceId(incomingSeqId);
			appMsgEntry.setSend(false);
			appMsgEntry.setMessageId(rmMsg.getMessageId());

			retransmitterMgr.insert(appMsgEntry);

			addCreateSequenceMessage(rmMsg);
			


			
		} else {
			//Sequence id is present
			//set sequence part
			//add message to retransmitter table with send=true;
		}

	}

	public void addCreateSequenceMessage(RMMsgContext applicationRMMsg)
			throws SandeshaException {
		MessageContext applicationMsg = applicationRMMsg.getMessageContext();
		if (applicationMsg == null)
			throw new SandeshaException("Message context is null");
		RMMsgContext createSeqRMMessage = RMMsgCreator
				.createCreateSeqMsg(applicationRMMsg);
		MessageContext createSeqMsg = createSeqRMMessage.getMessageContext();
		AbstractContext context = applicationRMMsg.getContext();
		if (context == null)
			throw new SandeshaException("Context is null");

		StorageMapBeanMgr storageMapMgr = AbstractBeanMgrFactory.getInstance(
				context).getStorageMapBeanMgr();
		RetransmitterBeanMgr retransmitterMgr = AbstractBeanMgrFactory
				.getInstance(context).getRetransmitterBeanMgr();
		String key = SandeshaUtil.storeMessageContext(createSeqRMMessage
				.getMessageContext());
		RetransmitterBean createSeqEntry = new RetransmitterBean();
		createSeqEntry.setKey(key);
		createSeqEntry.setLastSentTime(0);
		createSeqEntry.setMessageId(createSeqRMMessage.getMessageId());
		createSeqEntry.setSend(true);
		retransmitterMgr.insert(createSeqEntry);
	}

	public QName getName() {
		return new QName(Constants.OUT_HANDLER_NAME);
	}
}
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
package org.apache.sandesha2.wsrm;

import javax.xml.namespace.QName;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.soap.SOAPHeader;
import org.apache.axis2.soap.SOAPHeaderBlock;
import org.apache.sandesha2.Sandesha2Constants;

/**
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 * @author Saminda Abeyruwan  <saminda@opensource.lk>
 */

public class Sequence implements IOMRMPart {

	private OMElement sequenceElement;

	private Identifier identifier;

	private MessageNumber messageNumber;

	private LastMessage lastMessage = null;

	private SOAPFactory factory;
	
	OMNamespace seqNoNamespace = null;

	public Sequence(SOAPFactory factory) {
		this.factory = factory;
		seqNoNamespace = factory.createOMNamespace(
				Sandesha2Constants.WSRM.NS_URI_RM, Sandesha2Constants.WSRM.NS_PREFIX_RM);
		sequenceElement = factory.createOMElement(
				Sandesha2Constants.WSRM.SEQUENCE, seqNoNamespace);
	}

	public OMElement getOMElement() throws OMException {
		return sequenceElement;
	}

	public Object fromOMElement(OMElement headerElement) throws OMException {

		SOAPHeader header = (SOAPHeader) headerElement;
		if (header == null)
			throw new OMException(
					"Sequence element cannot be added to non-header element");

		OMElement sequencePart = sequenceElement = headerElement
				.getFirstChildWithName(new QName(Sandesha2Constants.WSRM.NS_URI_RM,
						Sandesha2Constants.WSRM.SEQUENCE));
		if (sequencePart == null)
			throw new OMException(
					"Cannot find Sequence element in the given element");

		sequenceElement = factory.createOMElement(
				Sandesha2Constants.WSRM.SEQUENCE, seqNoNamespace);

		identifier = new Identifier(factory);
		messageNumber = new MessageNumber(factory);
		identifier.fromOMElement(sequencePart);
		messageNumber.fromOMElement(sequencePart);

		OMElement lastMessageElement = sequencePart
				.getFirstChildWithName(new QName(Sandesha2Constants.WSRM.NS_URI_RM,
						Sandesha2Constants.WSRM.LAST_MSG));

		if (lastMessageElement != null) {
			lastMessage = new LastMessage(factory);
			lastMessage.fromOMElement(sequencePart);
		}

		return this;
	}

	public OMElement toOMElement(OMElement headerElement) throws OMException {

		if (headerElement == null || !(headerElement instanceof SOAPHeader))
			throw new OMException(
					"Cant add Sequence Part to a non-header element");

		SOAPHeader soapHeader = (SOAPHeader) headerElement;
		if (soapHeader == null)
			throw new OMException(
					"cant add the sequence part to a non-header element");
		if (sequenceElement == null)
			throw new OMException(
					"cant add Sequence Part since Sequence is null");
		if (identifier == null)
			throw new OMException(
					"Cant add Sequence part since identifier is null");
		if (messageNumber == null)
			throw new OMException(
					"Cant add Sequence part since MessageNumber is null");


		SOAPHeaderBlock sequenceHeaderBlock = soapHeader.addHeaderBlock(
				Sandesha2Constants.WSRM.SEQUENCE, seqNoNamespace);
		sequenceHeaderBlock.setMustUnderstand(true);
		identifier.toOMElement(sequenceHeaderBlock);
		messageNumber.toOMElement(sequenceHeaderBlock);
		if (lastMessage != null)
			lastMessage.toOMElement(sequenceHeaderBlock);


		//resetting the element. So that subsequest toOMElement calls will
		// attach a different object.
		this.sequenceElement = factory.createOMElement(
				Sandesha2Constants.WSRM.SEQUENCE, seqNoNamespace);

		return headerElement;
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	public LastMessage getLastMessage() {
		return lastMessage;
	}

	public MessageNumber getMessageNumber() {
		return messageNumber;
	}

	public void setIdentifier(Identifier identifier) {
		this.identifier = identifier;
	}

	public void setLastMessage(LastMessage lastMessage) {
		this.lastMessage = lastMessage;
	}

	public void setMessageNumber(MessageNumber messageNumber) {
		this.messageNumber = messageNumber;
	}

	public void toSOAPEnvelope(SOAPEnvelope envelope) {
		SOAPHeader header = envelope.getHeader();
		
		//detach if already exist.
		OMElement elem = header.getFirstChildWithName(new QName(Sandesha2Constants.WSRM.NS_URI_RM,
				Sandesha2Constants.WSRM.SEQUENCE));
		if (elem!=null)
			elem.detach();
		
		toOMElement(header);
	}

}
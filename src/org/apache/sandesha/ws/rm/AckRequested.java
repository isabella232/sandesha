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

package org.apache.sandesha.ws.rm;

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.sandesha.ws.utility.Identifier;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;
import java.util.Iterator;

/**
 * class AckRequested
 * 
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 */
public class AckRequested extends MessageElement implements IRmElement {

    /**
     * Field ackRequested
     */
    private MessageElement ackRequested;

    /**
     * Field identifier
     */
    private Identifier identifier;

    /**
     * Field messageNumber
     */
    private MessageNumber messageNumber;

    /**
     * Constructor AckRequested
     */
    public AckRequested() {

        ackRequested = new MessageElement();

        ackRequested.setName("wsrm:AckRequested");
    }

    /*
     * (non-Javadoc)
     * @see org.apache.sandesha.ws.rm.IRmElement#getSoapElement()
     */

    /**
     * Method getSoapElement
     * 
     * @return 
     * @throws SOAPException 
     */
    public MessageElement getSoapElement() throws SOAPException {

        ackRequested.addChildElement(identifier.getSoapElement());
        ackRequested.addChildElement(messageNumber.getSoapElement());

        return ackRequested;
    }

    /**
     * Method toSoapEnvelop
     * 
     * @param envelop 
     * @return 
     * @throws SOAPException 
     */
    public SOAPEnvelope toSoapEnvelop(SOAPEnvelope envelop)
            throws SOAPException {

        SOAPEnvelope env = envelop;

        /*
         * ackRequested.addChildElement(identifier.getSoapElement());
         * ackRequested.addChildElement(messageNumber.getSoapElement());
         *   env.addHeader((SOAPHeaderElement)ackRequested);
         */
        if (env.getHeader() == null) {
            env.addHeader();
        }

        Name name =
                env.createName("", "wsrm",
                        "http://schemas.xmlsoap.org/ws/2004/03/rm");
        SOAPHeaderElement headerElement =
                (SOAPHeaderElement) env.getHeader().addHeaderElement(name);

        // .setActor(null);
        headerElement.setActor(null);
        headerElement.setName("AckRequested");

        if (identifier != null) {
            identifier.toSOAPEnvelope(headerElement);
        }

        if (messageNumber != null) {
            messageNumber.toSOAPEnvelope(headerElement);
        }

        return env;
    }

    /**
     * Method fromSOAPEnveploe
     * 
     * @param headerElement 
     * @return 
     */
    public AckRequested fromSOAPEnveploe(SOAPHeaderElement headerElement) {

        Iterator iterator = headerElement.getChildElements();
        MessageElement childElement;

        while (iterator.hasNext()) {
            childElement = (MessageElement) iterator.next();

            if (childElement.getName().equals("wsu:Identifier")) {
                identifier = new Identifier();

                identifier.fromSOAPEnvelope(childElement);
            }

            if (childElement.getName().equals("Identifier")) {
                identifier = new Identifier();

                identifier.fromSOAPEnvelope(childElement);
            }

            if (childElement.getName().equals("wsrm:MessageaNumber")) {
                messageNumber = new MessageNumber();

                messageNumber.fromSOAPEnvelope(childElement);
            }

            if (childElement.getName().equals("wsrm:MessageaNumber")) {
                messageNumber = new MessageNumber();

                messageNumber.fromSOAPEnvelope(childElement);
            }
        }

        return this;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.sandesha.ws.rm.IRmElement#addChildElement(org.apache.axis.message.MessageElement)
     */

    /**
     * Method addChildElement
     * 
     * @param element 
     * @throws SOAPException 
     */
    public void addChildElement(MessageElement element) throws SOAPException {
        ackRequested.addChildElement(element);
    }

    /**
     * @return 
     */
    public Identifier getIdentifier() {
        return identifier;
    }

    /**
     * @return 
     */
    public MessageNumber getMessageNumber() {
        return messageNumber;
    }

    /**
     * @param identifier 
     */
    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    /**
     * @param number 
     */
    public void setMessageNumber(MessageNumber number) {
        messageNumber = number;
    }
}

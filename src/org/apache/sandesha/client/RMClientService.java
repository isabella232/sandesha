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
package org.apache.sandesha.client;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.addressing.Action;
import org.apache.axis.message.addressing.Address;
import org.apache.axis.message.addressing.From;
import org.apache.axis.message.addressing.MessageID;
import org.apache.axis.message.addressing.To;
import org.apache.axis.types.URI;
import org.apache.sandesha.Constants;
import org.apache.sandesha.RMMessage;
import org.apache.sandesha.RMSequence;
import org.apache.sandesha.ws.rm.AckRequested;
import org.apache.sandesha.ws.rm.AcknowledgementRange;
import org.apache.sandesha.ws.rm.LastMessage;
import org.apache.sandesha.ws.rm.MessageNumber;
import org.apache.sandesha.ws.rm.Sequence;
import org.apache.sandesha.ws.rm.SequenceAcknowledgement;
import org.apache.sandesha.ws.utility.Identifier;
import org.apache.commons.logging.Log;

import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeaderElement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * class RMClientService
 * 
 * @author Amila Navarathna
 * @author Jaliya Ekanayaka
 * @author Sudar Nimalan
 */
public class RMClientService {
    /**
     * Field log
     */
    protected static Log log = LogFactory.getLog(RMClientService.class.getName());

    /**
     * Field retransmissinInterval
     */
    private long retransmissionInterval;

    /**
     * Field retransmissionCount
     */
    private int retransmissionCount;

    /**
     * Constructor RMClientService
     */
    public RMClientService() {
        retransmissionInterval = Constants.RETRANSMISSION_INTERVAL;
    }

    /**
     * Method clientMethod
     * 
     * @param reqSOAPEnvelop     
     * @param sequenceID         
     * @param destinationURL     
     * @param toClientServiceURL 
     * @param isOneWay           
     * @param isLastMessage      
     * @param isCreateSequence   
     * @param isResponseExpected 
     * @return 
     */
    public String clientMethod(String reqSOAPEnvelop, String sequenceID,
                               String destinationURL,
                               String toClientServiceURL, String isOneWay,
                               String isLastMessage, String isCreateSequence,
                               String isResponseExpected) throws AxisFault {

        // create a Identifier object from the sequenceID
        Identifier identifier = new Identifier();

        identifier.setIdentifier(sequenceID);

        // create the message
        Message message = new Message(reqSOAPEnvelop);
        RMMessage rmMessage = new RMMessage(message);

        rmMessage.setDestinationURL(destinationURL);
        rmMessage.setToClientServiceURL(toClientServiceURL);
        rmMessage.setIsOneWay(isOneWay);
        rmMessage.setIsCreateSequence(isCreateSequence);
        rmMessage.setIsResponseExpected(isResponseExpected);
        rmMessage.setIdentifier(identifier);

        // get the singleton instance
        ClientMessageController controller =
                ClientMessageController.getInstance();
        RMSequence sequence =
                controller.retrieveIfSequenceExists(identifier);

        if (sequence == null) {    // means there is no sequence
            sequence = new RMSequence(identifier);

            // add this message to the sequence
            // sequence.getNextMessageNo();
            sequence.insertClientMessage(rmMessage);
            controller.storeSequence(sequence);
        } else {                   // means that there exists a sequence for this identifier
            sequence.insertClientMessage(rmMessage);
        }

        String stringReturn = null;

        if (isCreateSequence.compareTo("true") == 0) {
            if (isOneWay.compareTo("true") == 0) {
                if (isResponseExpected.compareTo("true") == 0) {

                    // need to wait for to return.
                    // call. invoke and handle the exception when terminating the HTTP
                    // put the message in to the singleton.
                } else {

                    // error no way for client to be contacted.
                }
            } else {

                // Call.invoke a but with anonymous url, Same HTTP:\
                // Return when we get the thing.
                // put the message in to the singleton and let the retransmission to happen.
            }
        } else {
            if (isOneWay.compareTo("true") == 0) {
                if (isResponseExpected.compareTo("true") == 0) {

                    // need to wait for to return.
                    // call. invoke and handle the exception when terminating the HTTP
                    // put to the singleton and then wait
                } else {

                    // call. invoke and handle the exception when terminating the HTTP
                    // can return.  wait for ack no return.
                }
            } else {
                if (isResponseExpected.compareTo("true") == 0) {

                    // need to wait for to return. ack will come with the return
                    // put to the singleton and then wait
                } else {

                    // System.out.println(isOneWay);
                    // ystem.out.println(    "isOneWay.compareTo(true)"+ isOneWay.compareTo("true"));
                    try {

                        // Crate amessage using the reqSOAPEnvelop string parameter.
                        Message msg = new Message(reqSOAPEnvelop);

                        // Get the envelop using the message.
                        SOAPEnvelope requestEnvelop = msg.getSOAPEnvelope();
                        SOAPEnvelope envelopToSend = new SOAPEnvelope();

                        envelopToSend.setSchemaVersion(requestEnvelop.getSchemaVersion());
                        envelopToSend.setSoapConstants(requestEnvelop.getSOAPConstants());
                        envelopToSend.setBody((org.apache.axis.message.SOAPBody) requestEnvelop.getBody());
                        envelopToSend.addNamespaceDeclaration(Constants.NS_PREFIX_RM, Constants.NS_URI_RM);
                        envelopToSend.addNamespaceDeclaration(org.apache.axis.message.addressing.Constants.NS_PREFIX_ADDRESSING,
                                org.apache.axis.message.addressing.Constants.NS_URI_ADDRESSING);
                        envelopToSend.addNamespaceDeclaration(Constants.WSU_PREFIX,
                                Constants.WSU_NS);

                        // New envelop to create the SOAP envelop to send. Why use of two envelop is not clear.
                        // adding the name spaces to the env
                        // now get the sequence element
                        Sequence seqElement = new Sequence();

                        seqElement.setIdentifier(identifier);

                        MessageNumber msgNumber = new MessageNumber();

                        msgNumber.setMessageNumber(rmMessage.getMessageNumber());

                        if (isLastMessage.equals("true")) {
                            LastMessage lastMessage = new LastMessage();

                            seqElement.setLastMessage(lastMessage);
                        }

                        seqElement.setMessageNumber(msgNumber);

                        // add the sequence element to the envelop to send
                        seqElement.toSoapEnvelop(envelopToSend);

                        // set the action
                        URI actionURI = new URI("urn:wsrm:Ping");
                        Action action = new Action(actionURI);

                        action.toSOAPHeaderElement(envelopToSend);

                        // Set from address.
                        // System.out.println(toClientServiceURL);
                        URI fromAddressURI = new URI(toClientServiceURL);
                        Address fromAddress = new Address(fromAddressURI);

                        // Set the from header.
                        From from = new From(fromAddress);

                        from.toSOAPHeaderElement(envelopToSend);

                        UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();
                        URI messageIDURI = new URI("uuid:"
                                + uuidGen.nextUUID());
                        MessageID messageID = new MessageID(messageIDURI);

                        messageID.toSOAPHeaderElement(envelopToSend);

                        // Set the to address.
                        // System.out.println(destinationURL);
                        URI toAddress = new To(destinationURL);
                        To to = new To(toAddress);

                        to.toSOAPHeaderElement(envelopToSend);

                        // now store this new message in the rmMessage
                        // so that it can be used for retrasmission
                        Message newMessage = new Message(envelopToSend);

                        rmMessage.setRequestMessage(newMessage);

                        // Invoke the expected service.
                        Service service = new Service();
                        Call call = (Call) service.createCall();

                        call.setTargetEndpointAddress(destinationURL);

                        // System.out.println("just before invoke 00000");
                        // System.out.println();
                        // System.out.println(
                        // "@@@@@@@@@@@@@@@ BeforeEnvoking from ClientService @@@@@@@@@@@@@@@@@@@@@@@@@");
                        // System.out.println(envelopToSend.toString());
                        // System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                        // System.out.println();
                        // invoke for the first time
                        try {
                            call.invoke(envelopToSend);

                            // System.out.println("the retransmisssion 55555555555" + rmMessage.getIdentifier().toString());
                        } catch (Exception e) {
                            System.out.println("The exception after invokeWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW");
                            e.printStackTrace();
                        }

                        boolean gotResponce = false;
                        int count = 0;
                        Message tempMessage =
                                rmMessage.getRequestMessage();
                        AckRequested ackRequested = new AckRequested();

                        ackRequested.setIdentifier(rmMessage.getIdentifier());

                        SOAPEnvelope retransmissionEnvelop =
                                tempMessage.getSOAPEnvelope();

                        // System.out.println(   "tempMessage.getSOAPEnvelope()" + retransmissionEnvelop.toString());
                        System.out.println(ackRequested.getIdentifier().toString());
                        ackRequested.toSoapEnvelop(retransmissionEnvelop);

                        // System.out.println(retransmissionEnvelop.toString());
                        while (count < Constants.MAXIMUM_RETRANSMISSION_COUNT) {
                            count++;

                            System.out.println("Retransmission ................................................>> "
                                    + count);
                            System.out.println();
                            Thread.sleep(2000);

                            if (!rmMessage.isAcknowledged()) {
                                Message retransmissionMessage =
                                        new Message(retransmissionEnvelop);
                                Service retransmissionService = new Service();
                                Call retransmissionCall =
                                        (Call) service.createCall();

                                retransmissionCall.setTargetEndpointAddress(destinationURL);

                                try {
                                    retransmissionCall.invoke(envelopToSend);

                                    // System.out.println("invoked");
                                } catch (Exception e) {

                                    // Not handle let finlly to handle it.
                                }

                                continue;

                                // /retransmete
                            }

                            if (new Boolean(rmMessage.getIsResponseExpected()).booleanValue()) {
                                if (rmMessage.getResponseMessage() != null) {
                                    gotResponce = true;

                                    try {
                                        stringReturn =
                                                rmMessage.getResponseMessage().getSOAPPartAsString();
                                    } catch (AxisFault e2) {
                                        log.error(e2);
                                    }

                                    break;
                                }
                            } else {
                                break;
                            }
                        }

                        if (!gotResponce) {
                            try {
                                SOAPEnvelope env = new SOAPEnvelope();

                                stringReturn = env.getAsString();

                                System.out.println("GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG");
                                System.out.println(stringReturn);
                            } catch (Exception e1) {
                                log.error(e1);
                            }
                        }

                        // /System.out.println(stringReturn);
                        // Not handle let finlly to handle it.
                    } catch (ServiceException e) {
                        log.error(e);
                        throw AxisFault.makeFault(e);
                    } catch (SOAPException e) {
                        log.error(e);
                        throw AxisFault.makeFault(e);
                    } catch (Exception e) {
                        // If it is comming to this location then there will be an severe error than the
                        // HTTP termination.
                        log.error(e);
                        throw AxisFault.makeFault(e);
                    }
                }
            }
        }

        System.out.println("Before returning no exception no exception no exception ...");

        return stringReturn;
    }

    /**
     * Method ackMethod
     * 
     * @param sequenceIdentifier 
     * @param messageNumber      
     */
    public void ackMethod(String sequenceIdentifier, String messageNumber) {

        // TODO: check the returned string fot the ack range and other elements
        // Check for sequenceId
        // put the ack in to the singleton and return.

        /*
         * Identifier identifier = new Identifier();
         * identifier.setIdentifier(sequenceIdentifier);
         *
         * //  get the singleton instance
         * ClientMessageController controller =
         * ClientMessageController.getInstance();
         *
         * RMSequence sequence = controller.retrieveIfSequenceExists(identifier);
         * RMMessage message = sequence.retrieveMessage(new Long(messageNumber));
         *
         * if (message != null) {
         * message.setAcknowledged(true);
         * sequence.insertClientMessage(message);
         * }
         */
    }

    /**
     * set the acknowledged messages as acknowledged=true and
     * put them back in the data structure
     * 
     * @param identifier 
     * @param message    
     * @throws AxisFault 
     */
    private void setAckedMessages(Identifier identifier, Message message)
            throws AxisFault {

        List ackRangeList = new ArrayList();
        SOAPHeaderElement header = null;
        SOAPEnvelope envelop = message.getSOAPEnvelope();
        Vector headers = envelop.getHeaders();
        Iterator ite = headers.iterator();

        while (ite.hasNext()) {
            header = (SOAPHeaderElement) ite.next();

            if (header.getLocalName() == "SequenceAcknowledgement") {
                break;
            }
        }

        if (header != null) {
            Iterator childIte = header.getChildElements();

            while (childIte.hasNext()) {
                MessageElement element = (MessageElement) childIte.next();

                if (element != null) {
                    if (element.getLocalName() == "AcknowledgementRange") {
                        String upper =
                                element.getAttributeValue("Upper");
                        String lower =
                                element.getAttributeValue("Lower");
                        AcknowledgementRange ackRange =
                                new AcknowledgementRange();

                        ackRange.setMaxValue(new Long(upper).longValue());
                        ackRange.setMinValue(new Long(lower).longValue());
                        ackRangeList.add(ackRange);
                    }
                }
            }
        }

        // now set that ackRangeList in the SequenceAcknowledgement
        SequenceAcknowledgement seqAck = new SequenceAcknowledgement();

        seqAck.setAckRanges(ackRangeList);

        // now add this SequenceAcknowledgement to the RMSequence
        // get the singleton instance
        ClientMessageController controller =
                ClientMessageController.getInstance();
        RMSequence sequence =
                controller.retrieveIfSequenceExists(identifier);

        if (sequence != null) {
            sequence.setSeqAck(seqAck);

            // now actual update goes
            sequence.updateAckedMessages(seqAck);
        }
    }
}

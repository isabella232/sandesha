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
import org.apache.axis.client.Call;
import org.apache.sandesha.Constants;
import org.apache.sandesha.RMMessageContext;
import org.apache.sandesha.util.PropertyLoader;

import javax.xml.namespace.QName;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Jaliya
 */
public class ClientPropertyValidator {

    public static RMMessageContext validate(Call call) throws AxisFault {

        RMMessageContext rmMessageContext = null;

        boolean inOut = getInOut(call);
        long msgNumber = getMessageNumber(call);
        boolean lastMessage = getLastMessage(call);
        boolean sync = getSync(call);
        String action = getAction(call);
        String sourceURL = null;
        String from = getFrom(call);
        String replyTo = getReplyTo(call);
        String acksTo=getAcksTo(call);

        try {
            if (from == null)
                sourceURL = getSourceURL(call);
            else
                sourceURL = from;
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            throw new AxisFault(e.getMessage());
        }

        String errorMsg = getValidated(msgNumber, action, replyTo, sync, inOut);
        if (errorMsg == null) {
            rmMessageContext = new RMMessageContext();

            rmMessageContext.setSync(sync);
            rmMessageContext.setHasResponse(inOut);
            rmMessageContext.setMsgNumber(msgNumber);
            rmMessageContext.setLastMessage(lastMessage);
            rmMessageContext.setSourceURL(sourceURL);
            rmMessageContext.setSequenceID(action);
            rmMessageContext.setReplyTo(replyTo);
            rmMessageContext.setFrom(from);
            rmMessageContext.setAction(action);
            rmMessageContext.setAcksTo(acksTo);
            return rmMessageContext;

        } else
            throw new AxisFault(errorMsg);

    }

    /**
     * This will decide whether we have an IN-OUT style service request
     * or IN-ONLY service request by checking the value of the QName
     * returned by the call.getReturnType().
     *
     * @param call
     * @return
     */
    private static boolean getInOut(Call call) {
        QName returnQName = (QName) call.getReturnType();
        if (returnQName != null)
            return true;
        else
            return false;

    }


    private static String getAction(Call call) {
        String action = (String) call.getProperty(Constants.ClientProperties.ACTION);
        if (action != null)
            return action;
        else
            return null;
     }

     private static String getAcksTo(Call call) {
        String acksTo = (String) call.getProperty(Constants.ClientProperties.ACKS_TO);
        if (acksTo != null)
            return acksTo;
        else
            return null;
     }

    private static boolean getSync(Call call) {
        Boolean synchronous = (Boolean) call.getProperty(Constants.ClientProperties.SYNC);
        if (synchronous != null) {
            return synchronous.booleanValue();
        } else
            return true;//If the user has not specified the synchronous
    }


    private static String getSourceURL(Call call) throws UnknownHostException {
        String sourceURI = null;
        InetAddress addr = InetAddress.getLocalHost();

        sourceURI = Constants.HTTP + Constants.COLON + Constants.SLASH + Constants.SLASH +
                addr.getHostAddress() + Constants.COLON + PropertyLoader.getClientSideListenerPort()
                + Constants.URL_RM_SERVICE;

        return sourceURI;
    }

    /**
     * @param call
     * @return
     */
    private static long getMessageNumber(Call call) {
        Object temp = call.getProperty(Constants.ClientProperties.MSG_NUMBER);
        long msgNumber = 0;
        if (temp != null)
            msgNumber = ((Long) temp).longValue();
        return msgNumber;
    }

    private static boolean getLastMessage(Call call) {
        Boolean lastMessage = (Boolean) call.getProperty(Constants.ClientProperties.LAST_MESSAGE);
        if (lastMessage != null)
            return lastMessage.booleanValue();
        else
            return false;

    }

    private static String getValidated(long msgNumber, String action, String replyTo, boolean sync, boolean inOut) {
        String errorMsg = null;

        if (sync && inOut && replyTo == null) {
            errorMsg = Constants.ErrorMessages.CLIENT_PROPERTY_VALIDATION_ERROR;
            return errorMsg;
        }

        if ((msgNumber == 0) || (action == null)) {
            errorMsg = Constants.ErrorMessages.MESSAGE_NUMBER_NOT_SPECIFIED;
            return errorMsg;
        }
        return errorMsg;
    }


    private static String getFrom(Call call) {
        return (String) call.getProperty(Constants.ClientProperties.FROM);

    }

    private static String getReplyTo(Call call) {
        return (String) call.getProperty(Constants.ClientProperties.REPLY_TO);

    }

}
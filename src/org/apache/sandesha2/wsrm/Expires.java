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
import org.apache.axis2.soap.SOAPFactory;
import org.apache.sandesha2.Sandesha2Constants;

/**
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 * @author Saminda Abeyruwan  <saminda@opensource.lk>
 */

public class Expires implements IOMRMElement {

	SOAPFactory factory;
	
	OMNamespace rmNamespace = null;

	OMElement expiresElement = null;

	String duration = null;

	public Expires(SOAPFactory factory) {
		this.factory = factory;
		rmNamespace = factory.createOMNamespace(
				Sandesha2Constants.WSRM.NS_URI_RM, Sandesha2Constants.WSRM.NS_PREFIX_RM);
		expiresElement = factory.createOMElement(
				Sandesha2Constants.WSRM.EXPIRES, rmNamespace);
	}

	public Object fromOMElement(OMElement element) throws OMException {
		OMElement expiresPart = element.getFirstChildWithName(new QName(
				Sandesha2Constants.WSRM.NS_URI_RM, Sandesha2Constants.WSRM.EXPIRES));
		if (expiresPart == null)
			throw new OMException(
					"Passed elemenet does not have a Expires part");
		String expiresText = expiresPart.getText();
		if (expiresText == null || expiresText == "")
			throw new OMException("The duration value is not valid");

		expiresElement = factory.createOMElement(
				Sandesha2Constants.WSRM.EXPIRES, rmNamespace);

		duration = expiresText;
		return element;
	}

	public OMElement getOMElement() throws OMException {
		// TODO Auto-generated method stub
		return expiresElement;
	}

	public OMElement toOMElement(OMElement element) throws OMException {
		if (expiresElement == null)
			throw new OMException("Cant set Expires. It is null");
		if (duration == null || duration == "")
			throw new OMException(
					"Cant set Expires. The duration value is not set");

		expiresElement.setText(duration);
		element.addChild(expiresElement);

		expiresElement = factory.createOMElement(
				Sandesha2Constants.WSRM.EXPIRES, rmNamespace);

		return element;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public OMElement getExpiresElement() {
		return expiresElement;
	}

	public void setExpiresElement(OMElement expiresElement) {
		this.expiresElement = expiresElement;
	}
}
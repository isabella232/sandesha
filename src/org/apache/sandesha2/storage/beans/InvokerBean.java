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

package org.apache.sandesha2.storage.beans;

/**
 * This bean is used at the receiving side (of both server and client)
 * There is one object for each application message to be invoked.
 * 
 * @author Chamikara Jayalath <chamikaramj@gmail.com>
 * @author Sanka Samaranayaka <ssanka@gmail.com>
 */

public class InvokerBean implements RMBean {

	/**
	 * Comment for <code>Key</code>
	 * 
	 * This is the key that is obtained after saving a message context in a storage.
	 */
	private String Key;

	/**
	 * Comment for <code>MsgNo</code>
	 * The message number of the message.
	 */
	private long MsgNo;

	/**
	 * Comment for <code>sequenceId</code>
	 * The sequence ID of the sequence the message belong to.
	 */
	private String sequenceId;
	
	/**
	 * Comment for <code>invoked</code>
	 * Weather the message has been invoked by the invoker.
	 */
	private boolean invoked = false;

	public InvokerBean() {

	}

	public InvokerBean(String key, long msgNo, String sequenceId) {
		this.Key = key;
		this.MsgNo = msgNo;
		this.sequenceId = sequenceId;
	}

	/**
	 * @return Returns the key.
	 */
	public String getKey() {
		return Key;
	}

	/**
	 * @param key
	 *            The key to set.
	 */
	public void setKey(String key) {
		Key = key;
	}

	/**
	 * @return Returns the msgNo.
	 */
	public long getMsgNo() {
		return MsgNo;
	}

	/**
	 * @param msgNo
	 *            The msgNo to set.
	 */
	public void setMsgNo(long msgNo) {
		MsgNo = msgNo;
	}

	/**
	 * @return Returns the sequenceId.
	 */
	public String getSequenceId() {
		return sequenceId;
	}

	/**
	 * @param sequenceId
	 *            The sequenceId to set.
	 */
	public void setSequenceId(String sequenceId) {
		this.sequenceId = sequenceId;
	}
	
	public boolean isInvoked() {
		return invoked;
	}
	
	public void setInvoked(boolean invoked) {
		this.invoked = invoked;
	}
}
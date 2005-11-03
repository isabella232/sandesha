/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.apache.sandesha2.storage.inmemory;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.axis2.context.AbstractContext;
import org.apache.sandesha2.Constants;
import org.apache.sandesha2.storage.beanmanagers.SequencePropertyBeanMgr;
import org.apache.sandesha2.storage.beans.SequencePropertyBean;

/**
 * @author Chamikara Jayalath <chamikara@wso2.com>
 * @author Sanka Samaranayake <ssanka@gmail.com>
 */
public class InMemorySequencePropertyBeanMgr implements SequencePropertyBeanMgr {
	private Hashtable table = null;

	/**
	 *  
	 */
	public InMemorySequencePropertyBeanMgr(AbstractContext context) {
		Object obj = context.getProperty(Constants.BeanMAPs.SEQUENCE_PROPERTY);
		if (obj != null) {
			table = (Hashtable) obj;
		} else {
			table = new Hashtable();
			context.setProperty(Constants.BeanMAPs.SEQUENCE_PROPERTY, table);
		}
	}

	public boolean delete(String sequenceId, String name) {
		return table.remove(sequenceId + ":" + name) != null;
	}

	public SequencePropertyBean retrieve(String sequenceId, String name) {
		return (SequencePropertyBean) table.get(sequenceId + ":" + name);
	}

	public boolean insert(SequencePropertyBean bean) {
		table.put(bean.getSequenceId() + ":" + bean.getName(), bean);
		return true;
	}

	public ResultSet find(String query) {
		throw new UnsupportedOperationException("selectRS() is not supported");
	}

	public Collection find(SequencePropertyBean bean) {
		ArrayList beans = new ArrayList();

		if (bean == null)
			return beans;

		Iterator iterator = table.values().iterator();
		SequencePropertyBean temp;

		while (iterator.hasNext()) {
			temp = (SequencePropertyBean) iterator.next();

			//			if ((bean.getSequenceId() != null && bean.getSequenceId().equals(
			//					temp.getSequenceId()))
			//					&& (bean.getName() != null && bean.getName().equals(
			//							temp.getName()))
			//					&& (bean.getValue() != null && bean.getValue().equals(
			//							temp.getValue()))) {
			//
			//				beans.add(temp);
			//			}

			boolean equal = true;

			if (bean.getSequenceId() != null
					&& !bean.getSequenceId().equals(temp.getSequenceId()))
				equal = false;

			if (bean.getName() != null
					&& !bean.getName().equals(temp.getName()))
				equal = false;

			if (bean.getValue() != null
					&& !bean.getValue().equals(temp.getValue()))
				equal = false;

			if (equal)
				beans.add(temp);

		}
		return beans;
	}

	public boolean update(SequencePropertyBean bean) {
		if (!table.contains(bean))
			return false;

		return table.put(getId(bean), bean) != null;

	}

	private String getId(SequencePropertyBean bean) {
		return bean.getSequenceId() + ":" + bean.getName();
	}

}
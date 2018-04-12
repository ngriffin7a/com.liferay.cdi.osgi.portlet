/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.cdi.osgi.portlet.internal;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

/**
 * @author Neil Griffin
 */
public class DescriptorSupportedEvent {

	public DescriptorSupportedEvent() {
	}

	public DescriptorSupportedEvent(BeanApp beanApp, String name) {
		_beanApp = beanApp;
		_name = name;
	}

	public QName getQName() {

		if ((_qName == null) && (_name != null)) {

			if (_beanApp != null) {
				return new QName(_beanApp.getDefaultNamespace(), _name);
			}

			return new QName(XMLConstants.NULL_NS_URI, _name);
		}

		return _qName;
	}

	public void setQName(QName qName) {
		_qName = qName;
	}

	private BeanApp _beanApp;
	private String _name;
	private QName _qName;
}

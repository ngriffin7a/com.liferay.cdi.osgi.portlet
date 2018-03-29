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

import javax.xml.namespace.QName;

/**
 * @author Neil Griffin
 */
public abstract class EventDefinitionBase implements EventDefinition {

	@Override
	public QName getQName() {
		return _qName;
	}

	@Override
	public String getValueType() {
		return _valueType;
	}

	@Override
	public void setQName(QName qName) {
		_qName = qName;
	}

	@Override
	public void setValueType(String valueType) {
		_valueType = valueType;
	}

	private QName _qName;
	private String _valueType;
}

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

package com.liferay.bean.portlet.extension.internal;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

/**
 * @author Neil Griffin
 */
public class EventDefinitionDescriptorImpl extends EventDefinitionBase {

	public EventDefinitionDescriptorImpl(BeanApp beanApp) {
		_beanApp = beanApp;
		_aliasQNames = new ArrayList<>();
	}

	@Override
	public List<QName> getAliasQNames() {
		return _aliasQNames;
	}

	@Override
	public QName getQName() {

		QName qName = super.getQName();

		if ((qName == null) && (_name != null)) {
			return new QName(_beanApp.getDefaultNamespace(), _name);
		}

		return qName;
	}

	@Override
	public void setName(String name) {
		_name = name;
	}

	private List<QName> _aliasQNames;
	private BeanApp _beanApp;
	private String _name;
}

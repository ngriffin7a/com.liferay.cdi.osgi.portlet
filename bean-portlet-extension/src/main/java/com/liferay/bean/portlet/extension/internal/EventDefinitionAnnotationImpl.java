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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.portlet.annotations.EventDefinition;
import javax.portlet.annotations.PortletQName;

import javax.xml.namespace.QName;

/**
 * @author Neil Griffin
 */
public class EventDefinitionAnnotationImpl extends EventDefinitionBase {

	public EventDefinitionAnnotationImpl(EventDefinition eventDefinition) {
		this(eventDefinition.qname());
		_aliasQNames = Arrays.stream(eventDefinition.alias())
			.map(
					portletQName ->
						QNameFactory.create(
							portletQName.namespaceURI(),
							portletQName.localPart()))
				.collect(Collectors.toList());
		setValueType(eventDefinition.payloadType()
			.getName());
	}

	public EventDefinitionAnnotationImpl(PortletQName portletQName) {
		setQName(
			new QName(portletQName.namespaceURI(), portletQName.localPart()));
	}

	@Override
	public List<QName> getAliasQNames() {
		return _aliasQNames;
	}

	@Override
	public void setName(String name) {

		// The @EventDefinition annotation does not have the name feature that
		// is available in portlet.xml.
		throw new UnsupportedOperationException();
	}

	private List<QName> _aliasQNames;
}

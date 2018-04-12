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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.annotations.PortletApplication;

import javax.xml.XMLConstants;

/**
 * @author Neil Griffin
 */
public class BeanAppAnnotationImpl extends BeanAppBase {

	public BeanAppAnnotationImpl(PortletApplication portletApplication) {

		if (portletApplication == null) {
			setDefaultNamespace(XMLConstants.NULL_NS_URI);
			_eventDefinitions = Collections.emptyList();
			_publicRenderParamMap = Collections.emptyMap();
		}
		else {
			setDefaultNamespace(portletApplication.defaultNamespaceURI());
			_eventDefinitions = new ArrayList<>();
			Arrays.stream(portletApplication.events())
				.map(
						eventDefinition ->
							EventDefinitionFactory.create(eventDefinition))
				.forEach(
					eventDefinition -> _eventDefinitions.add(eventDefinition));
			_publicRenderParamMap = new HashMap<>();
			Arrays.stream(portletApplication.publicParams())
				.map(prp -> PublicRenderParamFactory.create(prp))
				.forEach(
					prp -> _publicRenderParamMap.put(prp.getIdentifier(), prp));
		}
	}

	@Override
	public List<EventDefinition> getEventDefinitions() {
		return _eventDefinitions;
	}

	@Override
	public Map<String, PublicRenderParam> getPublicRenderParameterMap() {
		return _publicRenderParamMap;
	}

	private List<EventDefinition> _eventDefinitions;
	private Map<String, PublicRenderParam> _publicRenderParamMap;
}

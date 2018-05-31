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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Neil Griffin
 */
public class BeanAppDescriptorImpl extends BeanAppBase {

	public BeanAppDescriptorImpl(String specVersion) {
		_specVersion = specVersion;
	}

	@Override
	public Map<String, List<String>> getContainerRuntimeOptions() {
		return _containerRuntimeOptions;
	}

	@Override
	public Set<String> getCustomPortletModes() {
		return _customPortletModes;
	}

	@Override
	public List<EventDefinition> getEventDefinitions() {
		return _eventDefinitions;
	}

	@Override
	public Map<String, PublicRenderParam> getPublicRenderParameterMap() {
		return _publicRenderParamMap;
	}

	@Override
	public String getSpecVersion() {
		return _specVersion;
	}

	@Override
	public List<URLGenerationListener> getURLGenerationListeners() {
		return _urlGenerationListeners;
	}

	private Map<String, List<String>> _containerRuntimeOptions =
		new HashMap<>();
	private Set<String> _customPortletModes = new LinkedHashSet<>();
	private List<EventDefinition> _eventDefinitions = new ArrayList<>();
	private Map<String, PublicRenderParam> _publicRenderParamMap =
		new HashMap<>();
	private String _specVersion;
	private List<URLGenerationListener> _urlGenerationListeners =
		new ArrayList<>();
}

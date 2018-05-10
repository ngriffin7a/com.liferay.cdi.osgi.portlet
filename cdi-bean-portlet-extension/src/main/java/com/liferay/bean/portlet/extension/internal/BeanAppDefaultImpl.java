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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.portlet.annotations.PortletApplication;

/**
 * @author Neil Griffin
 */
public class BeanAppDefaultImpl extends BeanAppBase {

	@Override
	public Map<String, List<String>> getContainerRuntimeOptions() {
		return Collections.emptyMap();
	}

	@Override
	public List<EventDefinition> getEventDefinitions() {
		return Collections.emptyList();
	}

	@Override
	public Map<String, PublicRenderParam> getPublicRenderParameterMap() {
		return Collections.emptyMap();
	}

	@Override
	public String getSpecVersion() {

		PortletApplication defaultPortletApplication =
			DefaultPortletApplication.class.getAnnotation(
				PortletApplication.class);

		return defaultPortletApplication.version();
	}
}

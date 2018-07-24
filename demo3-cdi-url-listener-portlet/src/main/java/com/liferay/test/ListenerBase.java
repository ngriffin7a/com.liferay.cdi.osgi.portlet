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

package com.liferay.test;

import javax.portlet.PortletURL;
import javax.portlet.PortletURLGenerationListener;
import javax.portlet.ResourceURL;
import java.util.Map;

/**
 * @author Neil Griffin
 */
public abstract class ListenerBase implements PortletURLGenerationListener {

	@Override
	public void filterRenderURL(PortletURL portletURL) {
		// no-op
	}

	@Override
	public void filterResourceURL(ResourceURL resourceURL) {
		// no-op
	}

	protected String getParameterValue(PortletURL portletURL, String name) {

		Map<String, String[]> parameterMap = portletURL.getParameterMap();

		String[] values = parameterMap.get(name);

		if ((values == null) || (values.length == 0)) {
			return null;
		}

		return values[0];
	}
}

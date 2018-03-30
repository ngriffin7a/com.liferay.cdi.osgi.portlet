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
import java.util.Collections;
import java.util.List;

/**
 * @author Neil Griffin
 */
public class DescriptorFilterMapping {

	public void addPortletName(String portletName) {

		if (_portletNames.size() == 0) {
			_portletNames = new ArrayList<>();
		}

		_portletNames.add(portletName);
	}

	public String getFilterName() {
		return filterName;
	}

	public List<String> getPortletNames() {
		return _portletNames;
	}

	public void setFilterName(String filterName) {
		this.filterName = filterName;
	}

	private String filterName;
	private List<String> _portletNames = Collections.emptyList();
}

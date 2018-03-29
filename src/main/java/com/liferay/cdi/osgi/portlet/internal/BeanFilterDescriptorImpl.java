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
import java.util.List;

/**
 * @author Neil Griffin
 */
public class BeanFilterDescriptorImpl implements BeanFilter {

	@Override
	public Class<?> getFilterClass() {
		return _filterClass;
	}

	@Override
	public String getFilterName() {
		return _filterName;
	}

	@Override
	public List<String> getPortletNames() {
		return _portletNames;
	}

	public void setFilterClass(Class<?> filterClass) {
		_filterClass = filterClass;
	}

	public void setFilterName(String filterName) {
		_filterName = filterName;
	}

	public void setOrdinal(int ordinal) {
		_ordinal = ordinal;
	}

	@Override
	public PortletDictionary toDictionary(String portletName) {

		PortletDictionary portletDictionary = new PortletDictionary();

		portletDictionary.put("javax.portlet.name", portletName);

		if (_ordinal != null) {
			portletDictionary.put(
				"service.ranking:Integer", _ordinal.intValue());
		}

		return portletDictionary;
	}

	private Class<?> _filterClass;
	private String _filterName;
	private Integer _ordinal;
	private List<String> _portletNames = new ArrayList<>();
}

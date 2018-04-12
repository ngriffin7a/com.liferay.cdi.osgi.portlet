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

import java.util.Arrays;
import java.util.List;

import javax.portlet.annotations.PortletLifecycleFilter;

/**
 * @author Neil Griffin
 */
public class BeanFilterAnnotationImpl implements BeanFilter {

	public BeanFilterAnnotationImpl(
			Class<?> filterClass,
			PortletLifecycleFilter portletLifecycleFilter) {
		_filterClass = filterClass;
		_portletLifecycleFilter = portletLifecycleFilter;
	}

	@Override
	public Class<?> getFilterClass() {
		return _filterClass;
	}

	@Override
	public String getFilterName() {
		return _portletLifecycleFilter.filterName();
	}

	@Override
	public List<String> getPortletNames() {
		return Arrays.asList(_portletLifecycleFilter.portletNames());
	}

	@Override
	public PortletDictionary toDictionary(String portletName) {

		PortletDictionary portletDictionary = new PortletDictionary();

		portletDictionary.put("javax.portlet.name", portletName);
		portletDictionary.put(
			"service.ranking:Integer", _portletLifecycleFilter.ordinal());

		Arrays.stream(_portletLifecycleFilter.initParams())
			.forEach(
				initParameter ->
					portletDictionary.putIfNotNull(
						"javax.portlet.init-param." + initParameter.name(),
						initParameter.value()));

		return portletDictionary;
	}

	private Class<?> _filterClass;
	private PortletLifecycleFilter _portletLifecycleFilter;
}

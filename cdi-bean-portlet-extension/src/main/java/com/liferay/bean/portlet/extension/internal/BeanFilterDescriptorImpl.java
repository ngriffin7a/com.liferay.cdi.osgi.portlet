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
import java.util.Collections;
import java.util.List;

/**
 * @author Neil Griffin
 */
public class BeanFilterDescriptorImpl implements BeanFilter {

	public void addDescriptorInitParam(
			DescriptorInitParam descriptorInitParam) {

		if (_descriptorInitParams.size() == 0) {
			_descriptorInitParams = new ArrayList<>();
		}

		_descriptorInitParams.add(descriptorInitParam);
	}

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

		_descriptorInitParams.forEach(
			descriptorInitParam ->
				portletDictionary.putIfNotNull(
					"javax.portlet.init-param." + descriptorInitParam.getName(),
					descriptorInitParam.getValue()));

		return portletDictionary;
	}

	private List<DescriptorInitParam> _descriptorInitParams = Collections
		.emptyList();
	private Class<?> _filterClass;
	private String _filterName;
	private Integer _ordinal;
	private List<String> _portletNames = new ArrayList<>();
}

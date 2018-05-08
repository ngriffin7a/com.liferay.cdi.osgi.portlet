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
public class DescriptorPreference {

	public void addValue(String value) {

		if (_values.size() == 0) {
			_values = new ArrayList<>();
		}

		_values.add(value);
	}

	public String getName() {
		return _name;
	}

	public List<String> getValues() {
		return _values;
	}

	public boolean isReadOnly() {
		return _readOnly;
	}

	public void setName(String name) {
		_name = name;
	}

	public void setReadOnly(boolean readOnly) {
		_readOnly = readOnly;
	}

	private String _name;
	private boolean _readOnly;
	private List<String> _values = Collections.emptyList();
}

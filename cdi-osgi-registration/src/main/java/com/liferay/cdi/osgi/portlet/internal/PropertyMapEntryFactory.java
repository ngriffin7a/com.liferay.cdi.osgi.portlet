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

import java.util.AbstractMap;
import java.util.Map;

/**
 * @author Neil Griffin
 */
public class PropertyMapEntryFactory {

	public static Map.Entry<String, String> create(String property) {

		String name = property;
		String value = null;

		int equalsPos = property.indexOf("=");

		if (equalsPos > 0) {
			name = property.substring(0, equalsPos);
			value = property.substring(equalsPos + 1);
		}

		return new AbstractMap.SimpleEntry<>(name, value);
	}
}

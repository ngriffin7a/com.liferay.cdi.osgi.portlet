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
public class DescriptorSupports {

	public void addPortletMode(String portletMode) {

		if (_portletModes.isEmpty()) {
			_portletModes = new ArrayList<>();
		}

		_portletModes.add(portletMode);
	}

	public void addWindowState(String windowState) {

		if (_windowStates.isEmpty()) {
			_windowStates = new ArrayList<>();
		}

		_windowStates.add(windowState);
	}

	public String getMimeType() {
		return _mimeType;
	}

	public List<String> getPortletModes() {
		return _portletModes;
	}

	public List<String> getWindowStates() {
		return _windowStates;
	}

	public void setMimeType(String mimeType) {
		_mimeType = mimeType;
	}

	private List<String> _portletModes = Collections.emptyList();
	private String _mimeType;
	private List<String> _windowStates = Collections.emptyList();
}

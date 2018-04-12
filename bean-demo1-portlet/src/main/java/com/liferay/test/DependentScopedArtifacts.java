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

import javax.inject.Inject;

import javax.portlet.annotations.ContextPath;
import javax.portlet.annotations.Namespace;
import javax.portlet.annotations.PortletName;
import javax.portlet.annotations.PortletRequestScoped;
import javax.portlet.annotations.WindowId;

/**
 * @author Neil Griffin
 */
@PortletRequestScoped
public class DependentScopedArtifacts {

	public String getContextPath() {
		return _contextPath;
	}

	public String getNamespace() {
		return _namespace;
	}

	public String getPortletName() {
		return _portletName;
	}

	public String getWindowId() {
		return _windowId;
	}

	@Inject
	@PortletName
	String _portletName;

	@Inject
	@ContextPath
	String _contextPath;

	@Inject
	@Namespace
	String _namespace;

	@Inject
	@WindowId
	String _windowId;
}

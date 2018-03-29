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

import javax.portlet.annotations.PortletApplication;
import javax.portlet.annotations.PortletConfiguration;

/**
 * @author Neil Griffin
 */
public class BeanPortletFactory {

	public static BeanPortlet create(
			PortletConfiguration portletConfiguration, String portletClass) {
		return create(null, portletConfiguration, portletClass);
	}

	public static BeanPortlet create(
			PortletApplication portletApplication,
			PortletConfiguration portletConfiguration, String portletClass) {
		return new BeanPortletAnnotationImpl(
			portletApplication, portletConfiguration, portletClass);
	}

}

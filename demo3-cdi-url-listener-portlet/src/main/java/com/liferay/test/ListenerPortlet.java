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

import com.liferay.bean.portlet.extension.LiferayPortletConfiguration;
import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.annotations.PortletConfiguration;

/**
 * @author Neil Griffin
 */
@PortletConfiguration(portletName = "com_liferay_cdi_url_listener_portlet")
@LiferayPortletConfiguration(
	portletName = "com_liferay_cdi_url_listener_portlet",
	properties = {"requires-namespaced-parameters=false"}
)
public class ListenerPortlet extends GenericPortlet {

	@Override
	public void processAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws PortletException, IOException {

		actionResponse.setRenderParameter("a", actionRequest.getParameter("a"));
		actionResponse.setRenderParameter("b", actionRequest.getParameter("b"));
		actionResponse.setRenderParameter("c", actionRequest.getParameter("c"));
		actionResponse.setRenderParameter("d", actionRequest.getParameter("d"));
	}

	@Override
	protected void doView(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortletException, IOException {

		String viewPath = "/WEB-INF/views/portletViewMode.jspx";
		PortletRequestDispatcher portletRequestDispatcher = getPortletContext()
			.getRequestDispatcher(viewPath);
		portletRequestDispatcher.include(renderRequest, renderResponse);
	}
}

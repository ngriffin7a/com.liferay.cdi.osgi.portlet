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
import java.io.PrintWriter;

import javax.enterprise.inject.spi.BeanManager;

import javax.inject.Inject;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.annotations.ActionMethod;
import javax.portlet.annotations.EventMethod;
import javax.portlet.annotations.LocaleString;
import javax.portlet.annotations.PortletConfiguration;
import javax.portlet.annotations.PortletConfigurations;
import javax.portlet.annotations.PortletQName;
import javax.portlet.annotations.RenderMethod;
import javax.portlet.annotations.ServeResourceMethod;

/**
 * @author Neil Griffin
 */
@PortletConfigurations(
	@PortletConfiguration(
		portletName = "helloBeanPortlet",
		description = @LocaleString("hello bean portlet description"),
		title = @LocaleString("hello bean portlet title")
	)
)
@LiferayPortletConfiguration(
	portletName = "helloBeanPortlet",
	properties = {
		"com.liferay.portlet.display-category=category.sample"
	}
)
public class HelloBeanPortlet {

	@ActionMethod(portletName = "helloBeanPortlet", actionName = "foo")
	public void myActionMethod(
			ActionRequest actionRequest, ActionResponse actionResponse) {
	}

	@EventMethod(
		portletName = "helloBeanPortlet",
		processingEvents = {
				@PortletQName(
					namespaceURI =
						"http://www.apache.org/portals/pluto/ResourcePortlet",
					localPart = "Message"
				)
			}
	)
	public void myEventMethod(
			EventRequest eventRequest, EventResponse eventResponse) {

	}

	@RenderMethod(portletNames = {"helloBeanPortlet"})
	public void myRenderMethod(RenderRequest renderRequest) {
		// Invalid signature.
	}

	@RenderMethod(portletNames = {"helloBeanPortlet"})
	public void myRenderMethod(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException {
		PrintWriter writer = renderResponse.getWriter();
		writer.write("<p>This is helloBeanPortlet</p>");
	}

	@ServeResourceMethod(portletNames = {"helloBeanPortlet"})
	public void myServeResource(
			ResourceRequest resourceRequest,
			ResourceResponse resourceResponse) {

	}

	@Inject
	BeanManager _beanManager;
}

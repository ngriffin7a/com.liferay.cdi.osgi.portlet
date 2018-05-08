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
import java.io.Writer;

import java.util.List;
import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;

import javax.inject.Inject;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.ActionURL;
import javax.portlet.GenericPortlet;
import javax.portlet.MimeResponse;
import javax.portlet.MutableRenderParameters;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import javax.portlet.RenderParameters;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.RenderURL;
import javax.portlet.WindowState;
import javax.portlet.annotations.Dependency;
import javax.portlet.annotations.PortletConfiguration;

import javax.servlet.http.Cookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Neil Griffin
 */
@ApplicationScoped
@PortletConfiguration(
	portletName = "fooBeanPortlet",
	dependencies = {
			@Dependency(
				name = "PortletHub", scope = "javax.portlet", version = "3.0.0"
			),
			@Dependency(
				name = "yaya.js", scope = "com.yourcompany", version = "1.2.3"
			)
		}
)
@LiferayPortletConfiguration(
	portletName = "fooBeanPortlet",
	properties = {
			"com.liferay.portlet.requires-namespaced-parameters=false",
			"com.liferay.portlet.ajaxable=true",
			"com.liferay.portlet.display-category=category.sample"
		}
)
public class FooBeanPortlet extends GenericPortlet {

	@Override
	public void processAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws PortletException, IOException {

		_log.trace("inside processAction!");

		RenderParameters renderParameters = actionRequest.getRenderParameters();

		_renderStateScopedTestBean.setBar("action!");

		MutableRenderParameters mutableRenderParameters =
			actionResponse.getRenderParameters();
		mutableRenderParameters.set(renderParameters);
	}

	@Override
	public void render(RenderRequest request, RenderResponse response)
		throws PortletException, IOException {

		_log.trace("inside render!");
		super.render(request, response);
	}

	@Override
	protected void doView(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortletException, IOException {

		_mySessionScopedBean1.toString();
		_mySessionScopedBean2.toString();

		PrintWriter writer = renderResponse.getWriter();
		writer.write("<p>This is FooBeanPortlet!</p>");

		String foo = _renderStateScopedTestBean.getFoo();
		String bar = _renderStateScopedTestBean.getBar();

		if ((foo == null) && (bar == null)) {
			_renderStateScopedTestBean.setFoo("1");
			_renderStateScopedTestBean.setBar("a");
		}
		else {
			_renderStateScopedTestBean.setFoo(
				Integer.toString(Integer.parseInt(foo) + 1));
		}

		ActionURL actionURL = renderResponse.createActionURL();
		actionURL.setBeanParameter(_renderStateScopedTestBean);

		writer.write(
			"<p><a href=\"" + actionURL.toString() +
			"\">Invoke ActionURL</a></p>");

		RenderURL renderURL = renderResponse.createRenderURL();
		renderURL.setBeanParameter(_renderStateScopedTestBean);

		writer.write(
			"<p><a href=\"" + renderURL.toString() +
			"\">Invoke RenderURL</a></p>");
		writer.write("<table border=\"1\">");
		writeRow(
			writer, "portletRequest", "getContextPath",
			_portletRequest.getContextPath());
		writeRow(
			writer, "portletResponse", "getNamespace",
			_portletResponse.getNamespace());
		writeRow(
			writer, "portletConfig", "getPortletName",
			_portletConfig.getPortletName());
		writeRow(
			writer, "portletContext", "getContextPath",
			_portletContext.getContextPath());
		writeRow(writer, "contextPath", _dsa.getContextPath());
		writeRow(writer, "namespace", _dsa.getNamespace());
		writeRow(writer, "portletName", _dsa.getPortletName());
		writeRow(writer, "windowId", _dsa.getWindowId());
		writeRow(writer, "windowState", _windowState.toString());

		// writeRow(writer, "actionRequest", _actionRequest.toString());
		// writeRow(writer, "actionResponse", _actionResponse.toString());
		writeRow(writer, "renderRequest", _renderRequest.toString());
		writeRow(writer, "renderResponse", _renderResponse.toString());
		writeRow(writer, "mimeResponse", _mimeResponse.toString());

		writeRow(writer, "cookies", _cookies.toString());
		writeRow(writer, "locales", _locales.toString());
		writeRow(writer, "portletMode", _portletMode.toString());
		writeRow(writer, "portletPreferences", _portletPreferences.toString());
		writeRow(writer, "portletSession", _portletSession.toString());
		writeRow(writer, "renderParams", _renderParameters.toString());
		writeRow(
			writer, "renderStateScopedTestBean.foo",
			_renderStateScopedTestBean.getFoo());
		writeRow(
			writer, "renderStateScopedTestBean.bar",
			_renderStateScopedTestBean.getBar());

		writer.write("</table>");
	}

	private void writeRow(Writer writer, String objName, String value)
		throws IOException {
		writeRow(writer, objName, null, value);
	}

	private void writeRow(
			Writer writer, String objName, String methodName, String value)
		throws IOException {
		writer.write("<tr>");
		writer.write("<td>");
		writer.write(objName);

		if (methodName != null) {
			writer.write(".");
			writer.write(methodName);
			writer.write("()");
		}

		writer.write("</td>");
		writer.write("<td>");
		writer.write(value);
		writer.write("</td>");
		writer.write("</tr>");
	}

	private static final Logger _log = LoggerFactory.getLogger(
		FooBeanPortlet.class);

	@Inject
	RenderStateScopedTestBean _renderStateScopedTestBean;

	@Inject
	MySessionScopedBean1 _mySessionScopedBean1;

	@Inject
	MySessionScopedBean2 _mySessionScopedBean2;

	@Inject
	DependentScopedArtifacts _dsa;

	@Inject
	PortletConfig _portletConfig;

	@Inject
	PortletContext _portletContext;

	@Inject
	PortletRequest _portletRequest;

	@Inject
	PortletResponse _portletResponse;

	@Inject
	ActionRequest _actionRequest;

	@Inject
	ActionResponse _actionResponse;

	@Inject
	RenderRequest _renderRequest;

	@Inject
	RenderResponse _renderResponse;

	@Inject
	WindowState _windowState;

	@Inject
	List<Cookie> _cookies;

	@Inject
	List<Locale> _locales;

	@Inject
	MimeResponse _mimeResponse;

	@Inject
	PortletMode _portletMode;

	@Inject
	PortletPreferences _portletPreferences;

	@Inject
	PortletSession _portletSession;

	@Inject
	RenderParameters _renderParameters;
}

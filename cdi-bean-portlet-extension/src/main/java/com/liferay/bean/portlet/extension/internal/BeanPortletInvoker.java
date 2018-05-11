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

import com.liferay.portal.kernel.util.Validator;
import java.io.IOException;
import java.io.PrintWriter;

import java.lang.reflect.InvocationTargetException;

import java.util.List;
import java.util.stream.Collectors;

import javax.portlet.ActionParameters;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Event;
import javax.portlet.EventPortlet;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.HeaderPortlet;
import javax.portlet.HeaderRequest;
import javax.portlet.HeaderResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceServingPortlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Neil Griffin
 */
public class BeanPortletInvoker implements EventPortlet, HeaderPortlet, Portlet,
	ResourceServingPortlet {

	public BeanPortletInvoker(
			List<BeanMethod> actionMethods, List<BeanMethod> destroyMethods,
			List<BeanMethod> eventMethods, List<BeanMethod> headerMethods,
			List<BeanMethod> initMethods, List<BeanMethod> renderMethods,
			List<BeanMethod> serveResourceMethods) {

		_actionMethods = actionMethods;
		_destroyMethods = destroyMethods;
		_eventMethods = eventMethods;
		_headerMethods = headerMethods;
		_initMethods = initMethods;
		_renderMethods = renderMethods;
		_serveResourceMethods = serveResourceMethods;

		BeanMethodComparator beanMethodComparator = new BeanMethodComparator();

		_headerMethods.sort(beanMethodComparator);
		_renderMethods.sort(beanMethodComparator);
		_serveResourceMethods.sort(beanMethodComparator);
	}

	@Override
	public void destroy() {

		try {
			invokeBeanMethods(_destroyMethods);
		}
		catch (PortletException e) {
			_log.error(e.getMessage(), e);
		}
	}

	@Override
	public void init(PortletConfig portletConfig) throws PortletException {
		invokeBeanMethods(_initMethods, portletConfig);
		_portletConfig = portletConfig;
	}

	@Override
	public void processAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws PortletException, IOException {

		invokeBeanMethods(actionRequest, actionResponse, _actionMethods);
	}

	@Override
	public void processEvent(
			EventRequest eventRequest, EventResponse eventResponse)
		throws PortletException, IOException {

		Event event = eventRequest.getEvent();

		List<BeanMethod> eventMethods = _eventMethods.stream()
			.filter(beanMethod ->
						beanMethod.isEventProcessor(event.getQName()))
				.collect(Collectors.toList());

		invokeBeanMethods(eventRequest, eventResponse, eventMethods);
	}

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortletException, IOException {

		invokeBeanMethods(renderRequest, renderResponse, _renderMethods);
	}

	@Override
	public void renderHeaders(
			HeaderRequest headerRequest, HeaderResponse headerResponse)
		throws PortletException, IOException {

		invokeBeanMethods(headerRequest, headerResponse, _headerMethods);
	}

	@Override
	public void serveResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws PortletException, IOException {

		invokeBeanMethods(
			resourceRequest, resourceResponse, _serveResourceMethods);
	}

	protected void invokeBeanMethod(BeanMethod beanMethod, Object... args)
		throws InvocationTargetException, IllegalAccessException, IOException,
			PortletException {

		if (beanMethod.getType() == MethodType.ACTION) {

			ActionRequest actionRequest = (ActionRequest) args[0];
			ActionParameters actionParameters =
				actionRequest.getActionParameters();
			String actionName = actionParameters.getValue(
				ActionRequest.ACTION_NAME);

			if ((actionName == null) ||
				actionName.equals(beanMethod.getActionName())) {
				beanMethod.invoke(args);
			}
		}
		else if (beanMethod.getType() == MethodType.RENDER) {

			RenderRequest renderRequest = (RenderRequest) args[0];
			PortletMode portletMode = renderRequest.getPortletMode();

			PortletMode beanMethodPortletMode = beanMethod.getPortletMode();

			if ((beanMethodPortletMode == null) ||
				portletMode.equals(beanMethodPortletMode)) {

				if (beanMethod.getParameterCount() == 0) {

					String markup = (String) beanMethod.invoke(null);

					if (markup != null) {
						RenderResponse renderResponse = (RenderResponse)
							args[1];
						PrintWriter writer = renderResponse.getWriter();
						writer.write(markup);
					}
				}
				else {
					beanMethod.invoke(args);
				}
			}
		}
		else if (
			(beanMethod.getType() == MethodType.SERVE_RESOURCE) &&
			(beanMethod.getParameterCount() == 0)) {

			String markup = (String) beanMethod.invoke(null);

			if (markup != null) {
				ResourceResponse resourceResponse = (ResourceResponse) args[1];
				PrintWriter writer = resourceResponse.getWriter();
				writer.write(markup);
			}
		}
		else {
			beanMethod.invoke(args);
		}

		String include = beanMethod.getInclude();

		if (Validator.isNotNull(include)) {
			PortletContext portletContext = _portletConfig.getPortletContext();
			PortletRequestDispatcher portletRequestDispatcher =
				portletContext.getRequestDispatcher(include);

			if (portletRequestDispatcher != null) {
				portletRequestDispatcher.include(
					(PortletRequest) args[0], (PortletResponse) args[1]);
			}
			else {
				_log.error(
					"Unable to acquire dispatcher for include=" + include);
			}
		}
	}

	protected void invokeBeanMethods(
			List<BeanMethod> beanMethods, Object... args)
		throws PortletException {

		for (BeanMethod beanMethod : beanMethods) {

			try {
				invokeBeanMethod(beanMethod, args);
			}
			catch (Exception e) {
				Throwable cause = e.getCause();

				if (cause == null) {
					cause = e;
				}

				throw new PortletException(cause);
			}
		}
	}

	protected void invokeBeanMethods(
			PortletRequest portletRequest, PortletResponse portletResponse,
			List<BeanMethod> beanMethods) throws PortletException {

		ScopedBeanHolder scopedBeanHolder = new ScopedBeanHolder(
			portletRequest, portletResponse, _portletConfig);

		ScopedBeanHolder.setCurrentInstance(scopedBeanHolder);
		invokeBeanMethods(beanMethods, portletRequest, portletResponse);
		scopedBeanHolder.release();
		ScopedBeanHolder.setCurrentInstance(null);
	}

	private static final Logger _log = LoggerFactory.getLogger(
		BeanPortletInvoker.class);

	private List<BeanMethod> _actionMethods;
	private List<BeanMethod> _destroyMethods;
	private List<BeanMethod> _eventMethods;
	private List<BeanMethod> _headerMethods;
	private List<BeanMethod> _initMethods;
	private PortletConfig _portletConfig;
	private List<BeanMethod> _renderMethods;
	private List<BeanMethod> _serveResourceMethods;
}

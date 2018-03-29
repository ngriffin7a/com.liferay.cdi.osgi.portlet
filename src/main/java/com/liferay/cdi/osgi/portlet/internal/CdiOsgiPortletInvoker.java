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

import java.io.IOException;

import java.lang.reflect.InvocationTargetException;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventPortlet;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.HeaderPortlet;
import javax.portlet.HeaderRequest;
import javax.portlet.HeaderResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
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
public class CdiOsgiPortletInvoker implements EventPortlet, HeaderPortlet,
	Portlet, ResourceServingPortlet {

	public CdiOsgiPortletInvoker(
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
	}

	@Override
	public void processAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws PortletException, IOException {

		invokeBeanMethods(_actionMethods, actionRequest, actionResponse);
	}

	@Override
	public void processEvent(
			EventRequest eventRequest, EventResponse eventResponse)
		throws PortletException, IOException {

		invokeBeanMethods(_eventMethods, eventRequest, eventResponse);
	}

	@Override
	public void render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortletException, IOException {

		invokeBeanMethods(_renderMethods, renderRequest, renderResponse);
	}

	@Override
	public void renderHeaders(
			HeaderRequest headerRequest, HeaderResponse headerResponse)
		throws PortletException, IOException {

		invokeBeanMethods(_headerMethods, headerRequest, headerResponse);
	}

	@Override
	public void serveResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws PortletException, IOException {

		invokeBeanMethods(
			_serveResourceMethods, resourceRequest, resourceResponse);
	}

	protected void invokeBeanMethods(
			List<BeanMethod> beanMethods, Object... args)
		throws PortletException {

		for (BeanMethod beanMethod : beanMethods) {

			try {
				beanMethod.invoke(args);
			}
			catch (InvocationTargetException | IllegalAccessException e) {
				Throwable cause = e.getCause();
				String message = cause.getMessage();
				Class<?> beanClass = beanMethod.getBeanClass();

				if ((message != null) && message.startsWith("Config is null") &&
					(beanClass.getAnnotation(ApplicationScoped.class) ==
						null)) {

					_log.error(
						"Class {} is not annotated with @ApplicationScoped",
						beanClass.getName());
				}

				throw new PortletException(cause);
			}
		}
	}

	private static final Logger _log = LoggerFactory.getLogger(
		CdiOsgiPortletInvoker.class);

	private List<BeanMethod> _actionMethods;
	private List<BeanMethod> _destroyMethods;
	private List<BeanMethod> _eventMethods;
	private List<BeanMethod> _headerMethods;
	private List<BeanMethod> _initMethods;
	private List<BeanMethod> _renderMethods;
	private List<BeanMethod> _serveResourceMethods;
}

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

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;

import javax.inject.Inject;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.annotations.InitParameter;
import javax.portlet.annotations.PortletLifecycleFilter;
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.FilterConfig;
import javax.portlet.filter.RenderFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Neil Griffin
 */
@ApplicationScoped
@PortletLifecycleFilter(
	initParams = {
			@InitParameter(name = "foo", value = "1234"),
			@InitParameter(name = "bar", value = "7890")
		}
)
public class MyPortletFilter implements RenderFilter {

	@Override
	public void destroy() {
		_log.trace(
			"!@#$ MyPortletFilter destroy! portletContextName=" +
			_filterConfig.getPortletContext()
				.getPortletContextName());
	}

	@Override
	public void doFilter(
			RenderRequest request, RenderResponse response, FilterChain chain)
		throws IOException, PortletException {

		_log.trace(
			"inside doFilter! filterConfig={} this={} " +
			"INJECTED _beanManager={} RENDER_PORTLET={}" + _beanManager +
			_filterConfig, this, _beanManager,
			request.getAttribute("RENDER_PORTLET"));

		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws PortletException {
		_log.trace("inside init!");
		_filterConfig = filterConfig;
	}

	private static final Logger _log = LoggerFactory.getLogger(
		MyPortletFilter.class);

	@Inject
	BeanManager _beanManager;

	private FilterConfig _filterConfig;
}

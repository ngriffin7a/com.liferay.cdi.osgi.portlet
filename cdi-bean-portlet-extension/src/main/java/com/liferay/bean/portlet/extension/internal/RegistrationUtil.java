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

import com.liferay.portal.kernel.model.PortletConstants;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.ResourceBundleLoader;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.util.Validator;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;

import javax.portlet.Portlet;
import javax.portlet.filter.PortletFilter;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Neil Griffin
 */
public class RegistrationUtil {

	public static List<ServiceRegistration<PortletFilter>> registerBeanFilter(
			BundleContext bundleContext, String portletName,
			Set<String> allPortletNames, BeanFilter beanFilter,
			BeanManager beanManager, ServletContext servletContext) {

		List<ServiceRegistration<PortletFilter>> registrations =
			new ArrayList<>();

		String portletId = getPortletId(
			portletName, servletContext.getServletContextName());

		if (_log.isDebugEnabled()) {
			_log.debug(
				"Registering bean filter: " + beanFilter.getFilterName() +
				" for portletId: " + portletId);
		}

		if ("*".equals(portletName)) {
			allPortletNames.forEach(
				curPortletName ->
					registrations.add(
						bundleContext.registerService(
							PortletFilter.class,
							new BeanFilterInvoker(
								beanFilter.getFilterClass(), beanManager),
							beanFilter.toDictionary(curPortletName))));
		}
		else {
			registrations.add(
				bundleContext.registerService(
					PortletFilter.class,
					new BeanFilterInvoker(
						beanFilter.getFilterClass(), beanManager),
					beanFilter.toDictionary(portletName)));
		}

		Set<String> beanFilterNames = (Set<String>) servletContext.getAttribute(
			"com.liferay.beanFilterNames");

		if (beanFilterNames == null) {
			beanFilterNames = new HashSet<>();
		}

		beanFilterNames.add(beanFilter.getFilterName());

		servletContext.setAttribute(
			"com.liferay.beanFilterNames", beanFilterNames);

		return registrations;
	}

	public static ServiceRegistration<Portlet> registerBeanPortlet(
			BundleContext bundleContext, BeanPortlet beanPortlet,
			ServletContext servletContext) {

		try {

			String portletId = getPortletId(
				beanPortlet.getPortletName(),
				servletContext.getServletContextName());

			if (_log.isDebugEnabled()) {
				_log.debug("Registering bean portletId: " + portletId);
			}

			ServiceRegistration<Portlet> portletServiceRegistration =
				bundleContext.registerService(
					Portlet.class,
					new BeanPortletInvoker(
						beanPortlet.getBeanMethods(MethodType.ACTION),
						beanPortlet.getBeanMethods(MethodType.DESTROY),
						beanPortlet.getBeanMethods(MethodType.EVENT),
						beanPortlet.getBeanMethods(MethodType.HEADER),
						beanPortlet.getBeanMethods(MethodType.INIT),
						beanPortlet.getBeanMethods(MethodType.RENDER),
						beanPortlet.getBeanMethods(MethodType.SERVE_RESOURCE)),
					beanPortlet.toDictionary(portletId));

			if (portletServiceRegistration != null) {

				ServletRegistration.Dynamic servletRegistration =
					servletContext.addServlet(
						portletId + " Servlet",
						"com.liferay.portal.kernel.servlet.PortletServlet");

				servletRegistration.addMapping("/portlet-servlet/*");

				Set<String> beanPortletIds = (Set<String>)
					servletContext.getAttribute("com.liferay.beanPortletIds");

				if (beanPortletIds == null) {
					beanPortletIds = new HashSet<>();
				}

				beanPortletIds.add(portletId);

				servletContext.setAttribute(
					"com.liferay.beanPortletIds", beanPortletIds);
			}

			return portletServiceRegistration;
		}
		catch (Exception e) {
			_log.error(e.getMessage(), e);
		}

		return null;
	}

	public static ServiceRegistration<ResourceBundleLoader> registerResourceBundleLoader(
			BundleContext bundleContext, BeanPortlet beanPortlet,
			ServletContext servletContext) {

		String resourceBundle = beanPortlet.getResourceBundle();

		if (Validator.isNotNull(resourceBundle)) {

			ResourceBundleLoader resourceBundleLoader = ResourceBundleUtil
				.getResourceBundleLoader(
					resourceBundle, servletContext.getClassLoader());

			Dictionary<String, Object> properties = new Hashtable<>();

			properties.put("resource.bundle.base.name", resourceBundle);
			properties.put("service.ranking", Integer.MIN_VALUE);
			properties.put(
				"servlet.context.name", servletContext.getServletContextName());

			return bundleContext.registerService(
				ResourceBundleLoader.class, resourceBundleLoader, properties);
		}

		return null;
	}

	private static String getPortletId(
			String portletName, String servletContextName) {

		String portletId = portletName;

		if (Validator.isNotNull(servletContextName)) {
			portletId = portletId.concat(PortletConstants.WAR_SEPARATOR)
				.concat(servletContextName);
		}

		return PortalUtil.getJsSafePortletId(portletId);
	}

	private static final Logger _log = LoggerFactory.getLogger(
		RegistrationUtil.class);

}
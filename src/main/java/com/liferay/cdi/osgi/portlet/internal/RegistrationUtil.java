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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;

import javax.portlet.Portlet;
import javax.portlet.filter.PortletFilter;

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
			BeanManager beanManager) {

		List<ServiceRegistration<PortletFilter>> registrations =
			new ArrayList<>();

		System.err.println(
			"!@#$ REGISTERING BEAN FILTER: filterName=" +
			beanFilter.getFilterName() + " portletName=" + portletName);

		if ("*".equals(portletName)) {
			allPortletNames.forEach(
				curPortletName ->
					registrations.add(
						bundleContext.registerService(
							PortletFilter.class,
							new CdiOsgiFilterInvoker(
								beanFilter.getFilterClass(), beanManager),
							beanFilter.toDictionary(curPortletName))));
		}
		else {
			registrations.add(
				bundleContext.registerService(
					PortletFilter.class,
					new CdiOsgiFilterInvoker(
						beanFilter.getFilterClass(), beanManager),
					beanFilter.toDictionary(portletName)));
		}

		return registrations;
	}

	public static ServiceRegistration<Portlet> registerBeanPortlet(
			BundleContext bundleContext, BeanPortlet beanPortlet) {

		try {

			System.err.println(
				"!@#$ REGISTERING BEAN PORTLET: portletName=" +
				beanPortlet.getPortletName());

			return bundleContext.registerService(
				Portlet.class,
				new CdiOsgiPortletInvoker(
					beanPortlet.getBeanMethods(BeanMethod.Type.ACTION),
					beanPortlet.getBeanMethods(BeanMethod.Type.DESTROY),
					beanPortlet.getBeanMethods(BeanMethod.Type.EVENT),
					beanPortlet.getBeanMethods(BeanMethod.Type.HEADER),
					beanPortlet.getBeanMethods(BeanMethod.Type.INIT),
					beanPortlet.getBeanMethods(BeanMethod.Type.RENDER),
					beanPortlet.getBeanMethods(BeanMethod.Type.SERVE_RESOURCE)),
				beanPortlet.toDictionary());
		}
		catch (Exception e) {
			_log.error(e.getMessage(), e);
		}

		return null;
	}

	private static final Logger _log = LoggerFactory.getLogger(
		RegistrationUtil.class);

}

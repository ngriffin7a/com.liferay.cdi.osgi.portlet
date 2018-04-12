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

import java.lang.annotation.Annotation;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import javax.portlet.annotations.PortletSessionScoped;

/**
 * @author Neil Griffin
 */
public class PortletSessionBeanContext extends BeanContextBase {

	@Override
	public <T> T get(
			Contextual<T> contextual, CreationalContext<T> creationalContext) {

		ScopedBeanHolder scopedBeanHolder = ScopedBeanHolder
			.getCurrentInstance();
		Bean<T> bean = (Bean<T>) contextual;
		Class<?> beanClass = bean.getBeanClass();

		PortletSessionScoped portletSessionScoped = beanClass.getAnnotation(
			PortletSessionScoped.class);

		String beanName = getAttributeName(bean);

		if (creationalContext == null) {
			return scopedBeanHolder.getPortletSessionScopedBean(
				beanName, portletSessionScoped.value());
		}

		return scopedBeanHolder.getPortletSessionScopedBean(
			beanName, portletSessionScoped.value(), bean, creationalContext);
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return PortletSessionScoped.class;
	}
}

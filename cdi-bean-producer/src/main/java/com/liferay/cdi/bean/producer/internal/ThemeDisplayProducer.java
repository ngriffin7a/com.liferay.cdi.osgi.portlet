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

package com.liferay.cdi.bean.producer.internal;

import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;
import java.lang.annotation.Annotation;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import javax.portlet.PortletRequest;
import javax.portlet.annotations.PortletRequestScoped;

/**
 * @author Neil Griffin
 */
public class ThemeDisplayProducer extends DynamicProducerBase {

	public ThemeDisplayProducer(BeanManager beanManager) {
		_beanManager = beanManager;
	}

	@Override
	public Object create(CreationalContext creationalContext) {

		Bean bean = _beanManager.resolve(
			_beanManager.getBeans(PortletRequest.class));
		PortletRequest portletRequest = (PortletRequest)
			_beanManager.getReference(
				bean, bean.getBeanClass(),
				_beanManager.createCreationalContext(bean));

		return portletRequest.getAttribute(WebKeys.THEME_DISPLAY);
	}

	@Override
	public Class<?> getBeanClass() {
		return ThemeDisplay.class;
	}

	@Override
	public String getName() {
		return "themeDisplay";
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return PortletRequestScoped.class;
	}

	private BeanManager _beanManager;

}

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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

/**
 * @author Neil Griffin
 */
public class BeanMethodImpl implements BeanMethod {

	public BeanMethodImpl(
			BeanManager beanManager, MethodType type, Class<?> beanClass,
			Method method, int ordinal, String[] portletNames) {
		_beanManager = beanManager;
		_type = type;
		_beanClass = beanClass;
		_method = method;
		_bean = beanManager.resolve(beanManager.getBeans(beanClass));
		_ordinal = ordinal;
		_portletNames = portletNames;
	}

	@Override
	public Class<?> getBeanClass() {
		return _beanClass;
	}

	@Override
	public Method getMethod() {
		return _method;
	}

	@Override
	public int getOrdinal() {
		return _ordinal;
	}

	@Override
	public String[] getPortletNames() {
		return _portletNames;
	}

	@Override
	public MethodType getType() {
		return _type;
	}

	@Override
	public void invoke(Object... args) throws InvocationTargetException,
		IllegalAccessException {

		Object beanInstance = _beanManager.getReference(
			_bean, _bean.getBeanClass(),
			_beanManager.createCreationalContext(_bean));

		_method.invoke(beanInstance, args);
	}

	private BeanManager _beanManager;
	private Bean<?> _bean;
	private Class<?> _beanClass;
	private Method _method;
	private int _ordinal;
	private String[] _portletNames;
	private MethodType _type;
}

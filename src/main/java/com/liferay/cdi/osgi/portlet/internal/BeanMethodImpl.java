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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import javax.portlet.annotations.ActionMethod;
import javax.portlet.annotations.DestroyMethod;
import javax.portlet.annotations.EventMethod;
import javax.portlet.annotations.HeaderMethod;
import javax.portlet.annotations.InitMethod;
import javax.portlet.annotations.RenderMethod;
import javax.portlet.annotations.ServeResourceMethod;

/**
 * @author Neil Griffin
 */
public class BeanMethodImpl implements BeanMethod {

	public BeanMethodImpl(
			BeanManager beanManager, Type type, Class<?> beanClass,
			Method method, String configuredPortletName) {
		_beanManager = beanManager;
		_type = type;
		_beanClass = beanClass;
		_method = method;

		_bean = beanManager.resolve(beanManager.getBeans(beanClass));

		if (type == Type.ACTION) {
			ActionMethod actionMethod = method.getAnnotation(
				ActionMethod.class);

			if (actionMethod != null) {
				_portletNames = new String[] {actionMethod.portletName()};
			}
		}
		else if (type == Type.DESTROY) {
			DestroyMethod destroyMethod = method.getAnnotation(
				DestroyMethod.class);

			if (destroyMethod != null) {
				_portletNames = new String[] {destroyMethod.value()};
			}
		}
		else if (type == Type.EVENT) {
			EventMethod eventMethod = method.getAnnotation(EventMethod.class);

			if (eventMethod != null) {
				_portletNames = new String[] {eventMethod.portletName()};
			}
		}
		else if (type == Type.HEADER) {
			HeaderMethod headerMethod = method.getAnnotation(
				HeaderMethod.class);

			if (headerMethod != null) {
				_ordinal = headerMethod.ordinal();
				_portletNames = headerMethod.portletNames();
			}
		}
		else if (type == Type.INIT) {
			InitMethod initMethod = method.getAnnotation(InitMethod.class);

			if (initMethod != null) {
				_portletNames = new String[] {initMethod.value()};
			}
		}
		else if (type == Type.RENDER) {
			RenderMethod renderMethod = method.getAnnotation(
				RenderMethod.class);

			if (renderMethod != null) {
				_ordinal = renderMethod.ordinal();
				_portletNames = renderMethod.portletNames();
			}
		}
		else if (type == Type.SERVE_RESOURCE) {
			ServeResourceMethod serveResourceMethod = method.getAnnotation(
				ServeResourceMethod.class);

			if (serveResourceMethod != null) {
				_ordinal = serveResourceMethod.ordinal();
				_portletNames = serveResourceMethod.portletNames();
			}
		}

		if ((_portletNames == null) && (configuredPortletName != null)) {
			_portletNames = new String[] {configuredPortletName};
		}
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
	public Type getType() {
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
	private Type _type;
}

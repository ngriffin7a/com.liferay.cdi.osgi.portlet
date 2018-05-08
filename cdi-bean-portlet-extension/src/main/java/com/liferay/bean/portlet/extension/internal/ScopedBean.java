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

import java.io.Serializable;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Neil Griffin
 */
public class ScopedBean<T> implements Serializable {

	public ScopedBean(
			String name, Contextual<T> bean, T beanInstance,
			CreationalContext<T> creationalContext, String scopeName) {
		_name = name;
		_bean = bean;
		_beanInstance = beanInstance;
		_creationalContext = creationalContext;
		_scopeName = scopeName;
	}

	public void destroy() {
		_log.debug("Destroying @{} bean name={}", _scopeName, _name);
		_creationalContext.release();
		_bean.destroy(_beanInstance, _creationalContext);
	}

	public T getBeanInstance() {
		return _beanInstance;
	}

	private static final long serialVersionUID = 2388556996969921221L;

	private static final Logger _log = LoggerFactory.getLogger(
		ScopedBean.class);

	private Contextual<T> _bean;
	private T _beanInstance;
	private CreationalContext<T> _creationalContext;
	private String _name;
	private String _scopeName;
}

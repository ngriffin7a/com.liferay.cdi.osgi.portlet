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

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import javax.portlet.MutableRenderParameters;
import javax.portlet.PortletConfig;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import javax.portlet.RenderParameters;
import javax.portlet.StateAwareResponse;
import javax.portlet.annotations.PortletRequestScoped;
import javax.portlet.annotations.PortletSerializable;
import javax.portlet.annotations.PortletSessionScoped;
import javax.portlet.annotations.RenderStateScoped;

/**
 * @author Neil Griffin
 */
public class ScopedBeanHolder {

	public ScopedBeanHolder(
			PortletRequest portletRequest, PortletResponse portletResponse,
			PortletConfig portletConfig) {
		_portletRequest = portletRequest;
		_portletResponse = portletResponse;
		_portletConfig = portletConfig;
	}

	public PortletConfig getPortletConfig() {
		return _portletConfig;
	}

	public PortletRequest getPortletRequest() {
		return _portletRequest;
	}

	public <T> T getPortletRequestScopedBean(String name) {
		ScopedBean<?> scopedBean = (ScopedBean) _portletRequest.getAttribute(
			name);

		if (scopedBean == null) {
			return null;
		}

		return (T) scopedBean.getBeanInstance();
	}

	public <T> T getPortletRequestScopedBean(
			String name, Bean<T> bean, CreationalContext<T> creationalContext) {

		ScopedBean<?> scopedBean = (ScopedBean) _portletRequest.getAttribute(
			name);

		if (scopedBean == null) {

			T beanInstance = bean.create(creationalContext);

			scopedBean = new ScopedBean<>(
				name, bean, beanInstance, creationalContext,
				PortletRequestScoped.class.getSimpleName());

			_portletRequest.setAttribute(name, scopedBean);
		}

		return (T) scopedBean.getBeanInstance();
	}

	public PortletResponse getPortletResponse() {
		return _portletResponse;
	}

	public <T> T getPortletSessionScopedBean(String name, int subScope) {

		PortletSession portletSession = _portletRequest.getPortletSession(true);

		ScopedBean<?> scopedBean = (ScopedBean<?>) portletSession.getAttribute(
			name, subScope);

		if (scopedBean == null) {
			return null;
		}

		return (T) scopedBean.getBeanInstance();
	}

	public <T> T getPortletSessionScopedBean(
			String name, int subScope, Bean<T> bean,
			CreationalContext<T> creationalContext) {

		PortletSession portletSession = _portletRequest.getPortletSession(true);

		ScopedBean<?> scopedBean = (ScopedBean<?>) portletSession.getAttribute(
			name, subScope);

		if (scopedBean == null) {

			T beanInstance = bean.create(creationalContext);

			scopedBean = new ScopedBean<>(
				name, bean, beanInstance, creationalContext,
				PortletSessionScoped.class.getSimpleName());

			portletSession.setAttribute(name, scopedBean, subScope);
		}

		return (T) scopedBean.getBeanInstance();
	}

	public <T> T getRenderStateScopedBean(String name) {
		ScopedBean<?> scopedBean = (ScopedBean) _portletRequest.getAttribute(
			name);

		if (scopedBean == null) {
			return null;
		}

		return (T) scopedBean.getBeanInstance();
	}

	public <T> T getRenderStateScopedBean(
			String name, Bean<T> bean, CreationalContext<T> creationalContext) {

		ScopedBean<?> scopedBean = (ScopedBean) _portletRequest.getAttribute(
			name);

		if (scopedBean == null) {

			T beanInstance = bean.create(creationalContext);
			PortletSerializable portletSerializable = (PortletSerializable)
				beanInstance;
			String parameterName = getParameterName(portletSerializable);

			RenderParameters renderParameters =
				_portletRequest.getRenderParameters();
			String[] parameterValues = renderParameters.getValues(
				parameterName);

			if (parameterValues == null) {
				parameterValues = new String[] {};
			}

			portletSerializable.deserialize(parameterValues);

			scopedBean = new ScopedBean<>(
				name, bean, beanInstance, creationalContext,
				RenderStateScoped.class.getSimpleName());

			_portletRequest.setAttribute(name, scopedBean);
		}

		return (T) scopedBean.getBeanInstance();
	}

	public void release() {

		if (_portletResponse instanceof StateAwareResponse) {
			StateAwareResponse stateAwareResponse = (StateAwareResponse)
				_portletResponse;

			Enumeration<String> attributeNames =
				_portletRequest.getAttributeNames();

			while (attributeNames.hasMoreElements()) {

				String attributeName = attributeNames.nextElement();

				if (attributeName.startsWith(
						BeanContextBase.ATTRIBUTE_NAME_PREFIX)) {
					Object attributeValue = _portletRequest.getAttribute(
						attributeName);

					if (attributeValue instanceof ScopedBean) {

						ScopedBean scopedBean = (ScopedBean) attributeValue;
						Object beanInstance = scopedBean.getBeanInstance();
						Class<?> beanInstanceClass = beanInstance.getClass();
						RenderStateScoped renderStateScoped =
							beanInstanceClass.getAnnotation(
								RenderStateScoped.class);

						if (renderStateScoped != null) {

							if (!PortletSerializable.class.isAssignableFrom(
									beanInstanceClass)) {

								continue;
							}

							PortletSerializable portletSerializable =
								(PortletSerializable) beanInstance;

							String[] values = portletSerializable.serialize();

							MutableRenderParameters mutableRenderParameters =
								stateAwareResponse.getRenderParameters();

							String name = getParameterName(portletSerializable);

							mutableRenderParameters.setValues(name, values);
						}
					}
				}
			}
		}

		List<String> beanRequestAttributeNames = Collections.list(
				_portletRequest.getAttributeNames())
				.stream()
				.filter(
						name ->
							name.startsWith(
								BeanContextBase.ATTRIBUTE_NAME_PREFIX))
				.collect(Collectors.toList());

		beanRequestAttributeNames.stream()
			.map(name -> _portletRequest.getAttribute(name))
			.filter(Objects::nonNull)
			.filter(value -> value instanceof ScopedBean)
			.forEach(value -> ((ScopedBean) value).destroy());

		beanRequestAttributeNames
			.forEach(name -> _portletRequest.removeAttribute(name));
	}

	protected String getParameterName(PortletSerializable portletSerializable) {

		String parameterName = null;

		Class<?> beanClass = portletSerializable.getClass();

		RenderStateScoped renderStateScoped = beanClass.getAnnotation(
			RenderStateScoped.class);

		if (renderStateScoped != null) {
			parameterName = renderStateScoped.paramName();
		}

		if ((parameterName == null) || (parameterName.length() == 0)) {
			parameterName = beanClass.getSimpleName();
		}

		return parameterName;
	}

	public static ScopedBeanHolder getCurrentInstance() {
		return _INSTANCE.get();
	}

	public static void setCurrentInstance(ScopedBeanHolder scopedBeanHolder) {

		if (scopedBeanHolder == null) {
			_INSTANCE.remove();
		}
		else {
			_INSTANCE.set(scopedBeanHolder);
		}
	}

	private static ThreadLocal<ScopedBeanHolder> _INSTANCE =
		new ThreadLocal<>();

	private PortletConfig _portletConfig;
	private PortletRequest _portletRequest;
	private PortletResponse _portletResponse;
}

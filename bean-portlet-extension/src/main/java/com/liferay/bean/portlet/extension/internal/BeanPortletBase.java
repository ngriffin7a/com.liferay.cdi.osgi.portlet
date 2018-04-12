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

import com.liferay.portal.kernel.exception.PortletIdException;
import com.liferay.portal.kernel.model.PortletConstants;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.Validator;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.portlet.annotations.ActionMethod;
import javax.portlet.annotations.EventMethod;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

/**
 * @author Neil Griffin
 */
public abstract class BeanPortletBase implements BeanPortlet {
	public BeanPortletBase(BeanApp beanApp) {
		_beanApp = beanApp;
	}

	@Override
	public void addBeanMethod(BeanMethod beanMethod) {

		BeanMethod.Type beanMethodType = beanMethod.getType();

		if (beanMethodType == BeanMethod.Type.ACTION) {

			if (_actionMethods.isEmpty()) {
				_actionMethods = new ArrayList<>();
			}

			_actionMethods.add(beanMethod);
		}
		else if (beanMethodType == BeanMethod.Type.DESTROY) {

			if (_destroyMethods.isEmpty()) {
				_destroyMethods = new ArrayList<>();
			}

			_destroyMethods.add(beanMethod);
		}
		else if (beanMethodType == BeanMethod.Type.EVENT) {

			if (_eventMethods.isEmpty()) {
				_eventMethods = new ArrayList<>();
			}

			_eventMethods.add(beanMethod);
		}
		else if (beanMethodType == BeanMethod.Type.HEADER) {

			if (_headerMethods.isEmpty()) {
				_headerMethods = new ArrayList<>();
			}

			_headerMethods.add(beanMethod);
		}
		else if (beanMethodType == BeanMethod.Type.INIT) {

			if (_initMethods.isEmpty()) {
				_initMethods = new ArrayList<>();
			}

			_initMethods.add(beanMethod);
		}
		else if (beanMethodType == BeanMethod.Type.RENDER) {

			if (_renderMethods.isEmpty()) {
				_renderMethods = new ArrayList<>();
			}

			_renderMethods.add(beanMethod);
		}
		else {

			if (_serveResourceMethods.isEmpty()) {
				_serveResourceMethods = new ArrayList<>();
			}

			_serveResourceMethods.add(beanMethod);
		}
	}

	@Override
	public void addParsedLiferayPortletConfiguration(
			Map<String, String> parsedLiferayPortletConfiguration) {

		if (_parsedLiferayPortletConfiguration.size() == 0) {
			_parsedLiferayPortletConfiguration = new HashMap<>();
		}

		_parsedLiferayPortletConfiguration.putAll(
			parsedLiferayPortletConfiguration);
	}

	@Override
	public List<BeanMethod> getBeanMethods(BeanMethod.Type beanMethodType) {

		if (beanMethodType == BeanMethod.Type.ACTION) {
			return _actionMethods;
		}
		else if (beanMethodType == BeanMethod.Type.DESTROY) {
			return _destroyMethods;
		}
		else if (beanMethodType == BeanMethod.Type.EVENT) {
			return _eventMethods;
		}
		else if (beanMethodType == BeanMethod.Type.HEADER) {
			return _headerMethods;
		}
		else if (beanMethodType == BeanMethod.Type.INIT) {
			return _initMethods;
		}
		else if (beanMethodType == BeanMethod.Type.RENDER) {
			return _renderMethods;
		}
		else {
			return _serveResourceMethods;
		}
	}

	@Override
	public Dictionary<String, Object> toDictionary(String servletContextName) {

		PortletDictionary portletDictionary = new PortletDictionary();

		for (BeanMethod beanMethod : getBeanMethods(BeanMethod.Type.ACTION)) {

			Set<String> supportedPublishingEvents = (Set<String>)
				portletDictionary.get(
					"javax.portlet.supported-publishing-event");

			if (supportedPublishingEvents == null) {
				supportedPublishingEvents = new HashSet<>();
			}

			Method beanActionMethod = beanMethod.getMethod();
			ActionMethod actionMethod = beanActionMethod.getAnnotation(
				ActionMethod.class);

			if (actionMethod != null) {
				supportedPublishingEvents.addAll(
					Arrays.stream(actionMethod.publishingEvents())
						.map(
								portletQName ->
									portletQName.localPart() + ";" +
									portletQName.namespaceURI())
						.collect(Collectors.toList()));
			}

			portletDictionary.putIfNotEmpty(
				"javax.portlet.supported-publishing-event",
				supportedPublishingEvents);
		}

		for (BeanMethod beanMethod : getBeanMethods(BeanMethod.Type.EVENT)) {

			Set<String> supportedPublishingEvents = (Set<String>)
				portletDictionary.get(
					"javax.portlet.supported-publishing-event");

			if (supportedPublishingEvents == null) {
				supportedPublishingEvents = new HashSet<>();
			}

			Method beanEventMethod = beanMethod.getMethod();
			EventMethod eventMethod = beanEventMethod.getAnnotation(
				EventMethod.class);

			if (eventMethod != null) {
				supportedPublishingEvents.addAll(
					Arrays.stream(eventMethod.publishingEvents())
						.map(
								portletQName ->
									portletQName.localPart() + ";" +
									portletQName.namespaceURI())
						.collect(Collectors.toList()));
			}

			portletDictionary.putIfNotEmpty(
				"javax.portlet.supported-publishing-event",
				supportedPublishingEvents);

			Set<String> supportedProcessingEvents = (Set<String>)
				portletDictionary.get(
					"javax.portlet.supported-processing-event");

			if (supportedProcessingEvents == null) {
				supportedProcessingEvents = new HashSet<>();
			}

			if (eventMethod != null) {
				supportedProcessingEvents.addAll(
					Arrays.stream(eventMethod.processingEvents())
						.map(
								portletQName ->
									portletQName.localPart() + ";" +
									portletQName.namespaceURI())
						.collect(Collectors.toList()));
			}

			portletDictionary.putIfNotEmpty(
				"javax.portlet.supported-processing-event",
				supportedProcessingEvents);
		}

		return portletDictionary;
	}

	protected Map<String, String> getParsedLiferayPortletConfiguration() {
		return _parsedLiferayPortletConfiguration;
	}

	protected String getPortletId(
			String portletName, String servletContextName) {

		String portletId = portletName;

		if (Validator.isNotNull(servletContextName)) {
			portletId = portletId.concat(PortletConstants.WAR_SEPARATOR)
				.concat(servletContextName);
		}

		return PortalUtil.getJsSafePortletId(portletId);
	}

	protected String getPublicRenderParameterNamespaceURI(String id) {

		Map<String, PublicRenderParam> publicRenderParameterMap =
			_beanApp.getPublicRenderParameterMap();

		PublicRenderParam publicRenderParam = publicRenderParameterMap.get(id);

		if (publicRenderParam == null) {
			return XMLConstants.NULL_NS_URI;
		}

		QName qName = publicRenderParam.getQName();

		if (qName == null) {
			return XMLConstants.NULL_NS_URI;
		}

		String namespaceURI = qName.getNamespaceURI();

		if (namespaceURI == null) {
			return XMLConstants.NULL_NS_URI;
		}

		return namespaceURI;
	}

	private List<BeanMethod> _actionMethods = Collections.emptyList();
	private BeanApp _beanApp;
	private List<BeanMethod> _destroyMethods = Collections.emptyList();
	private List<BeanMethod> _eventMethods = Collections.emptyList();
	private List<BeanMethod> _headerMethods = Collections.emptyList();
	private List<BeanMethod> _initMethods = Collections.emptyList();
	private Map<String, String> _parsedLiferayPortletConfiguration = Collections
		.emptyMap();
	private List<BeanMethod> _renderMethods = Collections.emptyList();
	private List<BeanMethod> _serveResourceMethods = Collections.emptyList();
}

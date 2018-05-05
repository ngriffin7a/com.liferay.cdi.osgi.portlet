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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Neil Griffin
 */
public class BeanPortletDescriptorImpl extends BeanPortletBase {

	public BeanPortletDescriptorImpl(BeanApp beanApp) {
		super(beanApp);
	}

	public void addContainerRuntimeOption(String name, List<String> values) {

		if (_containerRuntimeOptions.size() == 0) {
			_containerRuntimeOptions = new HashMap<>();
		}

		_containerRuntimeOptions.put(name, values);
	}

	/*
	public void addCustomPortletMode(String customPortletMode) {

		if (_customPortletModes.isEmpty()) {
			_customPortletModes = new ArrayList<>();
		}

		_customPortletModes.add(customPortletMode);
	}

	public void addCustomWindowState(String customWindowState) {

		if (_customWindowStates.isEmpty()) {
			_customWindowStates = new ArrayList<>();
		}

		_customWindowStates.add(customWindowState);
	}
	*/

	public void addInitParam(DescriptorInitParam descriptorInitParam) {

		if (_descriptorInitParams.isEmpty()) {
			_descriptorInitParams = new ArrayList<>();
		}

		_descriptorInitParams.add(descriptorInitParam);
	}

	public void addKeywords(String keywords) {

		if (_keywords.isEmpty()) {
			_keywords = new ArrayList<>();
		}

		_keywords.add(keywords);
	}

	public void addPreference(DescriptorPreference descriptorPreference) {

		if (_descriptorPreferences.size() == 0) {
			_descriptorPreferences = new ArrayList<>();
		}

		_descriptorPreferences.add(descriptorPreference);
	}

	public void addSecurityRoleRef(
			DescriptorSecurityRoleRef descriptorSecurityRoleRef) {

		if (_descriptorSecurityRoleRefs.size() == 0) {
			_descriptorSecurityRoleRefs = new ArrayList<>();
		}

		_descriptorSecurityRoleRefs.add(descriptorSecurityRoleRef);
	}

	public void addShortTitle(String shortTitle) {

		if (_shortTitles.isEmpty()) {
			_shortTitles = new ArrayList<>();
		}

		_shortTitles.add(shortTitle);
	}

	public void addSupportedLocale(String supportedLocale) {

		if (_supportedLocales.size() == 0) {
			_supportedLocales = new ArrayList<>();
		}

		_supportedLocales.add(supportedLocale);
	}

	public void addSupportedProcessingEvent(
			DescriptorSupportedEvent descriptorSupportedEvent) {

		if (_supportedProcessingEvents.size() == 0) {
			_supportedProcessingEvents = new ArrayList<>();
		}

		_supportedProcessingEvents.add(descriptorSupportedEvent);
	}

	public void addSupportedPublicRenderParamName(
			String publicRenderParamName) {

		if (_supportedPublicRenderParams.size() == 0) {
			_supportedPublicRenderParams = new ArrayList<>();
		}

		_supportedPublicRenderParams.add(publicRenderParamName);
	}

	public void addSupportedPublishingEvent(
			DescriptorSupportedEvent descriptorSupportedEvent) {

		if (_supportedPublishingEvents.size() == 0) {
			_supportedPublishingEvents = new ArrayList<>();
		}

		_supportedPublishingEvents.add(descriptorSupportedEvent);
	}

	public void addSupports(DescriptorSupports descriptorSupports) {

		if (_descriptorSupports.isEmpty()) {
			_descriptorSupports = new ArrayList<>();
		}

		_descriptorSupports.add(descriptorSupports);
	}

	public void addTitle(String title) {

		if (_titles.isEmpty()) {
			_titles = new ArrayList<>();
		}

		_titles.add(title);
	}

	@Override
	public String getPortletClass() {
		return _portletClass;
	}

	@Override
	public String getPortletName() {
		return _portletName;
	}

	public void setAsyncSupported(boolean asyncSupported) {
		_asyncSupported = asyncSupported;
	}

	public void setDescription(String description) {
		_description = description;
	}

	public void setDisplayName(String displayName) {
		_displayName = displayName;
	}

	public void setExpirationCache(int expirationCache) {
		_expirationCache = expirationCache;
	}

	public void setPortletClass(String portletClass) {
		_portletClass = portletClass;
	}

	public void setPortletName(String portletName) {
		_portletName = portletName;
	}

	public void setResourceBundle(String resourceBundle) {
		_resourceBundle = resourceBundle;
	}

	@Override
	public Dictionary<String, Object> toDictionary(String portletId) {

		PortletDictionary portletDictionary = (PortletDictionary) super
			.toDictionary(portletId);

		portletDictionary.put("javax.portlet.async-supported", _asyncSupported);

		portletDictionary.putIfNotEmpty(
			"javax.portlet.container-runtime-option",
			_containerRuntimeOptions.entrySet()
				.stream()
				.map(
						entry -> {
							return entry.getValue()
								.stream()
								.map(
										value ->
											entry.getKey() +
											prependDelimiter(";", value))
								.collect(Collectors.toList());
						})
				.flatMap(Collection::stream)
				.collect(Collectors.toList()));

		portletDictionary.put(
			"javax.portlet.expiration-cache", _expirationCache);

		_descriptorInitParams.forEach(
			descriptorInitParam ->
				portletDictionary.putIfNotNull(
					"javax.portlet.init-param." + descriptorInitParam.getName(),
					descriptorInitParam.getValue()));

		portletDictionary.putIfNotNull(
			"javax.portlet.description", _description);

		portletDictionary.putIfNotNull(
			"javax.portlet.display-name", _displayName);

		portletDictionary.putIfNotNull(
			"javax.portlet.info.keywords", String.join(" ", _keywords));

		portletDictionary.putIfNotNull(
			"javax.portlet.info.short-title", String.join(" ", _shortTitles));

		if (_titles.size() == 0) {
			portletDictionary.put("javax.portlet.info.title", _portletName);
		}
		else {
			portletDictionary.put(
				"javax.portlet.info.title", String.join(" ", _titles),
				_portletName);
		}

		portletDictionary.putIfNotEmpty(
			"javax.portlet.portlet-mode",
			_descriptorSupports.stream()
				.map(
						supports ->
							supports.getMimeType() + ";" +
							supports.getPortletModes()
								.stream()
								.collect(Collectors.joining(",")))
				.collect(Collectors.toList()));

		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\"?>");
		sb.append("<portlet-preferences>");

		for (DescriptorPreference descriptorPreference : _descriptorPreferences) {

			sb.append("<preference>");
			sb.append("<name>");
			sb.append(descriptorPreference.getName());
			sb.append("</name>");

			descriptorPreference.getValues()
				.forEach(
					value ->
						sb.append("<value>")
							.append(value)
							.append("</value>"));

			sb.append("<read-only>");
			sb.append(descriptorPreference.isReadOnly());
			sb.append("</read-only>");

			sb.append("</preference>");
		}

		sb.append("</portlet-preferences>");

		portletDictionary.putIfNotNull(
			"javax.portlet.preferences", sb.toString());

		portletDictionary.putIfNotNull(
			"javax.portlet.resource-bundle", _resourceBundle);

		portletDictionary.putIfNotEmpty(
			"javax.portlet.security-role-ref",
			_descriptorSecurityRoleRefs.stream()
				.map(roleRef -> roleRef.getRoleName())
				.collect(Collectors.joining(",")));

		portletDictionary.put(
			"javax.portlet.supported-public-render-parameter",
			_supportedPublicRenderParams.stream()
				.map(
						identifier ->
							identifier +
							prependDelimiter(
								";",
								getPublicRenderParameterNamespaceURI(
									identifier)))
				.collect(Collectors.toList()));

		portletDictionary.putIfNotEmpty(
			"javax.portlet.window-state",
			_descriptorSupports.stream()
				.map(
						supports ->
							supports.getMimeType() + ";" +
							supports.getWindowStates()
								.stream()
								.collect(Collectors.joining(",")))
				.collect(Collectors.toList()));

		portletDictionary.put(
			"javax.portlet.supported-locale", _supportedLocales);

		@SuppressWarnings("unchecked")
		Set<String> supportedProcessingEvents = (Set<String>)
			portletDictionary.get("javax.portlet.supported-processing-event");

		if (supportedProcessingEvents == null) {
			supportedProcessingEvents = new HashSet<>();
		}

		supportedProcessingEvents.addAll(
			_supportedProcessingEvents.stream()
				.map(
						supportedEvent ->
							supportedEvent.getQName()
								.getLocalPart() + ";" +
							supportedEvent.getQName()
								.getNamespaceURI())
				.collect(Collectors.toList()));

		portletDictionary.put(
			"javax.portlet.supported-processing-event",
			supportedProcessingEvents);

		@SuppressWarnings("unchecked")
		Set<String> supportedPublishingEvents = (Set<String>)
			portletDictionary.get("javax.portlet.supported-publishing-event");

		if (supportedPublishingEvents == null) {
			supportedPublishingEvents = new HashSet<>();
		}

		supportedPublishingEvents.addAll(
			_supportedPublishingEvents.stream()
				.map(
						supportedEvent ->
							supportedEvent.getQName()
								.getLocalPart() + ";" +
							supportedEvent.getQName()
								.getNamespaceURI())
				.collect(Collectors.toList()));

		portletDictionary.put(
			"javax.portlet.supported-publishing-event",
			supportedPublishingEvents);

		portletDictionary.putAll(getLiferayConfiguration());

		return portletDictionary;
	}

	private boolean _asyncSupported;
	private String _description;
	private Map<String, List<String>> _containerRuntimeOptions = Collections
		.emptyMap();
	/*
	private List<String> _customPortletModes = Collections.emptyList();
	private List<String> _customWindowStates = Collections.emptyList();
	*/
	private List<DescriptorInitParam> _descriptorInitParams = Collections
		.emptyList();
	private List<DescriptorSecurityRoleRef> _descriptorSecurityRoleRefs =
		Collections.emptyList();
	private List<DescriptorSupports> _descriptorSupports = Collections
		.emptyList();
	private String _displayName;
	private int _expirationCache;
	private List<String> _keywords = Collections.emptyList();
	private String _portletClass;
	private String _portletName;
	private List<String> _supportedPublicRenderParams = Collections.emptyList();
	private List<DescriptorPreference> _descriptorPreferences = Collections
		.emptyList();
	private String _resourceBundle;
	private List<String> _shortTitles = Collections.emptyList();
	private List<String> _supportedLocales = Collections.emptyList();
	private List<DescriptorSupportedEvent> _supportedProcessingEvents =
		Collections.emptyList();
	private List<DescriptorSupportedEvent> _supportedPublishingEvents =
		Collections.emptyList();
	private List<String> _titles = Collections.emptyList();
}

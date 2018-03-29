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
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.portlet.Portlet;
import javax.portlet.annotations.Preference;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

/**
 * @author Neil Griffin
 */
public class BeanPortletDescriptorImpl extends BeanPortletBase {

	public BeanPortletDescriptorImpl(BeanApp beanApp) {
		super(beanApp);
	}

	public void addInitParam(InitParam initParam) {

		if (_initParams.isEmpty()) {
			_initParams = new ArrayList<>();
		}

		_initParams.add(initParam);
	}

	public void addKeywords(String keywords) {

		if (_keywords.isEmpty()) {
			_keywords = new ArrayList<>();
		}

		_keywords.add(keywords);
	}

	public void addPreference(Preference preference) {

		if (_preferences.size() == 0) {
			_preferences = new ArrayList<>();
		}

		_preferences.add(preference);
	}

	public void addRoleRef(RoleRef roleRef) {

		if (_roleRefs.size() == 0) {
			_roleRefs = new ArrayList<>();
		}

		_roleRefs.add(roleRef);
	}

	public void addShortTitle(String shortTitle) {

		if (_shortTitles.isEmpty()) {
			_shortTitles = new ArrayList<>();
		}

		_shortTitles.add(shortTitle);
	}

	public void addSupportedProcessingEvent(SupportedEvent supportedEvent) {

		if (_supportedProcessingEvents.size() == 0) {
			_supportedProcessingEvents = new ArrayList<>();
		}

		_supportedProcessingEvents.add(supportedEvent);
	}

	public void addSupportedPublicRenderParamName(
			String publicRenderParamName) {

		if (_supportedPublicRenderParams.size() == 0) {
			_supportedPublicRenderParams = new ArrayList<>();
		}

		_supportedPublicRenderParams.add(publicRenderParamName);
	}

	public void addSupportedPublishingEvent(SupportedEvent supportedEvent) {

		if (_supportedPublishingEvents.size() == 0) {
			_supportedPublishingEvents = new ArrayList<>();
		}

		_supportedPublishingEvents.add(supportedEvent);
	}

	public void addSupports(Supports supports) {

		if (_supports.isEmpty()) {
			_supports = new ArrayList<>();
		}

		_supports.add(supports);
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
	public Dictionary<String, Object> toDictionary() {

		PortletDictionary portletDictionary = (PortletDictionary) super
			.toDictionary();

		portletDictionary.putIfNotNull("javax.portlet.name", _portletName);

		portletDictionary.put(
			"javax.portlet.expiration-cache", _expirationCache);

		_initParams.forEach(
			initParam ->
				portletDictionary.putIfNotNull(
					"javax.portlet.init-param." + initParam.getName(),
					initParam.getValue()));

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
			_supports.stream().map(
				supports ->
					supports.getMimeType() + ";" +
					supports.getPortletModes().stream().collect(
						Collectors.joining(","))).collect(Collectors.toList()));

		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\"?>");
		sb.append("<portlet-preferences>");

		for (Preference preference : _preferences) {

			sb.append("<preference>");
			sb.append("<name>");
			sb.append(preference.getName());
			sb.append("</name>");

			preference.getValues().stream().forEach(
				value -> sb.append("<value>").append(value).append("</value>"));

			sb.append("</preference>");
		}

		sb.append("</portlet-preferences>");

		portletDictionary.putIfNotNull(
			"javax.portlet.preferences", sb.toString());

		portletDictionary.putIfNotNull(
			"javax.portlet.resource-bundle", _resourceBundle);

		portletDictionary.putIfNotEmpty(
			"javax.portlet.security-role-ref",
			_roleRefs.stream().map(
				roleRef -> roleRef.getRoleName() + "," + roleRef.getRoleLink())
				.collect(Collectors.toList()));

		portletDictionary.put(
			"javax.portlet.supported-public-render-parameter",
			_supportedPublicRenderParams.stream().map(
				identifier ->
					identifier + ";" +
					getPublicRenderParameterNamespaceURI(identifier)).collect(
				Collectors.toList()));

		portletDictionary.putIfNotEmpty(
			"javax.portlet.window-state",
			_supports.stream().map(
				supports ->
					supports.getMimeType() + ";" +
					supports.getWindowStates().stream().collect(
						Collectors.joining(","))).collect(Collectors.toList()));

		Set<String> supportedProcessingEvents = (Set<String>)
			portletDictionary.get("javax.portlet.supported-processing-event");

		if (supportedProcessingEvents == null) {
			supportedProcessingEvents = new HashSet<>();
		}

		supportedProcessingEvents.addAll(
			_supportedProcessingEvents.stream().map(
				supportedEvent ->
					supportedEvent.getQName().getLocalPart() + ";" +
					supportedEvent.getQName().getNamespaceURI()).collect(
				Collectors.toList()));

		portletDictionary.put(
			"javax.portlet.supported-processing-event",
			supportedProcessingEvents);

		Set<String> supportedPublishingEvents = (Set<String>)
			portletDictionary.get("javax.portlet.supported-publishing-event");

		if (supportedPublishingEvents == null) {
			supportedPublishingEvents = new HashSet<>();
		}

		supportedPublishingEvents.addAll(
			_supportedPublishingEvents.stream().map(
				supportedEvent ->
					supportedEvent.getQName().getLocalPart() + ";" +
					supportedEvent.getQName().getNamespaceURI()).collect(
				Collectors.toList()));

		portletDictionary.put(
			"javax.portlet.supported-publishing-event",
			supportedPublishingEvents);

		return portletDictionary;
	}

	public static class FilterMapping {

		public void addPortletName(String portletName) {

			if (_portletNames.size() == 0) {
				_portletNames = new ArrayList<>();
			}

			_portletNames.add(portletName);
		}

		public String getFilterName() {
			return filterName;
		}

		public List<String> getPortletNames() {
			return _portletNames;
		}

		public void setFilterName(String filterName) {
			this.filterName = filterName;
		}

		private String filterName;
		private List<String> _portletNames = Collections.emptyList();
	}

	public static class InitParam {

		public String getName() {
			return _name;
		}

		public String getValue() {
			return _value;
		}

		public void setName(String name) {
			_name = name;
		}

		public void setValue(String value) {
			_value = value;
		}

		private String _name;
		private String _value;
	}

	public static class Preference {

		public void addValue(String value) {

			if (_values.size() == 0) {
				_values = new ArrayList<>();
			}

			_values.add(value);
		}

		public String getName() {
			return _name;
		}

		public List<String> getValues() {
			return _values;
		}

		public void setName(String name) {
			_name = name;
		}

		private String _name;
		private List<String> _values = Collections.emptyList();
	}

	public static class RoleRef {

		public String getRoleLink() {
			return _roleLink;
		}

		public String getRoleName() {
			return _roleName;
		}

		public void setRoleLink(String roleLink) {
			_roleLink = roleLink;
		}

		public void setRoleName(String roleName) {
			_roleName = roleName;
		}

		private String _roleName = "";
		private String _roleLink = "";
	}

	public static class SupportedEvent {

		public SupportedEvent() {
		}

		public SupportedEvent(BeanApp beanApp, String name) {
			_beanApp = beanApp;
			_name = name;
		}

		public QName getQName() {

			if ((_qName == null) && (_name != null)) {

				if (_beanApp != null) {
					return new QName(_beanApp.getDefaultNamespace(), _name);
				}

				return new QName(XMLConstants.NULL_NS_URI, _name);
			}

			return _qName;
		}

		public void setQName(QName qName) {
			_qName = qName;
		}

		private BeanApp _beanApp;
		private String _name;
		private QName _qName;
	}

	public static class Supports {

		public void addPortletMode(String portletMode) {

			if (_portletModes.isEmpty()) {
				_portletModes = new ArrayList<>();
			}

			_portletModes.add(portletMode);
		}

		public void addWindowState(String windowState) {

			if (_windowStates.isEmpty()) {
				_windowStates = new ArrayList<>();
			}

			_windowStates.add(windowState);
		}

		public String getMimeType() {
			return _mimeType;
		}

		public List<String> getPortletModes() {
			return _portletModes;
		}

		public List<String> getWindowStates() {
			return _windowStates;
		}

		public void setMimeType(String mimeType) {
			_mimeType = mimeType;
		}

		private List<String> _portletModes = Collections.emptyList();
		private String _mimeType;
		private List<String> _windowStates = Collections.emptyList();
	}

	private String _description;
	private String _displayName;
	private int _expirationCache;
	private List<InitParam> _initParams = Collections.emptyList();
	private List<String> _keywords = Collections.emptyList();
	private String _portletClass;
	private String _portletName;
	private List<String> _supportedPublicRenderParams = Collections.emptyList();
	private List<Preference> _preferences = Collections.emptyList();
	private String _resourceBundle;
	private List<RoleRef> _roleRefs = Collections.emptyList();
	private List<String> _shortTitles = Collections.emptyList();
	private List<Supports> _supports = Collections.emptyList();
	private List<SupportedEvent> _supportedProcessingEvents = Collections
		.emptyList();
	private List<SupportedEvent> _supportedPublishingEvents = Collections
		.emptyList();
	private List<String> _titles = Collections.emptyList();
}

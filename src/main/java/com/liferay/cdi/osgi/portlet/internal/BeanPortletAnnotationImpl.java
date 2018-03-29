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

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.portlet.annotations.LocaleString;
import javax.portlet.annotations.PortletApplication;
import javax.portlet.annotations.PortletConfiguration;
import javax.portlet.annotations.Preference;

/**
 * @author Neil Griffin
 * @author Raymond Augé
 */
public class BeanPortletAnnotationImpl extends BeanPortletBase {

	public BeanPortletAnnotationImpl(
			PortletApplication portletApplication,
			PortletConfiguration portletConfiguration, String portletClass) {
		super(new BeanAppAnnotationImpl(portletApplication));
		_portletConfiguration = portletConfiguration;
		_portletClass = portletClass;
	}

	@Override
	public String getPortletClass() {
		return _portletClass;
	}

	@Override
	public String getPortletName() {
		return _portletConfiguration.portletName();
	}

	@Override
	public PortletDictionary toPortletDictionary() {

		PortletDictionary portletDictionary = super.toPortletDictionary();

		String portletName = _portletConfiguration.portletName();

		portletDictionary.putIfNotNull("javax.portlet.name", portletName);

		portletDictionary.put(
			"javax.portlet.expiration-cache",
			_portletConfiguration.cacheExpirationTime());

		Arrays.stream(_portletConfiguration.initParams()).forEach(
			initParameter ->
				portletDictionary.putIfNotNull(
					"javax.portlet.init-param." + initParameter.name(),
					initParameter.value()));

		portletDictionary.putIfNotNull(
			"javax.portlet.description",
			getEnglishText(_portletConfiguration.description()));

		portletDictionary.putIfNotNull(
			"javax.portlet.display-name",
			getEnglishText(_portletConfiguration.displayName()));

		portletDictionary.putIfNotNull(
			"javax.portlet.info.keywords",
			getEnglishText(_portletConfiguration.keywords()));

		portletDictionary.putIfNotNull(
			"javax.portlet.info.short-title",
			getEnglishText(_portletConfiguration.shortTitle()));

		portletDictionary.put(
			"javax.portlet.info.title",
			getEnglishText(_portletConfiguration.title()), portletName);

		portletDictionary.putIfNotEmpty(
			"javax.portlet.portlet-mode",
			Arrays.stream(_portletConfiguration.supports()).map(
				supports ->
					supports.mimeType() + ";" +
					Arrays.stream(supports.portletModes()).collect(
						Collectors.joining(","))).collect(Collectors.toList()));

		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\"?>");
		sb.append("<portlet-preferences>");

		for (Preference preference : _portletConfiguration.prefs()) {

			sb.append("<preference>");
			sb.append("<name>");
			sb.append(preference.name());
			sb.append("</name>");

			Arrays.stream(preference.values()).forEach(
				value -> sb.append("<value>").append(value).append("</value>"));

			sb.append("</preference>");
		}

		sb.append("</portlet-preferences>");

		portletDictionary.putIfNotNull(
			"javax.portlet.preferences", sb.toString());

		portletDictionary.putIfNotNull(
			"javax.portlet.resource-bundle",
			_portletConfiguration.resourceBundle());

		portletDictionary.putIfNotEmpty(
			"javax.portlet.security-role-ref",
			Arrays.stream(_portletConfiguration.roleRefs()).map(
				roleRef -> roleRef.roleName() + "," + roleRef.roleLink())
				.collect(Collectors.toList()));

		portletDictionary.put(
			"javax.portlet.supported-public-render-parameter",
			Arrays.stream(_portletConfiguration.publicParams()).map(
				identifier ->
					identifier + ";" +
					getPublicRenderParameterNamespaceURI(identifier)).collect(
				Collectors.toList()));

		portletDictionary.putIfNotEmpty(
			"javax.portlet.window-state",
			Arrays.stream(_portletConfiguration.supports()).map(
				supports ->
					supports.mimeType() + ";" +
					Arrays.stream(supports.windowStates()).collect(
						Collectors.joining(","))).collect(Collectors.toList()));

		return portletDictionary;
	}

	protected String getEnglishText(LocaleString[] localeStrings) {
		return getEnglishText(localeStrings, null);
	}

	protected String getEnglishText(
			LocaleString[] localeStrings, String defaultValue) {

		String english = Locale.ENGLISH.getLanguage();

		for (LocaleString localeString : localeStrings) {

			if ((localeString.locale() == null) ||
				english.equals(localeString.locale())) {

				return localeString.value();
			}
		}

		return defaultValue;
	}

	private PortletConfiguration _portletConfiguration;
	private String _portletClass;
}
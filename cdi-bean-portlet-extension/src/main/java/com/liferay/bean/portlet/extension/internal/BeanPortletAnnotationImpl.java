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

import com.liferay.bean.portlet.extension.LiferayPortletConfiguration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.portlet.annotations.LocaleString;
import javax.portlet.annotations.PortletApplication;
import javax.portlet.annotations.PortletConfiguration;
import javax.portlet.annotations.Preference;
import javax.portlet.annotations.SecurityRoleRef;

/**
 * @author Neil Griffin
 * @author Raymond Augé
 */
public class BeanPortletAnnotationImpl extends BeanPortletBase {

	public BeanPortletAnnotationImpl(
			PortletApplication portletApplication,
			PortletConfiguration portletConfiguration,
			LiferayPortletConfiguration liferayPortletConfiguration,
			String portletClass) {
		super(new BeanAppAnnotationImpl(portletApplication));
		_portletConfiguration = portletConfiguration;
		_portletClass = portletClass;

		String[] properties = null;

		if (liferayPortletConfiguration != null) {
			properties = liferayPortletConfiguration.properties();
		}

		if ((properties == null) || (properties.length == 0)) {
			_liferayPortletConfigurationProperties = Collections.emptyMap();
		}
		else {
			_liferayPortletConfigurationProperties = new HashMap<>();
			_liferayPortletConfigurationProperties.putAll(
				Arrays.stream(properties)
					.map(PropertyMapEntryFactory::create)
					.collect(
						Collectors.toMap(
							Map.Entry::getKey, Map.Entry::getValue)));
		}

		Arrays.stream(portletConfiguration.dependencies())
			.forEach(
				dependency ->
					addPortletDependency(
						new PortletDependency(
							dependency.name(), dependency.scope(),
							dependency.version())));
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
	public String getResourceBundle() {
		return _portletConfiguration.resourceBundle();
	}

	@Override
	public Dictionary<String, Object> toDictionary(String portletId) {

		PortletDictionary portletDictionary = (PortletDictionary) super
			.toDictionary(portletId);

		portletDictionary.put(
			"javax.portlet.async-supported",
			_portletConfiguration.asyncSupported());

		BeanApp beanApp = getBeanApp();
		Map<String, List<String>> containerRuntimeOptions = new HashMap<>(
			beanApp.getContainerRuntimeOptions());

		Arrays.stream(_portletConfiguration.runtimeOptions())
			.forEach(
				runtimeOption ->
					containerRuntimeOptions.put(
						runtimeOption.name(),
						Arrays.asList(runtimeOption.values())));

		containerRuntimeOptions.entrySet()
			.forEach(
				entry -> portletDictionary.put(
					"javax.portlet.container-runtime-option.".concat(
						entry.getKey()),
					entry.getValue()));

		portletDictionary.put(
			"javax.portlet.expiration-cache",
			_portletConfiguration.cacheExpirationTime());

		Arrays.stream(_portletConfiguration.initParams())
			.forEach(
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
			getEnglishText(_portletConfiguration.title()), getPortletName());

		Set<String> supportedPortletModes = new HashSet<>(getLiferayPortletModes());
		supportedPortletModes.addAll(beanApp.getCustomPortletModes());

		portletDictionary.putIfNotEmpty(
			"javax.portlet.portlet-mode",
			Arrays.stream(_portletConfiguration.supports())
				.map(
						supports ->
							PortletDictionaryUtil.formatNameValuePair(
								supports.mimeType(),
								Arrays.stream(supports.portletModes())
									.filter(supportedPortletModes::contains)
									.collect(Collectors.joining(","))))
				.collect(Collectors.toList()));

		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\"?>");
		sb.append("<portlet-preferences>");

		for (Preference preference : _portletConfiguration.prefs()) {

			sb.append("<preference>");
			sb.append("<name>");
			sb.append(preference.name());
			sb.append("</name>");

			Arrays.stream(preference.values())
				.forEach(
					value ->
						sb.append("<value>")
							.append(value)
							.append("</value>"));

			sb.append("<read-only>");
			sb.append(preference.isReadOnly());
			sb.append("</read-only>");

			sb.append("</preference>");
		}

		sb.append("</portlet-preferences>");

		portletDictionary.putIfNotNull(
			"javax.portlet.preferences", sb.toString());

		portletDictionary.putIfNotEmpty(
			"javax.portlet.resource-bundle",
			_portletConfiguration.resourceBundle());

		portletDictionary.putIfNotEmpty(
			"javax.portlet.security-role-ref",
			Arrays.stream(_portletConfiguration.roleRefs())
				.map(SecurityRoleRef::roleName)
				.collect(Collectors.joining(",")));

		portletDictionary.put(
			"javax.portlet.supported-locale",
			Arrays.stream(_portletConfiguration.supportedLocales())
				.collect(Collectors.toList()));

		portletDictionary.put(
			"javax.portlet.supported-public-render-parameter",
			Arrays.stream(_portletConfiguration.publicParams())
				.map(
						identifier ->
							PortletDictionaryUtil.formatNameValuePair(
								identifier,
								getPublicRenderParameterNamespaceURI(
									identifier)))
				.collect(Collectors.toList()));

		portletDictionary.putIfNotEmpty(
			"javax.portlet.window-state",
			Arrays.stream(_portletConfiguration.supports())
				.map(
						supports ->
							PortletDictionaryUtil.formatNameValuePair(
								supports.mimeType(),
								Arrays.stream(supports.windowStates())
									.collect(Collectors.joining(","))))
				.collect(Collectors.toList()));

		portletDictionary.putAll(_liferayPortletConfigurationProperties);
		portletDictionary.putAll(getLiferayConfiguration());

		return portletDictionary;
	}

	protected String getEnglishText(LocaleString[] localeStrings) {

		String english = Locale.ENGLISH.getLanguage();

		for (LocaleString localeString : localeStrings) {

			if (english.equals(localeString.locale())) {

				return localeString.value();
			}
		}

		return null;
	}

	private Map<String, String> _liferayPortletConfigurationProperties;
	private PortletConfiguration _portletConfiguration;
	private String _portletClass;
}

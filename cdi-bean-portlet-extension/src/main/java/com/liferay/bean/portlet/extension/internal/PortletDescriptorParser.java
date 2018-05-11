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

import com.liferay.portal.kernel.util.GetterUtil;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.portlet.annotations.PortletApplication;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stax.StAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

/**
 * @author Neil Griffin
 */
public class PortletDescriptorParser {

	public static PortletDescriptor parse(URL portletDescriptorURL)
		throws IOException, SAXException, XMLStreamException {

		PortletDescriptor portletDescriptor = new PortletDescriptor();
		List<BeanFilter> beanFilters = portletDescriptor.getBeanFilters();
		List<BeanPortlet> beanPortlets = portletDescriptor.getBeanPortlets();
		XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();

		String specVersion = _validateXML(
			xmlInputFactory, portletDescriptorURL);

		BeanApp beanApp = new BeanAppDescriptorImpl(specVersion);

		BeanFilterDescriptorImpl beanFilter = null;
		BeanPortletDescriptorImpl beanPortlet = null;

		String customPortletMode = null;
		boolean customPortletModePortalManaged = true;

		// String customWindowState = null;
		DescriptorContainerRuntimeOption descriptorContainerRuntimeOption =
			null;
		DescriptorFilterMapping descriptorFilterMapping = null;
		DescriptorInitParam descriptorInitParam = null;
		DescriptorPreference descriptorPreference = null;
		DescriptorSupportedEvent descriptorProcessingEvent = null;
		DescriptorSupportedEvent descriptorPublishingEvent = null;
		DescriptorPortletDependency descriptorResourceDependency = null;
		DescriptorSecurityRoleRef descriptorSecurityRoleRef = null;
		DescriptorSupports descriptorSupports = null;
		String elementName;
		String elementText = null;
		EventDefinition eventDefinition = null;
		String namespaceURI = null;
		PublicRenderParam publicRenderParam = null;
		InputStream inputStream = null;
		XMLStreamReader xmlStreamReader = null;

		try {
			inputStream = portletDescriptorURL.openStream();
			xmlStreamReader = xmlInputFactory.createXMLStreamReader(
				inputStream);

			while (xmlStreamReader.hasNext()) {
				xmlStreamReader.next();

				int eventType = xmlStreamReader.getEventType();

				if (eventType == XMLStreamConstants.START_ELEMENT) {
					elementName = xmlStreamReader.getLocalName();

					if ("container-runtime-option".equals(elementName)) {
						descriptorContainerRuntimeOption =
							new DescriptorContainerRuntimeOption();
					}
					else if ("dependency".equals(elementName)) {
						descriptorResourceDependency =
							new DescriptorPortletDependency();
					}
					else if ("event-definition".equals(elementName)) {
						eventDefinition = new EventDefinitionDescriptorImpl(
							beanApp);
					}
					else if ("filter".equals(elementName)) {
						beanFilter = new BeanFilterDescriptorImpl();
					}
					else if ("filter-mapping".equals(elementName)) {
						descriptorFilterMapping = new DescriptorFilterMapping();
					}
					else if ("init-param".equals(elementName)) {
						descriptorInitParam = new DescriptorInitParam();
					}
					else if ("portlet".equals(elementName)) {
						beanPortlet = new BeanPortletDescriptorImpl(beanApp);
					}
					else if ("preference".equals(elementName)) {
						descriptorPreference = new DescriptorPreference();
					}
					else if ("public-render-parameter".equals(elementName)) {
						publicRenderParam = new PublicRenderParamDescriptorImpl(
							beanApp);
					}
					else if ("security-role-ref".equals(elementName)) {
						descriptorSecurityRoleRef =
							new DescriptorSecurityRoleRef();
					}
					else if ("supported-processing-event".equals(elementName)) {
						descriptorProcessingEvent =
							new DescriptorSupportedEvent();
					}
					else if ("supported-publishing-event".equals(elementName)) {
						descriptorPublishingEvent =
							new DescriptorSupportedEvent();
					}
					else if ("supports".equals(elementName)) {
						descriptorSupports = new DescriptorSupports();
					}
				}
				else if (eventType == XMLStreamConstants.CHARACTERS) {
					elementText = xmlStreamReader.getText();
					elementText = elementText.trim();
				}
				else if (eventType == XMLStreamConstants.END_ELEMENT) {
					elementName = xmlStreamReader.getLocalName();

					if ("alias".equals(elementName)) {

						String prefix = _getPrefix(elementText);

						if (prefix != null) {
							namespaceURI = xmlStreamReader.getNamespaceURI(
								prefix);
						}

						String localPart = _getLocalPart(elementText);

						List<QName> aliasQNames =
							eventDefinition.getAliasQNames();
						aliasQNames.add(new QName(namespaceURI, localPart));
					}
					else if ("async-supported".equals(elementName)) {
						beanPortlet.setAsyncSupported(
							GetterUtil.getBoolean(elementText));
					}
					else if ("container-runtime-option".equals(elementName)) {

						if (beanPortlet == null) {
							Map<String, List<String>> containerRuntimeOptions =
								beanApp.getContainerRuntimeOptions();
							containerRuntimeOptions.put(
								descriptorContainerRuntimeOption.getName(),
								descriptorContainerRuntimeOption.getValues());
						}
						else {
							beanPortlet.addContainerRuntimeOption(
								descriptorContainerRuntimeOption.getName(),
								descriptorContainerRuntimeOption.getValues());
						}

						descriptorContainerRuntimeOption = null;
					}
					else if ("custom-portlet-mode".equals(elementName)) {

						if (!customPortletModePortalManaged) {

							Set<String> customPortletModes =
								beanApp.getCustomPortletModes();

							customPortletModes.add(customPortletMode);
						}

						customPortletMode = null;
						customPortletModePortalManaged = true;
					}

					//J-
					/*
					else if ("custom-window-state".equals(elementName)) {

						if (customWindowState != null) {

							// beanPortlet.addCustomWindowState(customWindowState);
							customWindowState = null;
						}
					}
					*/
					//J+
					else if ("default-namespace".equals(elementName)) {
						beanApp.setDefaultNamespace(elementText);
					}
					else if ("dependency".equals(elementName)) {
						beanPortlet.addPortletDependency(
							descriptorResourceDependency);
						descriptorResourceDependency = null;
					}
					else if ("description".equals(elementName)) {

						if (beanPortlet != null) {
							beanPortlet.setDescription(elementText);
						}
					}
					else if ("display-name".equals(elementName)) {
						beanPortlet.setDisplayName(elementText);
					}
					else if ("event-definition".equals(elementName)) {

						List<EventDefinition> eventDefinitions =
							beanApp.getEventDefinitions();
						eventDefinitions.add(eventDefinition);
						eventDefinition = null;
					}
					else if ("expiration-cache".equals(elementName)) {

						try {
							beanPortlet.setExpirationCache(
								Integer.parseInt(elementText));
						}
						catch (NumberFormatException e) {
							throw new XMLStreamException(e);
						}
					}
					else if ("filter".equals(elementName)) {
						beanFilters.add(beanFilter);
						beanFilter = null;
					}
					else if ("filter-class".equals(elementName)) {

						try {
							beanFilter.setFilterClass(
								Class.forName(elementText));
						}
						catch (ClassNotFoundException e) {
							throw new IOException(e);
						}
					}
					else if ("filter-mapping".equals(elementName)) {

						boolean found = false;

						for (BeanFilter curBeanFilter : beanFilters) {

							String filterName = curBeanFilter.getFilterName();

							if (filterName.equals(
									descriptorFilterMapping.getFilterName())) {

								List<String> portletNames =
									curBeanFilter.getPortletNames();

								portletNames.addAll(
									descriptorFilterMapping.getPortletNames());

								found = true;

								break;
							}
						}

						if (!found) {
							throw new IOException(
								"filter-mapping specified filter-name=" +
								descriptorFilterMapping.getFilterName() +
								" but there is no corresponding filter with" +
								" that name.");
						}

						descriptorFilterMapping = null;
					}
					else if ("filter-name".equals(elementName)) {

						if (descriptorFilterMapping != null) {
							descriptorFilterMapping.setFilterName(elementText);
						}
						else if (beanFilter != null) {
							beanFilter.setFilterName(elementText);
						}
					}
					else if ("identifier".equals(elementName)) {
						publicRenderParam.setIdentifier(elementText);
					}
					else if ("init-param".equals(elementName)) {

						if (beanFilter != null) {
							beanFilter.addDescriptorInitParam(
								descriptorInitParam);
						}
						else if (beanPortlet != null) {

							{
								beanPortlet.addInitParam(descriptorInitParam);
							}

							descriptorInitParam = null;
						}
					}
					else if ("keywords".equals(elementName)) {
						beanPortlet.addKeywords(elementText);
					}
					else if ("mime-type".equals(elementName)) {
						descriptorSupports.setMimeType(elementText);
					}
					else if ("name".equals(elementName)) {

						if (descriptorContainerRuntimeOption != null) {
							descriptorContainerRuntimeOption.setName(
								elementText);
						}
						else if (descriptorInitParam != null) {
							descriptorInitParam.setName(elementText);
						}
						else if (descriptorResourceDependency != null) {
							descriptorResourceDependency.setName(elementText);
						}
						else if (publicRenderParam != null) {
							publicRenderParam.setName(elementText);
						}
						else if (descriptorPreference != null) {
							descriptorPreference.setName(elementText);
						}
						else if (descriptorProcessingEvent != null) {
							descriptorProcessingEvent =
								new DescriptorSupportedEvent(
									beanApp, _getLocalPart(elementText));
						}
						else if (descriptorPublishingEvent != null) {
							descriptorPublishingEvent =
								new DescriptorSupportedEvent(
									beanApp, _getLocalPart(elementText));
						}
					}
					else if ("ordinal".equals(elementName)) {

						if (beanFilter != null) {
							beanFilter.setOrdinal(
								Integer.parseInt(elementText));
						}
					}
					else if ("portal-managed".equals(elementName)) {
						customPortletModePortalManaged = GetterUtil.getBoolean(
							elementText);
					}
					else if ("portlet".equals(elementName)) {
						beanPortlets.add(beanPortlet);
						beanPortlet = null;
					}
					else if ("portlet-class".equals(elementName)) {
						beanPortlet.setPortletClass(elementText);
					}
					else if ("portlet-name".equals(elementName)) {

						if (beanFilter != null) {
							List<String> portletNames =
								beanFilter.getPortletNames();
							portletNames.add(elementText);
						}
						else if (beanPortlet != null) {
							beanPortlet.setPortletName(elementText);
						}
						else if (descriptorFilterMapping != null) {
							descriptorFilterMapping.addPortletName(elementText);
						}
					}
					else if ("portlet-mode".equals(elementName)) {

						if (descriptorSupports != null) {
							descriptorSupports.addPortletMode(elementText);
						}
						//J-
						/*
						else {
							customPortletMode = elementText;
						}
						*/
						//J+
					}
					else if ("preference".equals(elementName)) {
						beanPortlet.addPreference(descriptorPreference);
						descriptorPreference = null;
					}
					else if ("public-render-parameter".equals(elementName)) {
						Map<String, PublicRenderParam> publicRenderParameterMap =
							beanApp.getPublicRenderParameterMap();
						publicRenderParameterMap.put(
							publicRenderParam.getIdentifier(),
							publicRenderParam);
						publicRenderParam = null;
					}
					else if ("ready-only".equals(elementName)) {

						if (descriptorPreference != null) {
							descriptorPreference.setReadOnly(
								GetterUtil.getBoolean(elementText));
						}
					}
					else if ("qname".equals(elementName)) {

						String prefix = _getPrefix(elementText);

						if (prefix != null) {
							namespaceURI = xmlStreamReader.getNamespaceURI(
								prefix);
						}

						String localPart = _getLocalPart(elementText);

						if (descriptorProcessingEvent != null) {
							descriptorProcessingEvent.setQName(
								new QName(namespaceURI, localPart));
						}
						else if (publicRenderParam != null) {
							publicRenderParam.setQName(
								new QName(namespaceURI, localPart));
						}
						else if (descriptorPublishingEvent != null) {
							descriptorPublishingEvent.setQName(
								new QName(namespaceURI, localPart));
						}
					}
					else if ("resource-bundle".equals(elementName)) {
						beanPortlet.setResourceBundle(elementText);
					}
					else if ("role-link".equals(elementName)) {
						descriptorSecurityRoleRef.setRoleLink(elementText);
					}
					else if ("role-name".equals(elementName)) {
						descriptorSecurityRoleRef.setRoleName(elementText);
					}
					else if ("scope".equals(elementName)) {
						descriptorResourceDependency.setScope(elementText);
					}
					else if ("security-role-ref".equals(elementName)) {
						beanPortlet.addSecurityRoleRef(
							descriptorSecurityRoleRef);
						descriptorSecurityRoleRef = null;
					}
					else if ("short-title".equals(elementName)) {
						beanPortlet.addShortTitle(elementText);
					}
					else if ("supported-locale".equals(elementName)) {
						beanPortlet.addSupportedLocale(elementText);
					}
					else if ("supported-processing-event".equals(elementName)) {
						beanPortlet.addSupportedProcessingEvent(
							descriptorProcessingEvent);
						descriptorProcessingEvent = null;
					}
					else if ("supported-publishing-event".equals(elementName)) {
						beanPortlet.addSupportedPublishingEvent(
							descriptorPublishingEvent);
						descriptorPublishingEvent = null;
					}
					else if (
						"supported-public-render-parameter".equals(
							elementName)) {
						beanPortlet.addSupportedPublicRenderParamName(
							elementText);
					}
					else if ("supports".equals(elementName)) {
						beanPortlet.addSupports(descriptorSupports);
						descriptorSupports = null;
					}
					else if ("title".equals(elementName)) {
						beanPortlet.addTitle(elementText);
					}
					else if ("value".equals(elementName)) {

						if (descriptorContainerRuntimeOption != null) {
							descriptorContainerRuntimeOption.addValue(
								elementText);
						}
						else if (descriptorInitParam != null) {
							descriptorInitParam.setValue(elementText);
						}
						else if (descriptorPreference != null) {
							descriptorPreference.addValue(elementText);
						}
					}
					else if ("version".equals(elementName)) {
						descriptorResourceDependency.setVersion(elementText);
					}
					else if ("window-state".equals(elementName)) {

						if (descriptorSupports != null) {
							descriptorSupports.addWindowState(elementText);
						}
						//J-
						/*
						else {
							customWindowState = elementText;
						}
						*/
						//J+
					}
				}
			}
		}
		catch (IOException | XMLStreamException e) {

			xmlStreamReader.close();

			if (inputStream != null) {
				inputStream.close();
			}

			throw e;
		}

		xmlStreamReader.close();
		inputStream.close();

		return portletDescriptor;
	}

	private static String _getDescriptorAttribute(
			XMLInputFactory xmlInputFactory, URL portletDescriptorURL,
			String localName) throws IOException, XMLStreamException {

		String descriptorAttribute = null;
		InputStream inputStream = null;
		XMLStreamReader xmlStreamReader = null;

		try {
			inputStream = portletDescriptorURL.openStream();
			xmlStreamReader = xmlInputFactory.createXMLStreamReader(
				inputStream);

			while (xmlStreamReader.hasNext()) {

				xmlStreamReader.next();

				int eventType = xmlStreamReader.getEventType();

				if (eventType == XMLStreamConstants.START_ELEMENT) {

					String elementName = xmlStreamReader.getLocalName();

					if ("portlet-app".equals(elementName)) {

						int attributeCount =
							xmlStreamReader.getAttributeCount();

						for (int i = 0; i < attributeCount; i++) {

							if (localName.equals(
									xmlStreamReader.getAttributeLocalName(i))) {
								descriptorAttribute =
									xmlStreamReader.getAttributeValue(i);

								break;
							}
						}
					}
				}

				if (descriptorAttribute != null) {
					break;
				}
			}
		}
		catch (IOException | XMLStreamException e) {

			if (xmlStreamReader != null) {
				xmlStreamReader.close();
			}

			if (inputStream != null) {
				inputStream.close();
			}

			throw e;
		}

		xmlStreamReader.close();
		inputStream.close();

		return descriptorAttribute;
	}

	private static String _getDescriptorSchemaURL(
			XMLInputFactory xmlInputFactory, URL portletDescriptorURL)
		throws IOException, XMLStreamException {

		String descriptorSchemaURL = _getDescriptorAttribute(
			xmlInputFactory, portletDescriptorURL, "schemaLocation");

		if (descriptorSchemaURL != null) {

			String[] parts = descriptorSchemaURL.split("\\s+");

			descriptorSchemaURL = null;

			for (String part : parts) {

				if (part.endsWith(".xsd")) {
					descriptorSchemaURL = part;

					break;
				}
			}
		}

		return descriptorSchemaURL;
	}

	private static String _getDescriptorVersion(
			XMLInputFactory xmlInputFactory, URL portletDescriptorURL,
			String defaultValue) throws IOException, XMLStreamException {

		String descriptorVersion = _getDescriptorAttribute(
			xmlInputFactory, portletDescriptorURL, "version");

		if (descriptorVersion == null) {
			return defaultValue;
		}

		return descriptorVersion;
	}

	private static String _getLocalPart(String name) {

		String localPart = name;

		int pos = name.indexOf(":");

		if (pos > 0) {
			localPart = name.substring(pos + 1);
		}

		return localPart;
	}

	private static String _getPrefix(String name) {

		String prefix = null;

		int pos = name.indexOf(":");

		if (pos > 0) {
			prefix = name.substring(0, pos);
		}

		return prefix;
	}

	private static String _validateXML(
			XMLInputFactory xmlInputFactory, URL portletDescriptorURL)
		throws IOException, SAXException, XMLStreamException {

		PortletApplication defaultPortletApplication =
			DefaultPortletApplication.class.getAnnotation(
				PortletApplication.class);

		String descriptorVersion = defaultPortletApplication.version();

		String descriptorSchemaURL = _getDescriptorSchemaURL(
			xmlInputFactory, portletDescriptorURL);

		if (descriptorSchemaURL == null) {
			descriptorVersion = _getDescriptorVersion(
				xmlInputFactory, portletDescriptorURL, descriptorVersion);

			if ((descriptorVersion != null) &&
				descriptorVersion.startsWith("2")) {
				descriptorSchemaURL =
					"http://java.sun.com/xml/ns/portlet/portlet-app_2_0.xsd";
			}
			else {
				descriptorSchemaURL =
					"http://xmlns.jcp.org/xml/ns/portlet/portlet-app_3_0.xsd";
			}
		}

		InputStream inputStream = null;
		XMLStreamReader xmlStreamReader = null;

		try {

			inputStream = portletDescriptorURL.openStream();
			xmlStreamReader = xmlInputFactory.createXMLStreamReader(
				inputStream);

			SchemaFactory schemaFactory = SchemaFactory.newInstance(
				XMLConstants.W3C_XML_SCHEMA_NS_URI);

			Schema schema = schemaFactory.newSchema(
				new URL(descriptorSchemaURL));

			Validator validator = schema.newValidator();
			StAXSource stAXSource = new StAXSource(xmlStreamReader);
			validator.validate(stAXSource);
		}
		catch (IOException | SAXException | XMLStreamException e) {

			if (xmlStreamReader != null) {
				xmlStreamReader.close();
			}

			if (inputStream != null) {
				inputStream.close();
			}

			throw e;
		}

		xmlStreamReader.close();
		inputStream.close();

		return descriptorVersion;
	}

}

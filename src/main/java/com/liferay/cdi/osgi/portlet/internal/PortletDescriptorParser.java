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

import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.util.List;
import java.util.Map;

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
		BeanApp beanApp = new BeanAppDecriptorImpl();
		XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();

		_validateXML(xmlInputFactory, portletDescriptorURL);

		String elementName;
		String elementText = null;
		BeanFilterDescriptorImpl beanFilter = null;
		BeanPortletDescriptorImpl beanPortlet = null;
		EventDefinition eventDefinition = null;
		BeanPortletDescriptorImpl.FilterMapping filterMapping = null;
		BeanPortletDescriptorImpl.InitParam initParam = null;
		String namespaceURI = null;
		BeanPortletDescriptorImpl.Preference preference = null;
		BeanPortletDescriptorImpl.RoleRef roleRef = null;
		BeanPortletDescriptorImpl.SupportedEvent supportedPublishingEvent =
			null;
		PublicRenderParam publicRenderParam = null;
		BeanPortletDescriptorImpl.SupportedEvent supportedProcessingEvent =
			null;
		BeanPortletDescriptorImpl.Supports supports = null;
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

					if ("event-definition".equals(elementName)) {
						eventDefinition = new EventDefinitionDescriptorImpl(
							beanApp);
					}
					else if ("filter".equals(elementName)) {
						beanFilter = new BeanFilterDescriptorImpl();
					}
					else if ("filter-mapping".equals(elementName)) {
						filterMapping =
							new BeanPortletDescriptorImpl.FilterMapping();
					}
					else if ("init-param".equals(elementName)) {
						initParam = new BeanPortletDescriptorImpl.InitParam();
					}
					else if ("portlet".equals(elementName)) {
						beanPortlet = new BeanPortletDescriptorImpl(beanApp);
					}
					else if ("preference".equals(elementName)) {
						preference = new BeanPortletDescriptorImpl.Preference();
					}
					else if ("public-render-parameter".equals(elementName)) {
						publicRenderParam = new PublicRenderParamDescriptorImpl(
							beanApp);
					}
					else if ("security-role-ref".equals(elementName)) {
						roleRef = new BeanPortletDescriptorImpl.RoleRef();
					}
					else if ("supported-processing-event".equals(elementName)) {
						supportedProcessingEvent =
							new BeanPortletDescriptorImpl.SupportedEvent();
					}
					else if ("supported-publishing-event".equals(elementName)) {
						supportedPublishingEvent =
							new BeanPortletDescriptorImpl.SupportedEvent();
					}
					else if ("supports".equals(elementName)) {
						supports = new BeanPortletDescriptorImpl.Supports();
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
					else if ("default-namespace".equals(elementName)) {
						beanApp.setDefaultNamespace(elementText);
					}
					else if ("description".equals(elementName)) {
						beanPortlet.setDescription(elementText);
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
									filterMapping.getFilterName())) {

								List<String> portletNames =
									curBeanFilter.getPortletNames();

								portletNames.addAll(
									filterMapping.getPortletNames());

								found = true;

								break;
							}
						}

						if (!found) {
							throw new IOException(
								"filter-mapping specified filter-name=" +
								filterMapping.getFilterName() +
								" but there is no corresponding filter with" +
								" that name.");
						}

						filterMapping = null;
					}
					else if ("filter-name".equals(elementName)) {

						if (filterMapping != null) {
							filterMapping.setFilterName(elementText);
						}
						else if (beanFilter != null) {
							beanFilter.setFilterName(elementText);
						}
					}
					else if ("identifier".equals(elementName)) {
						publicRenderParam.setIdentifier(elementText);
					}
					else if ("init-param".equals(elementName)) {
						beanPortlet.addInitParam(initParam);
						initParam = null;
					}
					else if ("keywords".equals(elementName)) {
						beanPortlet.addKeywords(elementText);
					}
					else if ("mime-type".equals(elementName)) {
						supports.setMimeType(elementText);
					}
					else if ("name".equals(elementName)) {

						if (initParam != null) {
							initParam.setName(elementText);
						}
						else if (publicRenderParam != null) {
							publicRenderParam.setName(elementText);
						}
						else if (preference != null) {
							preference.setName(elementText);
						}
						else if (supportedProcessingEvent != null) {
							supportedProcessingEvent =
								new BeanPortletDescriptorImpl.SupportedEvent(
									beanApp, _getLocalPart(elementText));
						}
						else if (supportedPublishingEvent != null) {
							supportedPublishingEvent =
								new BeanPortletDescriptorImpl.SupportedEvent(
									beanApp, _getLocalPart(elementText));
						}
					}
					else if ("ordinal".equals(elementName)) {

						if (beanFilter != null) {
							beanFilter.setOrdinal(
								Integer.parseInt(elementText));
						}
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
						else if (filterMapping != null) {
							filterMapping.addPortletName(elementText);
						}
					}
					else if ("portlet-mode".equals(elementName)) {
						supports.addPortletMode(elementText);
					}
					else if ("preference".equals(elementName)) {
						beanPortlet.addPreference(preference);
						preference = null;
					}
					else if ("public-render-parameter".equals(elementName)) {
						Map<String, PublicRenderParam> publicRenderParameterMap =
							beanApp.getPublicRenderParameterMap();
						publicRenderParameterMap.put(
							publicRenderParam.getIdentifier(),
							publicRenderParam);
						publicRenderParam = null;
					}
					else if ("qname".equals(elementName)) {

						String prefix = _getPrefix(elementText);

						if (prefix != null) {
							namespaceURI = xmlStreamReader.getNamespaceURI(
								prefix);
						}

						String localPart = _getLocalPart(elementText);

						if (supportedProcessingEvent != null) {
							supportedProcessingEvent.setQName(
								new QName(namespaceURI, localPart));
						}
						else if (publicRenderParam != null) {
							publicRenderParam.setQName(
								new QName(namespaceURI, localPart));
						}
						else if (supportedPublishingEvent != null) {
							supportedPublishingEvent.setQName(
								new QName(namespaceURI, localPart));
						}
					}
					else if ("resource-bundle".equals(elementName)) {
						beanPortlet.setResourceBundle(elementText);
					}
					else if ("role-link".equals(elementName)) {
						roleRef.setRoleLink(elementText);
					}
					else if ("role-name".equals(elementName)) {
						roleRef.setRoleName(elementText);
					}
					else if ("security-role-ref".equals(elementName)) {
						beanPortlet.addRoleRef(roleRef);
						roleRef = null;
					}
					else if ("short-title".equals(elementName)) {
						beanPortlet.addShortTitle(elementText);
					}
					else if ("supported-processing-event".equals(elementName)) {
						beanPortlet.addSupportedProcessingEvent(
							supportedProcessingEvent);
						supportedProcessingEvent = null;
					}
					else if ("supported-publishing-event".equals(elementName)) {
						beanPortlet.addSupportedPublishingEvent(
							supportedPublishingEvent);
						supportedPublishingEvent = null;
					}
					else if (
						"supported-public-render-parameter".equals(
							elementName)) {
						beanPortlet.addSupportedPublicRenderParamName(
							elementText);
					}
					else if ("supports".equals(elementName)) {
						beanPortlet.addSupports(supports);
						supports = null;
					}
					else if ("title".equals(elementName)) {
						beanPortlet.addTitle(elementText);
					}
					else if ("value".equals(elementName)) {

						if (initParam != null) {
							initParam.setValue(elementText);
						}
						else if (preference != null) {
							preference.addValue(elementText);
						}
					}
					else if ("value-type".equals(elementName)) {
						eventDefinition.setValueType(elementText);
					}
					else if ("window-state".equals(elementName)) {
						supports.addWindowState(elementText);
					}
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

	private static int _getDescriptorMajorVersion(
			XMLInputFactory xmlInputFactory, URL portletDescriptorURL)
		throws IOException, XMLStreamException {

		String descriptorVersion = _getDescriptorAttribute(
			xmlInputFactory, portletDescriptorURL, "version");

		if (descriptorVersion == null) {
			return 3;
		}

		try {
			descriptorVersion = descriptorVersion.trim();

			int pos = descriptorVersion.indexOf(".");

			if (pos > 0) {
				descriptorVersion = descriptorVersion.substring(0, pos);
			}

			return Integer.parseInt(descriptorVersion);
		}
		catch (NumberFormatException e) {
			throw new XMLStreamException(e);
		}
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

	private static void _validateXML(
			XMLInputFactory xmlInputFactory, URL portletDescriptorURL)
		throws IOException, SAXException, XMLStreamException {

		String descriptorSchemaURL = _getDescriptorSchemaURL(
			xmlInputFactory, portletDescriptorURL);

		if (descriptorSchemaURL == null) {
			int descriptorVersion = _getDescriptorMajorVersion(
				xmlInputFactory, portletDescriptorURL);

			if (descriptorVersion == 2) {
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
	}

}

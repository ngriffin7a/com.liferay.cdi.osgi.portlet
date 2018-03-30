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

import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.xml.sax.SAXException;

/**
 * @author Neil Griffin
 */
public class LiferayDescriptorParser {

	public static LiferayDescriptor parse(URL liferayDescriptorURL)
		throws IOException, SAXException, XMLStreamException {

		LiferayDescriptor liferayDescriptor = new LiferayDescriptor();
		XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();

		String elementName;
		String elementText = null;
		InputStream inputStream = null;
		Map<String, String> portletConfiguration = null;
		Location portletLocation = null;
		String portletName = null;
		XMLStreamReader xmlStreamReader = null;

		try {
			inputStream = liferayDescriptorURL.openStream();
			xmlStreamReader = xmlInputFactory.createXMLStreamReader(
				inputStream);

			while (xmlStreamReader.hasNext()) {
				xmlStreamReader.next();

				int eventType = xmlStreamReader.getEventType();

				if (eventType == XMLStreamConstants.START_ELEMENT) {
					elementName = xmlStreamReader.getLocalName();

					if ("portlet".equals(elementName)) {
						portletConfiguration = new HashMap<>();
						portletLocation = xmlStreamReader.getLocation();
					}
				}
				else if (eventType == XMLStreamConstants.CHARACTERS) {
					elementText = xmlStreamReader.getText();
					elementText = elementText.trim();
				}
				else if (eventType == XMLStreamConstants.END_ELEMENT) {
					elementName = xmlStreamReader.getLocalName();

					if ("portlet".equals(elementName)) {

						if (portletName == null) {
							throw new SAXException(
								"<portlet-name> not found " +
								"for <portlet> at line#" +
								portletLocation.getLineNumber());
						}

						liferayDescriptor.addPortletConfiguration(
							portletName, portletConfiguration);
						portletConfiguration = null;
						portletLocation = null;
					}
					else if ("portlet-name".equals(elementName)) {
						portletName = elementText;
					}
					else {

						if (portletConfiguration != null) {
							portletConfiguration.put(
								"com.liferay.portlet." + elementName,
								elementText);
						}
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

		return liferayDescriptor;
	}
}

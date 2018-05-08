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

import com.liferay.petra.string.StringPool;
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
public class DisplayDescriptorParser {

	public static Map<String, String> parse(URL displayDescriptorURL)
		throws IOException, SAXException, XMLStreamException {

		Map<String, String> displayCategoryMap = new HashMap<>();
		XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();

		String category = null;
		String elementName;
		InputStream inputStream = null;
		String portletId = null;
		XMLStreamReader xmlStreamReader = null;

		try {
			inputStream = displayDescriptorURL.openStream();
			xmlStreamReader = xmlInputFactory.createXMLStreamReader(
				inputStream);

			while (xmlStreamReader.hasNext()) {
				xmlStreamReader.next();

				int eventType = xmlStreamReader.getEventType();

				if (eventType == XMLStreamConstants.START_ELEMENT) {
					elementName = xmlStreamReader.getLocalName();

					if ("category".equals(elementName)) {
						category = xmlStreamReader.getAttributeValue(
							StringPool.BLANK, "name");

						if (category == null) {
							Location location = xmlStreamReader.getLocation();
							throw new SAXException(
								"name attribute not found " +
								"for <category> at line#" +
								location.getLineNumber());
						}
					}
					else if ("portlet".equals(elementName)) {
						portletId = xmlStreamReader.getAttributeValue(
							StringPool.BLANK, "id");

						if (portletId == null) {
							Location location = xmlStreamReader.getLocation();
							throw new SAXException(
								"id attribute not found " +
								"for <portlet> at line#" +
								location.getLineNumber());
						}
					}
				}
				else if (eventType == XMLStreamConstants.END_ELEMENT) {
					elementName = xmlStreamReader.getLocalName();

					if ("category".equals(elementName)) {
						category = null;
					}
					else if ("portlet".equals(elementName)) {
						displayCategoryMap.put(portletId, category);
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

		return displayCategoryMap;
	}
}

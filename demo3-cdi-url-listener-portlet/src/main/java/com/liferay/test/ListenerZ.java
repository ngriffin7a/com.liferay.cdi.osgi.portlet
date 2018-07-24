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

package com.liferay.test;

import javax.portlet.PortletURL;
import javax.portlet.annotations.PortletListener;

/**
 * @author Neil Griffin
 */
@PortletListener(ordinal = 1)
public class ListenerZ extends ListenerBase {

	@Override
	public void filterActionURL(PortletURL portletURL) {

		if ("true".equals(
				getParameterValue(portletURL, "urlToBeConcernedWith"))) {
			System.err.println("Executing ListenerZ ordinal=1");
			portletURL.setParameter("c", "success_ListenerZ");
			portletURL.setParameter("d", "fail_ListenerZ");
		}
	}
}

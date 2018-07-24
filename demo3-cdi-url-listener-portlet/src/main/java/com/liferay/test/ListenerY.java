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
@PortletListener(ordinal = 0)
public class ListenerY extends ListenerBase {

	@Override
	public void filterActionURL(PortletURL portletURL) {

		if ("true".equals(
				getParameterValue(portletURL, "urlToBeConcernedWith"))) {
			System.err.println("Executing ListenerY ordinal=0");
			portletURL.setParameter("b", "success_ListenerY");
			portletURL.setParameter("c", "fail_ListenerY");
			portletURL.setParameter("d", "fail_ListenerY");
		}
	}
}

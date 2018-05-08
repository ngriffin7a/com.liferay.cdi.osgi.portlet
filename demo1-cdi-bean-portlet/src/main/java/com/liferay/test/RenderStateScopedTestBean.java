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

import javax.portlet.annotations.PortletSerializable;
import javax.portlet.annotations.RenderStateScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Neil Griffin
 */
@RenderStateScoped(paramName = "renderStateParam1")
public class RenderStateScopedTestBean implements PortletSerializable {

	@Override
	public void deserialize(String[] state) {

		if (state.length >= 2) {
			foo = state[0];
			bar = state[1];
		}

		if (state.length > 2) {
			_log.error(
				"PortletURLImpl.generateToString() is STILL appending " +
				"old private render parameters. Pluto doesn't do this. Need " +
				"to revisit this after everything is merged.");
		}
	}

	public String getBar() {
		return bar;
	}

	public String getFoo() {
		return foo;
	}

	@Override
	public String[] serialize() {
		return new String[] {foo, bar};
	}

	public void setBar(String bar) {
		this.bar = bar;
	}

	public void setFoo(String foo) {
		this.foo = foo;
	}

	private static final Logger _log = LoggerFactory.getLogger(
		RenderStateScopedTestBean.class);

	private String foo;

	private String bar;
}

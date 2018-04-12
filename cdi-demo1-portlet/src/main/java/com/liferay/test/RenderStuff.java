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

import java.io.IOException;
import java.io.PrintWriter;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;

import javax.inject.Inject;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.annotations.RenderMethod;

/**
 * @author Neil Griffin
 */
@ApplicationScoped
public class RenderStuff {

	@RenderMethod(portletNames = {"helloBeanPortlet"})
	public void myRenderMethod(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws IOException {
		PrintWriter writer = renderResponse.getWriter();
		writer.write(
			"<p>Hooray! myRenderMethod</p> beanManager=" + _beanManager);
	}

	@Inject
	BeanManager _beanManager;
}

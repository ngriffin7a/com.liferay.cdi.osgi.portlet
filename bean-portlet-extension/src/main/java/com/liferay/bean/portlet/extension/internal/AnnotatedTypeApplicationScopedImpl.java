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

import java.lang.annotation.Annotation;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.AnnotatedType;

import javax.portlet.annotations.PortletRequestScoped;
import javax.portlet.annotations.PortletSessionScoped;

/**
 * @author Neil Griffin
 */
public class AnnotatedTypeApplicationScopedImpl<X>
	extends AnnotatedTypeWrapper<X> {

	public AnnotatedTypeApplicationScopedImpl(
			AnnotatedType<X> annotatedType) {

		super(annotatedType);

		_annotations = annotatedType.getAnnotations()
			.stream()
				.filter(
						annotation ->
							!annotation.annotationType()
								.equals(ConversationScoped.class) &&
							!annotation.annotationType()
								.equals(RequestScoped.class) &&
							!annotation.annotationType()
								.equals(PortletRequestScoped.class) &&
							!annotation.annotationType()
								.equals(PortletSessionScoped.class) &&
							!annotation.annotationType()
								.equals(SessionScoped.class))
				.collect(Collectors.toSet());

		_annotations.add(
			DefaultApplicationScoped.class.getAnnotation(
				ApplicationScoped.class));
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationType) {

		for (Annotation annotation : _annotations) {

			if (annotation.annotationType()
					.equals(annotationType)) {
				return (T) annotation;
			}
		}

		return null;
	}

	@Override
	public Set<Annotation> getAnnotations() {
		return _annotations;
	}

	@Override
	public boolean isAnnotationPresent(
			Class<? extends Annotation> annotationType) {

		for (Annotation annotation : _annotations) {

			if (annotation.annotationType()
					.equals(annotationType)) {
				return true;
			}
		}

		return false;
	}

	private Set<Annotation> _annotations = new HashSet<>();
}

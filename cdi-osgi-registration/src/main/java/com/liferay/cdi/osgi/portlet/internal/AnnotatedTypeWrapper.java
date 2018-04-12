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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

/**
 * @author Neil Griffin
 */
public class AnnotatedTypeWrapper<X> implements AnnotatedType<X> {

	public AnnotatedTypeWrapper(AnnotatedType<X> annotatedType) {
		_annotatedType = annotatedType;
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
		return _annotatedType.getAnnotation(annotationType);
	}

	@Override
	public Set<Annotation> getAnnotations() {
		return _annotatedType.getAnnotations();
	}

	@Override
	public Type getBaseType() {
		return _annotatedType.getBaseType();
	}

	@Override
	public Set<AnnotatedConstructor<X>> getConstructors() {
		return _annotatedType.getConstructors();
	}

	@Override
	public Set<AnnotatedField<? super X>> getFields() {
		return _annotatedType.getFields();
	}

	@Override
	public Class<X> getJavaClass() {
		return _annotatedType.getJavaClass();
	}

	@Override
	public Set<AnnotatedMethod<? super X>> getMethods() {
		return _annotatedType.getMethods();
	}

	@Override
	public Set<Type> getTypeClosure() {
		return _annotatedType.getTypeClosure();
	}

	@Override
	public boolean isAnnotationPresent(
			Class<? extends Annotation> annotationType) {
		return _annotatedType.isAnnotationPresent(annotationType);
	}

	private AnnotatedType<X> _annotatedType;
}
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

package com.liferay.cdi.bean.producer.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;

/**
 * @author Neil Griffin
 */
public abstract class DynamicProducerBase<T> implements Bean<T> {

	@Override
	public abstract T create(CreationalContext<T> creationalContext);

	@Override
	public abstract Class<?> getBeanClass();

	@Override
	public abstract String getName();

	@Override
	public abstract Class<? extends Annotation> getScope();

	@Override
	public void destroy(T t, CreationalContext<T> creationalContext) {
	}

	@Override
	public Set<InjectionPoint> getInjectionPoints() {
		return Collections.emptySet();
	}

	@Override
	public Set<Annotation> getQualifiers() {
		return Collections.singleton(new DefaultQualifier());
	}

	@Override
	public Set<Class<? extends Annotation>> getStereotypes() {
		return Collections.emptySet();
	}

	@Override
	public Set<Type> getTypes() {
		return new HashSet<>(Arrays.asList(getBeanClass(), Object.class));
	}

	@Override
	public boolean isAlternative() {
		return false;
	}

	@Override
	public boolean isNullable() {
		return false;
	}

	public static class DefaultQualifier extends AnnotationLiteral<Default> {
		private static final long serialVersionUID = 1L;
	}
}

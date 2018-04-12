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

import com.liferay.bean.portlet.extension.LiferayPortletConfiguration;
import com.liferay.bean.portlet.extension.LiferayPortletConfigurations;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventPortlet;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.HeaderPortlet;
import javax.portlet.HeaderRequest;
import javax.portlet.HeaderResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceServingPortlet;
import javax.portlet.annotations.ActionMethod;
import javax.portlet.annotations.ContextPath;
import javax.portlet.annotations.DestroyMethod;
import javax.portlet.annotations.EventMethod;
import javax.portlet.annotations.HeaderMethod;
import javax.portlet.annotations.InitMethod;
import javax.portlet.annotations.Namespace;
import javax.portlet.annotations.PortletApplication;
import javax.portlet.annotations.PortletConfiguration;
import javax.portlet.annotations.PortletConfigurations;
import javax.portlet.annotations.PortletLifecycleFilter;
import javax.portlet.annotations.PortletName;
import javax.portlet.annotations.PortletRequestScoped;
import javax.portlet.annotations.PortletSerializable;
import javax.portlet.annotations.PortletSessionScoped;
import javax.portlet.annotations.RenderMethod;
import javax.portlet.annotations.RenderStateScoped;
import javax.portlet.annotations.ServeResourceMethod;
import javax.portlet.annotations.WindowId;
import javax.portlet.filter.PortletFilter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Neil Griffin
 * @author Raymond Augé
 */
public class BeanPortletExtension implements Extension {

	public void afterBeanDiscovery(
			@Observes AfterBeanDiscovery afterBeanDiscovery) {

		BundleContext bundleContext = FrameworkUtil.getBundle(
				BeanPortletExtension.class)
				.getBundleContext();

		Bundle bundle = bundleContext.getBundle();

		URL portletDescriptorURL = bundle.getEntry("/WEB-INF/portlet.xml");

		if (portletDescriptorURL != null) {

			try {
				PortletDescriptor portletDescriptor = PortletDescriptorParser
					.parse(portletDescriptorURL);

				_beanFilters.addAll(portletDescriptor.getBeanFilters());

				portletDescriptor.getBeanPortlets()
					.forEach(
						beanPortlet ->
							_beanPortlets.put(
								beanPortlet.getPortletName(), beanPortlet));
			}
			catch (Exception e) {
				_log.error(e.getMessage(), e);
			}
		}

		_portletConfigurationsClasses.forEach(
			clazz ->
				Arrays.stream(
						clazz.getAnnotation(PortletConfigurations.class)
							.value())
					.forEach(
						portletConfiguration ->
							scanBeanPortlet(clazz, portletConfiguration)));

		_portletConfigurationClasses.forEach(
			clazz ->
				scanBeanPortlet(
					clazz, clazz.getAnnotation(PortletConfiguration.class)));

		_beanFilters.addAll(
			_portletLifecycleFilterClasses.stream()
				.map(
						annotatedClass ->
							BeanFilterFactory.create(
								annotatedClass,
								annotatedClass.getAnnotation(
									PortletLifecycleFilter.class)))
				.collect(Collectors.toList()));

		URL liferayDescriptorURL = bundle.getEntry(
			"WEB-INF/liferay-portlet.xml");

		if (liferayDescriptorURL != null) {

			try {
				LiferayDescriptor liferayDescriptor = LiferayDescriptorParser
					.parse(liferayDescriptorURL);

				liferayDescriptor.getPortletNames()
					.stream()
					.peek(
							portletName -> {

								if (!_beanPortlets.containsKey(portletName)) {
									_log.error(
										"liferay-portlet.xml specifies <portlet-name>" +
										"{}</portlet-name> but there is no such name " +
										"specified in portlet.xml or in " +
										"@PortletConfiguration", portletName);
								}
							})
					.map(portletName -> _beanPortlets.get(portletName))
					.filter(Objects::nonNull)
					.forEach(
						beanPortlet ->
							beanPortlet.addParsedLiferayPortletConfiguration(
								liferayDescriptor.getPortletConfiguration(
									beanPortlet.getPortletName())));
			}
			catch (Exception e) {
				_log.error(e.getMessage(), e);
			}
		}

		afterBeanDiscovery.addContext(new PortletRequestBeanContext());
		afterBeanDiscovery.addContext(new PortletSessionBeanContext());
		afterBeanDiscovery.addContext(new RenderStateBeanContext());
	}

	public void applicationScopedInitialized(
			@Observes
			@Initialized(ApplicationScoped.class)
			ServletContext servletContext, BeanManager beanManager) {

		associateMethods(beanManager, BeanMethod.Type.ACTION, _actionMethods);
		associateMethods(beanManager, BeanMethod.Type.DESTROY, _destroyMethods);
		associateMethods(beanManager, BeanMethod.Type.EVENT, _eventMethods);
		associateMethods(beanManager, BeanMethod.Type.HEADER, _headerMethods);
		associateMethods(beanManager, BeanMethod.Type.INIT, _initMethods);
		associateMethods(beanManager, BeanMethod.Type.RENDER, _renderMethods);
		associateMethods(
			beanManager, BeanMethod.Type.SERVE_RESOURCE, _serveResourceMethods);

		BundleContext bundleContext = FrameworkUtil.getBundle(
				BeanPortletExtension.class)
				.getBundleContext();

		String servletContextName = servletContext.getServletContextName();

		_portletRegistrations = _beanPortlets.entrySet()
			.stream()
				.map(
						entry ->
							RegistrationUtil.registerBeanPortlet(
								bundleContext, entry.getValue(),
								servletContextName))
				.collect(Collectors.toList());

		_beanFilters.stream()
			.forEach(
				beanFilter ->
					beanFilter.getPortletNames()
						.stream()
						.forEach(
							portletName ->
								_filterRegistrations.addAll(
									RegistrationUtil.registerBeanFilter(
										bundleContext, portletName,
										_beanPortlets.keySet(), beanFilter,
										beanManager))));

	}

	public void beforeBeanDiscovery(
			@Observes BeforeBeanDiscovery beforeBeanDiscovery,
			BeanManager beanManager) {
		beforeBeanDiscovery.addQualifier(ContextPath.class);
		beforeBeanDiscovery.addQualifier(Namespace.class);
		beforeBeanDiscovery.addQualifier(PortletName.class);
		beforeBeanDiscovery.addQualifier(WindowId.class);

		beforeBeanDiscovery.addScope(PortletRequestScoped.class, true, false);
		beforeBeanDiscovery.addScope(PortletSessionScoped.class, true, true);
		beforeBeanDiscovery.addScope(RenderStateScoped.class, true, false);

		AnnotatedType<?> beanAnnotatedType = beanManager.createAnnotatedType(
			JSR362BeanProducer.class);
		beforeBeanDiscovery.addAnnotatedType(beanAnnotatedType, null);
	}

	public <T> void processAnnotatedType(
			@Observes ProcessAnnotatedType<T> processAnnotatedType) {

		AnnotatedType<T> annotatedType =
			processAnnotatedType.getAnnotatedType();

		Class<T> annotatedClass = annotatedType.getJavaClass();

		Set<Type> typeClosure = annotatedType.getTypeClosure();

		if (typeClosure.contains(PortletConfig.class)) {
			annotatedType = new AnnotatedTypePortletConfigImpl(annotatedType);
			processAnnotatedType.setAnnotatedType(annotatedType);
		}

		Set<Class<? extends Annotation>> annotationClasses =
			annotatedType.getAnnotations()
				.stream()
				.map(annotation -> annotation.annotationType())
				.collect(Collectors.toSet());

		if (annotationClasses.contains(RequestScoped.class)) {
			annotatedType = new AnnotatedTypeRequestScopedImpl(
				annotatedType, annotationClasses);
			processAnnotatedType.setAnnotatedType(annotatedType);
		}

		if (annotationClasses.contains(SessionScoped.class)) {
			annotatedType = new AnnotatedTypeSessionScopedImpl<>(
				annotatedType, annotationClasses);
			processAnnotatedType.setAnnotatedType(annotatedType);
		}

		if (annotationClasses.contains(RenderStateScoped.class) &&
			!PortletSerializable.class.isAssignableFrom(annotatedClass)) {
			_log.warn(
				"{} does not implement {}", annotatedClass.getName(),
				PortletSerializable.class.getName());
		}

		if (annotationClasses.contains(PortletSessionScoped.class)) {

			PortletSessionScoped portletSessionScoped =
				annotatedClass.getAnnotation(PortletSessionScoped.class);

			if ((PortletSession.APPLICATION_SCOPE !=
					portletSessionScoped.value()) &&
				(PortletSession.PORTLET_SCOPE !=
					portletSessionScoped.value())) {

				if (_log.isWarnEnabled()) {
					_log.warn(
						"@PortletSessionScoped bean can only be " +
						"PortletSession.APPLICATION_SCOPE or " +
						"PortletSession.PORTLET_SCOPE");
				}
			}
		}

		if (annotationClasses.contains(PortletApplication.class)) {

			if (_portletApplicationClass == null) {
				_portletApplicationClass = annotatedClass;
			}
			else {
				_log.error(
					"Found more than one @PortletApplication annotated class.");
			}
		}

		if (annotationClasses.contains(PortletConfigurations.class)) {
			_portletConfigurationsClasses.add(annotatedClass);

		}

		if (annotationClasses.contains(PortletConfiguration.class)) {
			_portletConfigurationClasses.add(annotatedClass);
		}

		if (annotationClasses.contains(PortletLifecycleFilter.class)) {
			_portletLifecycleFilterClasses.add(annotatedClass);
		}

		if (annotationClasses.contains(LiferayPortletConfigurations.class)) {
			_liferayPortletConfigurationsClasses.add(annotatedClass);
		}

		if (annotationClasses.contains(LiferayPortletConfiguration.class)) {
			_liferayPortletConfigurationClasses.add(annotatedClass);
		}
	}

	protected void applicationScopedBeforeDestroyed(
			@Observes
			@Destroyed(ApplicationScoped.class)
			Object ignore) {

		_portletRegistrations.removeIf(
			serviceRegistration -> {
				serviceRegistration.unregister();

				return true;
			});

		_filterRegistrations.removeIf(
			serviceRegistration -> {
				serviceRegistration.unregister();

				return true;
			});
	}

	protected void associateMethods(
			BeanManager beanManager, BeanMethod.Type beanMethodType,
			List<ScannedMethod> scannedMethods) {

		for (ScannedMethod scannedMethod : scannedMethods) {

			Class<?> clazz = scannedMethod.getClazz();
			Method method = scannedMethod.getMethod();
			String configuredPortletName =
				scannedMethod.getConfiguredPortletName();
			BeanMethod beanMethod = new BeanMethodImpl(
				beanManager, beanMethodType, clazz, method,
				configuredPortletName);
			Class<?> beanClass = beanMethod.getBeanClass();
			String[] portletNames = beanMethod.getPortletNames();

			if (portletNames == null) {

				String beanClassName = beanClass.getName();

				_beanPortlets.entrySet()
					.stream()
					.map(entry -> entry.getValue())
					.filter(
							beanPortlet ->
								beanClassName.equals(
									beanPortlet.getPortletClass()))
					.forEach(
						beanPortlet -> beanPortlet.addBeanMethod(beanMethod));
			}
			else {

				Arrays.stream(portletNames)
					.peek(
							portletName -> {

								if (!_beanPortlets.containsKey(portletName)) {
									_log.error(
										"No portlet named \"" + portletName +
										"\" was registered via @PortletConfiguration " +
										"for @RenderMethod " + clazz.getName() +
										"." + method.getName());
								}
							})
					.filter(
							portletName ->
								_beanPortlets.containsKey(portletName))
					.forEach(
						portletName ->
							_beanPortlets.get(portletName)
								.addBeanMethod(beanMethod));
			}
		}
	}

	protected List<ScannedMethod> scanMethods(
			Class<?> javaClass, Class<? extends Annotation> annotationClass,
			MethodSignature methodSignature, String configuredPortletName) {

		return Arrays.stream(javaClass.getMethods())
			.filter(
					method ->
						(method.getAnnotation(annotationClass) != null) &&
						methodSignature.isMatch(method))
			.map(
					method ->
						ScannedMethod.create(
							javaClass, method, configuredPortletName))
			.collect(Collectors.toList());
	}

	protected void sessionScopeBeforeDestroyed(
			@Observes
			@Destroyed(SessionScoped.class)
			Object httpSessionObject) {

		HttpSession httpSession = (HttpSession) httpSessionObject;

		Collections.list(httpSession.getAttributeNames())
			.stream()
			.map(attributeName -> httpSession.getAttribute(attributeName))
			.filter(Objects::nonNull)
			.filter(attributeValue ->
						(attributeValue instanceof ScopedBean))
			.forEach(scopedBean -> ((ScopedBean) scopedBean).destroy());
	}

	private LiferayPortletConfiguration getLiferayPortletConfiguration(
			String portletName) {

		for (Class<?> annotatedClass : _liferayPortletConfigurationClasses) {

			LiferayPortletConfiguration liferayPortletConfiguration =
				annotatedClass.getAnnotation(LiferayPortletConfiguration.class);

			if (portletName.equals(liferayPortletConfiguration.portletName())) {
				return liferayPortletConfiguration;
			}
		}

		for (Class<?> annotatedClass : _liferayPortletConfigurationsClasses) {

			LiferayPortletConfigurations liferayPortletConfigurations =
				annotatedClass.getAnnotation(
					LiferayPortletConfigurations.class);

			for (
				LiferayPortletConfiguration liferayPortletConfiguration :
				liferayPortletConfigurations.value()) {

				if (portletName.equals(
						liferayPortletConfiguration.portletName())) {
					return liferayPortletConfiguration;
				}
			}
		}

		return null;
	}

	private void scanBeanPortlet(
			Class<?> annotatedClass,
			PortletConfiguration portletConfiguration) {

		String configuredPortletName = portletConfiguration.portletName();

		if ((configuredPortletName != null) &&
			(configuredPortletName.length() > 0)) {

			if (_portletApplicationClass == null) {
				_beanPortlets.put(
					configuredPortletName,
					BeanPortletFactory.create(
						portletConfiguration,
						getLiferayPortletConfiguration(configuredPortletName),
						annotatedClass.getName()));
			}
			else {
				_beanPortlets.put(
					configuredPortletName,
					BeanPortletFactory.create(
						_portletApplicationClass.getAnnotation(
							PortletApplication.class), portletConfiguration,
						getLiferayPortletConfiguration(configuredPortletName),
						annotatedClass.getName()));
			}
		}
		else {
			_log.error(
				"Invalid portletName attribute for {}",
				annotatedClass.getName());
		}

		if (Portlet.class.isAssignableFrom(annotatedClass)) {

			try {
				_actionMethods.add(
					new ScannedMethod(
						annotatedClass,
						annotatedClass.getMethod(
							"processAction", ActionRequest.class,
							ActionResponse.class), configuredPortletName));
				_destroyMethods.add(
					new ScannedMethod(
						annotatedClass, annotatedClass.getMethod("destroy"),
						configuredPortletName));
				_initMethods.add(
					new ScannedMethod(
						annotatedClass,
						annotatedClass.getMethod("init", PortletConfig.class),
						configuredPortletName));
				_renderMethods.add(
					new ScannedMethod(
						annotatedClass,
						annotatedClass.getMethod(
							"render", RenderRequest.class,
							RenderResponse.class), configuredPortletName));
			}
			catch (NoSuchMethodException e) {
				_log.error(e.getMessage(), e);
			}
		}

		if (EventPortlet.class.isAssignableFrom(annotatedClass)) {

			try {
				_eventMethods.add(
					new ScannedMethod(
						annotatedClass,
						annotatedClass.getMethod(
							"processEvent", EventRequest.class,
							EventResponse.class), configuredPortletName));
			}
			catch (NoSuchMethodException e) {
				_log.error(e.getMessage(), e);
			}
		}

		if (HeaderPortlet.class.isAssignableFrom(annotatedClass)) {

			try {
				_headerMethods.add(
					new ScannedMethod(
						annotatedClass,
						annotatedClass.getMethod(
							"renderHeaders", HeaderRequest.class,
							HeaderResponse.class), configuredPortletName));
			}
			catch (NoSuchMethodException e) {
				_log.error(e.getMessage(), e);
			}
		}

		if (ResourceServingPortlet.class.isAssignableFrom(annotatedClass)) {

			try {
				_serveResourceMethods.add(
					new ScannedMethod(
						annotatedClass,
						annotatedClass.getMethod(
							"serveResource", ResourceRequest.class,
							ResourceResponse.class), configuredPortletName));
			}
			catch (NoSuchMethodException e) {
				_log.error(e.getMessage(), e);
			}
		}

		_actionMethods.addAll(
			scanMethods(
				annotatedClass, ActionMethod.class, MethodSignature.ACTION,
				configuredPortletName));

		_destroyMethods.addAll(
			scanMethods(
				annotatedClass, DestroyMethod.class, MethodSignature.DESTROY,
				configuredPortletName));

		_eventMethods.addAll(
			scanMethods(
				annotatedClass, EventMethod.class, MethodSignature.EVENT,
				configuredPortletName));

		_headerMethods.addAll(
			scanMethods(
				annotatedClass, HeaderMethod.class, MethodSignature.HEADER,
				configuredPortletName));

		_initMethods.addAll(
			scanMethods(
				annotatedClass, InitMethod.class, MethodSignature.INIT,
				configuredPortletName));

		_renderMethods.addAll(
			scanMethods(
				annotatedClass, RenderMethod.class, MethodSignature.RENDER,
				configuredPortletName));

		_serveResourceMethods.addAll(
			scanMethods(
				annotatedClass, ServeResourceMethod.class,
				MethodSignature.SERVE_RESOURCE, configuredPortletName));
	}

	private static class ScannedMethod {

		public ScannedMethod(
				Class<?> clazz, Method method, String configuredPortletName) {
			_clazz = clazz;
			_method = method;
			_configuredPortletName = configuredPortletName;
		}

		public Class<?> getClazz() {
			return _clazz;
		}

		public String getConfiguredPortletName() {
			return _configuredPortletName;
		}

		public Method getMethod() {
			return _method;
		}

		public static ScannedMethod create(
				Class<?> clazz, Method method, String configuredPortletName) {
			return new ScannedMethod(clazz, method, configuredPortletName);
		}

		private Class<?> _clazz;
		private String _configuredPortletName;
		private Method _method;
	}

	private static final Logger _log = LoggerFactory.getLogger(
		BeanPortletExtension.class);

	private List<BeanFilter> _beanFilters = new ArrayList<>();
	private Map<String, BeanPortlet> _beanPortlets = new HashMap<>();
	private List<ServiceRegistration<Portlet>> _portletRegistrations;
	private List<ServiceRegistration<PortletFilter>> _filterRegistrations =
		new ArrayList<>();
	private List<ScannedMethod> _actionMethods = new ArrayList<>();
	private List<ScannedMethod> _destroyMethods = new ArrayList<>();
	private List<ScannedMethod> _eventMethods = new ArrayList<>();
	private List<ScannedMethod> _headerMethods = new ArrayList<>();
	private List<ScannedMethod> _initMethods = new ArrayList<>();
	private Class<?> _portletApplicationClass;
	private List<Class<?>> _liferayPortletConfigurationClasses =
		new ArrayList<>();
	private List<Class<?>> _liferayPortletConfigurationsClasses =
		new ArrayList<>();
	private List<Class<?>> _portletConfigurationClasses = new ArrayList<>();
	private List<Class<?>> _portletConfigurationsClasses = new ArrayList<>();
	private List<Class<?>> _portletLifecycleFilterClasses = new ArrayList<>();
	private List<ScannedMethod> _renderMethods = new ArrayList<>();
	private List<ScannedMethod> _serveResourceMethods = new ArrayList<>();
}

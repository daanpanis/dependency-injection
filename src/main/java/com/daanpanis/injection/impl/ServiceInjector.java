package com.daanpanis.injection.impl;

import com.daanpanis.injection.*;
import com.daanpanis.injection.exceptions.InjectionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ServiceInjector implements DependencyInjector {

    private final Map<Class<?>, ServiceProvider<?>> serviceProviders = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T> void addService(Class<T> instanceClass) {
        Service serviceDefinition = instanceClass.getAnnotation(Service.class);
        if (serviceDefinition == null) throw new InjectionException("No service annotation present on class " + instanceClass);
        Class<?> serviceClass = serviceDefinition.parent() == Service.class ? instanceClass : serviceDefinition.parent();
        if (!instanceClass.isAssignableFrom(serviceClass)) throw new InjectionException(instanceClass + " not assignable from " + serviceClass);

        if (instanceClass.getAnnotation(Singleton.class) != null) {
            addSingleton((Class<? super T>) serviceClass, instanceClass);
        } else {
            addScoped((Class<? super T>) serviceClass, instanceClass);
        }
    }

    @Override
    public <T> void addSingleton(Class<? super T> serviceClass, Class<T> instanceClass) {
        addSingleton(serviceClass, () -> inject(instanceClass));
    }

    @Override
    public <T> void addSingleton(Class<? super T> serviceClass, Supplier<T> instanceSupplier) {
        this.serviceProviders.put(serviceClass, new SingletonProvider<>(instanceSupplier));
    }

    @Override
    public <T> void addScoped(Class<? super T> serviceClass, Class<T> instanceClass) {
        addScoped(serviceClass, () -> inject(instanceClass));
    }

    @Override
    public <T> void addScoped(Class<? super T> serviceClass, Supplier<T> instanceSupplier) {
        this.serviceProviders.put(serviceClass, new ScopedProvider<>(instanceSupplier));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getService(Class<T> serviceClass) {
        ServiceProvider<T> provider = (ServiceProvider<T>) serviceProviders.get(serviceClass);
        return provider != null ? provider.provide() : null;
    }

    protected ServiceProvider getProvider(Class<?> serviceClass) {
        return serviceProviders.get(serviceClass);
    }

    @Override
    public boolean isRegistered(Class<?> serviceClass) {
        return serviceProviders.containsKey(serviceClass);
    }


    @Override
    public <T> T inject(Class<T> toInject) {
        Constructor<T> constructor = findInjectableConstructor(toInject);
        try {
            T instance = injectConstructor(constructor);
            injectFields(instance);
            return instance;
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new InjectionException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Constructor<T> findInjectableConstructor(Class<T> toInject) {
        List<Constructor<T>> constructors = new ArrayList<>();

        constructorLoop:
        for (Constructor<?> constructor : toInject.getDeclaredConstructors()) {

            for (Class<?> param : constructor.getParameterTypes()) {
                if (!isRegistered(param)) continue constructorLoop;
            }

            constructors.add((Constructor<T>) constructor);
        }

        if (constructors.isEmpty()) throw new InjectionException("No injectable constructors found!");
        if (constructors.size() > 1) throw new InjectionException("Multiple injectable constructors found!");
        return constructors.get(0);
    }

    private <T> T injectConstructor(Constructor<T> constructor) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Object[] parameters = new Object[constructor.getParameterTypes().length];
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = getService(constructor.getParameterTypes()[i]);
        }
        constructor.setAccessible(true);
        return constructor.newInstance(parameters);
    }

    private void injectFields(Object object) throws IllegalAccessException {
        for (Field field : object.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || field.getAnnotation(Inject.class) == null) continue;
            Inject inject = field.getAnnotation(Inject.class);
            if (Modifier.isFinal(field.getModifiers())) throw new InjectionException("Injectable field can't be final");
            if (inject.castFrom() == Null.class && !isRegistered(field.getType())) {
                throw new InjectionException("Trying to inject unregistered service: " + field.getType());
            } else if (inject.castFrom() != Null.class && !isRegistered(inject.castFrom())) {
                throw new InjectionException("Trying to inject unregistered service: " + inject.castFrom());
            }
            field.setAccessible(true);
            if (inject.castFrom() == Null.class) {
                field.set(object, getService(field.getType()));
            } else {
                Object service = getService(inject.castFrom());
                if (service != null && service.getClass().isAssignableFrom(field.getType())) {
                    field.set(object, service);
                }
            }
        }
    }
}

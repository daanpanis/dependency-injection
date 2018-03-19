package com.daanpanis.injection;

import java.util.function.Supplier;

public interface DependencyInjector {

    <T> void addSingleton(Class<? super T> serviceClass, Class<T> instanceClass);

    <T> void addSingleton(Class<? super T> serviceClass, Supplier<T> instanceSupplier);

    <T> void addScoped(Class<? super T> serviceClass, Class<T> instanceClass);

    <T> void addScoped(Class<? super T> serviceClass, Supplier<T> instanceSupplier);

    <T> T getService(Class<T> serviceClass);

    boolean isRegistered(Class<?> serviceClass);

    <T> T inject(Class<T> toInject);

}

package com.daanpanis.injection.impl;

import java.util.function.Supplier;

public class SingletonProvider<T> implements ServiceProvider<T> {

    private final Supplier<T> instanceSupplier;
    private T instance;

    public SingletonProvider(Supplier<T> instanceSupplier) {
        this.instanceSupplier = instanceSupplier;
    }

    @Override
    public T provide() {
        if (instance == null) {
            synchronized (instanceSupplier) {
                instance = instanceSupplier.get();
            }
        }
        return instance;
    }
}

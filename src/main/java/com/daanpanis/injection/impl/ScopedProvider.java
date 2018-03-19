package com.daanpanis.injection.impl;

import java.util.function.Supplier;

public class ScopedProvider<T> implements ServiceProvider<T> {

    private final Supplier<T> instanceSupplier;

    public ScopedProvider(Supplier<T> instanceSupplier) {
        this.instanceSupplier = instanceSupplier;
    }

    @Override
    public T provide() {
        return instanceSupplier.get();
    }
}

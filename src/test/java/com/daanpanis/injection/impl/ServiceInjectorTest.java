package com.daanpanis.injection.impl;

import com.daanpanis.injection.exceptions.InjectionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import services.ServiceNoAnnotation;
import services.ServiceNoParent;
import services.ServiceNoParentSingleton;
import services.ServiceParentNotAssignable;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.*;

public class ServiceInjectorTest {

    private ServiceInjector injector;

    @Before
    public void setUp() {
        this.injector = new ServiceInjector();
    }

    @After
    public void tearDown() {
        this.injector = null;
    }

    @Test(expected = InjectionException.class)
    public void addServiceNoAnnotation() {
        this.injector.addService(ServiceNoAnnotation.class);
    }

    @Test(expected = InjectionException.class)
    public void addServiceParentNotAssignable() {
        this.injector.addService(ServiceParentNotAssignable.class);
    }

    @Test
    public void addServiceNoParent() {
        this.injector.addService(ServiceNoParent.class);
        assertThat(this.injector.isRegistered(ServiceNoParent.class), is(true));
        assertThat(this.injector.getProvider(ServiceNoParent.class) instanceof ScopedProvider, is(true));
    }

    @Test
    public void addServiceNoParentSingleton() {
        this.injector.addService(ServiceNoParentSingleton.class);
        assertThat(this.injector.isRegistered(ServiceNoParentSingleton.class), is(true));
        assertThat(this.injector.getProvider(ServiceNoParentSingleton.class) instanceof SingletonProvider, is(true));
    }

}
package org.picketlink.idm.spi.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;

/**
 * Provides a base class for IdentityStore implementations
 * 
 * @author Shane Bryzak
 *
 */
public abstract class AbstractBaseIdentityStore implements IdentityStore {

    private static Method m;

    { 
        try {
            m = IdentityStore.class.getDeclaredMethod("getContext");
        } catch (Exception e) {
            throw new RuntimeException("Error creating IdentityStore - getContext() method not available", e);
        } 
    };

    public IdentityStore forContext(final IdentityStoreInvocationContext ctx) {
        final IdentityStore proxied = this;

        return (IdentityStore) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] {IdentityStore.class}, 
                new InvocationHandler(){

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.equals(m)) {
                            return ctx;
                        }
                        else {
                            return method.invoke(proxied, args);
                        }
                    }});
    }

    /**
     * No-op
     */
    public IdentityStoreInvocationContext getContext() {
        return null;
    }
}

/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketlink.test.weld;

import org.jboss.weld.context.unbound.RequestContextImpl;
import org.jboss.weld.manager.BeanManagerImpl;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.Context;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An ugly hacked up {@link javax.enterprise.inject.spi.Extension} for Weld so that RequestScoped and SessionScoped are active
 * in the JUnit/JavaSE environment.
 *
 * Remember, weld-se does not support either the request scope or the session scope.
 *
 * This is used for JUnit testing only. So no harm done.
 *
 * This extension is loaded via the JDK Service Loader Mechanism and look for a file in META-INF/services directory called
 * javax.enterprise.inject.spi.Extension
 *
 * @author Anil Saldhana
 * @since March 20, 2014
 */
public class WeldServletScopesSupportForSe implements Extension {
    public void afterDeployment(@Observes AfterDeploymentValidation event, BeanManager beanManager) {

        try {
            setContextActive(beanManager, SessionScoped.class);
            setContextActive(beanManager, RequestScoped.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setContextActive(BeanManager beanManager, Class<? extends Annotation> cls) throws Exception {
        BeanManagerImpl beanManagerImpl = (BeanManagerImpl) beanManager;
        // Ugly hack to make the "contexts" map inside the BeanManagerImpl accessible
        Field f = beanManagerImpl.getClass().getDeclaredField("contexts"); // NoSuchFieldException
        f.setAccessible(true);
        Map<Class<? extends Annotation>, List<Context>> contexts = (Map<Class<? extends Annotation>, List<Context>>) f
            .get(beanManagerImpl);

        List<Context> registeredContexts = contexts.get(cls);
        RequestContextImpl context = new RequestContextImpl();

        context.activate();

        List<Context> newList = new ArrayList<Context>();

        newList.add(context);

        contexts.put(cls, newList);

        boolean active = context.isActive();

        if (!active) {
            throw new Exception(cls.getName() + " scope is not active");
        }
    }
}

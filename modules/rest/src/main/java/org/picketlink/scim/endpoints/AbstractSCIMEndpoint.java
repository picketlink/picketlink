/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.scim.endpoints;

import org.jboss.logging.Logger;
import org.picketlink.scim.DataProvider;
import org.picketlink.scim.providers.PicketLinkIDMDataProvider;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;

/**
 * Base class for SCIM Endpoints
 *
 * @author anil saldhana
 * @since Apr 16, 2013
 */
public class AbstractSCIMEndpoint {
    private static Logger log = Logger.getLogger(AbstractSCIMEndpoint.class);

    @Inject
    protected DataProvider dataProvider;

    protected BeanManager getBeanManager(ServletContext sc) {
        InitialContext initialContext = null;
        BeanManager beanManager = null;
        try {
            beanManager = (BeanManager) sc.getAttribute("org.jboss.weld.environment.servlet." + BeanManager.class.getName());
            if (beanManager != null) {
                return beanManager;
            }

            initialContext = new InitialContext();
            beanManager = (BeanManager) initialContext.lookup("java:comp/BeanManager");
        } catch (NamingException e) {
            try {
                beanManager = (BeanManager) initialContext.lookup("java:comp/env/BeanManager");
            } catch (NamingException e1) {
                if (log.isTraceEnabled()) {
                    log.trace("Couldn't get BeanManager through JNDI");
                }

            }
        }
        return beanManager;
    }

    @SuppressWarnings("unchecked")
    protected <T> T getContextualInstance(final BeanManager manager, final Class<T> type) {
        T result = null;
        Bean<T> bean = (Bean<T>) manager.resolve(manager.getBeans(type));
        if (bean != null) {
            CreationalContext<T> context = manager.createCreationalContext(bean);
            if (context != null) {
                result = (T) manager.getReference(bean, type, context);
            }
        }
        return result;
    }

    protected DataProvider createDefaultDataProvider() {
        PicketLinkIDMDataProvider plidmp = new PicketLinkIDMDataProvider();
        return plidmp;
    }
}
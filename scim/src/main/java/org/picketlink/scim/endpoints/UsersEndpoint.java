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

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.picketlink.scim.DataProvider;
import org.picketlink.scim.codec.SCIMWriter;
import org.picketlink.scim.codec.SCIMWriterException;
import org.picketlink.scim.model.v11.SCIMUser;

/**
 * REST Endpoint for Users
 *
 * @author anil saldhana
 * @since Apr 9, 2013
 */
@Path("/Users")
@RequestScoped
public class UsersEndpoint extends AbstractSCIMEndpoint {

    @Inject
    private DataProvider dataProvider;

    // Get an user
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@Context HttpServletRequest request, @Context ServletContext sc, @PathParam("id") String userId) {
        if (dataProvider == null) {
            BeanManager beanManager = getBeanManager(sc);
            if (beanManager == null) {
                throw new IllegalStateException("BM null");
            }
            dataProvider = getContextualInstance(beanManager, DataProvider.class);
        }
        if (dataProvider == null) {
            System.out.println("dataProvider is not injected");
            dataProvider = createDefaultDataProvider();
        }
        try {
            initializeEntityManager();
            dataProvider.initializeConnection();
            SCIMUser user = dataProvider.getUser(userId);
            SCIMWriter writer = new SCIMWriter();

            String json = "";
            try {
                json = writer.json(user);
            } catch (SCIMWriterException e) {
                throw new RuntimeException(e);
            }
            return Response.status(200).entity(json).build();
        } finally {
            closeEntityManager();
            dataProvider.closeConnection();
        }
    }

    private BeanManager getBeanManager(ServletContext sc) {
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
                System.out.println("Couldn't get BeanManager through JNDI");
            }
        }
        return beanManager;
    }

    @SuppressWarnings("unchecked")
    public <T> T getContextualInstance(final BeanManager manager, final Class<T> type) {
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
}
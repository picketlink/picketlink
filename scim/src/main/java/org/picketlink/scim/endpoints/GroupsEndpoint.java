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

import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;
import org.picketlink.scim.DataProvider;
import org.picketlink.scim.codec.SCIMParser;
import org.picketlink.scim.codec.SCIMWriter;
import org.picketlink.scim.codec.SCIMWriterException;
import org.picketlink.scim.model.v11.SCIMGroups;

/**
 * REST Endpoint for Groups
 *
 * @author anil saldhana
 * @since Apr 9, 2013
 */
@Path("/Groups")
public class GroupsEndpoint extends AbstractSCIMEndpoint {
    private static Logger log = Logger.getLogger(UsersEndpoint.class);

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@Context HttpServletRequest request, @Context ServletContext sc, @PathParam("id") String groupId) {
        if (dataProvider == null) {
            BeanManager beanManager = getBeanManager(sc);
            if (beanManager == null) {
                throw new IllegalStateException("BM null");
            }
            dataProvider = getContextualInstance(beanManager, DataProvider.class);
        }
        if (dataProvider == null) {
            if (log.isTraceEnabled()) {
                log.trace("dataProvider is not injected. Create a default IDM driven data provider.");
            }
            dataProvider = createDefaultDataProvider();
        }
        try {
            dataProvider.initializeConnection();
            SCIMGroups group = dataProvider.getGroups(groupId);
            SCIMWriter writer = new SCIMWriter();

            String json = "";
            try {
                json = writer.json(group);
            } catch (SCIMWriterException e) {
                throw new RuntimeException(e);
            }
            return Response.status(200).entity(json).build();
        } finally {
            dataProvider.closeConnection();
        }
    }

    // Create a group
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGroup(@Context HttpServletRequest request, @Context ServletContext sc) {
        if (dataProvider == null) {
            BeanManager beanManager = getBeanManager(sc);
            if (beanManager == null) {
                throw new IllegalStateException("BM null");
            }
            dataProvider = getContextualInstance(beanManager, DataProvider.class);
        }
        if (dataProvider == null) {
            if (log.isTraceEnabled()) {
                log.trace("dataProvider is not injected. Creating a default IDM driven data provider.");
            }
            dataProvider = createDefaultDataProvider();
        }
        try {
            // Parse the data
            SCIMParser parser = new SCIMParser();
            SCIMGroups group = null;
            try {
                group = parser.parseGroup(request.getInputStream());
            } catch (Exception e1) {
                throw new RuntimeException(e1);
            }

            dataProvider.initializeConnection();
            String id = dataProvider.createGroup(group);

            group.setId(id);

            SCIMWriter writer = new SCIMWriter();

            String json = "";
            try {
                json = writer.json(group);
            } catch (SCIMWriterException e) {
                throw new RuntimeException(e);
            }
            return Response.status(200).entity(json).build();
        } finally {
            dataProvider.closeConnection();
        }
    }
}
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

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.picketlink.scim.DataProvider;
import org.picketlink.scim.DataProviderAnnotation;
import org.picketlink.scim.codec.SCIMWriter;
import org.picketlink.scim.codec.SCIMWriterException;
import org.picketlink.scim.model.v11.SCIMGroups;

/**
 * REST Endpoint for Groups
 *
 * @author anil saldhana
 * @since Apr 9, 2013
 */
@Path("/Users")
public class GroupsEndpoint {

    @Inject
    @DataProviderAnnotation
    private DataProvider dataProvider;

    // Get a group
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@Context HttpServletRequest request, @PathParam("id") String groupId) {
        if (dataProvider == null) {
            throw new IllegalStateException("dataProvider is not injected");
        }
        SCIMGroups groups = dataProvider.getGroups(groupId);

        SCIMWriter writer = new SCIMWriter();

        String json = "";
        try {
            json = writer.json(groups);
        } catch (SCIMWriterException e) {
            throw new RuntimeException(e);
        }

        return Response.status(200).entity(json).build();
    }
}
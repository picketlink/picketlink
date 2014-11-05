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

import org.picketlink.scim.SCIMResourceScannerExtension;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * REST Endpoint for Users
 *
 * @author anil saldhana
 * @since Apr 9, 2013
 */
@Path("/")
public class ResourceEndpoint {

    @Inject
    private SCIMResourceScannerExtension extension;

    @GET
    @Path("/{version}/{resource : .+}/{id : .+}")
    @Produces("application/json")
    public Response findById(@PathParam("version") String version, @PathParam("resource") String resource, @PathParam("id") String id) {
        return Response.ok().build();
    }

    @GET
    @Path("/{version}/{resource : .+}/{filter : .+}")
    @Produces("application/json")
    public Response find(@PathParam("version") String version, @PathParam("resource") String resource, @PathParam("filter") String filter) {
        return Response.ok().build();
    }

    @POST
    @Path("/{version}/{resource : .+}/.search")
    @Produces("application/json")
    public Response search(@PathParam("version") String version, @PathParam("resource") String resource) {
        return Response.ok().build();
    }

    @GET
    @Path("/{version}/{resource : .+}")
    @Produces("application/json")
    public Response findAll(@PathParam("version") String version, @PathParam("resource") String resource) {
        return Response.ok().build();
    }

    @POST
    @Path("/{version}/{resource : .+}")
    @Consumes("application/json")
    @Produces("application/json")
    public Response create(@PathParam("version") String version, @PathParam("resource") String resource) {
        return Response.ok().build();
    }

    @PUT
    @Path("/{version}/{resource : .+}")
    @Consumes("application/json")
    @Produces("application/json")
    public Response update(@PathParam("version") String version, @PathParam("resource") String resource, @PathParam("id") String id) {
        return Response.ok().build();
    }

    @DELETE
    @Path("/{version}/{resource : .+}")
    @Consumes("application/json")
    @Produces("application/json")
    public Response delete(@PathParam("version") String version, @PathParam("resource") String resource, @PathParam("id") String id) {
        return Response.ok().build();
    }

    public Response patch(@PathParam("version") String version, @PathParam("resource") String resource, @PathParam("id") String id) {
        /**
         * TODO: enable patch method
         */
        return Response.ok().build();
    }

}

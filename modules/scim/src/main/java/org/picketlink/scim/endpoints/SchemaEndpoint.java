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

import org.picketlink.scim.model.v11.parser.SCIMWriter;
import org.picketlink.scim.model.v11.parser.SCIMWriterException;
import org.picketlink.scim.model.v11.resource.SCIMResource;
import org.picketlink.scim.model.v11.schema.SCIMSchema;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * REST Endpoint for Users
 *
 * @author anil saldhana
 * @since Apr 9, 2013
 */
@Path("/")
public class SchemaEndpoint extends AbstractEndpoint {

    @GET
    @Path("/{version}/Schemas")
    @Produces("application/json")
    public Response getAll(@PathParam("version") String version) {
        List<SCIMSchema> schemas = new ArrayList<SCIMSchema>();

        for (Class<? extends SCIMResource> resourceType : getResourceTypes()) {
            schemas.add(SCIMSchema.fromResourceType(resourceType));
        }

        try {
            String entity = new SCIMWriter().toString(schemas.toArray(new SCIMSchema[schemas.size()]));
            return Response.ok(entity).build();
        } catch (SCIMWriterException e) {
            throw new RuntimeException("Could not return resource type.", e);
        }
    }

    @GET
    @Path("/{version}/Schemas/{resource : .+}")
    @Produces("application/json")
    public Response getResourceSchema(@PathParam("version") String version, @PathParam("resource") String resource) {
        return Response.ok().build();
    }
}

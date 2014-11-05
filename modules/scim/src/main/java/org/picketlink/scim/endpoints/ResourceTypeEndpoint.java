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
import org.picketlink.scim.annotations.ResourceDefinition;
import org.picketlink.scim.model.v11.parser.SCIMWriter;
import org.picketlink.scim.model.v11.parser.SCIMWriterException;
import org.picketlink.scim.model.v11.resource.SCIMResource;
import org.picketlink.scim.model.v11.schema.SCIMResourceType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * REST Endpoint for Users
 *
 * @author anil saldhana
 * @since Apr 9, 2013
 */
@Path("/")
public class ResourceTypeEndpoint {

    @Inject
    private SCIMResourceScannerExtension extension;

    @GET
    @Path("/{version}/ResourceTypes/{resource : .+}")
    @Produces("application/json")
    public Response getResouceType(@PathParam("version") String version, @PathParam("resource") String resource) {
        Class<? extends SCIMResource> resourceType = getResourceType(resource);

        if (resourceType == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        ResourceDefinition resourceDefinition = resourceType.getAnnotation(ResourceDefinition.class);

        SCIMResourceType scimResourceType = new SCIMResourceType();

        scimResourceType.setId(resourceDefinition.id());
        scimResourceType.setName(resourceDefinition.name());
        scimResourceType.setEndpoint(resourceDefinition.endpointName());
        scimResourceType.setSchema(resourceDefinition.schema());

        try {
            String entity = new SCIMWriter().toString(scimResourceType);
            return Response.ok(entity).build();
        } catch (SCIMWriterException e) {
            throw new RuntimeException("Could not return resource type.", e);
        }
    }

    private Class<? extends SCIMResource> getResourceType(String resource) {
        Map<String, Class<? extends SCIMResource>> resources = this.extension.getResources();
        String resourceEndpoint = resource;

        if (!resourceEndpoint.startsWith("/")) {
            resourceEndpoint = "/" + resourceEndpoint;
        }

        return resources.get(resourceEndpoint);
    }
}

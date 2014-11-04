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

import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.scim.SCIMResourceScannerExtension;
import org.picketlink.scim.annotations.ResourceAttributeDefinition;
import org.picketlink.scim.annotations.ResourceDefinition;
import org.picketlink.scim.model.v11.parser.SCIMWriter;
import org.picketlink.scim.model.v11.parser.SCIMWriterException;
import org.picketlink.scim.model.v11.resource.SCIMResource;
import org.picketlink.scim.model.v11.schema.SCIMResourceType;
import org.picketlink.scim.model.v11.schema.SCIMSchema;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.picketlink.common.util.StringUtil.isNullOrEmpty;
import static org.picketlink.scim.model.v11.schema.SCIMSchema.Attributes;

/**
 * REST Endpoint for Users
 *
 * @author anil saldhana
 * @since Apr 9, 2013
 */
@Path("/")
public class ResourceEndpoint {

    private final Map<String, Class<? extends SCIMResource>> resourceTypes = new HashMap<String, Class<? extends SCIMResource>>();

    @Inject
    private SCIMResourceScannerExtension extension;

    @GET
    @Path("/{version}/{resource : .+}")
    @Produces("application/json")
    public Response getResouceType(@PathParam("version") String version, @PathParam("resource") String resource) {
        Map<String, Class<? extends SCIMResource>> resources = this.extension.getResources();
        String resourceEndpoint = "/" + resource;
        Class<? extends SCIMResource> resourceType = resources.get(resourceEndpoint);

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

    @GET
    @Path("/{version}/{resource : .+}/schema")
    @Produces("application/json")
    public Response getResouceSchema(@PathParam("version") String version, @PathParam("resource") String resource) {
        Map<String, Class<? extends SCIMResource>> resources = this.extension.getResources();
        String resourceEndpoint = "/" + resource;
        resourceEndpoint = resourceEndpoint.replaceAll("/schema", "");
        Class<? extends SCIMResource> resourceType = resources.get(resourceEndpoint);

        if (resourceType == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        ResourceDefinition resourceDefinition = resourceType.getAnnotation(ResourceDefinition.class);

        SCIMSchema scimSchema = new SCIMSchema();

        scimSchema.setId(resourceDefinition.schema());
        scimSchema.setName(resourceDefinition.name());
        scimSchema.setDescription(resourceDefinition.description());
        scimSchema.setAttributes(createAttributes(resourceType));

        try {
            String entity = new SCIMWriter().toString(new SCIMSchema[] {scimSchema});
            return Response.ok(entity).build();
        } catch (SCIMWriterException e) {
            throw new RuntimeException("Could not return resource type.", e);
        }
    }

    private Attributes[] createAttributes(Class<? extends SCIMResource> resourceType) {
        List<Attributes> attributes = new ArrayList<Attributes>();
        List<Property<Object>> result = PropertyQueries
            .createQuery(resourceType)
            .addCriteria(new AnnotatedPropertyCriteria(ResourceAttributeDefinition.class))
            .getResultList();

        for (Property property : result) {
            ResourceAttributeDefinition annotation = property.getAnnotatedElement().getAnnotation(ResourceAttributeDefinition.class);

            Attributes attribute = new Attributes();

            String name = annotation.name();

            if (isNullOrEmpty(name)) {
                name = property.getName();
            }

            attribute.setName(name);

            attribute.setDescription(annotation.description());
            attribute.setCaseExact(annotation.caseExact());
            attribute.setMutability(annotation.mutability());
            attribute.setRequired(annotation.required());
            attribute.setReturned(annotation.returned());
            attribute.setUniqueness(annotation.uniqueness());

            Type baseType = property.getBaseType();

            attribute.setType(baseType.toString());

            attributes.add(attribute);
        }

        return attributes.toArray(new Attributes[attributes.size()]);
    }
}

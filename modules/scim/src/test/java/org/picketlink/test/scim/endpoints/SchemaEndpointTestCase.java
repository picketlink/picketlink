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
package org.picketlink.test.scim.endpoints;

import io.undertow.Undertow;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import org.jboss.resteasy.cdi.CdiInjectorFactory;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.test.TestPortProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picketlink.scim.model.v11.parser.SCIMParser;
import org.picketlink.scim.model.v11.resource.SCIMUser;
import org.picketlink.scim.model.v11.schema.SCIMSchema;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.picketlink.scim.model.v11.schema.SCIMSchema.Attribute;

/**
 *
 * @author Pedro Igor
 */
public class SchemaEndpointTestCase {

    private UndertowJaxrsServer server;

    @Before
    public void onBefore() {
        this.server = new UndertowJaxrsServer();

        ResteasyDeployment deployment = new ResteasyDeployment();

        deployment.setInjectorFactoryClass(CdiInjectorFactory.class.getName());
        deployment.setApplicationClass(SCIMApplication.class.getName());

        DeploymentInfo deploymentInfo = this.server.undertowDeployment(deployment);

        deploymentInfo.setDeploymentName("rest.war");
        deploymentInfo.setContextPath("/rest");
        deploymentInfo.addListeners(Servlets.listener(org.jboss.weld.environment.servlet.Listener.class));
        deploymentInfo.setClassLoader(SchemaEndpointTestCase.class.getClassLoader());

        server.deploy(deploymentInfo);

        this.server.start(
            Undertow.builder()
                .addHttpListener(8081, "localhost")

        );
    }

    @After
    public void onAfter() {
        this.server.stop();
    }


    @Test
    public void testGetAllSchemas() throws Exception {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(TestPortProvider.generateURL("/rest/v11/Schemas"));
        String response = target.request().get(String.class);
        SCIMSchema[] schemas = new SCIMParser().parseSchema(new ByteArrayInputStream(response.getBytes()));

        assertEquals(1, schemas.length);

        for (SCIMSchema schema : schemas) {
            String id = schema.getId();

            if (SCIMUser.ID.toString().equals(id)) {
                assertEquals("User", schema.getName());
                assertEquals("User Account", schema.getDescription());

                assertEquals(21, schema.getAttributes().length);

                for (Attribute attribute : schema.getAttributes()) {
                    if (attribute.getName().equals("userName")) {
                        assertEquals("string", attribute.getType());
                        assertFalse(attribute.isMultiValued());
                        assertEquals("Unique identifier for the User typically used by the user to directly authenticate to the service provider. Each User MUST include a non-empty userName value.  This identifier MUST be unique across the Service Consumer's entire set of Users.  REQUIRED", attribute.getDescription());
                        assertTrue(attribute.isRequired());
                        assertFalse(attribute.isCaseExact());
                        assertEquals("readWrite", attribute.getMutability());
                        assertEquals("default", attribute.getReturned());
                        assertEquals("server", attribute.getUniqueness());
                    } else if (attribute.getName().equals("name")) {
                        assertEquals("complex", attribute.getType());
                        assertFalse(attribute.isMultiValued());
                        assertEquals("The components of the user's real name. Providers MAY return just the full name as a single string in the formatted sub-attribute, or they MAY return just the individual component attributes using the other sub-attributes, or they MAY return both. If both variants are returned, they SHOULD be describing the same name, with the formatted name indicating how the component attributes should be combined.", attribute.getDescription());
                        assertFalse(attribute.isRequired());
                        assertFalse(attribute.isCaseExact());
                        assertEquals("readWrite", attribute.getMutability());
                        assertEquals("default", attribute.getReturned());
                        assertEquals("server", attribute.getUniqueness());

                        SCIMSchema.BasicAttribute[] subAttributes = attribute.getSubAttributes();

                        assertEquals(6, subAttributes.length);
                    }
                }
            }
        }
    }
}
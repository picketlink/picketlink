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
import org.picketlink.scim.model.v11.schema.SCIMResourceType;
import org.picketlink.scim.model.v11.schema.SCIMSchema;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.ByteArrayInputStream;

/**
 *
 * @author Pedro Igor
 */
public class ResourceEndpointTestCase {

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
        deploymentInfo.setClassLoader(ResourceEndpointTestCase.class.getClassLoader());

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
    public void testGetResourceType() throws Exception {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(TestPortProvider.generateURL("/rest/v11/User"));
        String response = target.request().get(String.class);
        SCIMResourceType scimResourceType = new SCIMParser().parseResourceType(new ByteArrayInputStream(response.getBytes()));
    }

    @Test
    public void testGetResourceTypeSchema() throws Exception {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(TestPortProvider.generateURL("/rest/v11/User/schema"));
        String response = target.request().get(String.class);
        SCIMSchema[] scimResourceType = new SCIMParser().parseSchema(new ByteArrayInputStream(response.getBytes()));
    }
}
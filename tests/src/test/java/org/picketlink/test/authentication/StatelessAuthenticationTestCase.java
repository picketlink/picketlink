/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketlink.test.authentication;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.picketlink.Identity;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.test.AbstractArquillianTestCase;
import org.picketlink.test.idm.config.IDMInitializer;
import org.picketlink.test.util.ArchiveUtils;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.picketlink.Identity.Stateless;

/**
 * @author Pedro Igor
 */
@RunAsClient
public class StatelessAuthenticationTestCase extends AbstractArquillianTestCase {

    @Deployment(name = "services")
    public static WebArchive deploy() {
        WebArchive deployment = ArchiveUtils.create(StatelessAuthenticationTestCase.class, AbstractArquillianTestCase.class);

        deployment.addClass(JaxRsActivator.class);
        deployment.addClass(AbstractAuthenticator.class);
        deployment.addClass(StatelessAuthenticator.class);
        deployment.addClass(StatefulAuthenticator.class);
        deployment.addClass(Token.class);
        deployment.addClass(IDMInitializer.class);

        return deployment;
    }

    @Test
    @OperateOnDeployment("services")
    public void testStatefulAuthentication(@ArquillianResource URL url) throws Exception {
        assertUserAuthenticated(url, "rest/stateful", true);
    }

    @Test
    @OperateOnDeployment("services")
    public void testStatelessAuthentication(@ArquillianResource URL url) throws Exception {
        assertUserAuthenticated(url, "rest/stateless", false);
    }

    private void assertUserAuthenticated(URL url, String basePath, boolean expectUserAuthenticated) throws URISyntaxException {
        Client client = ClientBuilder.newClient();
        String restContext = url.toURI() + basePath;
        WebTarget target = client.target(restContext + "/authenticate");

        DefaultLoginCredentials user = new DefaultLoginCredentials();

        user.setUserId("john");
        user.setPassword("john");

        ClientResponse response = (ClientResponse) target.request().post(Entity.json(user));
        Token token = response.readEntity(Token.class);

        assertNotNull(token);
        assertEquals(user.getUserId(), token.getId());

        target = client.target(restContext + "/isAuthenticated");

        response = (ClientResponse) target.request().get();

        Boolean isAuthenticated = response.readEntity(Boolean.class);

        if (expectUserAuthenticated) {
            assertTrue(isAuthenticated);
        } else {
            assertFalse(isAuthenticated);
        }
    }

    @ApplicationPath("/rest")
    public static class JaxRsActivator extends Application {

    }

    public static abstract class AbstractAuthenticator {

        @Inject
        private DefaultLoginCredentials credentials;

        @Path("/authenticate")
        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Response authenticate(DefaultLoginCredentials userCredentials) {
            this.credentials.setUserId(userCredentials.getUserId());
            this.credentials.setPassword(userCredentials.getPassword());

            getIdentity().login();

            if (getIdentity().isLoggedIn()) {
                Token token = new Token();

                token.setId(userCredentials.getUserId());

                return Response.ok(token).build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        }

        @Path("/isAuthenticated")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Response isAuthenticated() {
            return Response.ok().entity(getIdentity().isLoggedIn()).build();
        }

        protected abstract Identity getIdentity();
    }

    @Path("/stateless")
    public static class StatelessAuthenticator extends AbstractAuthenticator {

        @Inject
        @Stateless
        private Identity identity;

        @Override
        public Identity getIdentity() {
            return this.identity;
        }
    }

    @Path("/stateful")
    public static class StatefulAuthenticator extends AbstractAuthenticator {

        @Inject
        private Identity identity;

        @Override
        public Identity getIdentity() {
            return this.identity;
        }
    }

    public static class Token {

        private String id;

        public String getId() {
            return this.id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
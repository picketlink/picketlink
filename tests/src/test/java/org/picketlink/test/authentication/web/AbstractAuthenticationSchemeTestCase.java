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
package org.picketlink.test.authentication.web;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.WebResponse;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.httpclient.HttpStatus;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picketlink.common.util.Base64;
import static org.junit.Assert.assertEquals;
import static org.picketlink.test.util.ArchiveUtils.create;

/**
 * @author pedroigor
 */
@RunWith(Arquillian.class)
public abstract class AbstractAuthenticationSchemeTestCase {

    @ArquillianResource
    private URL contextPath;

    public static Archive<?> deploy(String webXml) {
        WebArchive archive = create(Resources.class);

        archive.addAsLibraries(
                DependencyResolvers.use(MavenDependencyResolver.class)
                        .artifact("net.sourceforge.htmlunit:htmlunit:2.4")
                        .resolveAs(JavaArchive.class));


        archive.addAsWebResource(AbstractAuthenticationSchemeTestCase.class.getResource("/deployments/" + webXml), "WEB-INF/web.xml");
        archive.add(new StringAsset("Index Page"), "index.html");
        archive.add(new StringAsset("Protected Page"), "protected/index.html");

        return archive;
    }

    @Test
    public void testNotProtectedResource() throws Exception {
        WebClient client = new WebClient();
        WebResponse webResponse = client.loadWebResponse(new WebRequestSettings(this.contextPath));

        assertEquals(HttpStatus.SC_OK, webResponse.getStatusCode());
        assertEquals("Index Page", webResponse.getContentAsString());
    }

    @Test
    public void testProtectedResource() throws Exception {
        WebClient client = new WebClient();
        WebResponse response = client.loadWebResponse(new WebRequestSettings(getProtectedResourceURL()));

        assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testUnprotectedMethod() throws Exception {
        WebClient client = new WebClient();
        WebRequestSettings request = new WebRequestSettings(getProtectedResourceURL());

        request.setHttpMethod(HttpMethod.OPTIONS);

        WebResponse response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
    }

    @Test
    public void testSuccessfulAuthentication() throws Exception {
        WebClient client = new WebClient();
        WebRequestSettings request = new WebRequestSettings(new URL(this.contextPath + "/protected"));
        WebResponse response = client.loadWebResponse(request);

        doPrepareForAuthentication(request, response);

        response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("Protected Page", response.getContentAsString());

        request.setUrl(this.contextPath);
        response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("Index Page", response.getContentAsString());

        request.setUrl(getProtectedResourceURL());
        response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("Protected Page", response.getContentAsString());
    }

    abstract void doPrepareForAuthentication(WebRequestSettings request, WebResponse response);
    abstract void doPrepareForInvalidAuthentication(WebRequestSettings request, WebResponse response);

    @Test
    public void testUnsuccessfulAuthentication() throws Exception {
        WebClient client = new WebClient();
        WebRequestSettings request = new WebRequestSettings(new URL(this.contextPath + "/protected"));
        WebResponse response = client.loadWebResponse(request);

        doPrepareForInvalidAuthentication(request, response);

        response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testWithoutReAuthenticationUserAlreadyAuthenticated() throws Exception {
        WebClient client = new WebClient();
        WebRequestSettings request = new WebRequestSettings(new URL(this.contextPath + "/protected"));
        WebResponse response = client.loadWebResponse(request);

        doPrepareForAuthentication(request, response);

        response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());

        request.addAdditionalHeader("Authorization", new String("Basic " + Base64.encodeBytes("john:bad_passwd".getBytes())));

        response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
    }

    private URL getProtectedResourceURL() throws MalformedURLException {
        return new URL(this.contextPath + "/protected/");
    }

}
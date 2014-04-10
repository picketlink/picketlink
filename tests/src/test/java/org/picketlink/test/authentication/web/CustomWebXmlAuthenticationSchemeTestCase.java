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

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.commons.httpclient.HttpStatus;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.WebResponse;

/**
 * @author pedroigor
 */
public class CustomWebXmlAuthenticationSchemeTestCase extends AbstractAuthenticationSchemeTestCase {

    @Deployment(name = "configured-in-web-xml", testable = false)
    public static Archive<?> deployWebXml() {
        return deploy("configured-in-web-xml.war", "authc-filter-custom-web.xml",
                CustomAbstractHttpAuthScheme.class,
                CustomUnqualifiedHttpAuthScheme.class);
    }

    @Deployment(name = "configured-by-qualified-bean", testable = false)
    public static Archive<?> deployQualified() {
        return deploy("configured-by-qualified-bean.war", "authc-filter-not-configured-web.xml",
                CustomAbstractHttpAuthScheme.class,
                CustomQualifiedHttpAuthScheme.class);
    }

    @Deployment(name = "configured-by-both", testable = false)
    public static Archive<?> deployBoth() {
        return deploy("configured-by-both.war", "authc-filter-custom-web.xml",
                CustomAbstractHttpAuthScheme.class,
                CustomQualifiedHttpAuthScheme.class,
                CustomUnqualifiedHttpAuthScheme.class);
    }

    @Test
    @OperateOnDeployment("configured-in-web-xml")
    public void testConfigurationByInitParam() throws IOException {
        WebClient client = new WebClient();
        WebRequestSettings request = new WebRequestSettings(getProtectedResourceURL());
        WebResponse response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals(
                CustomUnqualifiedHttpAuthScheme.class.getName() + ", initialized, has_filter_config, has_injected_identity",
                response.getContentAsString());
    }

    @Test
    @OperateOnDeployment("configured-by-qualified-bean")
    public void testConfigurationByQualifiedBean() throws IOException {
        WebClient client = new WebClient();
        WebRequestSettings request = new WebRequestSettings(getProtectedResourceURL());
        WebResponse response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals(
                CustomQualifiedHttpAuthScheme.class.getName() + ", initialized, has_filter_config, has_injected_identity",
                response.getContentAsString());
    }

    @Test
    @OperateOnDeployment("configured-by-both")
    public void testQualifiedBeanConfigurationOverridesInitParam() throws IOException {
        testConfigurationByQualifiedBean();
    }

}
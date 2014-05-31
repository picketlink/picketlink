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

import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.picketlink.test.AbstractArquillianTestCase;
import org.picketlink.test.util.ArchiveUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author pedroigor
 */
public abstract class AbstractAuthenticationSchemeTestCase extends AbstractArquillianTestCase {

    public static final String SESSION_HEADER_NAME = "JSESSIONID";

    @ArquillianResource
    private URL contextPath;

    public static WebArchive create(String name, String webXml, Class<?>... classesToAdd) {
        WebArchive archive = ArchiveUtils.create(name, classesToAdd);

        archive.addClass(Resources.class);

        archive.addAsLibraries(
            DependencyResolvers.use(MavenDependencyResolver.class)
                .artifact("net.sourceforge.htmlunit:htmlunit:2.4")
                .resolveAs(JavaArchive.class));


        ArchiveUtils.addWebXml(archive, webXml);

        archive.add(new StringAsset("Index Page"), "index.html");
        archive.add(new StringAsset("Protected Page"), "protected/index.html");

        return archive;
    }

    public static WebArchive create(String webXml, Class<?>... classesToAdd) {
        return create("test.war", webXml, classesToAdd);
    }

    protected URL getProtectedResourceURL() throws MalformedURLException {
        return new URL(getContextPath() + "/protected/");
    }

    protected URL getContextPath() {
        return this.contextPath;
    }
}
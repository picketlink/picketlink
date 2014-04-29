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

package org.picketlink.test;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.runner.RunWith;
import org.picketlink.test.util.ArchiveUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>Base class for integration tests using Arquillian.</p>
 * 
 * @author Pedro Igor
 *
 */
@RunWith (Arquillian.class)
public abstract class AbstractArquillianTestCase {

    public static WebArchive create(String name, String webXml, Class<?>... classesToAdd) {
        List<Class<?>> classes = new ArrayList<Class<?>>();

        classes.addAll(Arrays.asList(classesToAdd));

        WebArchive archive = ArchiveUtils.create(name, classes.toArray(new Class[classes.size()]));

        archive.addAsLibraries(
            DependencyResolvers.use(MavenDependencyResolver.class)
                .artifact("net.sourceforge.htmlunit:htmlunit:2.4")
                .resolveAs(JavaArchive.class));


        archive.addAsWebResource(AbstractArquillianTestCase.class.getResource("/deployments/" + webXml), "WEB-INF/web.xml");
        archive.add(new StringAsset("Index Page"), "index.html");
        archive.add(new StringAsset("Protected Page"), "protected/index.html");

        return archive;
    }
}

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

package org.picketlink.test.util;

import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;

/**
 * <p>
 * Utility classes to help create deployable archives during the integration tests.
 * </p>
 * <p>
 * Instead of adding the project's classes individually we add and test the final jar using the {@link org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver}.
 * In order to do that, we need the current project version which is obtained from a system property named
 * <i>project.version</i>. This allow us to test any project version by just providing this system property during the build,
 * which is a good thing in order to perform some regression tests.
 * </p>
 * 
 * @author Pedro Igor
 * 
 */
public class ArchiveUtils {

    public static WebArchive create(Class<?>... classesToAdd) {
        return create("test.war", classesToAdd);
    }

    public static WebArchive create(String name, Class<?>... classesToAdd) {
        WebArchive archive = ShrinkWrap
                .create(WebArchive.class, name)
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));

        addDependency(archive, "org.picketlink:picketlink-impl:" + getCurrentProjectVersion());

        if (classesToAdd != null) {
            for (Class<?> classToAdd : classesToAdd) {
                archive.addClass(classToAdd);
            }
        }

        return archive;
    }

    public static void addDependency(WebArchive archive, String gav) {
        archive.addAsLibraries(
                DependencyResolvers
                        .use(MavenDependencyResolver.class)
                        .loadMetadataFromPom("pom.xml")
                        .artifact(gav)
                        .resolveAs(JavaArchive.class)
        );
    }

    /**
     * <p>
     * During the build this property is automatically configured, but if you're running in an IDE, make sure you have this
     * property properly configured in your test execution.
     * </p>
     * <p>
     * If not defined, the specified default value would be used.
     * </p>
     * 
     * @return
     */
    public static String getCurrentProjectVersion() {
        return System.getProperty("project.version", "2.6.0-SNAPSHOT");
    }

}
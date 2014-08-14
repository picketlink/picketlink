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
package org.picketlink.test.weld;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.environment.se.discovery.AbstractWeldSEBeanDeploymentArchive;
import org.jboss.weld.environment.se.discovery.AbstractWeldSEDeployment;
import org.jboss.weld.environment.se.discovery.url.URLScanner;
import org.jboss.weld.environment.se.discovery.url.WeldSEResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * @author Pedro Igor
 */
public class TestWeldSEUrlDeployment extends AbstractWeldSEDeployment {

    private final BeanDeploymentArchive beanDeploymentArchive;

    public TestWeldSEUrlDeployment(final Set<String> testClass, final String excludePackage, Bootstrap bootstrap) {
        super(bootstrap);
        WeldSEResourceLoader resourceLoader = new WeldSEResourceLoader();
        final BeanDeploymentArchive scan = new URLScanner(resourceLoader, bootstrap, RESOURCES).scan();

        this.beanDeploymentArchive = new AbstractWeldSEBeanDeploymentArchive(scan.getId()) {
            @Override
            public Collection<BeanDeploymentArchive> getBeanDeploymentArchives() {
                return scan.getBeanDeploymentArchives();
            }

            @Override
            public Collection<String> getBeanClasses() {
                Collection<String> beanClasses = new ArrayList<String>(scan.getBeanClasses());

                for (String clazz : new ArrayList<String>(beanClasses)) {
                    if (clazz.contains(excludePackage)) {
                        if (!testClass.contains(clazz)) {
                            beanClasses.remove(clazz);
                        }
                    }
                }

                return beanClasses;
            }

            @Override
            public BeansXml getBeansXml() {
                return scan.getBeansXml();
            }
        };
        this.beanDeploymentArchive.getServices().add(ResourceLoader.class, resourceLoader);
    }

    @Override
    public Collection<BeanDeploymentArchive> getBeanDeploymentArchives() {
        ArrayList<BeanDeploymentArchive> beanDeploymentArchives = new ArrayList<BeanDeploymentArchive>();

        beanDeploymentArchives.add(this.beanDeploymentArchive);

        return beanDeploymentArchives;
    }

    @Override
    public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass) {
        return this.beanDeploymentArchive;
    }
}

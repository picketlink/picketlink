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
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.resources.spi.ResourceLoader;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Pedro Igor
 */
public class WeldTest extends Weld {

    private final Set<String> beanClassNames;
    private String excludePackageName;

    public WeldTest() {
        this.beanClassNames = new HashSet<String>();
    }

    @Override
    protected Deployment createDeployment(ResourceLoader resourceLoader, Bootstrap bootstrap) {
        return new TestWeldSEUrlDeployment(this.beanClassNames, this.excludePackageName, bootstrap);
    }

    public WeldTest excludeBeansFromPackage(String packageName) {
        this.excludePackageName = packageName;
        return this;
    }

    public WeldTest addClass(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            this.beanClassNames.add(clazz.getName());
        }

        return this;
    }
}

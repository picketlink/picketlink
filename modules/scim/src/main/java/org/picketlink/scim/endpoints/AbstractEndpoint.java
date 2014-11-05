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
package org.picketlink.scim.endpoints;

import org.picketlink.scim.SCIMResourceScannerExtension;
import org.picketlink.scim.model.v11.resource.SCIMResource;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;

/**
 * @author Pedro Igor
 */
public class AbstractEndpoint {

    @Inject
    private SCIMResourceScannerExtension extension;

    protected Collection<Class<? extends SCIMResource>> getResourceTypes() {
        return this.extension.getResources().values();
    }

    protected Class<? extends SCIMResource> getResourceType(String resource) {
        String resourceEndpoint = resource;

        if (!resourceEndpoint.startsWith("/")) {
            resourceEndpoint = "/" + resourceEndpoint;
        }

        Map<String, Class<? extends SCIMResource>> resources = this.extension.getResources();

        return resources.get(resourceEndpoint);
    }
}

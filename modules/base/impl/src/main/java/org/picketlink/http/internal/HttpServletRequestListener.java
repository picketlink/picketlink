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
package org.picketlink.http.internal;

import org.picketlink.Identity;
import org.picketlink.annotations.PicketLink;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.PartitionManager;
import org.picketlink.internal.el.ELProcessor;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Pedro Igor
 */
@WebListener
public class HttpServletRequestListener implements ServletRequestListener {

    private static ThreadLocal<HttpServletRequest> SERVLET_REQUEST = new ThreadLocal<HttpServletRequest>();

    @Inject
    private Instance<Identity> identityInstance;

    @Inject
    private Instance<DefaultLoginCredentials> credentialsInstance;

    @Inject
    private Instance<PartitionManager> partitionManager;

    @Inject
    private ELProcessor elProcessor;

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        SERVLET_REQUEST.set((HttpServletRequest) sre.getServletRequest());
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        SERVLET_REQUEST.remove();
    }

    @PicketLink
    @Produces
    @Typed(HttpServletRequest.class)
    public HttpServletRequest produce() {
        HttpServletRequest request = SERVLET_REQUEST.get();

        if (request != null) {
            return new PicketLinkHttpServletRequest(request,
                resolveInstance(this.identityInstance),
                resolveInstance(this.credentialsInstance),
                this.partitionManager.get(),
                this.elProcessor);
        }

        return null;
    }

    private <I> I resolveInstance(Instance<I> instance) {
        if (instance.isUnsatisfied()) {
            throw new IllegalStateException("Instance [" + instance + "] not found.");
        } else if (instance.isAmbiguous()) {
            throw new IllegalStateException("Instance [" + instance + "] is ambiguous.");
        }

        try {
            return (I) instance.get();
        } catch (Exception e) {
            throw new IllegalStateException("Could not retrieve instance [" + instance + "].", e);
        }
    }
}

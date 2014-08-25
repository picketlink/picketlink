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
package org.picketlink.http.internal.authorization;

import org.picketlink.Identity;
import org.picketlink.config.http.AuthorizationConfiguration;
import org.picketlink.config.http.PathConfiguration;
import org.picketlink.http.authorization.PathAuthorizer;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Pedro Igor
 */
public abstract class AbstractPathAuthorizer implements PathAuthorizer {

    @Inject
    private Instance<Identity> identityInstance;

    @Override
    public boolean authorize(PathConfiguration pathConfiguration, HttpServletRequest request, HttpServletResponse response) {
        AuthorizationConfiguration authorizationConfiguration = pathConfiguration.getAuthorizationConfiguration();

        if (authorizationConfiguration == null) {
            return true;
        }

        return doAuthorize(pathConfiguration, request, response);
    }

    protected abstract boolean doAuthorize(PathConfiguration pathConfiguration, HttpServletRequest request, HttpServletResponse response);

    protected Identity getIdentity() {
        return resolveInstance(this.identityInstance);
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

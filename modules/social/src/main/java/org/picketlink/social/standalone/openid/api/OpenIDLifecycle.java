/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.social.standalone.openid.api;

import org.picketlink.social.standalone.openid.api.exceptions.OpenIDLifeCycleException;

/**
 * Denotes the lifecycle methods the OpenIDManager calls back - Consumer Side
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jul 10, 2009
 */
public interface OpenIDLifecycle {
    /**
     * Handle an openid lifecycle event from the openid manager
     *
     * @param event
     */
    void handle(OpenIDLifecycleEvent event) throws OpenIDLifeCycleException;

    /**
     * Handle an array of lifecycle events from the OpenID Manager
     *
     * @param eventArr
     * @throws OpenIDLifeCycleException
     */
    void handle(OpenIDLifecycleEvent[] eventArr) throws OpenIDLifeCycleException;

    /**
     * Provide the value for an attribute to the openid manager
     *
     * @param name
     * @return
     */
    Object getAttributeValue(String name);
}
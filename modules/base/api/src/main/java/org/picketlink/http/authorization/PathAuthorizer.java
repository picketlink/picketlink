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
package org.picketlink.http.authorization;

import org.picketlink.config.http.PathConfiguration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>A {@link org.picketlink.http.authorization.PathAuthorizer} is responsible to perform
 * authorization checks for a specific {@link org.picketlink.config.http.PathConfiguration}.</p>
 *
 * @author Pedro Igor
 */
public interface PathAuthorizer {

    /**
     * <p>Performs an authorization check for a specific {@link org.picketlink.config.http.PathConfiguration} considering
     * an incoming {@link javax.servlet.http.HttpServletRequest}.</p>
     *
     * @param pathConfiguration The configuration associated with the given request.
     * @param request The incoming request,
     * @param response The response.
     *
     * @return True if the request is authorized to access the given path. Otherwise, returns false.
     */
    boolean authorize(PathConfiguration pathConfiguration, HttpServletRequest request, HttpServletResponse response);

}

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

package org.picketlink.authentication.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.picketlink.credential.DefaultLoginCredentials;


/**
 * <p>Defines the methods that should be implemented by classes that provide implementations for the HTTP Authentication Schemes
 * such as BASIC, FORM, DIGEST and CLIENT-CERT.</p>
 *
 * @author Pedro Silva
 *
 */
public interface HTTPAuthenticationScheme {

    /**
     * <p>Extracts the credentials from the given {@link HttpServletRequest} and populate the {@link DefaultLoginCredentials} with them.</p>
     *
     * @param request
     * @param creds
     */
    void extractCredential(HttpServletRequest request, DefaultLoginCredentials creds);

    /**
     * <p>Challenges the client if no credentials were supplied or the credentials were not extracted in order to continue with the authentication.</p>
     *
     * @param request
     * @param response
     * @throws IOException
     */
    void challengeClient(HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * <p>Performs any post-authentication logic regarding of the authentication result.</p>
     *
     * @param request
     * @param response
     * @return true if the processing should continue, false if the processing should stop.
     * @throws IOException
     */
    boolean postAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException;
}

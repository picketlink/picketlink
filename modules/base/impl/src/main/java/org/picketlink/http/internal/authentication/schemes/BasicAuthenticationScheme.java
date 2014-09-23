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

package org.picketlink.http.internal.authentication.schemes;

import org.picketlink.common.util.Base64;
import org.picketlink.common.util.StringUtil;
import org.picketlink.config.http.BasicAuthenticationConfiguration;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.http.authentication.HttpAuthenticationScheme;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.picketlink.http.internal.util.RequestUtil.isAjaxRequest;

/**
 * @author Shane Bryzak
 * @author anil saldhana
 * @author Pedro Igor
 */
public class BasicAuthenticationScheme implements HttpAuthenticationScheme<BasicAuthenticationConfiguration> {

    public static final String DEFAULT_REALM_NAME = "PicketLink Default Realm";

    private String realm = DEFAULT_REALM_NAME;

    @Override
    public void initialize(BasicAuthenticationConfiguration config) {
        String providedRealm = config.getRealmName();

        if (providedRealm != null) {
            this.realm = providedRealm;
        }
    }

    @Override
    public void extractCredential(HttpServletRequest request, DefaultLoginCredentials creds) {
        if (isBasicAuthentication(request)) {
            String[] usernameAndPassword = extractUsernameAndPassword(request);

            String username = usernameAndPassword[0];
            String password = usernameAndPassword[1];

            if (!(StringUtil.isNullOrEmpty(username) && StringUtil.isNullOrEmpty(password))) {
                creds.setUserId(username);
                creds.setPassword(password);
            }
        }
    }

    @Override
    public void challengeClient(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setHeader("WWW-Authenticate", "Basic realm=\"" + this.realm + "\"");

            // this usually means we have a failing authentication request from an ajax client. so we return SC_FORBIDDEN instead.
            // this is a workaround to avoid browsers to popup an authentication dialog when authentication via ajax.
            if (isAjaxRequest(request)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not challenge client credentials.", e);
        }
    }

    @Override
    public void onPostAuthentication(HttpServletRequest request, HttpServletResponse response) {
    }

    private boolean isBasicAuthentication(HttpServletRequest request) {
        return getAuthorizationHeader(request) != null && getAuthorizationHeader(request).startsWith("Basic ");
    }

    private String getAuthorizationHeader(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    public String[] extractUsernameAndPassword(HttpServletRequest request) {
        String authorizationHeader = getAuthorizationHeader(request);

        String base64Token = authorizationHeader.substring(6);
        String token = new String(Base64.decode(base64Token));

        String username = "";
        String password = "";

        int delim = token.indexOf(":");

        if (delim != -1) {
            username = token.substring(0, delim);
            password = token.substring(delim + 1);
        }

        return new String[]{username, password};
    }
}

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
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.picketlink.common.util.Base64;
import org.picketlink.common.util.StringUtil;
import org.picketlink.credential.DefaultLoginCredentials;

/**
 * @author Shane Bryzak
 * @author anil saldhana
 * @author Pedro Igor
 */
public class BasicAuthenticationScheme implements HTTPAuthenticationScheme {

    public static final String REALM_NAME_INIT_PARAM = "realmName";
    public static final String DEFAULT_REALM_NAME = "PicketLink Default Realm";

    private String realm = DEFAULT_REALM_NAME;

    public BasicAuthenticationScheme(FilterConfig config) {
        String providedRealm = config.getInitParameter(REALM_NAME_INIT_PARAM);

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
    public void challengeClient(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("WWW-Authenticate", "Basic realm=\"" + this.realm + "\"");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Override
    public boolean postAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return true;
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

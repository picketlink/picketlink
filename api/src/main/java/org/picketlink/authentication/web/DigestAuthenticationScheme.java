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
import java.util.Timer;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.picketlink.authentication.web.support.HTTPDigestUtil;
import org.picketlink.authentication.web.support.NonceCache;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.credential.Digest;

/**
 * @author Shane Bryzak
 * @author anil saldhana
 * @author Pedro Igor
 */
public class DigestAuthenticationScheme implements HTTPAuthenticationScheme {

    public static final String REALM_NAME_INIT_PARAM = "realmName";
    public static final String DEFAULT_REALM_NAME = "PicketLink Default Realm";

    private final Timer nonceCleanupTimer = new Timer("PicketLink_Digest_Nonce_Cache_Cleanup");

    private NonceCache nonceCache = new NonceCache();

    private String realm = DEFAULT_REALM_NAME;

    public DigestAuthenticationScheme(FilterConfig config) {
        String providedRealm = config.getInitParameter(REALM_NAME_INIT_PARAM);

        if (providedRealm != null) {
            this.realm = providedRealm;
        }

        this.nonceCleanupTimer
                .schedule(this.nonceCache, this.nonceCache.getNonceMaxValid(), this.nonceCache.getNonceMaxValid());
    }

    @Override
    public void extractCredential(HttpServletRequest request, DefaultLoginCredentials creds) {
        if (isDigestAuthentication(request)) {
            String[] tokens = extractTokens(request);

            if (tokens.length > 0) {
                Digest credential = HTTPDigestUtil.digest(tokens);

                credential.setMethod(request.getMethod());

                if (this.nonceCache.hasValidNonce(credential, request)) {
                    creds.setCredential(credential);
                }
            }
        }
    }

    @Override
    public void challengeClient(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String domain = request.getContextPath();

        if (domain == null)
            domain = "/";

        String newNonce = this.nonceCache.generateAndCacheNonce(request);

        StringBuilder str = new StringBuilder("Digest realm=\"");

        str.append(this.realm).append("\",");
        str.append("domain=\"").append(domain).append("\",");
        str.append("nonce=\"").append(newNonce).append("\",");
        str.append("algorithm=MD5,");
        str.append("qop=").append("auth").append(",");
        str.append("stale=\"").append(false).append("\"");

        response.setHeader("WWW-Authenticate", str.toString());
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Override
    public boolean postAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return true;
    }

    private String[] extractTokens(HttpServletRequest request) {
        String authorizationHeader = getAuthorizationHeader(request).substring(7).trim();

        // Derived from http://issues.apache.org/bugzilla/show_bug.cgi?id=37132
        return authorizationHeader.split(",(?=(?:[^\"]*\"[^\"]*\")+$)");
    }

    private String getAuthorizationHeader(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    private boolean isDigestAuthentication(HttpServletRequest request) {
        String authorizationHeader = getAuthorizationHeader(request);

        return authorizationHeader != null && authorizationHeader.startsWith("Digest ");
    }
}
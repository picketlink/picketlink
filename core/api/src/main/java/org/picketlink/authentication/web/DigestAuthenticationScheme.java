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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.picketlink.idm.credential.Digest;

/**
 * @author Shane Bryzak
 * @author anil saldhana
 * @author Pedro Igor
 * 
 */
public class DigestAuthenticationScheme implements HTTPAuthenticationScheme {

    private long nonceMaxValid = 3 * 60 * 1000;

    private static enum NONCE_VALIDATION_RESULT {
        INVALID, STALE, VALID
    }

    private UUIDNonceGenerator nonceGenerator = new UUIDNonceGenerator();

    private ConcurrentMap<String, List<String>> idVersusNonce = new ConcurrentHashMap<String, List<String>>();

    private String realm;

    public DigestAuthenticationScheme(String realm) {
        this.realm = realm;
    }

    @Override
    public String extractUsername(HttpServletRequest request, HttpServletResponse response) {
        Digest digestCredential = (Digest) extractCredential(request, response);

        if (digestCredential != null) {
            return digestCredential.getUsername();
        }

        return null;
    }

    @Override
    public Object extractCredential(HttpServletRequest request, HttpServletResponse response) {
        Digest credential = null;

        if (isDigestAuthentication(request)) {
            String[] tokens = extractTokens(request);

            if (tokens.length > 0) {
                credential = HTTPDigestUtil.digest(tokens);

                credential.setMethod(request.getMethod());

                NONCE_VALIDATION_RESULT nonceStatus = validateNonce(credential, request);

                if (nonceStatus != NONCE_VALIDATION_RESULT.VALID) {
                    credential = null;
                }
            }
        }

        return credential;
    }

    @Override
    public void challengeClient(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String domain = request.getContextPath();

        if (domain == null)
            domain = "/";

        String newNonce = this.nonceGenerator.get();

        HttpSession session = request.getSession();

        List<String> storedNonces = this.idVersusNonce.get(session.getId());

        if (storedNonces == null) {
            storedNonces = new ArrayList<String>();
            this.idVersusNonce.put(session.getId(), storedNonces);
        }

        storedNonces.add(newNonce);

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

    private NONCE_VALIDATION_RESULT validateNonce(Digest digest, HttpServletRequest request) {
        String nonce = digest.getNonce();

        List<String> storedNonces = this.idVersusNonce.get(request.getSession().getId());

        if (storedNonces == null) {
            return NONCE_VALIDATION_RESULT.INVALID;
        }

        if (storedNonces.contains(nonce) == false) {
            return NONCE_VALIDATION_RESULT.INVALID;
        }

        if (this.nonceGenerator.hasExpired(nonce, this.nonceMaxValid)) {
            return NONCE_VALIDATION_RESULT.STALE;
        }

        return NONCE_VALIDATION_RESULT.VALID;
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
        return getAuthorizationHeader(request) != null && getAuthorizationHeader(request).startsWith("Digest ");
    }
}

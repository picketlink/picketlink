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

package org.picketlink.idm.credential.internal;

import org.picketlink.idm.internal.util.Base64;

/**
 * <p>
 * Represents a Digest credential.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class Digest {

    private String username, password, realm, nonce, uri, qop, nc, cnonce, opaque, domain, stale, method;
    private String digest;

    public Digest setUsername(String username) {
        this.username = username;
        return this;
    }

    public Digest setPassword(String password) {
        this.password = password;
        return this;
    }

    public Digest setRealm(String realm) {
        this.realm = realm;
        return this;
    }

    public Digest setNonce(String nonce) {
        this.nonce = nonce;
        return this;
    }

    public Digest setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public Digest setQop(String qop) {
        this.qop = qop;
        return this;
    }

    public Digest setNc(String nc) {
        this.nc = nc;
        return this;
    }

    public Digest setCnonce(String cnonce) {
        this.cnonce = cnonce;
        return this;
    }

    public Digest setOpaque(String opaque) {
        this.opaque = opaque;
        return this;
    }

    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return this.password;
    }

    public String getRealm() {
        return realm;
    }

    public String getNonce() {
        return nonce;
    }

    public String getUri() {
        return uri;
    }

    public String getQop() {
        return qop;
    }

    public String getNc() {
        return nc;
    }

    public String getCnonce() {
        return cnonce;
    }

    public String getOpaque() {
        return opaque;
    }

    public String getDomain() {
        return domain;
    }

    public Digest setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public String getStale() {
        return stale;
    }

    public Digest setStale(String stale) {
        this.stale = stale;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public Digest setMethod(String method) {
        this.method = method;
        return this;
    }

    public String getDigest() {
        return digest;
    }

    public Digest setDigest(String digest) {
        this.digest = digest;
        return this;
    }
    
    public Digest setDigest(byte[] digest) {
        this.digest = Base64.encodeBytes(digest);
        return this;
    }

}

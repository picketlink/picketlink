/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.picketlink.idm.credential;

import org.picketlink.common.util.Base64;

/**
 * <p>
 * Represents a Digest credential.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class Digest {

    private String username;

    private String password;

    private String realm;

    private String nonce;

    private String uri;

    private String qop;

    private String nonceCount;

    private String clientNonce;

    private String opaque;

    private String domain;

    private String stale;

    private String method;

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

    public Digest setNonceCount(String nc) {
        this.nonceCount = nc;
        return this;
    }

    public Digest setClientNonce(String cnonce) {
        this.clientNonce = cnonce;
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

    public String getNonceCount() {
        return nonceCount;
    }

    public String getClientNonce() {
        return clientNonce;
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

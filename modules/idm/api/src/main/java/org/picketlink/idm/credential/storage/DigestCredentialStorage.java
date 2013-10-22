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

package org.picketlink.idm.credential.storage;

import org.picketlink.idm.credential.storage.annotations.Stored;

/**
 * <p>
 * {@link CredentialStorage} for Digest credentials.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class DigestCredentialStorage extends AbstractCredentialStorage {

    private String realm;
    private byte[] ha1;

    public DigestCredentialStorage() {

    }

    public DigestCredentialStorage(byte[] ha1, String realm) {
        this.ha1 = ha1;
        this.realm = realm;
    }

    @Stored
    public byte[] getHa1() {
        return this.ha1;
    }

    public void setHa1(byte[] ha1) {
        this.ha1 = ha1;
    }

    @Stored
    public String getRealm() {
        return this.realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

}
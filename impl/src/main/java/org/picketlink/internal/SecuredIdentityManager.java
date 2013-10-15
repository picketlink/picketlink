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

package org.picketlink.internal;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.query.IdentityQuery;

import javax.enterprise.inject.Typed;
import java.util.Date;
import java.util.List;

/**
 * Decorator for IdentityManager that provides secured identity management operations
 *
 * @author Shane Bryzak
 */
@Typed(SecuredIdentityManager.class)
public class SecuredIdentityManager implements IdentityManager {

    private static final long serialVersionUID = -8197103563768366958L;

    private IdentityManager decorated;

    public SecuredIdentityManager(IdentityManager decorated) {
        this.decorated = decorated;
    }

    @Override
    public void add(IdentityType identityType) throws IdentityManagementException {
        decorated.add(identityType);
    }

    @Override
    public void update(IdentityType identityType) throws IdentityManagementException {
        decorated.update(identityType);
    }

    @Override
    public void remove(IdentityType value) throws IdentityManagementException {
        decorated.remove(value);
    }

    @Override
    public <T extends IdentityType> T lookupIdentityById(Class<T> identityType, String id) {
        return decorated.lookupIdentityById(identityType, id);
    }

    @Override
    public <T extends IdentityType> IdentityQuery<T> createIdentityQuery(Class<T> identityType) {
        return decorated.createIdentityQuery(identityType);
    }

    @Override
    public void validateCredentials(Credentials credentials) {
        decorated.validateCredentials(credentials);
    }

    @Override
    public void updateCredential(Account account, Object credential) {
        decorated.updateCredential(account, credential);
    }

    @Override
    public void updateCredential(Account account, Object credential, Date effectiveDate, Date expiryDate) {
        decorated.updateCredential(account, credential, effectiveDate, expiryDate);
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(Account account, Class<T> storageClass) {
        return decorated.retrieveCurrentCredential(account, storageClass);
    }

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(Account account, Class<T> storageClass) {
        return decorated.retrieveCredentials(account, storageClass);
    }

}
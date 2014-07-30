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

package org.picketlink.idm.credential.handler;

import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;

import java.util.Date;

/**
 * Performs credential validation and persists credential state to a Store.
 *
 * @author Shane Bryzak
 */
public interface CredentialHandler<S extends IdentityStore<?>,V extends Credentials,U> {

    /**
     * <p>Validates a credential.</p>
     *
     * @param context The contextual invocation context.
     * @param credentials The credential to be validated.
     * @param store The underlying identity store.
     */
    void validate(IdentityContext context, V credentials, S store);

    /**
     * <p>Updates the credential for a certain {@link org.picketlink.idm.model.Account}.</p>
     *
     * @param context The contextual invocation context.
     * @param account The account which credentials should be removed.
     * @param credential The credential to be updated.
     * @param store The underlying identity store.
     * @param effectiveDate The date specifying from when this credential is valid.
     * @param expiryDate The date specifying when the credential expires.
     */
    void update(IdentityContext context, Account account, U credential, S store, Date effectiveDate, Date expiryDate);

    /**
     * @param store
     */
    void setup(S store);
}

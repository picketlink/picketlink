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
package org.picketlink.idm.spi;

import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.RelationshipQuery;

import java.util.Date;
import java.util.List;

/**
 * IdentityStore representation providing minimal SPI
 *
 * @author Boleslaw Dawidowicz
 * @author Shane Bryzak
 */
public interface IdentityStore<T extends IdentityStoreConfiguration> {

    /**
     * Sets the configuration and context in which the IdentityStore will execute its operations
     *
     * @param config
     * @param context
     */
    void setup(T config);

    /**
     * Returns the configuration for this IdentityStore instance
     *
     * @return
     */
    T getConfig();

    // General

    /**
     * Persists the specified IdentityType
     *
     * @param identityType
     */
    void add(IdentityContext context, AttributedType value);

    /**
     * Updates the specified IdentityType
     *
     * @param identityType
     */
    void update(IdentityContext context, AttributedType value);

    /**
     * Removes the specified IdentityType
     *
     * @param identityType
     */
    void remove(IdentityContext context, AttributedType value);

    // Identity query

    <V extends IdentityType> List<V> fetchQueryResults(IdentityContext context, IdentityQuery<V> identityQuery);

    <V extends IdentityType> int countQueryResults(IdentityContext context, IdentityQuery<V> identityQuery);

    // Relationship query

    <V extends Relationship> List<V> fetchQueryResults(IdentityContext context, RelationshipQuery<V> query);

    <V extends Relationship> int countQueryResults(IdentityContext context, RelationshipQuery<V> query);

    // Credentials

    /**
     * Validates the specified credentials.  Each IdentityStore implementation typically supports
     * a concrete set of Credentials types, and will generally obtain a CredentialHandler instance
     * from the IdentityStoreInvocationContext to process credential validation.
     *
     * @param credentials
     */
    void validateCredentials(IdentityContext context, Credentials credentials);

    /**
     * Updates the specified credential value for the specified Agent.
     *
     * @param agent
     * @param credential
     */
    void updateCredential(IdentityContext context, Account account, Object credential, Date effectiveDate, Date expiryDate);

}

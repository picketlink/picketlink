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
     * <p>This is the name of the identity type property that will be used to retrieve the account's
     * login name, used for account lookup.</p>
     */
    String LOGIN_NAME_PROPERTY = "LOGIN_NAME_PROPERTY";

    /**
     * <p>This property can be used to support additional {@link Account} types. The value can be either a Array
     * of {@link Account} types or a single type.</p>
     *
     * @deprecated This property is no longer necessary to support custom {@link org.picketlink.idm.model.Account} types. You can achieve
     * the same result by providing your types during the configuration, see {@link org.picketlink.idm.config.IdentityStoreConfigurationBuilder#supportType(Class[])}.
     */
    @Deprecated
    String SUPPORTED_ACCOUNT_TYPES_PROPERTY = "SUPPORTED_ACCOUNT_TYPES";

    /**
     *
     * @param credentials
     * @param store
     * @return
     */
    void validate(IdentityContext context, V credentials, S store);
    /**
     *
     * @param context
     * @param credential
     * @param store
     */
    void update(IdentityContext context, Account account, U credential, S store, Date effectiveDate, Date expiryDate);

    /**
     * @param store
     */
    void setup(S store);
}

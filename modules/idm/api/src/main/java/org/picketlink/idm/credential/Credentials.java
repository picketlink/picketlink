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

import org.picketlink.idm.model.Account;

/**
 * Represents the credentials the current user will use to authenticate, in addition to providing information about the current
 * state of the validation process.
 *
 * Only used during the authentication process
 *
 * @author Shane Bryzak
 */
public interface Credentials {
    public enum Status {
        /**
         * <p>
         * Indicates that the credential was not validated yet.
         * </p>
         */
        UNVALIDATED,

        /**
         * <p>
         * Indicates that the credential is being validated.
         * </p>
         */
        IN_PROGRESS,

        /**
         * <p>
         * Indicates that the credential is not valid after a validation attempt.
         * </p>
         */
        INVALID,

        /**
         * <p>
         * Indicates that the credential is valid after a validation attempt.
         * </p>
         */
        VALID,

        /**
         * <p>
         * Indicates that the credential has expired.
         * </p>
         */
        EXPIRED,

        /**
         * <p>
         * Indicates that the {@link Agent} which credentials were validated is disabled.
         * </p>
         */
        ACCOUNT_DISABLED
    };

    /**
     * <p>
     * Returns the {@link Account} instance used to validate the credential.
     * </p>
     *
     * @return
     */
    Account getValidatedAccount();

    /**
     * <p>
     * Returns the validation status.
     * </p>
     *
     * @return
     */
    Status getStatus();

    /**
     * <p>
     * Invalidates the credential.
     * </p>
     */
    void invalidate();
}

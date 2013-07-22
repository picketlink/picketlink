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

import java.util.Date;

/**
 * A marker interface that indicates a Class is used to store credential related state
 *
 * @author Shane Bryzak
 *
 */
public interface CredentialStorage {

    /**
     * Return the Date from when the credential becomes effective.  A result of null means the credential has
     * no effective date (and is current as long as the expiry date is either null, or in the future).
     *
     * @return
     */
    @Stored Date getEffectiveDate();

    /**
     * Return the Date when the credential expires.  A result of null means the credential has no expiry date.
     *
     * @return
     */
    @Stored Date getExpiryDate();

}

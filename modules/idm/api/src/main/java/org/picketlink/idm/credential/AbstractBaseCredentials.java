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
 * Abstract base class for Credentials
 *
 * @author Shane Bryzak
 */
public abstract class AbstractBaseCredentials implements Credentials {

    private Account validatedAccount;
    private Status status = Status.UNVALIDATED;

    @Override
    public Account getValidatedAccount() {
        return validatedAccount;
    }

    public void setValidatedAccount(Account account) {
        this.validatedAccount = account;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}

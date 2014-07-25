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

package org.picketlink.idm.event;

import java.util.Date;

import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.model.Account;

/**
 * <p>This event is raised whenever a credential is updated</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class CredentialUpdatedEvent extends AbstractBaseEvent {

    private final Account account;
    private final Object credential;
    private final Date effectiveDate;
    private final Date expiryDate;

    public CredentialUpdatedEvent(Account account, Object credential, Date effectiveDate, Date expiryDate, PartitionManager partitionManager) {
        super(partitionManager);
        this.account = account;
        this.credential = credential;
        this.effectiveDate = effectiveDate;
        this.expiryDate = expiryDate;
    }

    public Account getAccount() {
        return account;
    }

    public Object getCredential() {
        return credential;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }
}

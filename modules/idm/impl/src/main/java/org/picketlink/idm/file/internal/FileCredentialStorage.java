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

package org.picketlink.idm.file.internal;

import org.picketlink.idm.credential.spi.CredentialStorage;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class FileCredentialStorage implements CredentialStorage, Serializable {

    private static final String EXPIRY_DATE_ATTRIBUTE_NAME = "expiryDate";
    private static final String EFFECTIVE_DATE_ATTRIBUTE_NAME = "effectiveDate";

    private static final long serialVersionUID = -349640861496483678L;

    private Map<String, Serializable> storedFields = new HashMap<String, Serializable>();

    public Map<String, Serializable> getStoredFields() {
        return this.storedFields;
    }

    public void setStoredFields(Map<String, Serializable> storedFields) {
        this.storedFields = storedFields;
    }

    @Override
    public Date getEffectiveDate() {
        return (Date) getStoredFields().get(EFFECTIVE_DATE_ATTRIBUTE_NAME);
    }

    public void setEffectiveDate(Date effectiveDate) {
        getStoredFields().put(EFFECTIVE_DATE_ATTRIBUTE_NAME, effectiveDate);
    }

    @Override
    public Date getExpiryDate() {
        return (Date) getStoredFields().get(EXPIRY_DATE_ATTRIBUTE_NAME);
    }

    public void setExpiryDate(Date expiryDate) {
        getStoredFields().put(EXPIRY_DATE_ATTRIBUTE_NAME, expiryDate);
    }
}

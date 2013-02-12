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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.picketlink.idm.credential.spi.CredentialStorage;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class FileCredentialStorage implements CredentialStorage, Serializable {

    private static final long serialVersionUID = -349640861496483678L;

    private Map<String, Serializable> storedFields = new HashMap<String, Serializable>();

    public Map<String, Serializable> getStoredFields() {
        return storedFields;
    }

    public void setStoredFields(Map<String, Serializable> storedFields) {
        this.storedFields = storedFields;
    }

    @Override
    public Date getEffectiveDate() {
        Date effectiveDate = (Date) getStoredFields().get("effectiveDate");

        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        getStoredFields().put("effectiveDate", effectiveDate);
    }

    @Override
    public Date getExpiryDate() {
        Date expiryDate = (Date) getStoredFields().get("expiryDate");

        return expiryDate;
    }
    
    public void setExpiryDate(Date expiryDate) {
        getStoredFields().put("expiryDate", expiryDate);
    }
}

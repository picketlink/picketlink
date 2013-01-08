/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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

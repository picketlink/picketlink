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
package org.picketlink.idm.ldap.internal;

import static org.picketlink.idm.ldap.internal.LDAPConstants.CREATE_TIMESTAMP;
import static org.picketlink.idm.ldap.internal.LDAPConstants.CUSTOM_ATTRIBUTE_EXPIRY_DATE;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;

import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;

/**
 * <p>
 * An adaptor class that provides barebones implementation of the {@link DirContext}.
 * </p>
 * 
 * @author anil saldhana
 * @since Aug 30, 2012
 */
public abstract class LDAPIdentityType extends LDAPAttributedType implements DirContext, IdentityType {

    private static final long serialVersionUID = 1L;

    private boolean enabled = true;
    private Date expirationDate;
    private Date createDate = new Date();

    private Partition partition;

    public LDAPIdentityType(String dnSuffix) {
        super(dnSuffix);
    }

    @Override
    public boolean isEnabled() {
        LDAPCustomAttributes customAttributes = getCustomAttributes();

        if (customAttributes != null) {
            Object enabled = customAttributes.getAttribute(LDAPConstants.CUSTOM_ATTRIBUTE_ENABLED);

            if (enabled != null) {
                setEnabled(Boolean.valueOf(enabled.toString()));
            }
        }

        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        getCustomAttributes().addAttribute(LDAPConstants.CUSTOM_ATTRIBUTE_ENABLED, enabled);
        this.enabled = enabled;
    }

    @Override
    public Date getExpirationDate() {
        if (this.expirationDate == null) {
            LDAPCustomAttributes customAttributes = getCustomAttributes();
            
            if (customAttributes != null) {
                Object expiryDate = customAttributes.getAttribute(CUSTOM_ATTRIBUTE_EXPIRY_DATE);

                if (expiryDate != null) {
                    setExpirationDate(new Date(Long.valueOf(expiryDate.toString())));
                }
            }
        }

        return this.expirationDate;
    }

    @Override
    public void setExpirationDate(Date expirationDate) {
        if (expirationDate != null) {
            getCustomAttributes().addAttribute(CUSTOM_ATTRIBUTE_EXPIRY_DATE, String.valueOf(expirationDate.getTime()));
        } else {
            getCustomAttributes().removeAttribute(CUSTOM_ATTRIBUTE_EXPIRY_DATE);
        }

        this.expirationDate = expirationDate;
    }

    @Override
    public Date getCreatedDate() {
        return this.createDate;
    }

    @Override
    public void setCreatedDate(Date createdDate) {
        if (createdDate != null) {
            createdDate = new Date();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        getLDAPAttributes().put(new BasicAttribute(CREATE_TIMESTAMP, sdf.format(createdDate)));
        
        this.createDate = createdDate;
    }

    @Override
    public Partition getPartition() {
        return this.partition;
    }

    @Override
    public void setPartition(Partition partition) {
        this.partition = partition;
    }

    @Override
    public String getDN() {
        String dn = super.getDN();

        if (Realm.class.isInstance(getPartition()) && getPartition().getName().equals(Realm.DEFAULT_REALM)) {
            // TODO: logic to change the dn when the partition is not the default
        }

        return dn;
    }
}
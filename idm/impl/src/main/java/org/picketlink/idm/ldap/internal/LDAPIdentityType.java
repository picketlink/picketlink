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
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
package org.picketlink.idm.model;

import org.picketlink.idm.query.QueryParameter;

import java.util.Date;

/**
 * Abstract base class for IdentityType implementations
 *
 * @author Shane Bryzak
 */
public abstract class AbstractIdentityType extends AbstractAttributedType implements IdentityType {

    private static final long serialVersionUID = 2843998332737143820L;

    /**
     * A query parameter used to set the {@link Partition} value
     */
    public static final QueryParameter PARTITION = new QueryParameter() {};

    /**
     * A query parameter used to set the enabled value.
     */
    public static final QueryParameter ENABLED = new QueryParameter() {};

    /**
     * A query parameter used to set the createdDate value
     */
    public static final QueryParameter CREATED_DATE = new QueryParameter() {};

    /**
     * A query parameter used to set the created after date
     */
    public static final QueryParameter CREATED_AFTER = new QueryParameter() {};

    /**
     * A query parameter used to set the created before date
     */
    public static final QueryParameter CREATED_BEFORE = new QueryParameter() {};

    /**
     * A query parameter used to set the expiryDate value
     */
    public static final QueryParameter EXPIRY_DATE = new QueryParameter() {};

    /**
     * A query parameter used to set the expiration after date
     */
    public static final QueryParameter EXPIRY_AFTER = new QueryParameter() {};

    /**
     * A query parameter used to set the expiration before date
     */
    public static final QueryParameter EXPIRY_BEFORE = new QueryParameter() {};

    /**
     * Used to specify either a realm or tier-specific role.  The query should only
     * return IdentityType instances that have been granted the specified role
     */
    public static final QueryParameter HAS_ROLE = new QueryParameter() {};

    /**
     * Used to specify either a realm-specific User or Group, or a tier-specific Group.  The
     * query should only return Role instances that the specified User or Group is a member of
     */
    public static final QueryParameter ROLE_OF = new QueryParameter() {};

    /**
     * Used to specify either a realm-specific or tier-specific group role, of which both the Group
     * and Role must be provided as parameter values.  The query should only return IdentityType
     * instances that have been granted the specified group role.
     */
    public static final QueryParameter HAS_GROUP_ROLE = new QueryParameter() {};

    /**
     * Used to specify either a realm-specific User or Group, or a tier-specific Group.  The query
     * should only return GroupRole instances that the specified User or Group is a member of
     */
    public static final QueryParameter GROUP_ROLE_OF = new QueryParameter() {};

    /**
     * Used to specify either a realm-specific or tier-specific Group.  The query should only return
     * IdentityType instances that are a member of the specified group.
     */
    public static final QueryParameter MEMBER_OF = new QueryParameter() {};

    /**
     * Used to specify either a realm-specific User or Group, or a tier-specific Group.  The query
     * should only return Group instances that the specified User or Group is a member of
     */
    public static final QueryParameter HAS_MEMBER = new QueryParameter() {};

    private boolean enabled = true;
    private Date createdDate = new Date();
    private Date expirationDate = null;

    private Partition partition;

    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public Date getExpirationDate() {
        return this.expirationDate;
    }

    @Override
    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public Date getCreatedDate() {
        return this.createdDate;
    }

    @Override
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!getClass().isInstance(obj)) {
            return false;
        }

        IdentityType other = (IdentityType) obj;

        return (getId() != null && other.getId() != null && getPartition() != null && other.getPartition() != null)
                && (getId().equals(other.getId()) && getPartition().equals(other.getPartition()));
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}

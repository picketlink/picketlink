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

import java.util.Date;
import org.picketlink.idm.query.AttributeParameter;
import org.picketlink.idm.query.QueryParameter;

/**
 * This interface is the base for all identity model objects.  It declares a number of
 * properties that must be supported by all identity types, in addition to defining the API
 * for identity attribute management.
 *
 * @author Shane Bryzak
 */
public interface IdentityType extends AttributedType {

    /**
     * A query parameter used to set the {@link Partition} value
     */
    QueryParameter PARTITION = new AttributeParameter("partition");

    /**
     * A query parameter used to set the enabled value.
     */
    QueryParameter ENABLED = new AttributeParameter("enabled");

    /**
     * A query parameter used to set the createdDate value
     */
    QueryParameter CREATED_DATE = new AttributeParameter("createdDate");

    /**
     * A query parameter used to set the created after date
     */
    QueryParameter CREATED_AFTER = new AttributeParameter("createdDate");

    /**
     * A query parameter used to set the created before date
     */
    QueryParameter CREATED_BEFORE = new AttributeParameter("createdDate");

    /**
     * A query parameter used to set the expiryDate value
     */
    QueryParameter EXPIRY_DATE = new AttributeParameter("expirationDate");

    /**
     * A query parameter used to set the expiration after date
     */
    QueryParameter EXPIRY_AFTER = new AttributeParameter("expirationDate");

    /**
     * A query parameter used to set the expiration before date
     */
    QueryParameter EXPIRY_BEFORE = new AttributeParameter("expirationDate");

    /**
     * Indicates the current enabled status of this IdentityType.
     *
     * @return A boolean value indicating whether this IdentityType is enabled.
     */
    boolean isEnabled();

    /**
     * <p>Sets the current enabled status of this {@link IdentityType}.</p>
     *
     * @param enabled
     */
    void setEnabled(boolean enabled);

    /**
     * Returns the date that this IdentityType instance was created.
     *
     * @return Date value representing the creation date
     */
    Date getCreatedDate();

    /**
     * <p>Sets the date that this {@link IdentityType} was created.</p>
     *
     * @param expirationDate
     */
    void setCreatedDate(Date createdDate);

    /**
     * Returns the date that this IdentityType expires, or null if there is no expiry date.
     *
     * @return
     */
    Date getExpirationDate();

    /**
     * <p>Sets the date that this {@link IdentityType} expires.</p>
     *
     * @param expirationDate
     */
    void setExpirationDate(Date expirationDate);

    /**
     * Returns the owning Partition for this identity object.
     *
     * @return
     */
    Partition getPartition();

    /**
     * <p>Sets the {@link Partition} for this object.</p>
     *
     * @param partition
     */
    void setPartition(Partition partition);
}

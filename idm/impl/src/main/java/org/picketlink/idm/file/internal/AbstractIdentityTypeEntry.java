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

import static org.picketlink.idm.IDMMessages.MESSAGES;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Tier;

/**
 * @author Pedro Silva
 *
 */
public abstract class AbstractIdentityTypeEntry<T extends IdentityType> extends AbstractAttributedTypeEntry<T> {

    private static final long serialVersionUID = -142418066761172579L;

    protected AbstractIdentityTypeEntry(String version, T object) {
        super(version, object);
    }

    @Override
    protected T doPopulateEntry(Map<String, Serializable> properties) throws Exception {
        T identityType = super.doPopulateEntry(properties);

        String partitionType = properties.get("partitionType").toString();

        Partition partition = null;

        if (partitionType.equals(Realm.class.getName())) {
            partition = new Realm(properties.get("partitionName").toString());
        } else if (partitionType.equals(Tier.class.getName())) {
            partition = new Tier(properties.get("partitionName").toString());
        } else {
            MESSAGES.partitionUnsupportedType(partitionType);
        }

        partition.setId(properties.get("partitionId").toString());

        identityType.setPartition(partition);

        identityType.setCreatedDate((Date) properties.get("createdDate"));
        identityType.setExpirationDate((Date) properties.get("expirationDate"));
        identityType.setEnabled((Boolean) properties.get("enabled"));

        return identityType;
    }

    @Override
    protected void doPopulateProperties(Map<String, Serializable> properties) throws Exception {
        super.doPopulateProperties(properties);

        T identityType = getEntry();

        properties.put("partitionName", identityType.getPartition().getName());
        properties.put("partitionId", identityType.getPartition().getId());
        properties.put("partitionType", identityType.getPartition().getClass().getName());
        properties.put("createdDate", identityType.getCreatedDate());
        properties.put("expirationDate", identityType.getExpirationDate());
        properties.put("enabled", identityType.isEnabled());
    }
}

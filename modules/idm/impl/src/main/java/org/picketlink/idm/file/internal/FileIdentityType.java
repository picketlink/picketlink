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

import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author pedroigor
 */
public class FileIdentityType extends AbstractFileAttributedType<IdentityType> {

    private static final String VERSION = "1";

    protected FileIdentityType(IdentityType object) {
        super(VERSION, object);
    }

    @Override
    protected IdentityType doPopulateEntry(Map<String, Serializable> properties) throws Exception {
        IdentityType identityType = super.doPopulateEntry(properties);

        String partitionType = properties.get("partitionType").toString();
        String partitionId = properties.get("partitionId").toString();

        Partition partition =
                (Partition) Class.forName(partitionType).getConstructor(new Class[]{String.class}).newInstance("");

        partition.setId(partitionId);

        identityType.setPartition(partition);

        identityType.setCreatedDate((Date) properties.get("createdDate"));
        identityType.setExpirationDate((Date) properties.get("expirationDate"));
        identityType.setEnabled((Boolean) properties.get("enabled"));

        return identityType;
    }

    @Override
    protected void doPopulateProperties(Map<String, Serializable> properties) throws Exception {
        super.doPopulateProperties(properties);

        IdentityType identityType = getEntry();

        if (identityType.getPartition() == null) {
            throw new IllegalStateException("Partition Null");
        }

        if (identityType.getPartition().getId() == null) {
            throw new IllegalStateException("Partition ID Null");
        }

        properties.put("partitionId", identityType.getPartition().getId());
        properties.put("partitionType", identityType.getPartition().getClass().getName());
        properties.put("createdDate", identityType.getCreatedDate());

        if (identityType.getExpirationDate() != null) {
            properties.put("expirationDate", identityType.getExpirationDate());
        }

        properties.put("enabled", identityType.isEnabled());
    }
}

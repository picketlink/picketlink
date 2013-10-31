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
package org.picketlink.idm.jdbc.internal.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.picketlink.idm.IDMMessages;
import org.picketlink.idm.jdbc.internal.model.db.PartitionStorageUtil;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.query.AttributeParameter;
import org.picketlink.idm.query.QueryParameter;

/**
 * The JDBC Type for {@link Partition}
 * @author Anil Saldhana
 * @since October 22, 2013
 */
public class PartitionJdbcType extends AbstractJdbcType implements Partition {
    protected String name, typeName, configurationName;

    public PartitionJdbcType() {
    }

    public PartitionJdbcType(String name) {
        this.name = name;
    }

    public PartitionJdbcType setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getTypeName() {
        return typeName;
    }

    public PartitionJdbcType setTypeName(String typeName) {
        this.typeName = typeName;
        return this;
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public PartitionJdbcType setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
        return this;
    }

    @Override
    public void setAttribute(Attribute<? extends Serializable> attribute) {
        throw IDMMessages.MESSAGES.unexpectedType(attribute.getClass());
    }

    @Override
    public void removeAttribute(String name) {
        throw new RuntimeException();
    }

    @Override
    public <T extends Serializable> Attribute<T> getAttribute(String name) {
        throw new RuntimeException();
    }

    @Override
    public Collection<Attribute<? extends Serializable>> getAttributes() {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public void delete(AttributedType attributedType) {
        throw IDMMessages.MESSAGES.unexpectedType(attributedType.getClass());
    }

    @Override
    public void deleteRelationships(AttributedType attributedType) {
        throw IDMMessages.MESSAGES.unexpectedType(attributedType.getClass());
    }

    @Override
    public void persist(AttributedType attributedType) {
        PartitionJdbcType partition = (PartitionJdbcType) attributedType;
        if (load(partition.getId(), partition) == null) {
            PartitionStorageUtil partitionStorageUtil = new PartitionStorageUtil();
            partitionStorageUtil.storePartition(dataSource, partition);
        }
    }

    @Override
    public AttributedType load(String id, AttributedType attributedType) {
        PartitionStorageUtil partitionStorageUtil = new PartitionStorageUtil();
        return partitionStorageUtil.loadPartitionById(dataSource, id);
    }

    @Override
    public AttributedType load(String id, Class<? extends AttributedType> attributedType) {
        PartitionStorageUtil partitionStorageUtil = new PartitionStorageUtil();
        return partitionStorageUtil.loadPartitionById(dataSource, id);
    }

    @Override
    public List<? extends AttributedType> load(Map<QueryParameter, Object[]> params,
            Class<? extends AttributedType> attributedType) {
        List<AttributedType> result = new ArrayList<AttributedType>();
        Object[] name = getValuesFromParamMap(params,new AttributeParameter("name"));
        PartitionStorageUtil partitionStorageUtil = new PartitionStorageUtil();
        result.add(partitionStorageUtil.loadPartitionByName(dataSource, (String) name[0]));
        return result;
    }

    @Override
    public void update(AttributedType attributedType) {
        throw IDMMessages.MESSAGES.unexpectedType(attributedType.getClass());
    }
}
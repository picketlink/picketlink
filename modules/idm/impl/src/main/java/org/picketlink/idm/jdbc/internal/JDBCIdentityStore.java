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
package org.picketlink.idm.jdbc.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.picketlink.idm.config.JDBCIdentityStoreConfiguration;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.internal.AbstractIdentityStore;
import org.picketlink.idm.jdbc.internal.mappers.JdbcMapper;
import org.picketlink.idm.jdbc.internal.model.AbstractJdbcType;
import org.picketlink.idm.jdbc.internal.model.PartitionJdbcType;
import org.picketlink.idm.jdbc.internal.model.RelationshipJdbcType;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.query.AttributeParameter;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.spi.AttributeStore;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.PartitionStore;

/**
 * Implementation of {@link IdentityStore} using JDBC
 * @author Anil Saldhana
 * @since September 25, 2013
 */
public class JDBCIdentityStore extends AbstractIdentityStore<JDBCIdentityStoreConfiguration> implements
        CredentialStore<JDBCIdentityStoreConfiguration>, PartitionStore<JDBCIdentityStoreConfiguration>,
        AttributeStore<JDBCIdentityStoreConfiguration> {

    private DataSource dataSource = null;
    private JdbcMapper mapper = new JdbcMapper();

    @Override
    public void setup(JDBCIdentityStoreConfiguration config) {
        super.setup(config);
        this.dataSource = config.getDataSource();
        Map<String,Class<?>> customClassMapping = config.getCustomClassMapping();
        if(customClassMapping != null){
            Set<String> keyset = customClassMapping.keySet();
            for(String key: keyset){
                JdbcMapper.map(key,customClassMapping.get(key));
            }
        }
    }

    @Override
    protected void removeFromRelationships(IdentityContext context, IdentityType identityType) {
        AbstractJdbcType att = mapper.getInstance(identityType.getClass());
        att.setDataSource(dataSource);
        att.deleteRelationships(identityType);
    }

    @Override
    protected void removeCredentials(IdentityContext context, Account account) {
        //TODO: Deal with removing account credentials
    }

    protected void addAttributedType(IdentityContext context, AttributedType attributedType) {
        // Store attributedType in DB
        AbstractJdbcType att = mapper.getInstance(attributedType.getClass());
        att.setDataSource(dataSource).persist(attributedType);
    }

    @Override
    protected void updateAttributedType(IdentityContext context, AttributedType attributedType) {
        AbstractJdbcType ajt = mapper.getInstance(attributedType.getClass());
        ajt.setDataSource(dataSource);
        ajt.update(attributedType);
    }

    @Override
    protected void removeAttributedType(IdentityContext context, AttributedType attributedType) {
        AbstractJdbcType ajt = mapper.getInstance(attributedType.getClass());
        ajt.setDataSource(dataSource);
        ajt.delete(attributedType);
    }

    @Override
    public void storeCredential(IdentityContext context, Account account, CredentialStorage storage) {
        throw new RuntimeException();
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(IdentityContext context, Account account,
            Class<T> storageClass) {
        throw new RuntimeException();
    }

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(IdentityContext context, Account account,
            Class<T> storageClass) {
        throw new RuntimeException();
    }

    @Override
    public <V extends IdentityType> List<V> fetchQueryResults(IdentityContext context, IdentityQuery<V> identityQuery) {
        List<V> result = new ArrayList<V>();
        AttributedType attributedType = null;

        if (identityQuery.getParameter(IdentityType.ID) != null) {
            Object[] parameter = identityQuery.getParameter(IdentityType.ID);

            if (parameter.length > 0) {
                Object id = parameter[0];
                // ADD to result
                AbstractJdbcType ajt = mapper.getInstance(identityQuery.getIdentityType());
                ajt.setDataSource(dataSource);
                attributedType = ajt.load((String) id, identityQuery.getIdentityType());
                if (attributedType != null) {
                    result.add((V) attributedType);
                }
            } else {
                throw new RuntimeException();
            }
        } else {
            AbstractJdbcType ajt = mapper.getInstance(identityQuery.getIdentityType());
            ajt.setDataSource(dataSource);
            List<? extends AttributedType> list = ajt.load(identityQuery.getParameters(), identityQuery.getIdentityType());
            if (!list.isEmpty()) {
                result.addAll((Collection<? extends V>) list);
            }
        }
        return result;
    }

    @Override
    public <V extends IdentityType> int countQueryResults(IdentityContext context, IdentityQuery<V> identityQuery) {
        throw new RuntimeException();
    }

    @Override
    public <V extends Relationship> List<V> fetchQueryResults(IdentityContext context, RelationshipQuery<V> query) {
        RelationshipJdbcType relationshipJdbcType = new RelationshipJdbcType();
        relationshipJdbcType.setDataSource(dataSource);

        List<V> result = new ArrayList<V>();
        List<? extends AttributedType> list = relationshipJdbcType.load(query.getParameters(), query.getRelationshipClass());
        if (list.isEmpty() == false) {
            result.addAll((Collection<? extends V>) list);
        }
        return result;
    }

    @Override
    public <V extends Relationship> int countQueryResults(IdentityContext context, RelationshipQuery<V> query) {
        throw new RuntimeException();
    }

    @Override
    public void setAttribute(IdentityContext context, AttributedType attributedType, Attribute<? extends Serializable> attribute) {
        AbstractJdbcType ajt = mapper.getInstance(attributedType.getClass());
        ajt.setId(attributedType.getId());
        ajt.setDataSource(dataSource);
        ajt.setType(attributedType);
        ajt.setAttribute(attribute);
    }

    @Override
    public <V extends Serializable> Attribute<V> getAttribute(IdentityContext context, AttributedType attributedType,
            String attributeName) {
        AbstractJdbcType ajt = mapper.getInstance(attributedType.getClass());
        ajt.setId(attributedType.getId());
        ajt.setDataSource(dataSource);
        ajt.setType(attributedType);
        return ajt.getAttribute(attributeName);
    }

    @Override
    public void removeAttribute(IdentityContext context, AttributedType attributedType, String attributeName) {
        AbstractJdbcType ajt = mapper.getInstance(attributedType.getClass());
        ajt.setId(attributedType.getId());
        ajt.setDataSource(dataSource);
        ajt.setType(attributedType);
        ajt.removeAttribute(attributeName);
    }

    @Override
    public void loadAttributes(IdentityContext context, AttributedType attributedType) {
        if (attributedType != null) {
            // We need to load the attributes from DB into attributedType
            AbstractJdbcType ajt = mapper.getInstance(attributedType.getClass());
            ajt.setDataSource(dataSource);
            ajt.setId(attributedType.getId());

            Collection<? extends Attribute> attributes = ajt.getAttributes();
            if (attributes != null) {
                for (Attribute attribute : attributes) {
                    attributedType.setAttribute(attribute);
                }
            }
        }
    }

    @Override
    public String getConfigurationName(IdentityContext identityContext, Partition partition) {
        // TODO: get the config name
        return "SIMPLE_JDBC_STORE_CONFIG";
    }

    @Override
    public <P extends Partition> P get(IdentityContext identityContext, Class<P> partitionClass, String name) {
        PartitionJdbcType pjt = new PartitionJdbcType(name);
        pjt.setDataSource(dataSource);
        Map<QueryParameter, Object[]> map = new HashMap<QueryParameter, Object[]>();
        map.put(new AttributeParameter("name"), new Object[] { name });
        return (P) pjt.load(map, Partition.class).get(0);
    }

    @Override
    public <P extends Partition> List<P> get(IdentityContext identityContext, Class<P> partitionClass) {
        throw new RuntimeException();
    }

    @Override
    public <P extends Partition> P lookupById(IdentityContext context, Class<P> partitionClass, String id) {
        throw new RuntimeException();
    }

    @Override
    public void add(IdentityContext identityContext, Partition partition, String configurationName) {
        PartitionJdbcType partitionJdbcType = new PartitionJdbcType(partition.getName());
        partitionJdbcType.setDataSource(dataSource);
        if (partition.getId() == null) {
            if (partition instanceof Realm) {
                partitionJdbcType.setId(Realm.DEFAULT_REALM);
            } else {
                partitionJdbcType.setId(identityContext.getIdGenerator().generate());
            }
        }
        partitionJdbcType.setConfigurationName(configurationName).setTypeName(partition.getClass().getName());
        partitionJdbcType.persist(partitionJdbcType);
    }

    @Override
    public void update(IdentityContext identityContext, Partition partition) {
        throw new RuntimeException();
    }

    @Override
    public void remove(IdentityContext identityContext, Partition partition) {
        throw new RuntimeException();
    }
}

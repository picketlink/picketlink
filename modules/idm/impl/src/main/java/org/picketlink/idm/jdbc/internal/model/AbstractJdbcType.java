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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.query.AttributeParameter;
import org.picketlink.idm.query.QueryParameter;

/**
 * Base class for the JDBC Types that are equivalent to the IDM model
 * @author Anil Saldhana
 * @since October 22, 2013
 */
public abstract class AbstractJdbcType implements AttributedType, Serializable {
    private static final long serialVersionUID = 1L;
    protected String id;
    protected DataSource dataSource;
    protected AttributedType type;

    /**
     * @see org.picketlink.idm.model.AttributedType#getId()
     * @return
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the ID
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Set the {@link DataSource}
     * @param dataSource
     * @return
     */
    public AbstractJdbcType setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    /**
     * Get the {@link DataSource}
     * @return
     */
    public DataSource getDataSource() {
        return this.dataSource;
    }

    public AbstractJdbcType setType(AttributedType attributedType) {
        this.type = attributedType;
        return this;
    }

    public AttributedType getType() {
        return type;
    }

    /**
     * Delete {@link AttributedType}
     * @param attributedType
     */
    public abstract void delete(AttributedType attributedType);

    /**
     * Delete all the {@link Relationship} the {@link AttributedType}
     * is involved in.
     * @param attributedType
     */
    public abstract void deleteRelationships(AttributedType attributedType);

    /**
     * Load an {@link AttributedType} given its id
     * @param id
     * @param attributedType
     * @return
     */
    public abstract AttributedType load(String id, AttributedType attributedType);

    /**
     * Load an {@link AttributedType} given its id and the type
     * @param id
     * @param attributedType
     * @return
     */
    public abstract AttributedType load(String id, Class<? extends AttributedType> attributedType);

    /**
     * Load a list of {@link AttributedType} given various parameters
     * @param params
     * @param attributedType
     * @return
     */
    public abstract List<? extends AttributedType> load(Map<QueryParameter, Object[]> params,
            Class<? extends AttributedType> attributedType);

    /**
     * Store the {@link AttributedType} in the database
     * @param attributedType
     */
    public abstract void persist(AttributedType attributedType);

    /**
     * Update the stored {@link AttributedType} in the database
     * @param attributedType
     */
    public abstract void update(AttributedType attributedType);

    protected Object[] getValuesFromParamMap(Map<QueryParameter, Object[]> params,AttributeParameter attributeParameter){
        Set<QueryParameter> keys = params.keySet();
        if(keys != null){
            for(QueryParameter key: keys){
                if(key instanceof  AttributeParameter){
                    AttributeParameter aparam = (AttributeParameter) key;
                    if(aparam.getName().equalsIgnoreCase(attributeParameter.getName())){
                        return params.get(key);
                    }
                }
            }
        }
        return null;
    }
}
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

import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.query.QueryParameter;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * @author Anil Saldhana
 * @since October 22, 2013
 */
public abstract class AbstractJdbcType implements AttributedType,Serializable{
    private static final long serialVersionUID = 1L;
    protected String id;
    protected DataSource dataSource;
    protected AttributedType type;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AbstractJdbcType setDataSource(DataSource dataSource){
        this.dataSource = dataSource;
        return this;
    }

    public DataSource getDataSource(){
        return  this.dataSource;
    }

    public AbstractJdbcType setType(AttributedType attributedType){
        this.type = attributedType;
        return this;
    }

    public AttributedType getType(){
        return type;
    }

    public abstract void delete(AttributedType attributedType);
    public abstract void persist(AttributedType attributedType);

    public abstract AttributedType load(String id, AttributedType attributedType);
    public abstract AttributedType load(String id, Class<? extends AttributedType> attributedType);
    public abstract List<? extends  AttributedType> load(Map<QueryParameter,Object[]> params, Class<? extends AttributedType> attributedType);

    public abstract void update(AttributedType attributedType);
}
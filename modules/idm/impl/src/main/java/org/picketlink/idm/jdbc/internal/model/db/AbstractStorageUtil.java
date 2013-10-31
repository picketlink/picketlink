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
package org.picketlink.idm.jdbc.internal.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.picketlink.idm.jdbc.internal.model.PartitionJdbcType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.query.AttributeParameter;
import org.picketlink.idm.query.QueryParameter;

/**
 * @author Anil Saldhana
 * @since October 24, 2013
 */
public abstract class AbstractStorageUtil {

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

    protected Partition loadPartition(DataSource dataSource, String id) {
        if (dataSource == null) {
            throw new RuntimeException("Null datasource");
        }
        if (id == null) {
            throw new RuntimeException("Null id");
        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select name from Partition where id =?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Partition partition = new PartitionJdbcType(resultSet.getString(1));
                return partition;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(resultSet);
            safeClose(preparedStatement);
            safeClose(connection);
        }
        return null;
    }

    protected void safeClose(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
            }
        }
    }

    protected void safeClose(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
            }
        }
    }

    protected void safeClose(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
            }
        }
    }
}

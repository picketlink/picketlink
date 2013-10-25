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

import org.picketlink.idm.jdbc.internal.model.PartitionJdbcType;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.query.AttributeParameter;
import org.picketlink.idm.query.QueryParameter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Set;

/**
 * @author Anil Saldhana
 * @since October 24, 2013
 */
public class RoleStorageUtil extends AbstractStorageUtil {

    public void storeRole(DataSource dataSource,Role role){
        Calendar calendar = new GregorianCalendar(500 + 1900, 12, 12);
        Date expiration = calendar.getTime();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            String sql = "insert into Role set name=?,id=?," +
                    "createdDate=?,expirationDate=?,partitionID=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,role.getName());
            preparedStatement.setString(2,role.getId());
            preparedStatement.setDate(3, new java.sql.Date(role.getCreatedDate().getTime()));
            preparedStatement.setDate(4,new java.sql.Date(expiration.getTime()));
            preparedStatement.setString(5,role.getPartition().getId());
            int result = preparedStatement.executeUpdate();
            if(result == 0){
                throw new RuntimeException("Insert into Role failed");
            }

            //Ensure that the Partition is also stored
            PartitionJdbcType pj = new PartitionJdbcType("dummy");
            pj.setDataSource(dataSource);
            pj.persist(role.getPartition());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            safeClose(preparedStatement);
            safeClose(connection);
        }
    }


    public Role loadRole(DataSource dataSource,String id){
        if(dataSource == null){
            throw new RuntimeException("Null datasource");
        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select name,partitionID from Role where id =?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                Role role = new Role();
                role.setName(resultSet.getString(1));
                role.setId(id);
                role.setPartition(loadPartition(dataSource,resultSet.getString(2)));
                return role;
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
    public Role loadRole(DataSource dataSource,Map<QueryParameter,Object[]> params){
        Set<QueryParameter> queryParameters = params.keySet();
        for(QueryParameter queryParameter: queryParameters){
            if(queryParameter instanceof AttributeParameter){
                AttributeParameter attributeParameter = (AttributeParameter) queryParameter;
                Object[] paramValues = params.get(queryParameter);
                String attributeName = attributeParameter.getName();
                if("name".equals(attributeName)){
                    String loginNameValue = (String) paramValues[0];
                    return loadRoleByName(dataSource,loginNameValue);
                }else throw new RuntimeException();
            }
        }
        throw new RuntimeException();
    }

    public Role loadRoleByName(DataSource dataSource,String roleName){
        if(dataSource == null){
            throw new RuntimeException("Null datasource");
        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select id,partitionID from Role where name =?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,roleName);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                Role role = new Role();
                role.setId(resultSet.getString(1));
                role.setName(roleName);
                role.setPartition(loadPartition(dataSource,resultSet.getString(2)));
                return role;
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
}

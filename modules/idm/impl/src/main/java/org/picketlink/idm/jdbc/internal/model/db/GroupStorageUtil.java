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
import org.picketlink.idm.model.basic.Group;
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
public class GroupStorageUtil extends AbstractStorageUtil {

    public void storeGroup(DataSource dataSource,Group group){
        Calendar calendar = new GregorianCalendar(500 + 1900, 12, 12);
        Date expiration = calendar.getTime();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            String sql = "insert into Groups set name=?,id=?," +
                    "createdDate=?,expirationDate=?,partitionID=?,parentGroup=?,path=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,group.getName());
            preparedStatement.setString(2,group.getId());
            preparedStatement.setDate(3, new java.sql.Date(group.getCreatedDate().getTime()));
            preparedStatement.setDate(4, new java.sql.Date(expiration.getTime()));
            preparedStatement.setString(5, group.getPartition().getId());
            preparedStatement.setString(6, group.getParentGroup().getId());
            preparedStatement.setString(7, group.getPath());
            int result = preparedStatement.executeUpdate();
            if(result == 0){
                throw new RuntimeException("Insert into Role failed");
            }

            //Ensure that the Partition is also stored
            PartitionJdbcType pj = new PartitionJdbcType("dummy");
            pj.setDataSource(dataSource);
            pj.persist(group.getPartition());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            safeClose(preparedStatement);
            safeClose(connection);
        }
    }


    public Group loadGroup(DataSource dataSource,String id){
        if(dataSource == null){
            throw new RuntimeException("Null datasource");
        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select name,partitionID,parentGroup,path from Groups where id =?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                Group group = new Group();
                group.setName(resultSet.getString(1));
                group.setId(id);
                group.setPartition(loadPartition(dataSource, resultSet.getString(2)));
                group.setParentGroup(loadGroup(dataSource, resultSet.getString(3)));
                group.setPath(resultSet.getString(4));
                return group;
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
    public Group loadGroup(DataSource dataSource,Map<QueryParameter,Object[]> params){
        Set<QueryParameter> queryParameters = params.keySet();
        for(QueryParameter queryParameter: queryParameters){
            if(queryParameter instanceof AttributeParameter){
                AttributeParameter attributeParameter = (AttributeParameter) queryParameter;
                Object[] paramValues = params.get(queryParameter);
                String attributeName = attributeParameter.getName();
                if("name".equals(attributeName)){
                    String loginNameValue = (String) paramValues[0];
                    return loadGroupByName(dataSource, loginNameValue);
                }else if("path".equals(attributeName)){
                    String loginNameValue = (String) paramValues[0];
                    return loadGroupByPath(dataSource, loginNameValue);
                }else throw new RuntimeException();
            }
        }
        throw new RuntimeException();
    }

    public Group loadGroupByName(DataSource dataSource,String groupName){
        if(dataSource == null){
            throw new RuntimeException("Null datasource");
        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select id,partitionID,parentGroup,path from Groups where name =?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,groupName);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                Group group = new Group();
                group.setId(resultSet.getString(1));
                group.setName(groupName);
                group.setPartition(loadPartition(dataSource, resultSet.getString(2)));
                group.setParentGroup(loadGroup(dataSource, resultSet.getString(3)));
                group.setPath(resultSet.getString(4));
                return  group;
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

    public Group loadGroupByPath(DataSource dataSource,String path){
        if(dataSource == null){
            throw new RuntimeException("Null datasource");
        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select id,name,partitionID,parentGroup from Groups where path =?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,path);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                Group group = new Group();
                group.setId(resultSet.getString(1));
                group.setName(resultSet.getString(2));
                group.setPartition(loadPartition(dataSource, resultSet.getString(3)));
                group.setParentGroup(loadGroup(dataSource, resultSet.getString(4)));
                group.setPath(path);
                return  group;
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
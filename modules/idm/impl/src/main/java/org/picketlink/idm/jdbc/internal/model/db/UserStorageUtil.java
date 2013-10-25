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
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.basic.User;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Anil Saldhana
 * @since October 24, 2013
 */
public class UserStorageUtil extends AbstractStorageUtil {
    public void deleteUser(DataSource dataSource, User user){
        Calendar calendar = new GregorianCalendar(500 + 1900, 12, 12);
        Date expiration = calendar.getTime();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            String sql = "delete from User where id=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,user.getId());
            int result = preparedStatement.executeUpdate();
            if(result == 0){
                throw new RuntimeException("Delete User failed");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            safeClose(preparedStatement);
            safeClose(connection);
        }
    }


    public User loadUser(DataSource dataSource, Map<QueryParameter,Object[]> params){
        Set<QueryParameter> queryParameters = params.keySet();
        for(QueryParameter queryParameter: queryParameters){
            if(queryParameter instanceof AttributeParameter){
                AttributeParameter attributeParameter = (AttributeParameter) queryParameter;
                Object[] paramValues = params.get(queryParameter);
                String attributeName = attributeParameter.getName();
                if("loginName".equals(attributeName)){
                    String loginNameValue = (String) paramValues[0];
                    return loadUserByLoginName(dataSource,loginNameValue);
                }else throw new RuntimeException();
            }
        }
        throw new RuntimeException();
    }

    public User loadUser(DataSource dataSource, String id){
        if(dataSource == null){
            throw new RuntimeException("Null datasource");
        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select firstName,lastName,email,loginName,partitionID from User where id =?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                User user = new User();
                user.setFirstName(resultSet.getString(1));
                user.setLastName(resultSet.getString(2));
                user.setEmail(resultSet.getString(3));
                user.setLoginName(resultSet.getString(4));
                user.setPartition(loadPartition(dataSource,resultSet.getString(5)));
                user.setId(id);

                //Get attributes also
                AttributeStorageUtil attributeStorageUtil = new AttributeStorageUtil();
                List<Attribute> attributeList = attributeStorageUtil.getAttributes(dataSource,id);
                if(attributeList.isEmpty() == false){
                    for(Attribute attribute: attributeList){
                        user.setAttribute(attribute);
                    }
                }
                return user;
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

    public User loadUserByLoginName(DataSource dataSource, String loginName){
        if(dataSource == null){
            throw new RuntimeException("Null datasource");
        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select id, firstName,lastName,email,partitionID from User where loginName =?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,loginName);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                User user = new User();
                user.setId(resultSet.getString(1));
                user.setFirstName(resultSet.getString(2));
                user.setLastName(resultSet.getString(3));
                user.setEmail(resultSet.getString(4));
                user.setLoginName(loginName);
                user.setPartition(loadPartition(dataSource,resultSet.getString(5)));

                //Get attributes also
                AttributeStorageUtil attributeStorageUtil = new AttributeStorageUtil();
                List<Attribute> attributeList = attributeStorageUtil.getAttributes(dataSource,user.getId());
                if(attributeList.isEmpty() == false){
                    for(Attribute attribute: attributeList){
                        user.setAttribute(attribute);
                    }
                }
                return user;
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

    public void updateUser(DataSource dataSource, User user){
        if(dataSource == null){
            throw new RuntimeException("Null datasource");
        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "update User set firstName=?,lastName=?,email=?,loginName=? where id =?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,user.getFirstName());
            preparedStatement.setString(2,user.getLastName());
            preparedStatement.setString(3,user.getEmail());
            preparedStatement.setString(4,user.getLoginName());
            preparedStatement.setString(5,user.getId());
            int result = preparedStatement.executeUpdate();
            if(result == 0){
                throw new RuntimeException("Update into user failed");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(resultSet);
            safeClose(preparedStatement);
            safeClose(connection);
        }
    }

    public void storeUser(DataSource dataSource, User user){
        Calendar calendar = new GregorianCalendar(500 + 1900, 12, 12);
        Date userExpiration = user.getExpirationDate();
        Date expiration = calendar.getTime();
        if(userExpiration != null){
            expiration = userExpiration;
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            String sql = "insert into user set firstName=?,lastName=?,email=?,loginName=?,id=?," +
                    "createdDate=?,expirationDate=?,partitionID=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,user.getFirstName());
            preparedStatement.setString(2,user.getLastName());
            preparedStatement.setString(3,user.getEmail());
            preparedStatement.setString(4, user.getLoginName());
            preparedStatement.setString(5, user.getId());
            preparedStatement.setDate(6, new java.sql.Date(user.getCreatedDate().getTime()));
            preparedStatement.setDate(7,new java.sql.Date(expiration.getTime()));
            preparedStatement.setString(8,user.getPartition().getId());
            int result = preparedStatement.executeUpdate();
            if(result == 0){
                throw new RuntimeException("Insert into user failed");
            }

            //Ensure that the Partition is also stored
            PartitionJdbcType pj = new PartitionJdbcType("dummy");
            pj.setDataSource(dataSource);
            pj.persist(user.getPartition());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            safeClose(preparedStatement);
            safeClose(connection);
        }
    }
}

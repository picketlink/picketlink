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

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.picketlink.common.util.Base64;
import org.picketlink.idm.model.Attribute;

/**
 * @author Anil Saldhana
 * @since October 25, 2013
 */
public class AttributeStorageUtil extends AbstractStorageUtil {
    public Attribute getAttribute(DataSource dataSource, String id, String attributeName) {
        if (dataSource == null) {
            throw new RuntimeException("Null datasource");
        }
        Attribute attribute = null;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select attributeType from Attributes where owner =? and name=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, attributeName);
            resultSet = preparedStatement.executeQuery();
            String attributeType = null;
            while (resultSet.next()) {
                attributeType = resultSet.getString(1);
            }
            List<String> valList = getAttributeValues(dataSource, id, attributeName);
            if (valList.size() > 1) {
                attribute = new Attribute(attributeName, "dummy");
                // Multi valued attribute
                Serializable[] serialArray = new Serializable[valList.size()];
                int i = 0;
                for (String attributeValue : valList) {
                    serialArray[i++] = (Serializable) Base64.decodeToObject(attributeValue);
                }
                attribute.setValue(serialArray);
            }else {
                attribute = new Attribute(attributeName,(Serializable) Base64.decodeToObject(valList.get(0)));
            }
            return attribute;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(resultSet);
            safeClose(preparedStatement);
            safeClose(connection);
        }
    }

    public List<Attribute> getAttributes(DataSource dataSource, String ownerId) {
        if (dataSource == null) {
            throw new RuntimeException("Null datasource");
        }
        List<Attribute> attributes = new ArrayList<Attribute>();

        UserStorageUtil userStorageUtil = new UserStorageUtil();
        RoleStorageUtil roleStorageUtil = new RoleStorageUtil();
        GroupStorageUtil groupStorageUtil = new GroupStorageUtil();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select name,value from Attributes where owner =?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, ownerId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String storedName = resultSet.getString(1);

                Attribute attribute = getAttribute(dataSource,ownerId,storedName);
                attributes.add(attribute);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(resultSet);
            safeClose(preparedStatement);
            safeClose(connection);
        }
        return attributes;
    }

    public void setAttribute(DataSource dataSource, String ownerId, Attribute attribute) {
        if (dataSource == null) {
            throw new RuntimeException("Null datasource");
        }
        Serializable values = attribute.getValue();

        if (!values.getClass().isArray()) {
            values = new Serializable[] { values };
        }

        if (values instanceof byte[]) {
            values = new Serializable[] { values };
        }

        for (Serializable attributeValue : (Serializable[]) values) {
            UserStorageUtil userStorageUtil = new UserStorageUtil();
            RoleStorageUtil roleStorageUtil = new RoleStorageUtil();
            GroupStorageUtil groupStorageUtil = new GroupStorageUtil();

            Connection connection = null;
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            try {
                connection = dataSource.getConnection();
                String sql = "insert into Attributes set owner =?, name=?, value=?,attributeType=?";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, ownerId);
                preparedStatement.setString(2, attribute.getName());
                preparedStatement.setString(3, Base64.encodeObject(attributeValue));
                preparedStatement.setString(4, attribute.getClass().getName());
                int result = preparedStatement.executeUpdate();
                if (result == 0) {
                    throw new RuntimeException("Update failed");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                safeClose(resultSet);
                safeClose(preparedStatement);
                safeClose(connection);
            }
        }
    }

    public void deleteAttribute(DataSource dataSource, String ownerId, String attributeName) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "Delete from Attributes where owner =? and name=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, ownerId);
            preparedStatement.setString(2, attributeName);
            int result = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(resultSet);
            safeClose(preparedStatement);
            safeClose(connection);
        }
    }

    private List<String> getAttributeValues(DataSource dataSource, String ownerId, String attributeName) {
        List<String> list = new ArrayList<String>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select value from Attributes where owner =? and name=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, ownerId);
            preparedStatement.setString(2, attributeName);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(resultSet);
            safeClose(preparedStatement);
            safeClose(connection);
        }
        return list;
    }
}

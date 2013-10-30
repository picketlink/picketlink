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
import org.picketlink.idm.IDMMessages;
import org.picketlink.idm.model.Attribute;

/**
 * Storage utility for attributes
 *
 * @author Anil Saldhana
 * @since October 25, 2013
 */
public class AttributeStorageUtil extends AbstractStorageUtil {
    /**
     * Get the {@link Attribute} given its name and an id
     *
     * @param dataSource
     * @param id
     * @param attributeName
     * @return
     */
    public Attribute getAttribute(DataSource dataSource, String id, String attributeName) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
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
                break;
            }
            List<? extends Serializable> valList = getAttributeValues(dataSource, id, attributeName);
            if (valList.size() > 1) {
                attribute = new Attribute(attributeName, "dummy");
                if (isPrimitiveNativeType(attributeType)) {
                    handlePrimitiveAttributeType(attribute, attributeType, valList);
                } else {
                    // Multi valued attribute
                    Serializable[] serialArray = new Serializable[valList.size()];
                    int i = 0;
                    for (Serializable attributeValue : valList) {
                        serialArray[i++] = attributeValue;
                    }
                    attribute.setValue(serialArray);
                }
            } else {
                attribute = new Attribute(attributeName, valList.get(0));
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

    /**
     * Get a list of {@link Attribute} for an identity type
     *
     * @param dataSource
     * @param ownerId
     * @return
     */
    public List<Attribute> getAttributes(DataSource dataSource, String ownerId) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
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

                Attribute attribute = getAttribute(dataSource, ownerId, storedName);
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

    /**
     * Set the {@link Attribute} for an {@link org.picketlink.idm.model.IdentityType}
     *
     * @param dataSource
     * @param ownerId
     * @param attribute
     */
    public void setAttribute(DataSource dataSource, String ownerId, Attribute attribute) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        Object values = attribute.getValue();

        if (!values.getClass().isArray()) {
            Serializable serializedValues = (Serializable) values;
            values = new Serializable[] { serializedValues };
        }

        if (values instanceof byte[]) {
            Serializable serializedValues = (Serializable) values;
            values = new Serializable[] { serializedValues };
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
                preparedStatement.setString(4, attributeValue.getClass().getName());
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

    /**
     * Delete an {@link Attribute} given its name and owner
     *
     * @param dataSource
     * @param ownerId
     * @param attributeName
     */
    public void deleteAttribute(DataSource dataSource, String ownerId, String attributeName) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
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

    private List<? extends Serializable> getAttributeValues(DataSource dataSource, String ownerId, String attributeName) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        List<Serializable> list = new ArrayList<Serializable>();
        List<String> stringList = new ArrayList<String>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select value,attributeType from Attributes where owner =? and name=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, ownerId);
            preparedStatement.setString(2, attributeName);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String attributeType = resultSet.getString(2);
                if (attributeType.equals(String.class.getName())) {
                    stringList.add((String) Base64.decodeToObject(resultSet.getString(1)));
                } else {
                    list.add((Serializable) Base64.decodeToObject(resultSet.getString(1)));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(resultSet);
            safeClose(preparedStatement);
            safeClose(connection);
        }
        if (stringList.isEmpty() == false) {
            return stringList;
        }
        return list;
    }

    private boolean isPrimitiveNativeType(String attributeType) {
        if (String.class.getName().equals(attributeType) || Integer.class.getName().equals(attributeType)) {
            return true;
        }
        return false;
    }

    private void handlePrimitiveAttributeType(Attribute attribute, String attributeType, List<? extends Serializable> valueList) {
        if (String.class.getName().equals(attributeType)) {
            // We have a string array
            String[] serialArray = new String[valueList.size()];
            int i = 0;
            for (Serializable str : valueList) {
                serialArray[i++] = str.toString();
            }
            attribute.setValue(serialArray);
        } else if (Integer.class.getName().equals(attributeType)) {
            Integer[] serialArray = new Integer[valueList.size()];
            int i = 0;
            for (Serializable str : valueList) {
                serialArray[i++] = (Integer) str;
            }
            attribute.setValue(serialArray);
        } else if (Long.class.getName().equals(attributeType)) {
            Long[] serialArray = new Long[valueList.size()];
            int i = 0;
            for (Serializable str : valueList) {
                serialArray[i++] = (Long) str;
            }
            attribute.setValue(serialArray);
        } else if (Double.class.getName().equals(attributeType)) {
            Double[] serialArray = new Double[valueList.size()];
            int i = 0;
            for (Serializable str : valueList) {
                serialArray[i++] = (Double) str;
            }
            attribute.setValue(serialArray);
        } else if (Float.class.getName().equals(attributeType)) {
            Float[] serialArray = new Float[valueList.size()];
            int i = 0;
            for (Serializable str : valueList) {
                serialArray[i++] = (Float) str;
            }
            attribute.setValue(serialArray);
        } else if (Short.class.getName().equals(attributeType)) {
            Short[] serialArray = new Short[valueList.size()];
            int i = 0;
            for (Serializable str : valueList) {
                serialArray[i++] = (Short) str;
            }
            attribute.setValue(serialArray);
        }
    }
}
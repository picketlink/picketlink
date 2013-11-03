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
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.picketlink.idm.IDMMessages;
import org.picketlink.idm.jdbc.internal.model.PartitionJdbcType;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.query.AttributeParameter;
import org.picketlink.idm.query.QueryParameter;

/**
 * Storage utility for {@link Role}
 * @author Anil Saldhana
 * @since October 24, 2013
 */
public class RoleStorageUtil extends AbstractStorageUtil {
    /**
     * Delete {@link Role}
     * @param dataSource
     * @param role
     */
    public void deleteRole(DataSource dataSource, Role role) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            String sql = "delete from Role where id=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, role.getId());
            int result = preparedStatement.executeUpdate();
            if (result == 0) {
                throw new RuntimeException("Delete Role failed");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(preparedStatement);
            safeClose(connection);
        }
    }

    /**
     * Load {@link Role} given its id
     * @param dataSource
     * @param id
     * @return
     */
    public Role loadRole(DataSource dataSource, String id) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select name,partitionID,enabled,createdDate,expirationDate from Role where id =?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Role role = new Role();
                role.setName(resultSet.getString(1));
                role.setId(id);
                role.setPartition(loadPartition(dataSource, resultSet.getString(2)));
                role.setEnabled("y".equalsIgnoreCase(resultSet.getString(3)));
                Timestamp creationDate = resultSet.getTimestamp(4);
                if (creationDate != null) {
                    role.setCreatedDate(new Date(creationDate.getTime()));
                }
                Timestamp expirationDate = resultSet.getTimestamp(5);
                if (expirationDate != null) {
                    role.setExpirationDate(new Date(expirationDate.getTime()));
                }
                // Get attributes also
                AttributeStorageUtil attributeStorageUtil = new AttributeStorageUtil();
                List<Attribute> attributeList = attributeStorageUtil.getAttributes(dataSource, role.getId());
                if (attributeList.isEmpty() == false) {
                    for (Attribute attribute : attributeList) {
                        role.setAttribute(attribute);
                    }
                }
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

    /**
     * Load {@link Role} given parameters
     * @param dataSource
     * @param params
     * @return
     */
    public Role loadRole(DataSource dataSource, Map<QueryParameter, Object[]> params) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        Set<QueryParameter> queryParameters = params.keySet();
        for (QueryParameter queryParameter : queryParameters) {
            if (queryParameter instanceof AttributeParameter) {
                AttributeParameter attributeParameter = (AttributeParameter) queryParameter;
                Object[] paramValues = getValuesFromParamMap(params,attributeParameter);
                String attributeName = attributeParameter.getName();
                if ("name".equals(attributeName)) {
                    String loginNameValue = (String) paramValues[0];
                    return loadRoleByName(dataSource, loginNameValue);
                } else
                    throw new RuntimeException();
            }
        }
        throw new RuntimeException();
    }

    /**
     * Load {@link Role} given its name
     * @param dataSource
     * @param roleName
     * @return
     */
    public Role loadRoleByName(DataSource dataSource, String roleName) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select id,partitionID,enabled,createdDate,expirationDate from Role where name =?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, roleName);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Role role = new Role();
                role.setId(resultSet.getString(1));
                role.setName(roleName);
                role.setPartition(loadPartition(dataSource, resultSet.getString(2)));
                role.setEnabled("y".equalsIgnoreCase(resultSet.getString(3)));
                Timestamp creationDate = resultSet.getTimestamp(4);
                if (creationDate != null) {
                    role.setCreatedDate(new Date(creationDate.getTime()));
                }
                Timestamp expirationDate = resultSet.getTimestamp(5);
                if (expirationDate != null) {
                    role.setExpirationDate(new Date(expirationDate.getTime()));
                }
                // Get attributes also
                AttributeStorageUtil attributeStorageUtil = new AttributeStorageUtil();
                List<Attribute> attributeList = attributeStorageUtil.getAttributes(dataSource, role.getId());
                if (attributeList.isEmpty() == false) {
                    for (Attribute attribute : attributeList) {
                        role.setAttribute(attribute);
                    }
                }
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

    /**
     * Store a {@link Role}
     * @param dataSource
     * @param role
     */
    public void storeRole(DataSource dataSource, Role role) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            String sql = "insert into Role set name=?,id=?," + "createdDate=?,expirationDate=?,partitionID=?," + "enabled=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, role.getName());
            preparedStatement.setString(2, role.getId());
            preparedStatement.setTimestamp(3, new Timestamp(role.getCreatedDate().getTime()));
            if (role.getExpirationDate() != null) {
                preparedStatement.setTimestamp(4, new Timestamp(role.getExpirationDate().getTime()));
            } else {
                preparedStatement.setTimestamp(4, null);
            }
            preparedStatement.setString(5, role.getPartition().getId());
            if (role.isEnabled()) {
                preparedStatement.setString(6, "y");
            }

            int result = preparedStatement.executeUpdate();
            if (result == 0) {
                throw new RuntimeException("Insert into Role failed");
            }

            // Ensure that the Partition is also stored
            PartitionJdbcType pj = new PartitionJdbcType("dummy");
            pj.setDataSource(dataSource);
            pj.persist(role.getPartition());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(preparedStatement);
            safeClose(connection);
        }
    }

    /**
     * Update the stored {@link User}
     * @param dataSource
     * @param role
     */
    public void updateRole(DataSource dataSource, Role role) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        String updateSql = "update Role set name=?,enabled=?," + "createdDate=?,expirationDate=? where id =?";

        if (role.getExpirationDate() == null) {
            updateSql = "update Role set name=?,enabled=?," + "createdDate=? where id =?";
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();

            preparedStatement = connection.prepareStatement(updateSql);
            preparedStatement.setString(1, role.getName());
            if (role.isEnabled()) {
                preparedStatement.setString(2, "y");
            } else {
                preparedStatement.setString(2, "n");
            }
            preparedStatement.setDate(3, new java.sql.Date(role.getCreatedDate().getTime()));

            if (role.getExpirationDate() != null) {
                preparedStatement.setTimestamp(4, new Timestamp(role.getExpirationDate().getTime()));
                preparedStatement.setString(5, role.getId());
            } else {
                preparedStatement.setString(4, role.getId());
            }

            int numberOfRows = preparedStatement.executeUpdate();
            if (numberOfRows == 0) {
                System.out.println("Update Role failed");
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
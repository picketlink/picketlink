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
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.query.AttributeParameter;
import org.picketlink.idm.query.QueryParameter;

/**
 * Storage utility for groups
 *
 * @author Anil Saldhana
 * @since October 24, 2013
 */
public class GroupStorageUtil extends AbstractStorageUtil {
    /**
     * Delete {@link Group}
     *
     * @param dataSource
     * @param user
     */
    public void deleteGroup(DataSource dataSource, Group group) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            String sql = "delete from Groups where id=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, group.getId());
            int result = preparedStatement.executeUpdate();
            if (result == 0) {
                throw new RuntimeException("Delete group failed for name=" + group.getName());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(preparedStatement);
            safeClose(connection);
        }
    }

    /**
     * Load {@link Group} given its id
     *
     * @param dataSource
     * @param id
     * @return
     */
    public Group loadGroup(DataSource dataSource, String id) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select name,partitionID,parentGroup,path,enabled,createdDate,expirationDate from Groups where id =?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Group group = new Group();
                group.setName(resultSet.getString(1));
                group.setId(id);
                group.setPartition(loadPartition(dataSource, resultSet.getString(2)));
                group.setParentGroup(loadGroup(dataSource, resultSet.getString(3)));
                group.setPath(resultSet.getString(4));
                group.setEnabled("y".equalsIgnoreCase(resultSet.getString(5)));
                Timestamp creationDate = resultSet.getTimestamp(6);
                if (creationDate != null) {
                    group.setCreatedDate(new Date(creationDate.getTime()));
                }
                Timestamp expirationDate = resultSet.getTimestamp(7);
                if (expirationDate != null) {
                    group.setExpirationDate(new Date(expirationDate.getTime()));
                }

                // Get attributes also
                AttributeStorageUtil attributeStorageUtil = new AttributeStorageUtil();
                List<Attribute> attributeList = attributeStorageUtil.getAttributes(dataSource, id);
                if (attributeList.isEmpty() == false) {
                    for (Attribute attribute : attributeList) {
                        group.setAttribute(attribute);
                    }
                }
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

    /**
     * Load a {@link Group} given parameters
     *
     * @param dataSource
     * @param params
     * @return
     */
    public Group loadGroup(DataSource dataSource, Map<QueryParameter, Object[]> params) {
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
                    return loadGroupByName(dataSource, loginNameValue);
                } else if ("path".equals(attributeName)) {
                    String loginNameValue = (String) paramValues[0];
                    return loadGroupByPath(dataSource, loginNameValue);
                } else
                    throw new RuntimeException();
            }
        }
        throw new RuntimeException();
    }

    /**
     * Load a {@link Group} given its name
     *
     * @param dataSource
     * @param groupName
     * @return
     */
    public Group loadGroupByName(DataSource dataSource, String groupName) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select id,partitionID,parentGroup,path,enabled,createdDate,expirationDate"
                    + " from Groups where name =?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, groupName);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Group group = new Group();
                group.setId(resultSet.getString(1));
                group.setName(groupName);
                group.setPartition(loadPartition(dataSource, resultSet.getString(2)));
                group.setParentGroup(loadGroup(dataSource, resultSet.getString(3)));
                group.setPath(resultSet.getString(4));
                group.setEnabled("y".equalsIgnoreCase(resultSet.getString(5)));
                Timestamp creationDate = resultSet.getTimestamp(6);
                if (creationDate != null) {
                    group.setCreatedDate(new Date(creationDate.getTime()));
                }
                Timestamp expirationDate = resultSet.getTimestamp(7);
                if (expirationDate != null) {
                    group.setExpirationDate(new Date(expirationDate.getTime()));
                }
                // Get attributes also
                AttributeStorageUtil attributeStorageUtil = new AttributeStorageUtil();
                List<Attribute> attributeList = attributeStorageUtil.getAttributes(dataSource, group.getId());
                if (attributeList.isEmpty() == false) {
                    for (Attribute attribute : attributeList) {
                        group.setAttribute(attribute);
                    }
                }
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

    /**
     * Load a {@link Group} given its path
     *
     * @param dataSource
     * @param path
     * @return
     */
    public Group loadGroupByPath(DataSource dataSource, String path) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select id,name,partitionID,parentGroup,enabled,createdDate,expirationDate"
                    + " from Groups where path =?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, path);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Group group = new Group();
                group.setId(resultSet.getString(1));
                group.setName(resultSet.getString(2));
                group.setPartition(loadPartition(dataSource, resultSet.getString(3)));
                group.setParentGroup(loadGroup(dataSource, resultSet.getString(4)));
                group.setPath(path);
                group.setEnabled("y".equalsIgnoreCase(resultSet.getString(5)));
                Timestamp creationDate = resultSet.getTimestamp(6);
                if (creationDate != null) {
                    group.setCreatedDate(new Date(creationDate.getTime()));
                }
                Timestamp expirationDate = resultSet.getTimestamp(7);
                if (expirationDate != null) {
                    group.setExpirationDate(new Date(expirationDate.getTime()));
                }

                // Get attributes also
                AttributeStorageUtil attributeStorageUtil = new AttributeStorageUtil();
                List<Attribute> attributeList = attributeStorageUtil.getAttributes(dataSource, group.getId());
                if (attributeList.isEmpty() == false) {
                    for (Attribute attribute : attributeList) {
                        group.setAttribute(attribute);
                    }
                }
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

    /**
     * Store a {@link Group}
     *
     * @param dataSource
     * @param group
     */
    public void storeGroup(DataSource dataSource, Group group) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            String sql = "insert into Groups set name=?,id=?,"
                    + "createdDate=?,expirationDate=?,partitionID=?,parentGroup=?,path=?,enabled=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, group.getName());
            preparedStatement.setString(2, group.getId());
            preparedStatement.setTimestamp(3, new Timestamp(group.getCreatedDate().getTime()));
            if (group.getExpirationDate() != null) {
                preparedStatement.setTimestamp(4, new Timestamp(group.getExpirationDate().getTime()));
            } else {
                preparedStatement.setTimestamp(4, null);
            }
            preparedStatement.setString(5, group.getPartition().getId());
            if (group.getParentGroup() == null) {
                preparedStatement.setString(6, null);
            } else {
                preparedStatement.setString(6, group.getParentGroup().getId());
            }
            preparedStatement.setString(7, group.getPath());
            if (group.isEnabled()) {
                preparedStatement.setString(8, "y");
            } else {
                preparedStatement.setString(8, "n");
            }
            int result = preparedStatement.executeUpdate();
            if (result == 0) {
                throw new RuntimeException("Insert into Group failed");
            }

            // Ensure that the Partition is also stored
            PartitionJdbcType pj = new PartitionJdbcType("dummy");
            pj.setDataSource(dataSource);
            pj.persist(group.getPartition());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(preparedStatement);
            safeClose(connection);
        }
    }

    /**
     * Update the stored {@link org.picketlink.idm.model.basic.Group}
     *
     * @param dataSource
     * @param user
     */
    public void updateGroup(DataSource dataSource, Group group) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        String updateSql = "update Groups set name=?,parentGroup=?,partitionID=?,enabled=?,"
                + "createdDate=?,expirationDate=? where id =?";

        if (group.getExpirationDate() == null) {
            updateSql = "update Groups set name=?,parentGroup=?,partitionID=?,enabled=?,createdDate=? where id =?";
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();

            preparedStatement = connection.prepareStatement(updateSql);
            preparedStatement.setString(1, group.getName());
            if (group.getParentGroup() == null) {
                preparedStatement.setString(2, null);
            } else {
                preparedStatement.setString(2, group.getParentGroup().getId());
            }
            if (group.getPartition() == null) {
                preparedStatement.setString(3, null);
            } else {
                preparedStatement.setString(3, group.getPartition().getId());
            }
            if (group.isEnabled()) {
                preparedStatement.setString(4, "y");
            } else {
                preparedStatement.setString(4, "n");
            }
            preparedStatement.setTimestamp(5, new Timestamp(group.getCreatedDate().getTime()));

            if (group.getExpirationDate() != null) {
                preparedStatement.setTimestamp(6, new Timestamp(group.getExpirationDate().getTime()));
                preparedStatement.setString(7, group.getId());
            } else {
                preparedStatement.setString(6, group.getId());
            }

            int numberOfRows = preparedStatement.executeUpdate();
            if (numberOfRows == 0) {
                System.out.println("Update Group failed");
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
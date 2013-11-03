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
import org.picketlink.idm.model.basic.Agent;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.AttributeParameter;
import org.picketlink.idm.query.QueryParameter;

/**
 * Storage utility for {@link User}
 *
 * @author Anil Saldhana
 * @since October 24, 2013
 */
public class UserStorageUtil extends AbstractStorageUtil {
    /**
     * Count the number of {@link User} with an id
     *
     * @param dataSource
     * @param id
     * @return
     */
    public int countUsers(DataSource dataSource, String id) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select count(*) from User where id =?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(resultSet);
            safeClose(preparedStatement);
            safeClose(connection);
        }
        return 0;
    }

    /**
     * Delete {@link Agent}
     *
     * @param dataSource
     * @param user
     */
    public void deleteAgent(DataSource dataSource, Agent agent) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            String sql = "delete from User where id=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, agent.getId());
            int result = preparedStatement.executeUpdate();
            if (result == 0) {
                throw new RuntimeException("Delete Agent failed");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(preparedStatement);
            safeClose(connection);
        }
    }

    /**
     * Delete {@link User}
     *
     * @param dataSource
     * @param user
     */
    public void deleteUser(DataSource dataSource, User user) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            String sql = "delete from User where id=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user.getId());
            int result = preparedStatement.executeUpdate();
            if (result == 0) {
                throw new RuntimeException("Delete User failed");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(preparedStatement);
            safeClose(connection);
        }
    }

    /**
     * Load an {@link User} given various parameters
     *
     * @param dataSource
     * @param params
     * @return
     */
    public User loadUser(DataSource dataSource, Map<QueryParameter, Object[]> params) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        Set<QueryParameter> queryParameters = params.keySet();
        for (QueryParameter queryParameter : queryParameters) {
            if (queryParameter instanceof AttributeParameter) {
                AttributeParameter attributeParameter = (AttributeParameter) queryParameter;
                Object[] paramValues = getValuesFromParamMap(params,attributeParameter);
                String attributeName = attributeParameter.getName();
                if ("loginName".equals(attributeName)) {
                    String loginNameValue = (String) paramValues[0];
                    return loadUserByLoginName(dataSource, loginNameValue);
                } else
                    throw new RuntimeException();
            }
        }
        throw new RuntimeException();
    }

    /**
     * Load {@link User} given its id
     *
     * @param dataSource
     * @param id
     * @return
     */
    public User loadUser(DataSource dataSource, String id) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select firstName,lastName,email,loginName,partitionID,enabled,"
                    + "createdDate,expirationDate from User where id =?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                User user = new User();
                user.setFirstName(resultSet.getString(1));
                user.setLastName(resultSet.getString(2));
                user.setEmail(resultSet.getString(3));
                user.setLoginName(resultSet.getString(4));
                user.setPartition(loadPartition(dataSource, resultSet.getString(5)));
                user.setEnabled("y".equalsIgnoreCase(resultSet.getString(6)));

                Timestamp creationDate = resultSet.getTimestamp(7);
                if (creationDate != null) {
                    user.setCreatedDate(new Date(creationDate.getTime()));
                }
                Timestamp expirationDate = resultSet.getTimestamp(8);
                if (expirationDate != null) {
                    user.setExpirationDate(new Date(expirationDate.getTime()));
                }
                user.setId(id);

                // Get attributes also
                AttributeStorageUtil attributeStorageUtil = new AttributeStorageUtil();
                List<Attribute> attributeList = attributeStorageUtil.getAttributes(dataSource, id);
                if (attributeList.isEmpty() == false) {
                    for (Attribute attribute : attributeList) {
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

    /**
     * Load {@link User} given the login name
     *
     * @param dataSource
     * @param loginName
     * @return
     */
    public User loadUserByLoginName(DataSource dataSource, String loginName) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select id, firstName,lastName,email,partitionID,enabled,createdDate,expirationDate"
                    + " from User where loginName =?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, loginName);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                User user = new User();
                user.setId(resultSet.getString(1));
                user.setFirstName(resultSet.getString(2));
                user.setLastName(resultSet.getString(3));
                user.setEmail(resultSet.getString(4));
                user.setLoginName(loginName);
                user.setPartition(loadPartition(dataSource, resultSet.getString(5)));
                user.setEnabled("y".equalsIgnoreCase(resultSet.getString(6)));
                Timestamp creationDate = resultSet.getTimestamp(7);
                if (creationDate != null) {
                    user.setCreatedDate(new Date(creationDate.getTime()));
                }
                Timestamp expirationDate = resultSet.getTimestamp(8);
                if (expirationDate != null) {
                    user.setExpirationDate(new Date(expirationDate.getTime()));
                }

                // Get attributes also
                AttributeStorageUtil attributeStorageUtil = new AttributeStorageUtil();
                List<Attribute> attributeList = attributeStorageUtil.getAttributes(dataSource, user.getId());
                if (attributeList.isEmpty() == false) {
                    for (Attribute attribute : attributeList) {
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

    /**
     * Store a new {@link Agent}
     *
     * @param dataSource
     * @param user
     */
    public void storeAgent(DataSource dataSource, Agent agent) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        String storeSql = "insert into user set loginName=?,id=?," + "createdDate=?,partitionID=?,enabled=?,expirationDate=?";
        if (agent.getExpirationDate() == null) {
            storeSql = "insert into user set loginName=?,id=?," + "createdDate=?,partitionID=?,enabled=?";
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(storeSql);
            preparedStatement.setString(1, agent.getLoginName());
            preparedStatement.setString(2, agent.getId());

            preparedStatement.setTimestamp(3, new Timestamp(agent.getCreatedDate().getTime()));
            preparedStatement.setString(4, agent.getPartition().getId());
            String enabledStr = "n";
            if (agent.isEnabled()) {
                enabledStr = "y";
            }
            preparedStatement.setString(5, enabledStr);
            if (agent.getExpirationDate() != null) {
                preparedStatement.setTimestamp(6, new Timestamp(agent.getExpirationDate().getTime()));
            }

            int result = preparedStatement.executeUpdate();
            if (result == 0) {
                throw new RuntimeException("Insert agent failed");
            }

            // Ensure that the Partition is also stored
            PartitionJdbcType pj = new PartitionJdbcType("dummy");
            pj.setDataSource(dataSource);
            pj.persist(agent.getPartition());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(preparedStatement);
            safeClose(connection);
        }
    }

    /**
     * Store a new {@link User}
     *
     * @param dataSource
     * @param user
     */
    public void storeUser(DataSource dataSource, User user) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        String storeSql = "insert into user set firstName=?,lastName=?,email=?,loginName=?,id=?,"
                + "createdDate=?,partitionID=?,enabled=?,expirationDate=?";
        if (user.getExpirationDate() == null) {
            storeSql = "insert into user set firstName=?,lastName=?,email=?,loginName=?,id=?,"
                    + "createdDate=?,partitionID=?,enabled=?";
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(storeSql);
            preparedStatement.setString(1, user.getFirstName());
            preparedStatement.setString(2, user.getLastName());
            preparedStatement.setString(3, user.getEmail());
            preparedStatement.setString(4, user.getLoginName());
            preparedStatement.setString(5, user.getId());

            preparedStatement.setTimestamp(6, new Timestamp(user.getCreatedDate().getTime()));
            preparedStatement.setString(7, user.getPartition().getId());
            String enabledStr = "n";
            if (user.isEnabled()) {
                enabledStr = "y";
            }
            preparedStatement.setString(8, enabledStr);
            if (user.getExpirationDate() != null) {
                preparedStatement.setTimestamp(9, new Timestamp(user.getExpirationDate().getTime()));
            }

            int result = preparedStatement.executeUpdate();
            if (result == 0) {
                throw new RuntimeException("Insert into user failed");
            }

            // Ensure that the Partition is also stored
            PartitionJdbcType pj = new PartitionJdbcType("dummy");
            pj.setDataSource(dataSource);
            pj.persist(user.getPartition());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(preparedStatement);
            safeClose(connection);
        }
    }

    /**
     * Update the stored {@link Agent}
     *
     * @param dataSource
     * @param user
     */
    public void updateAgent(DataSource dataSource, Agent agent) {
        String updateSql = "update User set loginName=?,enabled=?," + "createdDate=?,expirationDate=? where id =?";

        if (agent.getExpirationDate() == null) {
            updateSql = "update User set loginName=?,enabled=?," + "createdDate=? where id =?";
        }
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();

            preparedStatement = connection.prepareStatement(updateSql);
            preparedStatement.setString(1, agent.getLoginName());
            if (agent.isEnabled()) {
                preparedStatement.setString(2, "y");
            } else {
                preparedStatement.setString(2, "n");
            }
            preparedStatement.setTimestamp(3, new Timestamp(agent.getCreatedDate().getTime()));

            if (agent.getExpirationDate() != null) {
                preparedStatement.setTimestamp(4, new Timestamp(agent.getExpirationDate().getTime()));
                preparedStatement.setString(5, agent.getId());
            } else {
                preparedStatement.setString(4, agent.getId());
            }

            int numberOfRows = preparedStatement.executeUpdate();
            if (numberOfRows == 0) {
                System.out.println("Update Agent failed");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(resultSet);
            safeClose(preparedStatement);
            safeClose(connection);
        }
    }

    /**
     * Update the stored {@link User}
     *
     * @param dataSource
     * @param user
     */
    public void updateUser(DataSource dataSource, User user) {
        String updateSql = "update User set firstName=?,lastName=?,email=?,loginName=?,enabled=?,"
                + "createdDate=?,expirationDate=? where id =?";

        if (user.getExpirationDate() == null) {
            updateSql = "update User set firstName=?,lastName=?,email=?,loginName=?,enabled=?," + "createdDate=? where id =?";
        }
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();

            preparedStatement = connection.prepareStatement(updateSql);
            preparedStatement.setString(1, user.getFirstName());
            preparedStatement.setString(2, user.getLastName());
            preparedStatement.setString(3, user.getEmail());
            preparedStatement.setString(4, user.getLoginName());
            if (user.isEnabled()) {
                preparedStatement.setString(5, "y");
            } else {
                preparedStatement.setString(5, "n");
            }
            preparedStatement.setTimestamp(6, new Timestamp(user.getCreatedDate().getTime()));

            if (user.getExpirationDate() != null) {
                preparedStatement.setTimestamp(7, new Timestamp(user.getExpirationDate().getTime()));
                preparedStatement.setString(8, user.getId());
            } else {
                preparedStatement.setString(7, user.getId());
            }

            int numberOfRows = preparedStatement.executeUpdate();
            if (numberOfRows == 0) {
                System.out.println("Update user failed");
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
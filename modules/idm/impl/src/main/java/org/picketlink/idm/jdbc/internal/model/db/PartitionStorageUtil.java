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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.sql.DataSource;

import org.picketlink.idm.IDMMessages;
import org.picketlink.idm.jdbc.internal.model.PartitionJdbcType;
import org.picketlink.idm.model.Partition;

/**
 * Storage utility for {@link Partition}
 *
 * @author Anil Saldhana
 * @since October 24, 2013
 */
public class PartitionStorageUtil extends AbstractStorageUtil {
    /**
     * Load a {@link Partition} given its id
     *
     * @param dataSource
     * @param id
     * @return
     */
    public Partition loadPartitionById(DataSource dataSource, String id) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select name,typeName,configurationName from Partition where id =?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                PartitionJdbcType partition = new PartitionJdbcType(resultSet.getString(1));
                partition.setId(id);
                partition.setTypeName(resultSet.getString(2));
                partition.setConfigurationName(resultSet.getString(3));
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

    /**
     * Load a {@link Partition} given its name
     *
     * @param dataSource
     * @param name
     * @return
     */
    public Partition loadPartitionByName(DataSource dataSource, String name) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select id,typeName,configurationName from Partition where name =?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, name);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                PartitionJdbcType partitionJdbcType = new PartitionJdbcType(name);
                partitionJdbcType.setId(resultSet.getString(1));
                partitionJdbcType.setTypeName(resultSet.getString(2));
                partitionJdbcType.setConfigurationName(resultSet.getString(3));
                return partitionJdbcType;
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
     * Store a {@link Partition}
     *
     * @param dataSource
     * @param partition
     */
    public void storePartition(DataSource dataSource, PartitionJdbcType partition) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        Date now = new Date();
        Calendar calendar = new GregorianCalendar(500 + 1900, 12, 12);
        Date expiration = calendar.getTime();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            String sql = "insert into Partition set name=?,id=?,typeName=?,configurationName=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, partition.getName());
            preparedStatement.setString(2, partition.getId());
            preparedStatement.setString(3, partition.getTypeName());
            preparedStatement.setString(4, partition.getConfigurationName());
            int result = preparedStatement.executeUpdate();
            if (result == 0) {
                throw new RuntimeException("Insert into partition failed");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(preparedStatement);
            safeClose(connection);
        }
    }
}
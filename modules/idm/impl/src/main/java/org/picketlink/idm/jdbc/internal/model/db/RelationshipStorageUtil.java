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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.sql.DataSource;

import org.picketlink.idm.IDMMessages;
import org.picketlink.idm.model.basic.Agent;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.GroupMembership;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;

/**
 * Storage utility for {@link Relationship}
 *
 * @author Anil Saldhana
 * @since October 25, 2013
 */
public class RelationshipStorageUtil extends AbstractStorageUtil {
    /**
     * Delete a {@link Grant}
     *
     * @param dataSource
     * @param id
     */
    public void deleteGrant(DataSource dataSource, String id) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        if (id == null) {
            throw IDMMessages.MESSAGES.nullArgument("id");
        }
        Calendar calendar = new GregorianCalendar(500 + 1900, 12, 12);
        Date expiration = calendar.getTime();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            String sql = "delete from Relationship where id=? and type=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, Grant.class.getName());
            int result = preparedStatement.executeUpdate();
            if (result == 0) {
                throw new RuntimeException("Delete Grant failed");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(preparedStatement);
            safeClose(connection);
        }
    }

    /**
     * Delete a {@link GroupMembership}
     *
     * @param dataSource
     * @param id
     */
    public void deleteGroupMembership(DataSource dataSource, String id) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        if (id == null) {
            throw IDMMessages.MESSAGES.nullArgument("id");
        }

        Calendar calendar = new GregorianCalendar(500 + 1900, 12, 12);
        Date expiration = calendar.getTime();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            String sql = "delete from Relationship where id=? and type=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, GroupMembership.class.getName());
            int result = preparedStatement.executeUpdate();
            if (result == 0) {
                throw new RuntimeException("Delete Group Membership failed");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(preparedStatement);
            safeClose(connection);
        }
    }

    /**
     * Load {@link Grant} given its id
     *
     * @param dataSource
     * @param id
     * @return
     */
    public Grant loadGrant(DataSource dataSource, String id) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        if (id == null) {
            throw IDMMessages.MESSAGES.nullArgument("id");
        }
        UserStorageUtil userStorageUtil = new UserStorageUtil();
        RoleStorageUtil roleStorageUtil = new RoleStorageUtil();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select relBegin,relEnd from Relationship where id =? " + "and type=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, Grant.class.getName());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Grant grant = new Grant();
                grant.setId(id);
                grant.setAssignee(userStorageUtil.loadUser(dataSource, resultSet.getString(1)));
                String roleId = resultSet.getString(2);
                grant.setRole(roleStorageUtil.loadRole(dataSource, roleId));
                return grant;
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
     * Load {@link GroupMembership} given its id
     *
     * @param dataSource
     * @param id
     * @return
     */
    public GroupMembership loadGroupMembership(DataSource dataSource, String id) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        if (id == null) {
            throw IDMMessages.MESSAGES.nullArgument("id");
        }
        UserStorageUtil userStorageUtil = new UserStorageUtil();
        GroupStorageUtil groupStorageUtil = new GroupStorageUtil();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select relBegin,relEnd from Relationship where id =?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                GroupMembership groupMembership = new GroupMembership();
                groupMembership.setId(id);
                groupMembership.setMember(userStorageUtil.loadUser(dataSource, resultSet.getString(1)));

                String groupID = resultSet.getString(2);
                groupMembership.setGroup(groupStorageUtil.loadGroup(dataSource, groupID));

                return groupMembership;
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
     * Load {@link Grant} for an {@link Agent}
     * @param dataSource
     * @param user
     * @return
     */
    public List<Grant> loadGrantsForAgent(DataSource dataSource, Agent agent) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        List<Grant> grants = new ArrayList<Grant>();

        UserStorageUtil userStorageUtil = new UserStorageUtil();
        RoleStorageUtil roleStorageUtil = new RoleStorageUtil();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select id,relEnd from Relationship where relBegin =? " + "and type=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, agent.getId());
            preparedStatement.setString(2, Grant.class.getName());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Grant grant = new Grant();
                grant.setId(resultSet.getString(1));
                grant.setAssignee(userStorageUtil.loadUser(dataSource, agent.getId()));
                String roleId = resultSet.getString(2);
                grant.setRole(roleStorageUtil.loadRole(dataSource, roleId));
                grants.add(grant);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(resultSet);
            safeClose(preparedStatement);
            safeClose(connection);
        }
        return grants;
    }

    /**
     * Load {@link Grant} for an {@link User}
     *
     * @param dataSource
     * @param user
     * @return
     */
    public List<Grant> loadGrantsForUser(DataSource dataSource, User user) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        List<Grant> grants = new ArrayList<Grant>();

        UserStorageUtil userStorageUtil = new UserStorageUtil();
        RoleStorageUtil roleStorageUtil = new RoleStorageUtil();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select id,relEnd from Relationship where relBegin =? " + "and type=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user.getId());
            preparedStatement.setString(2, Grant.class.getName());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Grant grant = new Grant();
                grant.setId(resultSet.getString(1));
                grant.setAssignee(userStorageUtil.loadUser(dataSource, user.getId()));
                String roleId = resultSet.getString(2);
                grant.setRole(roleStorageUtil.loadRole(dataSource, roleId));
                grants.add(grant);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(resultSet);
            safeClose(preparedStatement);
            safeClose(connection);
        }
        return grants;
    }

    /**
     * Load {@link GroupMembership} for an {@link org.picketlink.idm.model.basic.Agent}
     *
     * @param dataSource
     * @param user
     * @return
     */
    public List<GroupMembership> loadGroupMembershipsForAgent(DataSource dataSource, Agent agent) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        List<GroupMembership> groupMemberships = new ArrayList<GroupMembership>();

        UserStorageUtil userStorageUtil = new UserStorageUtil();
        RoleStorageUtil roleStorageUtil = new RoleStorageUtil();
        GroupStorageUtil groupStorageUtil = new GroupStorageUtil();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select id,relEnd from Relationship where relBegin =? " + "and type=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, agent.getId());
            preparedStatement.setString(2, GroupMembership.class.getName());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                GroupMembership groupMembership = new GroupMembership();
                groupMembership.setId(resultSet.getString(1));

                groupMembership.setMember(userStorageUtil.loadUser(dataSource, agent.getId()));
                groupMembership.setGroup(groupStorageUtil.loadGroup(dataSource, resultSet.getString(2)));

                groupMemberships.add(groupMembership);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(resultSet);
            safeClose(preparedStatement);
            safeClose(connection);
        }
        return groupMemberships;
    }

    /**
     * Load {@link GroupMembership} for an {@link User}
     *
     * @param dataSource
     * @param user
     * @return
     */
    public List<GroupMembership> loadGroupMembershipsForUser(DataSource dataSource, User user) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        List<GroupMembership> groupMemberships = new ArrayList<GroupMembership>();

        UserStorageUtil userStorageUtil = new UserStorageUtil();
        RoleStorageUtil roleStorageUtil = new RoleStorageUtil();
        GroupStorageUtil groupStorageUtil = new GroupStorageUtil();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select id,relEnd from Relationship where relBegin =? " + "and type=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user.getId());
            preparedStatement.setString(2, GroupMembership.class.getName());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                GroupMembership groupMembership = new GroupMembership();
                groupMembership.setId(resultSet.getString(1));

                groupMembership.setMember(userStorageUtil.loadUser(dataSource, user.getId()));
                groupMembership.setGroup(groupStorageUtil.loadGroup(dataSource, resultSet.getString(2)));

                groupMemberships.add(groupMembership);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(resultSet);
            safeClose(preparedStatement);
            safeClose(connection);
        }
        return groupMemberships;
    }

    /**
     * Load {@link Grant} for an {@link Role}
     *
     * @param dataSource
     * @param user
     * @return
     */
    public List<Grant> loadGrantsForRole(DataSource dataSource, Role role) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        List<Grant> grants = new ArrayList<Grant>();

        UserStorageUtil userStorageUtil = new UserStorageUtil();
        RoleStorageUtil roleStorageUtil = new RoleStorageUtil();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select id,relBegin from Relationship where relEnd =? " + "and type=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, role.getId());
            preparedStatement.setString(2, Grant.class.getName());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Grant grant = new Grant();
                grant.setId(resultSet.getString(1));
                grant.setAssignee(userStorageUtil.loadUser(dataSource, resultSet.getString(2)));
                grant.setRole(role);
                grants.add(grant);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(resultSet);
            safeClose(preparedStatement);
            safeClose(connection);
        }
        return grants;
    }

    /**
     * Load {@link Grant} for a {@link Group}
     *
     * @param dataSource
     * @param user
     * @return
     */
    public List<GroupMembership> loadGroupMembershipForGroup(DataSource dataSource, Group group) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        List<GroupMembership> groupMemberships = new ArrayList<GroupMembership>();

        UserStorageUtil userStorageUtil = new UserStorageUtil();
        RoleStorageUtil roleStorageUtil = new RoleStorageUtil();
        GroupStorageUtil groupStorageUtil = new GroupStorageUtil();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String sql = "select id,relBegin from Relationship where relEnd =? " + "and type=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, group.getId());
            preparedStatement.setString(2, GroupMembership.class.getName());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                GroupMembership groupMembership = new GroupMembership();
                groupMembership.setId(resultSet.getString(1));

                groupMembership.setMember(userStorageUtil.loadUser(dataSource, resultSet.getString(2)));
                groupMembership.setGroup(group);

                groupMemberships.add(groupMembership);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(resultSet);
            safeClose(preparedStatement);
            safeClose(connection);
        }
        return groupMemberships;
    }

    /**
     * Store a {@link Grant}
     *
     * @param dataSource
     * @param grant
     */
    public void storeGrant(DataSource dataSource, Grant grant) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        Calendar calendar = new GregorianCalendar(500 + 1900, 12, 12);
        Date expiration = calendar.getTime();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            String sql = "insert into Relationship set id=?,relBegin=?,relEnd=?,type=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, grant.getId());
            preparedStatement.setString(2, grant.getAssignee().getId());
            preparedStatement.setString(3, grant.getRole().getId());
            preparedStatement.setString(4, grant.getClass().getName());
            int result = preparedStatement.executeUpdate();
            if (result == 0) {
                throw new RuntimeException("Insert into Grant failed");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(preparedStatement);
            safeClose(connection);
        }
    }

    /**
     * Store {@link GroupMembership}
     * @param dataSource
     * @param groupMembership
     */
    public void storeGroupMembership(DataSource dataSource, GroupMembership groupMembership) {
        if (dataSource == null) {
            throw IDMMessages.MESSAGES.nullArgument("datasource");
        }
        Calendar calendar = new GregorianCalendar(500 + 1900, 12, 12);
        Date expiration = calendar.getTime();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            String sql = "insert into Relationship set id=?,relBegin=?,relEnd=?,type=?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, groupMembership.getId());
            preparedStatement.setString(2, groupMembership.getMember().getId());
            preparedStatement.setString(3, groupMembership.getGroup().getId());
            preparedStatement.setString(4, groupMembership.getClass().getName());
            int result = preparedStatement.executeUpdate();
            if (result == 0) {
                throw new RuntimeException("Insert into GroupMembership failed");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeClose(preparedStatement);
            safeClose(connection);
        }
    }
}
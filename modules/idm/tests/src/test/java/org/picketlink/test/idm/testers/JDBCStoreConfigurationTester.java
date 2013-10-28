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
package org.picketlink.test.idm.testers;

import org.h2.jdbcx.JdbcDataSource;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;

import java.io.File;
import java.sql.Connection;

/**
 * An {@link IdentityConfigurationTester} for {@link org.picketlink.idm.jdbc.internal.JDBCIdentityStore}
 * @author Anil Saldhana
 * @since September 25, 2013
 */
public class JDBCStoreConfigurationTester implements IdentityConfigurationTester {
    public static final String SIMPLE_JDBC_STORE_CONFIG = "SIMPLE_JDBC_STORE_CONFIG";

    private JdbcDataSource ds = null;

    @Override
    public DefaultPartitionManager getPartitionManager() {

        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder.named(SIMPLE_JDBC_STORE_CONFIG).stores().jdbc().setDataSource(ds).supportType(User.class).supportType(Role.class)
                .supportAllFeatures();

        DefaultPartitionManager partitionManager = new DefaultPartitionManager(builder.buildAll());

        if (partitionManager.getPartition(Realm.class, Realm.DEFAULT_REALM) == null) {
            partitionManager.add(new Realm(Realm.DEFAULT_REALM));
        }

        return partitionManager;
    }

    private void setupDB(JdbcDataSource ds) throws Exception{
        Connection connection = ds.getConnection();

        //User
        connection.createStatement().executeUpdate("drop table if exists User");
        connection.createStatement().executeUpdate("create table User(id varchar,firstName varchar,lastName varchar," +
                "email varchar,loginName varchar,enabled varchar,createdDate timestamp,expirationDate timestamp,partitionID varchar)");

        //Role
        connection.createStatement().executeUpdate("drop table if exists Role");
        connection.createStatement().executeUpdate("create table Role(id varchar,name varchar," +
                "enabled varchar,createdDate timestamp,expirationDate timestamp,partitionID varchar)");
        //Group
        connection.createStatement().executeUpdate("drop table if exists Groups");
        connection.createStatement().executeUpdate("create table Groups(id varchar,name varchar," +
                "enabled varchar,createdDate timestamp,expirationDate timestamp,parentGroup varchar," +
                "path varchar,partitionID varchar)");

        //Partition
        connection.createStatement().executeUpdate("drop table if exists Partition");
        connection.createStatement().executeUpdate("create table Partition(id varchar,name varchar," +
                "typeName varchar,configurationName varchar)");

        //Attribute
        connection.createStatement().executeUpdate("drop table if exists Attributes");
        connection.createStatement().executeUpdate("create table Attributes(owner varchar,name varchar," +
                "value varchar,attributeType varchar)");

        //Relationship
        connection.createStatement().executeUpdate("drop table if exists Relationship");
        connection.createStatement().executeUpdate("create table Relationship(id varchar,relBegin varchar," +
                "relEnd varchar,type varchar,enabled varchar)");
    }

    @Override
    public void beforeTest() {
        // Construct DataSource
        ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:file:~/picketlink-idm-jdbc");
        ds.setUser("sa");
        ds.setPassword("");
        //Create Tables
        try {
            setupDB(ds);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterTest() {
        ds = null;
        File dbFile = new File(System.getProperty("user.home") + "/" + "picketlink-idm-jdbc.h2.db");
        dbFile.delete();
        File traceFile = new File(System.getProperty("user.home") + "/" + "picketlink-idm-jdbc.trace.db");
        traceFile.delete();
    }
}
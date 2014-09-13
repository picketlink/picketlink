/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketlink.identity.federation.core.sts.registry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Implementation of {@link org.picketlink.identity.federation.core.sts.registry.SecurityTokenRegistry} using JDBC
 *
 * This is a working implementation implementation based on the plink JDBCRevocationRegistry which allows better jndi configuration
 * (eg global datasources under JBAS 7+ -- https://docs.jboss.org/author/display/AS71/JNDI+Reference) and serialization to blob
 * fields for Oracle (and other DBs) Initial implementation used a varchar for the serialized class.
 *
 * This implementation uses a new set of tables..
 *
 * Oracle create Script
 *
 * <pre>
 * ALTER TABLE STS_REVOCATION_REGISTRY DROP PRIMARY KEY CASCADE;
 *
 * DROP TABLE STS_REVOCATION_REGISTRY CASCADE CONSTRAINTS;
 *
 * CREATE TABLE STS_REVOCATION_REGISTRY (
 *  TOKEN_ID    VARCHAR2(1024)  NOT NULL,
 *   TOKEN_TYPE    VARCHAR2(2048)                  NOT NULL,
 *   CREATED_DATE  TIMESTAMP(6) WITH TIME ZONE     NOT NULL
 * );
 *
 *
 * CREATE UNIQUE INDEX STS_REVOCATION_REGISTRY_PK ON STS_REVOCATION_REGISTRY (TOKEN_ID);
 *
 *
 * ALTER TABLE STS_REVOCATION_REGISTRY ADD (
 *   CONSTRAINT STS_REVOCATION_REGISTRY_PK PRIMARY KEY (TOKEN_ID) USING INDEX STS_REVOCATION_REGISTRY_PK
 *  );
 * </pre>
 *
 * Picketlink.xml condfiguration:
 *
 * <pre>
 * &lt;TokenProvider ... &gt;
 *  ...
 *  &lt;Property Key="RevocationRegistry" Value="OJDBC" /&gt;
 *  &lt;Property Key="RevocationRegistryJDBCNameSpace" Value="java:jboss" /&gt; &lt;!-- default value is java:jboss and can be
 * ommited --&gt;
 *  &lt;Property Key="RevocationRegistryJDBCDataSource" Value="jdbc/picketlink-sts" /&gt; &lt;!-- default value is
 * jdbc/picketlink-sts and can be ommited --&gt;
 *  ...
 *  &lt;/TokenProvider&gt;
 * </pre>
 *
 * @author Alexandros Papadakis
 * @author Anil Saldhana
 * @since September 11, 2014
 */
public class OJDBCRevocationRegistry extends AbstractJDBCRegistry implements RevocationRegistry {

    private static final String EXISTS_TABLE_SQL = "SELECT COUNT(*) FROM STS_REVOCATION_REGISTRY WHERE TOKEN_ID = ? AND TOKEN_TYPE = ?";
    private static final String INSERT_TABLE_SQL = "INSERT INTO STS_REVOCATION_REGISTRY (TOKEN_ID, TOKEN_TYPE, CREATED_DATE) VALUES (?,?,?)";

    public OJDBCRevocationRegistry() {
        super("java:jboss", "jdbc/picketlink-sts");
    }

    public OJDBCRevocationRegistry(String initial, String jndiName) {
        super(initial, jndiName);
    }

    public OJDBCRevocationRegistry(String jndiName) {
        super("java:jboss", jndiName);
    }

    /**
     * @see RevocationRegistry#isRevoked(String, String)
     */
    public boolean isRevoked(String tokenType, String tokenID) {
        if (dataSource == null) {
            throw logger.datasourceIsNull();
        }

        Connection conn = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            conn = dataSource.getConnection();

            preparedStatement = conn.prepareStatement(EXISTS_TABLE_SQL);
            preparedStatement.setString(1, tokenID);
            preparedStatement.setString(2, tokenType);
            resultSet = preparedStatement.executeQuery();

            return resultSet.getInt(1) > 0;
        } catch (SQLException e) {
            throw logger.runtimeException("revokeToken", e);
        } finally {
            safeClose(resultSet);
            safeClose(preparedStatement);
            safeClose(conn);
        }
    }

    /**
     * @see RevocationRegistry#revokeToken(String, String)
     */
    public void revokeToken(String tokenType, String tokenID) {
        if (dataSource == null) {
            throw logger.datasourceIsNull();
        }

        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try {
            conn = dataSource.getConnection();
            Date tokenCreationDate = Calendar.getInstance().getTime();

            preparedStatement = conn.prepareStatement(INSERT_TABLE_SQL);
            preparedStatement.setString(1, tokenID);
            preparedStatement.setString(2, tokenType);
            preparedStatement
                .setTimestamp(3, new Timestamp(tokenCreationDate.getTime()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw logger.runtimeException("revokeToken", e);
        } finally {
            safeClose(preparedStatement);
            safeClose(conn);
        }
    }
}
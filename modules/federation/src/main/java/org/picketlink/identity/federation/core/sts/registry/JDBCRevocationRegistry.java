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
 * Oracle create Script
 *
 * <pre>
 * ALTER TABLE STS_REVOCATION_REGISTRY DROP PRIMARY KEY CASCADE;
 *
 * DROP TABLE STS_REVOCATION_REGISTRY CASCADE CONSTRAINTS;
 *
 * CREATE TABLE STS_REVOCATION_REGISTRY (
 *   TOKEN_ID      VARCHAR2(1024)             NOT NULL,
 *   TOKEN_TYPE    VARCHAR2(2048)             NOT NULL,
 *   CREATED_DATE  TIMESTAMP(6) WITH TIME ZONE     NOT NULL
 * );
 *
 *
 * CREATE UNIQUE INDEX STS_REVOCATION_REGISTRY_PK ON STS_REVOCATION_REGISTRY (TOKEN_ID);
 *
 * ALTER TABLE STS_REVOCATION_REGISTRY ADD (
 *   CONSTRAINT STS_REVOCATION_REGISTRY_PK PRIMARY KEY (TOKEN_ID) USING INDEX STS_REVOCATION_REGISTRY_PK
 *  );
 * </pre>
 * @author Anil Saldhana
 * @since August 06, 2013
 */
public class JDBCRevocationRegistry extends AbstractJDBCRegistry implements RevocationRegistry {

    private static final String EXISTS_TABLE_SQL = "SELECT COUNT(*) FROM STS_REVOCATION_REGISTRY WHERE TOKEN_ID = ? AND TOKEN_TYPE = ?";
    private static final String INSERT_TABLE_SQL = "INSERT INTO STS_REVOCATION_REGISTRY (TOKEN_ID, TOKEN_TYPE, CREATED_DATE) VALUES (?,?,?)";

    public JDBCRevocationRegistry() {
        super("jdbc/picketlink-sts");
    }

    public JDBCRevocationRegistry(String jndiName) {
        super(jndiName);
    }

    /**
     * @see RevocationRegistry#isRevoked(String, String)
     */
    public boolean isRevoked(String tokenType, String tokenID) {
        if (getDataSource() == null) {
            throw logger.datasourceIsNull();
        }

        Connection conn = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            conn = getDataSource().getConnection();

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
        if (this.getDataSource() == null) {
            throw logger.datasourceIsNull();
        }

        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try {
            conn = this.getDataSource().getConnection();
            Date tokenCreationDate = Calendar.getInstance().getTime();

            preparedStatement = conn.prepareStatement(INSERT_TABLE_SQL);
            preparedStatement.setString(1, tokenID);
            preparedStatement.setString(2, tokenType);
            preparedStatement.setTimestamp(3, new Timestamp(tokenCreationDate.getTime()),
                    Calendar.getInstance(TimeZone.getTimeZone("UTC")));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw logger.runtimeException("revokeToken", e);
        } finally {
            safeClose(preparedStatement);
            safeClose(conn);
        }
    }
}
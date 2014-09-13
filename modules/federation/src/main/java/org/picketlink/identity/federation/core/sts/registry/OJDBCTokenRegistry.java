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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Implementation of {@link SecurityTokenRegistry} using JDBC
 *
 * This is a working implementation implementation based on the plink JDBCTokenRegistry which allows better jndi configuration (eg
 * global datasources under JBAS 7+ -- https://docs.jboss.org/author/display/AS71/JNDI+Reference) and serialization to blob fields
 * for Oracle (and other DBs) Initial implementation used a varchar for the serialized class.
 *
 * This implementation uses a new set of tables..
 *
 * Oracle Create Table
 *
 * <pre>
 * ALTER TABLE STS_TOKEN_REGISTRY  DROP PRIMARY KEY CASCADE;
 *
 * DROP TABLE STS_TOKEN_REGISTRY CASCADE CONSTRAINTS;
 *
 * CREATE TABLE STS_TOKEN_REGISTRY(
 *  TOKEN_ID    VARCHAR2(1024)  NOT NULL,
 *   CREATED_DATE  TIMESTAMP(6) WITH TIME ZONE     NOT NULL,
 *   TOKEN         BLOB                            NOT NULL
 * );
 *
 *
 * CREATE UNIQUE INDEX STS_TOKEN_REGISTRY_PK ON STS_TOKEN_REGISTRY (TOKEN_ID);
 *
 *
 * ALTER TABLE STS_TOKEN_REGISTRY ADD (
 *   CONSTRAINT STS_TOKEN_REGISTRY_PK PRIMARY KEY (TOKEN_ID)  USING INDEX STS_TOKEN_REGISTRY_PK
 * );
 * </pre>
 *
 * Picketlink.xml condfiguration:
 *
 * <pre>
 * &lt;TokenProvider ... &gt;
 *  ...
 *  &lt;Property Key="TokenRegistry" Value="OJDBC"/&gt;
 *  &lt;Property Key="TokenRegistryJDBCNameSpace" Value="java:jboss" /&gt; &lt;!-- default value is java:jboss and can be ommited
 * --&gt;
 *  &lt;Property Key="TokenRegistryJDBCDataSource" Value="jdbc/picketlink-sts" /&gt; &lt;!-- default value is jdbc/picketlink-sts
 * and can be ommited --&gt;
 *  ...
 * &lt;/TokenProvider&gt;
 * </pre>
 *
 * @author Alexandros Papadakis
 * @author Anil Saldhana
 * @since September 11, 2014
 */
public class OJDBCTokenRegistry extends AbstractJDBCRegistry implements SecurityTokenRegistry {

    private static final String INSERT_SQL = "INSERT INTO STS_TOKEN_REGISTRY (TOKEN_ID, TOKEN, CREATED_DATE) VALUES (?,?,?)";
    private static final String DELETE_SQL = "DELETE FROM STS_TOKEN_REGISTRY WHERE TOKEN_ID = ?";
    private static final String SELECT_SQL = "SELECT TOKEN FROM STS_TOKEN_REGISTRY WHERE TOKEN_ID = ?";

    public OJDBCTokenRegistry() {
        super("jdbc/picketlink-sts");
    }

    public OJDBCTokenRegistry(String jndiName) {
        super(jndiName);
    }

    public OJDBCTokenRegistry(String initial, String jndiName) {
        super(initial, jndiName);
    }

    /**
     * @see SecurityTokenRegistry#addToken(String, Object)
     */
    @Override
    public void addToken(String tokenID, Object token) throws IOException {
        if (dataSource == null) {
            throw logger.datasourceIsNull();
        }
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try {
            byte[] marshalledToken = marshallToken(token);

            conn = dataSource.getConnection();
            Date tokenCreationDate = Calendar.getInstance().getTime();

            preparedStatement = conn.prepareStatement(INSERT_SQL);
            preparedStatement.setString(1, tokenID);
            preparedStatement.setBytes(2, marshalledToken);
            preparedStatement
                .setTimestamp(3, new Timestamp(tokenCreationDate.getTime()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IOException(e);
        } finally {
            safeClose(preparedStatement);
            safeClose(conn);
        }
    }

    /**
     * @see SecurityTokenRegistry#removeToken(String)
     */
    @Override
    public void removeToken(String tokenID) throws IOException {
        if (dataSource == null) {
            throw logger.datasourceIsNull();
        }

        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try {
            conn = dataSource.getConnection();

            preparedStatement = conn.prepareStatement(DELETE_SQL);
            preparedStatement.setString(1, tokenID);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new IOException(e);
        } finally {
            safeClose(preparedStatement);
            safeClose(conn);
        }
    }

    /**
     * @see SecurityTokenRegistry#getToken(String)
     */
    @Override
    public Object getToken(String tokenID) {
        try {
            return unmarshalToken(getLOB(tokenID));
        } catch (IOException e) {
            throw logger.runtimeException("getToken", e);
        }
    }

    /**
     * Serialize object to bytes
     *
     * @param token
     *
     * @return bytes
     *
     * @throws IOException
     */
    private byte[] marshallToken(Object token) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(token);
        return baos.toByteArray();
    }

    /**
     * De-Serialize bytes to object
     *
     * @param serialized
     *
     * @return object
     */
    private Object unmarshalToken(byte[] serialized) {
        try {
            ByteArrayInputStream byteArray = new ByteArrayInputStream(serialized);
            return new ObjectInputStream(byteArray).readObject();
        } catch (Exception e) {
            throw logger.errorUnmarshallingToken(e);
        }
    }

    /**
     * Retrieve token from DB
     *
     * @param tokenID
     *
     * @return object as bytes
     *
     * @throws IOException
     */
    private byte[] getLOB(String tokenID) throws IOException {
        if (dataSource == null) {
            throw logger.datasourceIsNull();
        }

        Connection conn = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            conn = dataSource.getConnection();

            preparedStatement = conn.prepareStatement(SELECT_SQL);
            preparedStatement.setString(1, tokenID);
            resultSet = preparedStatement.executeQuery();

            return resultSet.getBytes(1);
        } catch (SQLException e) {
            throw new IOException(e);
        } finally {
            safeClose(resultSet);
            safeClose(preparedStatement);
            safeClose(conn);
        }
    }
}
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

/**
 * Implementation of {@link SecurityTokenRegistry} using JDBC
 *
 * @author Anil Saldhana
 * @since August 06, 2013
 */
public class JDBCTokenRegistry extends AbstractJDBCRegistry implements SecurityTokenRegistry {

    public JDBCTokenRegistry() {
        super("jdbc/picketlink-sts");
    }

    public JDBCTokenRegistry(String jndiName) {
        super(jndiName);
    }

    /**
     * @see SecurityTokenRegistry#addToken(String, Object)
     */
    public void addToken(String tokenID, Object token) throws IOException {
        if (dataSource == null) {
            throw logger.datasourceIsNull();
        }
        String insertTableSQL = "INSERT INTO TOKEN_REGISTRY" + "(TOKEN_ID, TOKEN, CREATED_DATE) VALUES" + "(?,?,?)";
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try {
            conn = dataSource.getConnection();
            Date tokenCreationDate = Calendar.getInstance().getTime();
            byte[] marshalledToken = marshallToken(token);

            String theToken = new String(marshalledToken, "UTF-8");

            preparedStatement = conn.prepareStatement(insertTableSQL);
            preparedStatement.setString(1, tokenID);
            preparedStatement.setString(2, theToken);
            preparedStatement.setTimestamp(3, new Timestamp(tokenCreationDate.getTime()));
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
    public void removeToken(String tokenID) throws IOException {
        if (dataSource == null) {
            throw logger.datasourceIsNull();
        }
        String deleteSQL = "DELETE FROM TOKEN_REGISTRY WHERE TOKEN_ID = ?";
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try {
            conn = dataSource.getConnection();

            preparedStatement = conn.prepareStatement(deleteSQL);
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
    public Object getToken(String tokenID) {
        try {
            return unmarshalToken(getLOB(tokenID));
        } catch (IOException e) {
            throw logger.runtimeException("getToken", e);
        }
    }

    private byte[] marshallToken(Object token) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(token);
        return baos.toByteArray();
    }

    private Object unmarshalToken(String serializedText) {
        try {
            ByteArrayInputStream byteArray = new ByteArrayInputStream(serializedText.getBytes("UTF-8"));
            return new ObjectInputStream(byteArray).readObject();
        } catch (Exception e) {
            throw logger.errorUnmarshallingToken(e);
        }
    }

    private String getLOB(String tokenID) throws IOException {
        if (dataSource == null) {
            throw logger.datasourceIsNull();
        }
        String selectSQL = "select TOKEN from TOKEN_REGISTRY where TOKEN_ID = ?";
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            conn = dataSource.getConnection();

            preparedStatement = conn.prepareStatement(selectSQL);
            preparedStatement.setString(1, tokenID);
            resultSet = preparedStatement.executeQuery();

            return resultSet.getString(1);
        } catch (SQLException e) {
            throw new IOException(e);
        } finally {
            safeClose(resultSet);
            safeClose(preparedStatement);
            safeClose(conn);
        }
    }
}
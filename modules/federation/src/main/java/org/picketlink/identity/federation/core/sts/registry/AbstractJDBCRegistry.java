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

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * @author Anil Saldhana
 * @since August 06, 2013
 */
public class AbstractJDBCRegistry {

    protected static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();
    protected DataSource dataSource;

    public AbstractJDBCRegistry() {
        this("java:comp/env", "jdbc/picketlink-sts");
    }

    public AbstractJDBCRegistry(String jndiName) {
        this("java:comp/env", jndiName);
    }

    public AbstractJDBCRegistry(String envName, String jndiName) {
        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup(envName);
            dataSource = (DataSource) envContext.lookup(jndiName);
            if (dataSource == null) {
                throw logger.datasourceIsNull();
            }
        } catch (NamingException e) {
            throw logger.jbdcInitializationError(e);
        }
    }

    protected void safeClose(AutoCloseable auto) {
        if (auto != null) {
            try {
                auto.close();
            } catch (Exception e) {
            }
        }
    }
}
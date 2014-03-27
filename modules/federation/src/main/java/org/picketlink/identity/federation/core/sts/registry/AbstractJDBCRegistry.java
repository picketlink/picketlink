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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;

/**
 * @author Anil Saldhana
 * @since August 06, 2013
 */
public class AbstractJDBCRegistry {

    private static final String JNDI_COMP_ENV = "java:comp/env";

    private static final String JNDI_JBOSS = "java:comp/env";

    protected static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();
    private DataSource dataSource;

    public AbstractJDBCRegistry(String jndiName) {
        try {
            Context initContext = new InitialContext();
            Context envContext;
            if(jndiName != null && !jndiName.trim().isEmpty() && jndiName.startsWith(JNDI_COMP_ENV)){
                envContext = (Context) initContext.lookup(JNDI_COMP_ENV);
                jndiName = jndiName.substring(JNDI_COMP_ENV.length());
            } else {
                envContext = (Context) initContext.lookup(JNDI_JBOSS);
            }
            dataSource = (DataSource) envContext.lookup(jndiName);
            if (dataSource == null) {
                throw logger.datasourceIsNull();
            }
        } catch (NamingException e) {
            throw logger.jbdcInitializationError(e);
        }
    }

    /**
     * Retrieve the datasource
     *
     * @return DataSource
     */
    protected DataSource getDataSource() {
        return dataSource;
    }

    protected void safeClose(AutoCloseable closable) {
        if (closable != null) {
            try {
                closable.close();
            } catch (Exception e) {
            }
        }
    }
}
/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.picketlink.idm.config;


import javax.sql.DataSource;

/**
 * <p>{@link org.picketlink.idm.config.IdentityStoreConfigurationBuilder} implementation which knows how to build a
 * {@link org.picketlink.idm.config.JDBCIdentityStoreConfiguration}.</p>
 *
 * @author Anil Saldhana
 */
public class JDBCStoreConfigurationBuilder extends
        IdentityStoreConfigurationBuilder<JDBCIdentityStoreConfiguration, JDBCStoreConfigurationBuilder> {

    private JDBCIdentityStoreConfiguration jdbcIdentityStoreConfiguration = null;
    private DataSource dataSource = null;

    public JDBCStoreConfigurationBuilder(IdentityStoresConfigurationBuilder builder) {
        super(builder);
    }

    public JDBCStoreConfigurationBuilder setDataSource(DataSource ds){
        this.dataSource = ds;
        return this;
    }

    @Override
    protected JDBCIdentityStoreConfiguration create() {
        if(jdbcIdentityStoreConfiguration == null){
            jdbcIdentityStoreConfiguration = new JDBCIdentityStoreConfiguration(getSupportedTypes(),
                    getUnsupportedTypes(),
                    getContextInitializers(),
                    getCredentialHandlerProperties(),
                    getCredentialHandlers(),
                    isSupportAttributes(),
                    isSupportCredentials());
        }
        if(dataSource != null){
            jdbcIdentityStoreConfiguration.setDataSource(dataSource);
        }
        return jdbcIdentityStoreConfiguration;
    }

    @Override
    protected void validate() {
        super.validate();
    }

    @Override
    protected JDBCStoreConfigurationBuilder readFrom(JDBCIdentityStoreConfiguration configuration) {
        super.readFrom(configuration);

        return this;
    }
}

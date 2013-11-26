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
import java.util.HashMap;
import java.util.Map;

/**
 * <p>{@link org.picketlink.idm.config.IdentityStoreConfigurationBuilder} implementation which knows how to build a
 * {@link org.picketlink.idm.config.JDBCIdentityStoreConfiguration}.</p>
 *
 * @author Anil Saldhana
 */
public class JDBCStoreConfigurationBuilder extends
        IdentityStoreConfigurationBuilder<JDBCIdentityStoreConfiguration, JDBCStoreConfigurationBuilder> {

    private JDBCIdentityStoreConfiguration jdbcIdentityStoreConfiguration = null;
    private Map<String,Class<?>> customClassMapping = new HashMap<String, Class<?>>();
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
                    isSupportCredentials(),
                    isSupportPermissions());
        }
        if(dataSource != null){
            jdbcIdentityStoreConfiguration.setDataSource(dataSource);
        }
        jdbcIdentityStoreConfiguration.setCustomClassMapping(customClassMapping);

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

    /**
     * Map a custom JDBC mapping class for a type such as {@link org.picketlink.idm.model.basic.User}
     * @param key
     * @param value
     */
    public void map(String key, Class<?> value){
        customClassMapping.put(key, value);
    }
}
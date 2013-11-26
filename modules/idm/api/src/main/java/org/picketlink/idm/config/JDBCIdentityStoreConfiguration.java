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
package org.picketlink.idm.config;

import org.picketlink.idm.credential.handler.CredentialHandler;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.spi.ContextInitializer;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link IdentityStoreConfiguration} for JDBC Identity Stores
 * @author Anil Saldhana
 * @since September 25, 2013
 */
public class JDBCIdentityStoreConfiguration extends AbstractIdentityStoreConfiguration {
    private DataSource dataSource;
    private Map<String,Class<?>> customClassMapping = new HashMap<String, Class<?>>();

    protected JDBCIdentityStoreConfiguration(String jndiName,Map<Class<? extends AttributedType>, Set<IdentityOperation>> supportedTypes, Map<Class<? extends AttributedType>,
            Set<IdentityOperation>> unsupportedTypes, List<ContextInitializer> contextInitializers, Map<String, Object> credentialHandlerProperties,
                                             Set<Class<? extends CredentialHandler>> credentialHandlers, boolean supportsAttribute, boolean supportsCredential,
                                             boolean supportsPermissions) {
        super(supportedTypes,
              unsupportedTypes,
              contextInitializers,
              credentialHandlerProperties,
              credentialHandlers,
              supportsAttribute,
              supportsCredential,
              supportsPermissions);
        //Look up
        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:comp/env");
            dataSource = (DataSource) envContext.lookup(jndiName);
            if (dataSource == null) {
                throw new RuntimeException("Null datasource");
            }
        } catch (NamingException e) {
            throw new RuntimeException("JDBC initialization error",e);
        }
    }

    protected JDBCIdentityStoreConfiguration(Map<Class<? extends AttributedType>, Set<IdentityOperation>> supportedTypes, Map<Class<? extends AttributedType>,
            Set<IdentityOperation>> unsupportedTypes, List<ContextInitializer> contextInitializers, Map<String, Object> credentialHandlerProperties,
                                             Set<Class<? extends CredentialHandler>> credentialHandlers, boolean supportsAttribute, boolean supportsCredential,
                                             boolean supportsPermissions) {
        super(supportedTypes,
              unsupportedTypes,
              contextInitializers,
              credentialHandlerProperties,
              credentialHandlers,
              supportsAttribute,
              supportsCredential,
              supportsPermissions);
    }
    protected void initConfig() {
    }

    public DataSource getDataSource(){
        return this.dataSource;
    }

    public JDBCIdentityStoreConfiguration setDataSource(DataSource ds){
        this.dataSource = ds;
        return this;
    }

    public Map<String,Class<?>> getCustomClassMapping(){
        return Collections.unmodifiableMap(customClassMapping);
    }

    public void setCustomClassMapping(Map<String,Class<?>>  customMap){
        customClassMapping.putAll(customMap);
    }

    public void map(String key, Class<?> value){
        customClassMapping.put(key,value);
    }
}
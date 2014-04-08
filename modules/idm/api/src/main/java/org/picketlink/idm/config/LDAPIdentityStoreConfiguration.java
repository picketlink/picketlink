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

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.credential.handler.CredentialHandler;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.spi.ContextInitializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.picketlink.common.constants.LDAPConstants.ENTRY_UUID;
import static org.picketlink.common.constants.LDAPConstants.OBJECT_GUID;

/**
 * A {@link AbstractIdentityStoreConfiguration} for the LDAP store.
 *
 * @author anil saldhana
 * @since Sep 6, 2012
 */

public class LDAPIdentityStoreConfiguration extends AbstractIdentityStoreConfiguration {

    public static final String ENTRY_IDENTIFIER_ATTRIBUTE_NAME = "org.picketlink.idm.config.ldap.id_attribute_name";

    private final String ldapURL;
    private String factoryName = "com.sun.jndi.ldap.LdapCtxFactory";
    private String authType = "simple";
    private String protocol;
    private final String bindDN;
    private final String bindCredential;
    private final boolean activeDirectory;
    private final Properties connectionProperties;

    private String baseDN;
    private final Map<Class<? extends AttributedType>, LDAPMappingConfiguration> mappingConfig;

    LDAPIdentityStoreConfiguration(
            String url,
            final Properties connectionProperties, String bindDN,
            String bindCredential,
            String baseDN,
            final boolean activeDirectory,
            Map<Class<? extends AttributedType>, LDAPMappingConfiguration> mappingConfig, Map<Class<? extends AttributedType>, Set<IdentityOperation>> supportedTypes,
            Map<Class<? extends AttributedType>, Set<IdentityOperation>> unsupportedTypes,
            List<ContextInitializer> contextInitializers,
            Map<String, Object> credentialHandlerProperties,
            Set<Class<? extends CredentialHandler>> credentialHandlers,
            boolean supportsCredential) {
        super(supportedTypes, unsupportedTypes, contextInitializers, credentialHandlerProperties, credentialHandlers,
                false, supportsCredential, false);
        this.ldapURL = url;
        this.connectionProperties = connectionProperties;
        this.bindDN = bindDN;
        this.bindCredential = bindCredential;
        this.activeDirectory = activeDirectory;
        this.baseDN = baseDN;
        this.mappingConfig = mappingConfig;
    }

    public String getLdapURL() {
        return this.ldapURL;
    }

    public String getFactoryName() {
        return this.factoryName;
    }

    public String getAuthType() {
        return this.authType;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public String getBaseDN() {
        return this.baseDN;
    }

    public String getBindDN() {
        return this.bindDN;
    }

    public String getBindCredential() {
        return this.bindCredential;
    }

    public boolean isActiveDirectory() {
        return this.activeDirectory;
    }

    public Properties getConnectionProperties() {
        return this.connectionProperties;
    }

    public Map<Class<? extends AttributedType>, LDAPMappingConfiguration> getMappingConfig() {
        return this.mappingConfig;
    }

    public Class<? extends AttributedType> getSupportedTypeByBaseDN(String baseDN, List<String> objectClasses) {
        for (LDAPMappingConfiguration mappingConfig : this.mappingConfig.values()) {
            if (mappingConfig.getBaseDN() != null) {
                if (!Relationship.class.isAssignableFrom(mappingConfig.getMappedClass())) {
                    if (mappingConfig.getBaseDN().equalsIgnoreCase(baseDN)
                            || mappingConfig.getParentMapping().values().contains(baseDN)) {
                        return mappingConfig.getMappedClass();
                    }
                }
            }
        }

        for (LDAPMappingConfiguration mappingConfig : this.mappingConfig.values()) {
            if (!Relationship.class.isAssignableFrom(mappingConfig.getMappedClass())) {
                for (String objectClass : objectClasses) {
                    if (mappingConfig.getObjectClasses().contains(objectClass)) {
                        return mappingConfig.getMappedClass();
                    }
                }
            }
        }

        throw new IdentityManagementException("No type found for Base DN [" + baseDN + "] or objectClasses [" + objectClasses + ".");
    }

    public LDAPMappingConfiguration getMappingConfig(Class<? extends AttributedType> attributedType) {
        for (LDAPMappingConfiguration mappingConfig : this.mappingConfig.values()) {
            if (attributedType.equals(mappingConfig.getMappedClass())) {
                return mappingConfig;
            }
        }

        return null;
    }

    public List<LDAPMappingConfiguration> getRelationshipConfigs() {
        ArrayList<LDAPMappingConfiguration> result = new ArrayList<LDAPMappingConfiguration>();

        for (LDAPMappingConfiguration mappingConfig : this.mappingConfig.values()) {
            if (mappingConfig.getRelatedAttributedType() != null) {
                result.add(mappingConfig);
            }
        }

        return result;
    }

    @Override
    public boolean supportsPartition() {
        return false;
    }

    public String getUniqueIdentifierAttributeName() {
        String property = System.getProperty(ENTRY_IDENTIFIER_ATTRIBUTE_NAME);

        if (property != null) {
            return property;
        }

        return isActiveDirectory() ? OBJECT_GUID : ENTRY_UUID;
    }
}
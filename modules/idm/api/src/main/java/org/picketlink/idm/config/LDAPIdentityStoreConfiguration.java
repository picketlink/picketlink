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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.spi.ContextInitializer;

/**
 * A {@link AbstractIdentityStoreConfiguration} for the LDAP store.
 *
 * @author anil saldhana
 * @since Sep 6, 2012
 */

public class LDAPIdentityStoreConfiguration extends AbstractIdentityStoreConfiguration {

    private String ldapURL;
    private String userDNSuffix;
    private String roleDNSuffix;
    private String groupDNSuffix;
    private String factoryName = "com.sun.jndi.ldap.LdapCtxFactory";
    private String authType = "simple";
    private String protocol;
    private String bindDN;
    private String bindCredential;
    private String standardAttributesFileName = "standardattributes.txt";
    private boolean isActiveDirectory = false;
    private Properties additionalProperties = new Properties();

    private String agentDNSuffix;
    private String baseDN;
    private Map<String, String> groupMapping = new HashMap<String, String>();

    LDAPIdentityStoreConfiguration(
            String url,
            String bindDN,
            String bindCredential,
            String baseDN,
            String agentDNSuffix,
            String userDNSuffix,
            String roleDNSuffix,
            String groupDNSuffix,
            Map<String, String> groupMapping,
            Map<Class<? extends AttributedType>, Set<IdentityOperation>> supportedTypes,
            Map<Class<? extends AttributedType>, Set<IdentityOperation>> unsupportedTypes,
            List<ContextInitializer> contextInitializers,
            Map<String, Object> credentialHandlerProperties,
            List<Class<? extends CredentialHandler>> credentialHandlers) {
        super(supportedTypes, unsupportedTypes, contextInitializers, credentialHandlerProperties, credentialHandlers);
        this.ldapURL = url;
        this.bindDN = bindDN;
        this.bindCredential = bindCredential;
        this.baseDN = baseDN;
        this.agentDNSuffix = agentDNSuffix;
        this.userDNSuffix = userDNSuffix;
        this.roleDNSuffix = roleDNSuffix;
        this.groupDNSuffix = groupDNSuffix;
        this.groupMapping = groupMapping;
    }

    @Override
    protected void initConfig() throws SecurityConfigurationException {
    }

    public String getStandardAttributesFileName() {
        return this.standardAttributesFileName;
    }

    public String getLdapURL() {
        return this.ldapURL;
    }

    public String getUserDNSuffix() {
        return this.userDNSuffix;
    }

    public String getRoleDNSuffix() {
        return this.roleDNSuffix;
    }

    public String getGroupDNSuffix() {
        return this.groupDNSuffix;
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

    public String getBindDN() {
        return this.bindDN;
    }

    public String getBindCredential() {
        return this.bindCredential;
    }

    public boolean isActiveDirectory() {
        return this.isActiveDirectory;
    }

    public Properties getAdditionalProperties() {
        return this.additionalProperties;
    }

    public String getAgentDNSuffix() {
        return this.agentDNSuffix;
    }

    public String getBaseDN() {
        return this.baseDN;
    }

    public String getGroupMappingDN(String groupPath) {
        Set<Entry<String, String>> entrySet = this.groupMapping.entrySet();

        for (Entry<String, String> entry : entrySet) {
            if (groupPath.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return this.groupMapping.get(groupPath);
    }

    public boolean isGroupNamespace(String nameInNamespace) {
        if (nameInNamespace.endsWith(getGroupDNSuffix())) {
            return true;
        }

        Set<Entry<String, String>> entrySet = this.groupMapping.entrySet();

        for (Entry<String, String> entry : entrySet) {
            if (nameInNamespace.endsWith(entry.getValue())) {
                return true;
            }
        }

        return false;
    }
}
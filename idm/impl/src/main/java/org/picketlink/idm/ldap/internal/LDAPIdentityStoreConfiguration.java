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
package org.picketlink.idm.ldap.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.naming.NamingException;

import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.BaseAbstractStoreConfiguration;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.IdentityStoreConfiguration;

/**
 * A {@link IdentityStoreConfiguration} for LDAP
 * 
 * @author anil saldhana
 * @since Sep 6, 2012
 */

public class LDAPIdentityStoreConfiguration extends BaseAbstractStoreConfiguration {

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

    private LDAPOperationManager ldapManager;
    private String agentDNSuffix;
    private String baseDN;
    private Map<String, String> groupMapping = new HashMap<String, String>();

    @Override
    public void initConfig() throws SecurityConfigurationException {

        if (getUserDNSuffix() == null) {
            throw new SecurityConfigurationException("User baseDN not provided.");
        }

        if (getRoleDNSuffix() == null) {
            throw new SecurityConfigurationException("Role baseDN not provided.");
        }

        if (getGroupDNSuffix() == null) {
            throw new SecurityConfigurationException("Group baseDN not provided.");
        }

        if (getAgentDNSuffix() == null) {
            throw new SecurityConfigurationException("Agent baseDN not provided.");
        }

        try {
            this.ldapManager = new LDAPOperationManager(this);
        } catch (NamingException e) {
            throw new SecurityConfigurationException(e);
        }
        
        getFeatureSet().removeFeature(FeatureGroup.realm);
        getFeatureSet().removeFeature(FeatureGroup.tier);
    }

    public String getStandardAttributesFileName() {
        return standardAttributesFileName;
    }

    public LDAPIdentityStoreConfiguration setStandardAttributesFileName(String standardAttributesFileName) {
        this.standardAttributesFileName = standardAttributesFileName;
        return this;
    }

    public LDAPIdentityStoreConfiguration setLdapURL(String ldapURL) {
        this.ldapURL = ldapURL;
        return this;
    }

    public LDAPIdentityStoreConfiguration setUserDNSuffix(String userDNSuffix) {
        this.userDNSuffix = userDNSuffix;
        return this;
    }

    public LDAPIdentityStoreConfiguration setRoleDNSuffix(String roleDNSuffix) {
        this.roleDNSuffix = roleDNSuffix;
        return this;
    }

    public LDAPIdentityStoreConfiguration setGroupDNSuffix(String groupDNSuffix) {
        this.groupDNSuffix = groupDNSuffix;
        return this;
    }

    public LDAPIdentityStoreConfiguration setFactoryName(String factoryName) {
        this.factoryName = factoryName;
        return this;
    }

    public LDAPIdentityStoreConfiguration setAuthType(String authType) {
        this.authType = authType;
        return this;
    }

    public LDAPIdentityStoreConfiguration setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public LDAPIdentityStoreConfiguration setBindDN(String bindDN) {
        this.bindDN = bindDN;
        return this;
    }

    public LDAPIdentityStoreConfiguration setBindCredential(String bindCredential) {
        this.bindCredential = bindCredential;
        return this;
    }

    public String getLdapURL() {
        return ldapURL;
    }

    public String getUserDNSuffix() {
        return userDNSuffix;
    }

    public String getRoleDNSuffix() {
        return roleDNSuffix;
    }

    public String getGroupDNSuffix() {
        return groupDNSuffix;
    }

    public String getFactoryName() {
        return factoryName;
    }

    public String getAuthType() {
        return authType;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getBindDN() {
        return bindDN;
    }

    public String getBindCredential() {
        return bindCredential;
    }

    public boolean isActiveDirectory() {
        return isActiveDirectory;
    }

    public void setActiveDirectory(boolean isActiveDirectory) {
        this.isActiveDirectory = isActiveDirectory;
    }

    public Properties getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Properties additionalProperties) {
        this.additionalProperties.putAll(additionalProperties);
    }

    public LDAPOperationManager getLdapManager() {
        return this.ldapManager;
    }

    public String getAgentDNSuffix() {
        return this.agentDNSuffix;
    }

    public LDAPIdentityStoreConfiguration setAgentDNSuffix(String agentDNSuffix) {
        this.agentDNSuffix = agentDNSuffix;
        return this;
    }

    public LDAPIdentityStoreConfiguration setBaseDN(String baseDN) {
        this.baseDN = baseDN;
        return this;
    }

    public String getBaseDN() {
        return this.baseDN;
    }

    public void addGroupMapping(String groupPath, String groupBaseDN) {
        this.groupMapping.put(groupPath, groupBaseDN);
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
        if (nameInNamespace.endsWith(nameInNamespace)) {
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
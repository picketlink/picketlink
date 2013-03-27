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

import static org.picketlink.idm.IDMMessages.MESSAGES;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;

/**
 * A {@link BaseAbstractStoreConfiguration} for the LDAP store.
 *
 * @author anil saldhana
 * @since Sep 6, 2012
 */

public class LDAPIdentityStoreConfiguration extends BaseAbstractStoreConfiguration<LDAPIdentityStoreConfiguration> {

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

    @Override
    protected void initConfig() throws SecurityConfigurationException {
        if (getUserDNSuffix() == null) {
            throw MESSAGES.ldapConfigUserDNNotProvided();
        }

        if (getRoleDNSuffix() == null) {
            throw MESSAGES.ldapConfigRoleDNNotProvided();
        }

        if (getGroupDNSuffix() == null) {
            throw MESSAGES.ldapConfigGroupDNNotProvided();
        }

        if (getAgentDNSuffix() == null) {
            setAgentDNSuffix(getUserDNSuffix());
        }

        getFeatureSet().removeFeature(FeatureGroup.realm);
        getFeatureSet().removeFeature(FeatureGroup.tier);
        getFeatureSet().setSupportsCustomRelationships(false);
        getFeatureSet().setSupportsMultiRealm(false);
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

    public LDAPIdentityStoreConfiguration addGroupMapping(String groupPath, String groupBaseDN) {
        this.groupMapping.put(groupPath, groupBaseDN);
        return this;
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
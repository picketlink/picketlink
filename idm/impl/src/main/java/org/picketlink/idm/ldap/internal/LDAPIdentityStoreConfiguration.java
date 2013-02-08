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
package org.picketlink.idm.ldap.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.naming.NamingException;

import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.model.Grant;
import org.picketlink.idm.model.GroupMembership;

/**
 * A {@link IdentityStoreConfiguration} for LDAP
 * 
 * @author anil saldhana
 * @since Sep 6, 2012
 */

public class LDAPIdentityStoreConfiguration extends IdentityStoreConfiguration {

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
    private FeatureSet featureSet = new FeatureSet();
    private LDAPOperationManager ldapManager;
    private String agentDNSuffix;
    private String baseDN;
    private Map<String, String> groupMapping = new HashMap<String, String>();
    
    
    @Override
    public void init() throws SecurityConfigurationException {
        this.featureSet.addSupportedFeature(Feature.all);
        this.featureSet.addSupportedRelationship(Grant.class);
        this.featureSet.addSupportedRelationship(GroupMembership.class);
        
        getSupportedCredentialHandlers().add(LDAPPlainTextPasswordCredentialHandler.class);
        
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
    }

    @Override
    public FeatureSet getFeatureSet() {
        return this.featureSet;
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
            if (groupPath.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        return this.groupMapping.get(groupPath);
    }
}
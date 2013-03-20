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

package org.picketlink.idm.internal.config;

import java.util.Properties;

import org.picketlink.idm.config.LDAPIdentityStoreConfiguration;

/**
 * @author Pedro Silva
 *
 */
public class LDAPIdentityStoreConfigurationBuilder extends IdentityStoreConfigurationBuilder<LDAPIdentityStoreConfigurationBuilder, LDAPIdentityStoreConfiguration> {

    protected LDAPIdentityStoreConfigurationBuilder(ConfigurationBuilder<?> builder) {
        super(new LDAPIdentityStoreConfiguration(), builder);
    }

    public LDAPIdentityStoreConfigurationBuilder setStandardAttributesFileName(String standardAttributesFileName) {
        getConfiguration().setStandardAttributesFileName(standardAttributesFileName);
        return this;
    }

    public LDAPIdentityStoreConfigurationBuilder setLdapURL(String ldapURL) {
        getConfiguration().setLdapURL(ldapURL);
        return this;
    }

    public LDAPIdentityStoreConfigurationBuilder setUserDNSuffix(String userDNSuffix) {
        getConfiguration().setUserDNSuffix(userDNSuffix);
        return this;
    }

    public LDAPIdentityStoreConfigurationBuilder setRoleDNSuffix(String roleDNSuffix) {
        getConfiguration().setRoleDNSuffix(roleDNSuffix);
        return this;
    }

    public LDAPIdentityStoreConfigurationBuilder setGroupDNSuffix(String groupDNSuffix) {
        getConfiguration().setGroupDNSuffix(groupDNSuffix);
        return this;
    }

    public LDAPIdentityStoreConfigurationBuilder setFactoryName(String factoryName) {
        getConfiguration().setFactoryName(factoryName);
        return this;
    }

    public LDAPIdentityStoreConfigurationBuilder setAuthType(String authType) {
        getConfiguration().setAuthType(authType);
        return this;
    }

    public LDAPIdentityStoreConfigurationBuilder setProtocol(String protocol) {
        getConfiguration().setProtocol(protocol);
        return this;
    }

    public LDAPIdentityStoreConfigurationBuilder setBindDN(String bindDN) {
        getConfiguration().setBindDN(bindDN);
        return this;
    }

    public LDAPIdentityStoreConfigurationBuilder setBindCredential(String bindCredential) {
        getConfiguration().setBindCredential(bindCredential);
        return this;
    }

    public LDAPIdentityStoreConfigurationBuilder setActiveDirectory(boolean isActiveDirectory) {
        getConfiguration().setActiveDirectory(isActiveDirectory);
        return this;
    }

    public LDAPIdentityStoreConfigurationBuilder setAdditionalProperties(Properties additionalProperties) {
        getConfiguration().setAdditionalProperties(additionalProperties);
        return this;
    }

    public LDAPIdentityStoreConfigurationBuilder setAgentDNSuffix(String agentDNSuffix) {
        getConfiguration().setAgentDNSuffix(agentDNSuffix);
        return this;
    }

    public LDAPIdentityStoreConfigurationBuilder setBaseDN(String baseDN) {
        getConfiguration().setBaseDN(baseDN);
        return this;
    }

    public LDAPIdentityStoreConfigurationBuilder addGroupMapping(String groupPath, String groupBaseDN) {
        getConfiguration().addGroupMapping(groupPath, groupBaseDN);
        return this;
    }

}
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

import java.util.HashMap;
import java.util.Map;
import org.picketlink.idm.model.Partition;
import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * @author Pedro Igor
 *
 */
public class LDAPStoreConfigurationBuilder extends
        AbstractIdentityStoreConfigurationBuilder<LDAPIdentityStoreConfiguration, LDAPStoreConfigurationBuilder> {

    private String url;
    private String baseDN;
    private String agentDNSuffix;
    private String userDNSuffix;
    private String roleDNSuffix;
    private String groupDNSuffix;
    private String bindDN;
    private String bindCredential;
    private Map<String, String> groupMapping = new HashMap<String, String>();

    public LDAPStoreConfigurationBuilder(IdentityStoresConfigurationBuilder builder) {
        super(builder);
    }

    public LDAPStoreConfigurationBuilder url(String url) {
        this.url = url;
        return this;
    }

    public LDAPStoreConfigurationBuilder baseDN(String baseDN) {
        this.baseDN = baseDN;
        return this;
    }

    public LDAPStoreConfigurationBuilder agentDNSuffix(String agentDNSuffix) {
        this.agentDNSuffix = agentDNSuffix;
        return this;
    }

    public LDAPStoreConfigurationBuilder userDNSuffix(String userDNSuffix) {
        this.userDNSuffix = userDNSuffix;
        return this;
    }

    public LDAPStoreConfigurationBuilder roleDNSuffix(String roleDNSuffix) {
        this.roleDNSuffix = roleDNSuffix;
        return this;
    }

    public LDAPStoreConfigurationBuilder groupDNSuffix(String groupDNSuffix) {
        this.groupDNSuffix = groupDNSuffix;
        return this;
    }

    public LDAPStoreConfigurationBuilder bindDN(String bindDN) {
        this.bindDN = bindDN;
        return this;
    }

    public LDAPStoreConfigurationBuilder bindCredential(String bindCredential) {
        this.bindCredential = bindCredential;
        return this;
    }

    public LDAPStoreConfigurationBuilder addGroupMapping(String groupPath, String groupBaseDN) {
        this.groupMapping.put(groupPath, groupBaseDN);
        return this;
    }

    @Override
    public LDAPIdentityStoreConfiguration create() {
        return new LDAPIdentityStoreConfiguration(
                this.url,
                this.bindDN,
                this.bindCredential,
                this.baseDN,
                this.agentDNSuffix,
                this.userDNSuffix,
                this.roleDNSuffix,
                this.groupDNSuffix,
                this.groupMapping,
                getSupportedTypes(),
                getUnsupportedTypes(),
                getContextInitializers(),
                getCredentialHandlerProperties(),
                getCredentialHandlers());
    }

    @Override
    public void validate() {
        super.validate();

        if (this.userDNSuffix == null) {
            throw MESSAGES.ldapConfigUserDNNotProvided();
        }

        if (this.roleDNSuffix == null) {
            throw MESSAGES.ldapConfigRoleDNNotProvided();
        }

        if (this.groupDNSuffix == null) {
            throw MESSAGES.ldapConfigGroupDNNotProvided();
        }

        if (this.agentDNSuffix == null) {
            this.agentDNSuffix = this.userDNSuffix;
        }

        unsupportType(Partition.class);
    }

    @Override
    public LDAPStoreConfigurationBuilder readFrom(LDAPIdentityStoreConfiguration configuration) {
        super.readFrom(configuration);

        this.agentDNSuffix = configuration.getAgentDNSuffix();
        this.baseDN = configuration.getBaseDN();
        this.bindCredential = configuration.getBindCredential();
        this.bindDN = configuration.getBindDN();
        this.groupDNSuffix = configuration.getGroupDNSuffix();
        this.roleDNSuffix = configuration.getRoleDNSuffix();
        this.url = configuration.getLdapURL();
        this.userDNSuffix = configuration.getUserDNSuffix();

        return this;
    }
}
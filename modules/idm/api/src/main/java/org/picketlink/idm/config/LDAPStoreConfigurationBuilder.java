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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;

/**
 * @author Pedro Igor
 *
 */
public class LDAPStoreConfigurationBuilder extends
        IdentityStoreConfigurationBuilder<LDAPIdentityStoreConfiguration, LDAPStoreConfigurationBuilder> {

    private String url;
    private String baseDN;
    private String agentDNSuffix;
    private String userDNSuffix;
    private String roleDNSuffix;
    private String groupDNSuffix;
    private String bindDN;
    private String bindCredential;
    private Map<String, String> groupMapping = new HashMap<String, String>();
    private Set<LDAPMappingConfigurationBuilder> mappingBuilders = new HashSet<LDAPMappingConfigurationBuilder>();

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

    public LDAPMappingConfigurationBuilder mapping(Class<? extends AttributedType> attributedType) {
        LDAPMappingConfigurationBuilder ldapMappingConfigurationBuilder = new LDAPMappingConfigurationBuilder(attributedType, this);

        this.mappingBuilders.add(ldapMappingConfigurationBuilder);

        supportType(attributedType);

        return ldapMappingConfigurationBuilder;
    }

    public LDAPMappingConfigurationBuilder mappingRelationship(Class<? extends Relationship> relationshipClass) {
        LDAPMappingConfigurationBuilder ldapMappingConfigurationBuilder = new LDAPMappingConfigurationBuilder(relationshipClass, this);

        this.mappingBuilders.add(ldapMappingConfigurationBuilder);

        return ldapMappingConfigurationBuilder;
    }

    @Override
    protected LDAPIdentityStoreConfiguration create() {
        Map<Class<? extends AttributedType>, LDAPMappingConfiguration> mappingConfig = new HashMap<Class<? extends AttributedType>, LDAPMappingConfiguration>();

        for (LDAPMappingConfigurationBuilder builder: this.mappingBuilders) {
            LDAPMappingConfiguration ldapMappingConfiguration = builder.create();

            mappingConfig.put(ldapMappingConfiguration.getMappedClass(), ldapMappingConfiguration);
        }

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
                mappingConfig,
                getSupportedTypes(),
                getUnsupportedTypes(),
                getContextInitializers(),
                getCredentialHandlerProperties(),
                getCredentialHandlers());
    }

    @Override
    protected void validate() {
        super.validate();

        for (LDAPMappingConfigurationBuilder builder: this.mappingBuilders) {
            builder.validate();
        }

        unsupportType(Partition.class);
    }

    @Override
    protected LDAPStoreConfigurationBuilder readFrom(LDAPIdentityStoreConfiguration configuration) {
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
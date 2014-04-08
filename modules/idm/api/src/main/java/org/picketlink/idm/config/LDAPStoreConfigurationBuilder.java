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

import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.picketlink.common.util.StringUtil.isNullOrEmpty;

/**
 * <p>{@link IdentityStoreConfigurationBuilder} implementation which knows how to build a
 * {@link LDAPIdentityStoreConfiguration}.</p>
 *
 * @author Pedro Igor
 *
 */
public class LDAPStoreConfigurationBuilder extends
        IdentityStoreConfigurationBuilder<LDAPIdentityStoreConfiguration, LDAPStoreConfigurationBuilder> {

    private String url;
    private String baseDN;
    private String bindDN;
    private String bindCredential;
    private boolean activeDirectory;
    private Properties connectionProperties;
    private Set<LDAPMappingConfigurationBuilder> mappingBuilders = new HashSet<LDAPMappingConfigurationBuilder>();

    public LDAPStoreConfigurationBuilder(IdentityStoresConfigurationBuilder builder) {
        super(builder);
    }

    /**
     * <p>Configures the URL of the LDAP server. The URL should be in the format ldap://ldapserver.com:389.</p>
     *
     * @param url
     * @return
     */
    public LDAPStoreConfigurationBuilder url(String url) {
        this.url = url;
        return this;
    }

    /**
     * <p>Sets the base DN.</p>
     *
     * @param baseDN
     * @return
     */
    public LDAPStoreConfigurationBuilder baseDN(String baseDN) {
        this.baseDN = baseDN;
        return this;
    }

    /**
     * <p>Sets the DN used to connect to the LDAP server.</p>
     *
     * @param bindDN
     * @return
     */
    public LDAPStoreConfigurationBuilder bindDN(String bindDN) {
        this.bindDN = bindDN;
        return this;
    }

    /**
     * <p>Sets the credential for the <code>bindDN</code> used to connect to the LDAP server.</p>
     *
     * @param bindCredential
     * @return
     */
    public LDAPStoreConfigurationBuilder bindCredential(String bindCredential) {
        this.bindCredential = bindCredential;
        return this;
    }

    /**
     * <p>Indicates if the underlying server is a Microsft Activde Directory.</p>
     *
     * @param activeDirectory
     * @return
     */
    public LDAPStoreConfigurationBuilder activeDirectory(boolean activeDirectory) {
        this.activeDirectory = activeDirectory;
        return this;
    }

    /**
     * <p>Maps a specific {@link AttributedType}.</p>
     *
     * @param attributedType
     * @return
     */
    public LDAPMappingConfigurationBuilder mapping(Class<? extends AttributedType> attributedType) {
        LDAPMappingConfigurationBuilder ldapMappingConfigurationBuilder = new LDAPMappingConfigurationBuilder(attributedType, this);

        this.mappingBuilders.add(ldapMappingConfigurationBuilder);

        supportType(attributedType);

        if (Relationship.class.isAssignableFrom(attributedType)) {
            supportGlobalRelationship((Class <? extends Relationship>) attributedType);
        }

        return ldapMappingConfigurationBuilder;
    }

    /**
     * <p>Set additional connection properties.</p>
     *
     * @return
     */
    public LDAPStoreConfigurationBuilder connectionProperties(Properties connectionProperties) {
        this.connectionProperties = connectionProperties;
        return this;
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
                this.connectionProperties,
                this.bindDN,
                this.bindCredential,
                this.baseDN,
                this.activeDirectory,
                mappingConfig,
                getSupportedTypes(),
                getUnsupportedTypes(),
                getContextInitializers(),
                getCredentialHandlerProperties(),
                getCredentialHandlers(),
                isSupportCredentials());
    }

    @Override
    protected void validate() {
        super.validate();

        if (isNullOrEmpty(this.baseDN)) {
            throw new SecurityConfigurationException("You must provide the Base DN.");
        }

        if (isNullOrEmpty(this.bindDN)) {
            throw new SecurityConfigurationException("You must provide the Bind DN.");
        }

        if (isNullOrEmpty(this.bindCredential)) {
            throw new SecurityConfigurationException("You must provide the credentials for the Bind DN.");
        }

        if (this.mappingBuilders.isEmpty()) {
            throw new SecurityConfigurationException("No mappings provided.");
        }

        for (LDAPMappingConfigurationBuilder builder: this.mappingBuilders) {
            builder.validate();
        }

        unsupportType(Partition.class);
    }

    @Override
    protected LDAPStoreConfigurationBuilder readFrom(LDAPIdentityStoreConfiguration configuration) {
        super.readFrom(configuration);

        this.baseDN = configuration.getBaseDN();
        this.bindCredential = configuration.getBindCredential();
        this.bindDN = configuration.getBindDN();
        this.url = configuration.getLdapURL();
        this.activeDirectory = configuration.isActiveDirectory();
        this.connectionProperties = configuration.getConnectionProperties();

        for (Class<? extends AttributedType> attributedType: configuration.getMappingConfig().keySet()) {
            LDAPMappingConfiguration mappingConfiguration = configuration.getMappingConfig().get(attributedType);

            mapping(attributedType).readFrom(mappingConfiguration);
        }

        return this;
    }
}
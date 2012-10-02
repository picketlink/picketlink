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

import org.picketlink.idm.spi.IdentityStoreConfiguration;

/**
 * A {@link IdentityStoreConfiguration} for LDAP
 *
 * @author anil saldhana
 * @since Sep 6, 2012
 */
public class LDAPConfiguration extends IdentityStoreConfiguration {
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

    public String getStandardAttributesFileName() {
        return standardAttributesFileName;
    }

    public LDAPConfiguration setStandardAttributesFileName(String standardAttributesFileName) {
        this.standardAttributesFileName = standardAttributesFileName;
        return this;
    }

    public LDAPConfiguration setLdapURL(String ldapURL) {
        this.ldapURL = ldapURL;
        return this;
    }

    public LDAPConfiguration setUserDNSuffix(String userDNSuffix) {
        this.userDNSuffix = userDNSuffix;
        return this;
    }

    public LDAPConfiguration setRoleDNSuffix(String roleDNSuffix) {
        this.roleDNSuffix = roleDNSuffix;
        return this;
    }

    public LDAPConfiguration setGroupDNSuffix(String groupDNSuffix) {
        this.groupDNSuffix = groupDNSuffix;
        return this;
    }

    public LDAPConfiguration setFactoryName(String factoryName) {
        this.factoryName = factoryName;
        return this;
    }

    public LDAPConfiguration setAuthType(String authType) {
        this.authType = authType;
        return this;
    }

    public LDAPConfiguration setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public LDAPConfiguration setBindDN(String bindDN) {
        this.bindDN = bindDN;
        return this;
    }

    public LDAPConfiguration setBindCredential(String bindCredential) {
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
}
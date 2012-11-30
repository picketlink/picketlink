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

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingException;

import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStore.Feature;

/**
 * A {@link IdentityStoreConfiguration} for LDAP
 *
 * @author anil saldhana
 * @since Sep 6, 2012
 */

// TODO suggest renaming this to LDAPIdentityStoreConfiguration and moving to org.picketlink.idm.config 
// package within API module
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
    private Set<Feature> featuresSet = new HashSet<IdentityStore.Feature>();
    private LDAPOperationManager ldapManager;

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

    @Override
    public Set<Feature> getSupportedFeatures() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void init() throws SecurityConfigurationException {
        constructContext();
        this.featuresSet.add(Feature.all);
    }

    @Override
    public Set<Feature> getFeatureSet() {
        return this.featuresSet ;
    }
    
    private void constructContext() {
        Properties env = new Properties();
        env.setProperty(Context.INITIAL_CONTEXT_FACTORY, getFactoryName());
        env.setProperty(Context.SECURITY_AUTHENTICATION, getAuthType());

        String protocol = getProtocol();
        if (protocol != null) {
            env.setProperty(Context.SECURITY_PROTOCOL, protocol);
        }
        String bindDN = getBindDN();
        char[] bindCredential = null;

        if (getBindCredential() != null) {
            bindCredential = getBindCredential().toCharArray();
        }

        if (bindDN != null) {
            env.setProperty(Context.SECURITY_PRINCIPAL, bindDN);
            env.put(Context.SECURITY_CREDENTIALS, bindCredential);
        }

        String url = getLdapURL();
        if (url == null) {
            throw new RuntimeException("url");
        }

        env.setProperty(Context.PROVIDER_URL, url);

        // Just dump the additional properties
        Properties additionalProperties = getAdditionalProperties();
        Set<Object> keys = additionalProperties.keySet();
        for (Object key : keys) {
            env.setProperty((String) key, additionalProperties.getProperty((String) key));
        }

        try {
            this.ldapManager = new LDAPOperationManager(env);
        } catch (NamingException e1) {
            throw new RuntimeException(e1);
        }
    }

    public LDAPOperationManager getLdapManager() {
        return this.ldapManager;
    }
}
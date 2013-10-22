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

package org.picketlink.test.idm.config;

import org.junit.Test;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.config.idm.XMLConfigurationProvider;
import org.picketlink.config.idm.resolver.BasicPropertyResolver;
import org.picketlink.config.idm.resolver.PropertyResolverMapper;
import org.picketlink.idm.config.FileIdentityStoreConfiguration;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration;
import org.picketlink.idm.config.LDAPIdentityStoreConfiguration;
import org.picketlink.idm.config.LDAPMappingConfiguration;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.jpa.model.sample.simple.DigestCredentialTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.GroupTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.PartitionTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.PasswordCredentialTypeEntity;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.GroupMembership;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.idm.basic.MyCustomAccountEntity;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Test for parsing of IDM configuration from XML file and checking that content of builders is as expected
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class XMLConfigurationTestCase {

    @Test
    public void testParseFileConfiguration() throws ParsingException {
        List<IdentityConfiguration> configs = buildFromFile("config/embedded-file-config.xml");
        assertEquals(configs.size(), 1);
        IdentityConfiguration config = configs.get(0);
        assertEquals("SIMPLE_FILE_STORE_CONFIG", config.getName());

        assertTrue(config.getRelationshipPolicy().isGlobalRelationshipSupported(Relationship.class));

        assertEquals(config.getStoreConfiguration().size(), 1);
        assertTrue(config.getStoreConfiguration().get(0) instanceof FileIdentityStoreConfiguration);
        FileIdentityStoreConfiguration fileStoreConfig = (FileIdentityStoreConfiguration)config.getStoreConfiguration().get(0);
        assertTrue(fileStoreConfig.isAlwaysCreateFiles());
    }

    @Test
    public void testParseLDAPConfiguration() throws ParsingException {
        List<IdentityConfiguration> configs = buildFromFile("config/embedded-ldap-config.xml");
        assertEquals(configs.size(), 1);
        IdentityConfiguration config = configs.get(0);
        assertEquals("SIMPLE_LDAP_STORE_CONFIG", config.getName());

        assertTrue(config.getRelationshipPolicy().isGlobalRelationshipSupported(Grant.class));
        assertTrue(config.getRelationshipPolicy().isGlobalRelationshipSupported(GroupMembership.class));

        assertEquals(config.getStoreConfiguration().size(), 1);
        assertTrue(config.getStoreConfiguration().get(0) instanceof LDAPIdentityStoreConfiguration);
        LDAPIdentityStoreConfiguration ldapStoreConfig = (LDAPIdentityStoreConfiguration)config.getStoreConfiguration().get(0);
        assertEquals("dc=jboss,dc=org", ldapStoreConfig.getBaseDN());
        assertEquals("uid=admin,ou=system", ldapStoreConfig.getBindDN());
        assertEquals("secret", ldapStoreConfig.getBindCredential());
        assertEquals("ldap://localhost:10389", ldapStoreConfig.getLdapURL());

        ldapStoreConfig.supportsType(IdentityType.class, IdentityStoreConfiguration.IdentityOperation.read);
        ldapStoreConfig.supportsType(IdentityType.class, IdentityStoreConfiguration.IdentityOperation.create);

        // Some mapping testing
        Map<Class<? extends AttributedType>, LDAPMappingConfiguration> mappingConfig = ldapStoreConfig.getMappingConfig();
        assertEquals(mappingConfig.size(), 6);

        LDAPMappingConfiguration userMapping = mappingConfig.get(User.class);
        assertTrue(userMapping.getObjectClasses().contains("inetOrgPerson"));
        assertTrue(userMapping.getObjectClasses().contains("organizationalPerson"));
        assertTrue(userMapping.getReadOnlyAttributes().contains("createdDate"));
        assertEquals("loginName", userMapping.getIdProperty().getName());
        Map<String, String> mappedProps = userMapping.getMappedProperties();
        assertEquals(mappedProps.size(), 5);
        assertEquals("uid", mappedProps.get("loginName"));
        assertEquals("sn", mappedProps.get("lastName"));

        LDAPMappingConfiguration groupMapping = mappingConfig.get(Group.class);
        assertEquals("ou=Groups,dc=jboss,dc=org", groupMapping.getBaseDN());
        assertEquals("name", groupMapping.getIdProperty().getName());
        assertEquals("member", groupMapping.getParentMembershipAttributeName());

        LDAPMappingConfiguration grantMapping = mappingConfig.get(Grant.class);
        assertEquals("member", grantMapping.getMappedProperties().get("assignee"));
        assertEquals(grantMapping.getRelatedAttributedType(), Role.class);
    }

    @Test
    public void testParseJPAConfiguration() throws ParsingException {
        List<IdentityConfiguration> configs = buildFromFile("config/embedded-jpa-config.xml");
        assertEquals(configs.size(), 1);
        IdentityConfiguration config = configs.get(0);
        assertEquals("SIMPLE_JPA_STORE_CONFIG", config.getName());

        assertTrue(config.getRelationshipPolicy().isGlobalRelationshipSupported(Relationship.class));

        assertEquals(config.getStoreConfiguration().size(), 1);
        assertTrue(config.getStoreConfiguration().get(0) instanceof JPAIdentityStoreConfiguration);
        JPAIdentityStoreConfiguration jpaStoreConfig = (JPAIdentityStoreConfiguration)config.getStoreConfiguration().get(0);

        jpaStoreConfig.supportsType(IdentityType.class, IdentityStoreConfiguration.IdentityOperation.read);
        jpaStoreConfig.supportsType(IdentityType.class, IdentityStoreConfiguration.IdentityOperation.create);
        jpaStoreConfig.supportsType(Partition.class, IdentityStoreConfiguration.IdentityOperation.read);
        jpaStoreConfig.supportsType(Relationship.class, IdentityStoreConfiguration.IdentityOperation.read);

        Set<Class<?>> entityTypes = jpaStoreConfig.getEntityTypes();
        assertEquals(entityTypes.size(), 14);
        assertTrue(entityTypes.contains(PasswordCredentialTypeEntity.class));
        assertTrue(entityTypes.contains(DigestCredentialTypeEntity.class));
        assertTrue(entityTypes.contains(PartitionTypeEntity.class));
        assertTrue(entityTypes.contains(MyCustomAccountEntity.class));
        assertTrue(entityTypes.contains(GroupTypeEntity.class));
    }

    @Test
    public void testParseCustomConfiguration() throws ParsingException {
        // First we need to register custom Resolver for MethodInvocationContext type, used in custom config (or alternative is to add it programmatically to builder later)
        final CustomIdentityStoreTestCase.MethodInvocationContext methodInvocationContext = new CustomIdentityStoreTestCase.MethodInvocationContext();

        PropertyResolverMapper.getInstance().addPropertyResolver(CustomIdentityStoreTestCase.MethodInvocationContext.class,
                new BasicPropertyResolver<CustomIdentityStoreTestCase.MethodInvocationContext>() {

                    @Override
                    protected CustomIdentityStoreTestCase.MethodInvocationContext resolvePropertyFromString(String stringPropertyValue, Class<CustomIdentityStoreTestCase.MethodInvocationContext> propertyClass) {
                        return methodInvocationContext;
                    }
                });

        List<IdentityConfiguration> configs = buildFromFile("config/embedded-custom-config.xml");
        assertEquals(configs.size(), 1);
        IdentityConfiguration config = configs.get(0);
        assertEquals("default", config.getName());

        assertTrue(config.getRelationshipPolicy().isGlobalRelationshipSupported(Relationship.class));

        assertEquals(config.getStoreConfiguration().size(), 1);
        assertTrue(config.getStoreConfiguration().get(0) instanceof CustomIdentityStoreTestCase.MyIdentityStoreConfiguration);
        CustomIdentityStoreTestCase.MyIdentityStoreConfiguration myStoreConfig = (CustomIdentityStoreTestCase.MyIdentityStoreConfiguration)config.getStoreConfiguration().get(0);

        assertEquals(methodInvocationContext, myStoreConfig.getMethodInvocationContext());
    }

    @Test (expected = SecurityConfigurationException.class)
    public void testInvalidConfiguration() throws ParsingException {
        buildFromFile("config/embedded-invalid-config.xml");
    }

    private List<IdentityConfiguration> buildFromFile(String configFilePath) {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream(configFilePath);
        XMLConfigurationProvider xmlConfigurationProvider = new XMLConfigurationProvider();
        IdentityConfigurationBuilder idmConfigBuilder = xmlConfigurationProvider.readIDMConfiguration(configStream);

        assertNotNull(idmConfigBuilder);
        return idmConfigBuilder.buildAll();
    }


}

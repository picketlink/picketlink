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

package org.picketlink.test.idm.config;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.AbstractIdentityStoreConfigurationBuilder;
import org.picketlink.idm.config.BaseAbstractStoreConfiguration;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.FeatureSet.FeatureOperation;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.config.IdentityStoresConfigurationBuilder;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.internal.IdentityManagerFactory;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.spi.ContextInitializer;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.SecurityContext;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertEquals;

/**
 *
 * @author Pedro Igor
 * 
 */
public class CustomIdentityStoreTestCase {

    @Test
    public void testConfiguration() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        // let's use this instance to test the custom store configuration and check for the methods invocation
        MethodInvocationContext methodInvocationContext = new MethodInvocationContext();

        builder
            .stores()
                .add(MyIdentityStoreConfiguration.class, 
                     MyIdentityStore.class, 
                     MyIdentityStoreConfigurationBuilder.class)
                    .methodInvocationContext(methodInvocationContext)
                .addRealm(Realm.DEFAULT_REALM)
                .addTier("SomeTier")
                .supportAllFeatures();

        IdentityConfiguration configuration = builder.build();
        IdentityManagerFactory identityManagerFactory = new IdentityManagerFactory(configuration);
        
        IdentityManager identityManager = identityManagerFactory.createIdentityManager();
        
        identityManager.add(new User("john"));

        assertEquals("addAttributedType", methodInvocationContext.getMethodName());

        identityManager.getUser("john");

        assertEquals("getUser", methodInvocationContext.getMethodName());
    }

    public static class MyIdentityStoreConfigurationBuilder extends
            AbstractIdentityStoreConfigurationBuilder<MyIdentityStoreConfiguration, MyIdentityStoreConfigurationBuilder> {

        private MethodInvocationContext methodInvocationContext;

        public MyIdentityStoreConfigurationBuilder(IdentityStoresConfigurationBuilder builder) {
            super(builder);
        }

        @Override
        public MyIdentityStoreConfiguration create() {
            MyIdentityStoreConfiguration config = new MyIdentityStoreConfiguration(getSupportedFeatures(), getSupportedRelationships(), getRealms(),
                    getTiers(), getContextInitializers(), getCredentialHandlerProperties(), getCredentialHandlers());

            config.setMethodInvocationContext(this.methodInvocationContext);

            return config;
        }

        public MyIdentityStoreConfigurationBuilder methodInvocationContext(MethodInvocationContext methodInvocationContext) {
            this.methodInvocationContext = methodInvocationContext;
            return this;
        }
    }

    public static class MyIdentityStoreConfiguration extends BaseAbstractStoreConfiguration {

        private MethodInvocationContext methodInvocationContext;

        public MyIdentityStoreConfiguration(Map<FeatureGroup, Set<FeatureOperation>> supportedFeatures,
                Map<Class<? extends Relationship>, Set<FeatureOperation>> supportedRelationships, Set<String> realms,
                Set<String> tiers, List<ContextInitializer> contextInitializers,
                Map<String, Object> credentialHandlerProperties, List<Class<? extends CredentialHandler>> credentialHandlers) {
            super(supportedFeatures, supportedRelationships, realms, tiers, contextInitializers, credentialHandlerProperties,
                    credentialHandlers);
        }

        @Override
        protected void initConfig() {

        }

        public void setMethodInvocationContext(MethodInvocationContext assertion) {
            this.methodInvocationContext = assertion;
        }

        public MethodInvocationContext getMethodInvocationContext() {
            return this.methodInvocationContext;
        }
    }

    public static class MyIdentityStore implements CredentialStore<MyIdentityStoreConfiguration> {

        private MyIdentityStoreConfiguration config;

        @Override
        public void setup(MyIdentityStoreConfiguration config) {
            this.config = config;
        }

        @Override
        public MyIdentityStoreConfiguration getConfig() {
            return this.config;
        }

        @Override
        public void add(SecurityContext context, AttributedType value) {
            value.setId(context.getIdGenerator().generate());
            getConfig().getMethodInvocationContext().setMethodName("addAttributedType");
        }

        @Override
        public void update(SecurityContext context, AttributedType value) {

        }

        @Override
        public void remove(SecurityContext context, AttributedType value) {

        }

        @Override
        public Agent getAgent(SecurityContext context, String loginName) {
            return null;
        }

        @Override
        public User getUser(SecurityContext context, String loginName) {
            getConfig().getMethodInvocationContext().setMethodName("getUser");
            return null;
        }

        @Override
        public Group getGroup(SecurityContext context, String groupPath) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Group getGroup(SecurityContext context, String name, Group parent) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Role getRole(SecurityContext context, String name) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <V extends IdentityType> List<V> fetchQueryResults(SecurityContext context, IdentityQuery<V> identityQuery) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <V extends IdentityType> int countQueryResults(SecurityContext context, IdentityQuery<V> identityQuery) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public <V extends Relationship> List<V> fetchQueryResults(SecurityContext context, RelationshipQuery<V> query) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <V extends Relationship> int countQueryResults(SecurityContext context, RelationshipQuery<V> query) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setAttribute(SecurityContext context, IdentityType identityType, Attribute<? extends Serializable> attribute) {
            // TODO Auto-generated method stub

        }

        @Override
        public <V extends Serializable> Attribute<V> getAttribute(SecurityContext context, IdentityType identityType,
                String attributeName) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void removeAttribute(SecurityContext context, IdentityType identityType, String attributeName) {
            // TODO Auto-generated method stub

        }

        @Override
        public void validateCredentials(SecurityContext context, Credentials credentials) {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateCredential(SecurityContext context, Agent agent, Object credential, Date effectiveDate,
                Date expiryDate) {
            // TODO Auto-generated method stub

        }

        @Override
        public void storeCredential(SecurityContext context, Agent agent, CredentialStorage storage) {
            // TODO Auto-generated method stub

        }

        @Override
        public <T extends CredentialStorage> T retrieveCurrentCredential(SecurityContext context, Agent agent,
                Class<T> storageClass) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <T extends CredentialStorage> List<T> retrieveCredentials(SecurityContext context, Agent agent,
                Class<T> storageClass) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    public static class MethodInvocationContext {

        private String methodName;

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public String getMethodName() {
            return this.methodName;
        }
    }
}

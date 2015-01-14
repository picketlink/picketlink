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

package org.picketlink.test.idm.attribute;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.AbstractAttributedType;
import org.picketlink.idm.model.AbstractIdentityType;
import org.picketlink.idm.model.AbstractPartition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.IdentityQueryBuilder;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.query.RelationshipQueryParameter;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.FileManagedAttributeConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAManagedAttributeConfigurationTester;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * <p>
 * Test case for the {@link org.picketlink.idm.model.basic.User} basic management operations using only the default realm.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@Configuration(include = {FileManagedAttributeConfigurationTester.class, JPAManagedAttributeConfigurationTester.class})
public class ManagedAttributeTestCase extends AbstractPartitionManagerTestCase {

    public ManagedAttributeTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Test
    public void testIdentityTypeAttributeToProperty() throws Exception {
        ManagedAttributeTestCase.CustomUser newUser = new ManagedAttributeTestCase.CustomUser("jduke");

        newUser.setEmail("jduke@jboss.org");
        newUser.setFirstName("Java");
        newUser.setLastName("Duke");

        IdentityManager identityManager = getIdentityManager();

        identityManager.add(newUser);

        IdentityQueryBuilder queryBuilder = identityManager.getQueryBuilder();
        ManagedAttributeTestCase.CustomUser storedUser = queryBuilder
                .createIdentityQuery(newUser.getClass())
                .where(queryBuilder.equal(ManagedAttributeTestCase.CustomUser.LOGIN_NAME, newUser.getLoginName()))
                .getResultList().get(0);

        storedUser.setProfileUrl("http://picketlink.org/me");

        identityManager.update(storedUser);

        storedUser = queryBuilder
                .createIdentityQuery(newUser.getClass())
                .where(queryBuilder.equal(ManagedAttributeTestCase.CustomUser.LOGIN_NAME, newUser.getLoginName()))
                .getResultList().get(0);

        assertEquals("http://picketlink.org/me", storedUser.getProfileUrl());
        assertEquals("http://picketlink.org/me", storedUser.getAttribute("profileUrl").getValue());

        storedUser.setProfileUrl(null);

        identityManager.update(storedUser);

        storedUser = queryBuilder
                .createIdentityQuery(newUser.getClass())
                .where(queryBuilder.equal(ManagedAttributeTestCase.CustomUser.LOGIN_NAME, newUser.getLoginName()))
                .getResultList().get(0);

        assertNull(storedUser.getProfileUrl());
        assertNull(storedUser.getAttribute("profileUrl"));

        storedUser.setLoginAttempts(2);

        identityManager.update(storedUser);

        storedUser = queryBuilder
                .createIdentityQuery(newUser.getClass())
                .where(queryBuilder.equal(ManagedAttributeTestCase.CustomUser.LOGIN_NAME, newUser.getLoginName()))
                .getResultList().get(0);

        assertEquals(2, storedUser.getLoginAttempts());
        assertNotNull(storedUser.getAttribute("loginAttempts"));

        storedUser.setLoginFailedRatio(new BigDecimal(3));

        identityManager.update(storedUser);

        storedUser = queryBuilder
                .createIdentityQuery(newUser.getClass())
                .where(queryBuilder.equal(ManagedAttributeTestCase.CustomUser.LOGIN_NAME, newUser.getLoginName()))
                .getResultList().get(0);

        assertEquals(BigDecimal.valueOf(3), storedUser.getLoginFailedRatio());
        assertNotNull(storedUser.getAttribute("loginFailedRatio"));
    }

    @Test
    public void testPartitionAttributeToProperty() throws Exception {
        CustomPartition partition = new CustomPartition("test");

        partition.setEnforceSsl(true);

        PartitionManager partitionManager = getPartitionManager();

        partitionManager.add(partition);

        CustomPartition storedPartition = partitionManager.lookupById(partition.getClass(), partition.getId());

        assertEquals(true, storedPartition.isEnforceSsl());
        assertEquals(true, storedPartition.getAttribute("enforceSsl").getValue());

        Date expectedDate = Calendar.getInstance().getTime();

        storedPartition.setExpirePasswordsDate(expectedDate);

        partitionManager.update(storedPartition);

        assertEquals(expectedDate, storedPartition.getExpirePasswordsDate());
        assertNotNull(storedPartition.getAttribute("expirePasswordsDate"));
    }

    @Test
    public void testRelationshipAttributeToProperty() throws Exception {
        IdentityManager identityManager = getPartitionManager().createIdentityManager();

        CustomUser user = new CustomUser("user");

        identityManager.add(user);

        CustomApplication application = new CustomApplication("sales");

        identityManager.add(application);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.add(new CustomRelationShip(user, application));

        RelationshipQuery<CustomRelationShip> query = relationshipManager.createRelationshipQuery(CustomRelationShip.class);

        query.setParameter(CustomRelationShip.USER, user);
        query.setParameter(CustomRelationShip.APPLICATION, application);

        List<CustomRelationShip> result = query.getResultList();

        assertFalse(result.isEmpty());

        CustomRelationShip customRelationShip = result.get(0);

        customRelationShip.setToken("123");

        relationshipManager.update(customRelationShip);

        query = relationshipManager.createRelationshipQuery(CustomRelationShip.class);

        query.setParameter(CustomRelationShip.USER, user);
        query.setParameter(CustomRelationShip.APPLICATION, application);

        result = query.getResultList();
        customRelationShip = result.get(0);

        assertEquals("123", customRelationShip.getToken());
        assertEquals("123", customRelationShip.getAttribute("token").getValue());
    }

    public static class CustomUser extends User {

        @AttributeProperty (managed = true)
        private String profileUrl;

        @AttributeProperty(managed = true)
        private int loginAttempts;

        @AttributeProperty(managed = true)
        private BigDecimal loginFailedRatio;

        private CustomUser() {
            super();
        }

        public CustomUser(String loginName) {
            super(loginName);
        }

        public String getProfileUrl() {
            return this.profileUrl;
        }

        public void setProfileUrl(String profileUrl) {
            this.profileUrl = profileUrl;
        }

        public int getLoginAttempts() {
            return this.loginAttempts;
        }

        public void setLoginAttempts(int loginAttempts) {
            this.loginAttempts = loginAttempts;
        }

        public BigDecimal getLoginFailedRatio() {
            return this.loginFailedRatio;
        }

        public void setLoginFailedRatio(BigDecimal loginFailedRatio) {
            this.loginFailedRatio = loginFailedRatio;
        }


    }

    public static class CustomPartition extends AbstractPartition {

        @AttributeProperty(managed = true)
        private boolean enforceSsl;

        @AttributeProperty(managed = true)
        private Date expirePasswordsDate;

        private CustomPartition() {
            this(null);
        }

        public CustomPartition(String name) {
            super(name);
        }

        public boolean isEnforceSsl() {
            return this.enforceSsl;
        }

        public void setEnforceSsl(boolean enforceSsl) {
            this.enforceSsl = enforceSsl;
        }

        public Date getExpirePasswordsDate() {
            return this.expirePasswordsDate;
        }

        public void setExpirePasswordsDate(Date expirePasswordsDate) {
            this.expirePasswordsDate = expirePasswordsDate;
        }
    }

    public static class CustomApplication extends AbstractIdentityType {

        @AttributeProperty(managed = true)
        private String name;

        private CustomApplication() {
            this(null);
        }

        public CustomApplication(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class CustomRelationShip extends AbstractAttributedType implements Relationship {

        public static final RelationshipQueryParameter USER = RELATIONSHIP_QUERY_ATTRIBUTE.byName("user");
        public static final RelationshipQueryParameter APPLICATION = RELATIONSHIP_QUERY_ATTRIBUTE.byName("application");

        private ManagedAttributeTestCase.CustomUser user;
        private ManagedAttributeTestCase.CustomApplication application;

        @AttributeProperty(managed = true)
        private String token;

        private CustomRelationShip() {
            this(null, null);
        }

        public CustomRelationShip(CustomUser user, CustomApplication application) {
            this.user = user;
            this.application = application;
        }

        public ManagedAttributeTestCase.CustomUser getUser() {
            return this.user;
        }

        public void setUser(ManagedAttributeTestCase.CustomUser user) {
            this.user = user;
        }

        public ManagedAttributeTestCase.CustomApplication getApplication() {
            return this.application;
        }

        public void setApplication(ManagedAttributeTestCase.CustomApplication application) {
            this.application = application;
        }

        public String getToken() {
            return this.token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}

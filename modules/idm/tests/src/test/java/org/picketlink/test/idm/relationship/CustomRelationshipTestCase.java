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

package org.picketlink.test.idm.relationship;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.AbstractAttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.query.RelationshipQueryParameter;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPUserGroupJPARoleConfigurationTester;
import org.picketlink.test.idm.testers.SingleConfigLDAPJPAStoreConfigurationTester;

import java.util.List;

import static org.junit.Assert.*;

/**
 * <p>
 * Test case for custom {@link Relationship} types.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
@Configuration(include= {JPAStoreConfigurationTester.class, FileStoreConfigurationTester.class,
        SingleConfigLDAPJPAStoreConfigurationTester.class,
        LDAPUserGroupJPARoleConfigurationTester.class})
public class CustomRelationshipTestCase extends AbstractPartitionManagerTestCase {

    public CustomRelationshipTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Test
    public void testCreate() throws Exception {
        CustomRelationship relationship = new CustomRelationship();

        IdentityManager identityManager = getIdentityManager();

        User user = createUser("user");

        relationship.setIdentityTypeA(user);

        Role role = createRole("role");

        relationship.setIdentityTypeB(role);

        Group group = createGroup("group");

        relationship.setIdentityTypeC(group);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.add(relationship);

        RelationshipQuery<CustomRelationship> query = relationshipManager.createRelationshipQuery(CustomRelationship.class);

        query.setParameter(CustomRelationship.IDENTITY_TYPE_A, user);

        List<CustomRelationship> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(relationship.getId(), result.get(0).getId());

        query = relationshipManager.createRelationshipQuery(CustomRelationship.class);

        query.setParameter(CustomRelationship.IDENTITY_TYPE_B, role);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(relationship.getId(), result.get(0).getId());

        query = relationshipManager.createRelationshipQuery(CustomRelationship.class);

        query.setParameter(CustomRelationship.IDENTITY_TYPE_C, group);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(relationship.getId(), result.get(0).getId());

        query = relationshipManager.createRelationshipQuery(CustomRelationship.class);

        query.setParameter(CustomRelationship.IDENTITY_TYPE_A, user);
        query.setParameter(CustomRelationship.IDENTITY_TYPE_B, role);
        query.setParameter(CustomRelationship.IDENTITY_TYPE_C, group);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(relationship.getId(), result.get(0).getId());
    }

    @Test
    public void testFormalAttributes() throws Exception {
        CustomRelationship relationship = new CustomRelationship();

        User user = createUser("user");

        relationship.setIdentityTypeA(user);

        Role role = createRole("role");

        relationship.setIdentityTypeB(role);

        Group group = createGroup("group");

        relationship.setIdentityTypeC(group);

        relationship.setAttributeA("Value for A");
        relationship.setAttributeB(99l);
        relationship.setAttributeC(true);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.add(relationship);

        RelationshipQuery<CustomRelationship> query = relationshipManager.createRelationshipQuery(CustomRelationship.class);

        query.setParameter(CustomRelationship.IDENTITY_TYPE_A, user);

        List<CustomRelationship> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(relationship.getId(), result.get(0).getId());
        assertEquals(relationship.getAttributeA(), result.get(0).getAttributeA());
        assertEquals(relationship.getAttributeB(), result.get(0).getAttributeB());
        assertEquals(relationship.isAttributeC(), result.get(0).isAttributeC());

        relationship.setAttributeA("Changed Value A");
        relationship.setAttributeB(76l);
        relationship.setAttributeC(false);

        relationshipManager.update(relationship);

        query = relationshipManager.createRelationshipQuery(CustomRelationship.class);

        query.setParameter(CustomRelationship.ID, relationship.getId());

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(relationship.getId(), result.get(0).getId());
        assertEquals(relationship.getAttributeA(), result.get(0).getAttributeA());
        assertEquals(relationship.getAttributeB(), result.get(0).getAttributeB());
        assertEquals(relationship.isAttributeC(), result.get(0).isAttributeC());
    }

    public static class CustomRelationship extends AbstractAttributedType implements Relationship {

        private static final long serialVersionUID = 1030652086550754965L;

        public static final RelationshipQueryParameter IDENTITY_TYPE_A = new RelationshipQueryParameter() {

            @Override
            public String getName() {
                return "identityTypeA";
            }
        };

        public static final RelationshipQueryParameter IDENTITY_TYPE_B = new RelationshipQueryParameter() {

            @Override
            public String getName() {
                return "identityTypeB";
            }
        };

        public static final RelationshipQueryParameter IDENTITY_TYPE_C = new RelationshipQueryParameter() {

            @Override
            public String getName() {
                return "identityTypeC";
            }
        };

        private IdentityType identityTypeA;
        private IdentityType identityTypeB;
        private IdentityType identityTypeC;

        @AttributeProperty
        private String attributeA;

        @AttributeProperty
        private Long attributeB;

        @AttributeProperty
        private boolean attributeC;

        public IdentityType getIdentityTypeA() {
            return this.identityTypeA;
        }

        public void setIdentityTypeA(IdentityType identityTypeA) {
            this.identityTypeA = identityTypeA;
        }

        public IdentityType getIdentityTypeB() {
            return this.identityTypeB;
        }

        public void setIdentityTypeB(IdentityType identityTypeB) {
            this.identityTypeB = identityTypeB;
        }

        public IdentityType getIdentityTypeC() {
            return this.identityTypeC;
        }

        public void setIdentityTypeC(IdentityType identityTypeC) {
            this.identityTypeC = identityTypeC;
        }

        public String getAttributeA() {
            return attributeA;
        }

        public void setAttributeA(final String attributeA) {
            this.attributeA = attributeA;
        }

        public Long getAttributeB() {
            return attributeB;
        }

        public void setAttributeB(final Long attributeB) {
            this.attributeB = attributeB;
        }

        public boolean isAttributeC() {
            return attributeC;
        }

        public void setAttributeC(final boolean attributeC) {
            this.attributeC = attributeC;
        }
    }

}

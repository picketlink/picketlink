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
package org.picketlink.test.idm.basic;

import java.util.Date;
import java.util.List;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.AbstractIdentityType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.model.annotation.Unique;
import org.picketlink.idm.model.sample.Realm;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.test.idm.IdentityConfigurationTestVisitor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author pedroigor
 */
public class CustomIdentityTypeTestCase extends AbstractIdentityTypeTestCase {


    public CustomIdentityTypeTestCase(IdentityConfigurationTestVisitor builder) {
        super(builder);
    }

    @Override
    protected IdentityType createIdentityType() {
        return addIdentityType("Custom 1");
    }

    @Override
    protected IdentityType getIdentityType() {
        IdentityQuery<MyCustomIdentityType> query = getIdentityManager().createIdentityQuery(MyCustomIdentityType.class);

        query.setParameter(MyCustomIdentityType.SOME_IDENTIFIER, "Custom 1");

        List<MyCustomIdentityType> result = query.getResultList();

        if (result.isEmpty()) {
            return null;
        }

        return result.get(0);
    }

    @Test
    public void testCreate() throws Exception {
        MyCustomIdentityType customIdentityType = addIdentityType("Custom 2");

        assertNotNull(customIdentityType.getId());

        IdentityQuery<MyCustomIdentityType> query = getIdentityManager().createIdentityQuery(MyCustomIdentityType.class);

        query.setParameter(MyCustomIdentityType.SOME_IDENTIFIER, customIdentityType.getSomeIdentifier());

        List<MyCustomIdentityType> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());

        MyCustomIdentityType storedCustomIdentityType = result.get(0);

        assertNotNull(storedCustomIdentityType);
        assertEquals(customIdentityType.getId(), storedCustomIdentityType.getId());
        assertEquals(customIdentityType.getSomeIdentifier(), storedCustomIdentityType.getSomeIdentifier());
        assertEquals(Realm.DEFAULT_REALM, storedCustomIdentityType.getPartition().getName());
        assertTrue(storedCustomIdentityType.isEnabled());
        assertNull(storedCustomIdentityType.getExpirationDate());
        assertNotNull(storedCustomIdentityType.getCreatedDate());
        assertTrue(new Date().compareTo(storedCustomIdentityType.getCreatedDate()) >= 0);
    }

    @Test
    public void testUpdate() throws Exception {
        MyCustomIdentityType customIdentityType = addIdentityType("Custom 2");

        assertNotNull(customIdentityType.getId());

        customIdentityType.setSomeAttribute("Some Attribute");

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(customIdentityType);

        IdentityQuery<MyCustomIdentityType> query = getIdentityManager().createIdentityQuery(MyCustomIdentityType.class);

        query.setParameter(MyCustomIdentityType.SOME_IDENTIFIER, "Custom 2");

        List<MyCustomIdentityType> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());

        customIdentityType = result.get(0);

        assertNotNull(customIdentityType);
        assertEquals("Some Attribute", customIdentityType.getSomeAttribute());
    }

    public static class MyCustomIdentityType extends AbstractIdentityType {

        public static final QueryParameter SOME_IDENTIFIER = QUERY_ATTRIBUTE.byName("someIdentifier");

        private String someIdentifier;
        private String someAttribute;

        @AttributeProperty
        @Unique
        public String getSomeIdentifier() {
            return this.someIdentifier;
        }

        public void setSomeIdentifier(String someIdentifier) {
            this.someIdentifier = someIdentifier;
        }

        @AttributeProperty
        public String getSomeAttribute() {
            return this.someAttribute;
        }

        public void setSomeAttribute(String someAttribute) {
            this.someAttribute = someAttribute;
        }
    }

    private MyCustomIdentityType addIdentityType(String someIdentifier) {
        MyCustomIdentityType identityType = new MyCustomIdentityType();

        identityType.setSomeIdentifier(someIdentifier);

        IdentityQuery<MyCustomIdentityType> query = getIdentityManager().createIdentityQuery(MyCustomIdentityType.class);

        query.setParameter(MyCustomIdentityType.SOME_IDENTIFIER, someIdentifier);

        List<MyCustomIdentityType> result = query.getResultList();

        if (!result.isEmpty()) {
            getIdentityManager().remove(result.get(0));
        }

        getIdentityManager().add(identityType);

        return identityType;
    }
}

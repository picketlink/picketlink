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

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.AbstractIdentityType;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.model.annotation.Unique;
import org.picketlink.idm.model.sample.Realm;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.test.idm.IgnoreTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author pedroigor
 */
@IgnoreTester(LDAPStoreConfigurationTester.class)
public class CustomAccountTestCase extends AbstractIdentityTypeTestCase<CustomAccountTestCase.MyCustomAccount> {

    public static final String LOGIN_NAME = "bob";

    public CustomAccountTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Test
    public void testCreate() throws Exception {
        MyCustomAccount customIdentityType = createIdentityType();

        assertNotNull(customIdentityType.getId());

        IdentityQuery<MyCustomAccount> query = getIdentityManager().createIdentityQuery(MyCustomAccount.class);

        query.setParameter(MyCustomAccount.LOGIN_NAME, LOGIN_NAME);

        List<MyCustomAccount> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());

        MyCustomAccount storedCustomIdentityType = result.get(0);

        assertNotNull(storedCustomIdentityType);
        assertEquals(customIdentityType.getId(), storedCustomIdentityType.getId());
        assertEquals(customIdentityType.getLoginName(), storedCustomIdentityType.getLoginName());
        assertEquals(Realm.DEFAULT_REALM, storedCustomIdentityType.getPartition().getName());
        assertTrue(storedCustomIdentityType.isEnabled());
        assertNull(storedCustomIdentityType.getExpirationDate());
        assertNotNull(storedCustomIdentityType.getCreatedDate());
        assertTrue(new Date().compareTo(storedCustomIdentityType.getCreatedDate()) >= 0);
    }

    @Test
    public void testUpdate() throws Exception {
        MyCustomAccount customIdentityType = createIdentityType();

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(customIdentityType);

        IdentityQuery<MyCustomAccount> query = getIdentityManager().createIdentityQuery(MyCustomAccount.class);

        query.setParameter(IdentityType.ID, customIdentityType.getId());

        List<MyCustomAccount> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    public void testUpdateFormalAttributes() throws Exception {
        MyCustomAccount customIdentityType = createIdentityType();

        IdentityManager identityManager = getIdentityManager();

        customIdentityType.setLoginAttempts(10);

        identityManager.update(customIdentityType);

        IdentityQuery<MyCustomAccount> query = getIdentityManager().createIdentityQuery(MyCustomAccount.class);

        query.setParameter(IdentityType.ID, customIdentityType.getId());

        List<MyCustomAccount> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(10), result.get(0).getLoginAttempts());
    }

    @Override
    protected MyCustomAccount createIdentityType() {
        MyCustomAccount identityType = new MyCustomAccount();

        identityType.setLoginName(LOGIN_NAME);

        IdentityQuery<MyCustomAccount> query = getIdentityManager().createIdentityQuery(MyCustomAccount.class);

        query.setParameter(MyCustomAccount.LOGIN_NAME, LOGIN_NAME);

        List<MyCustomAccount> result = query.getResultList();

        if (!result.isEmpty()) {
            getIdentityManager().remove(result.get(0));
        }

        getIdentityManager().add(identityType);

        return identityType;
    }

    @Override
    protected MyCustomAccount getIdentityType() {
        IdentityQuery<MyCustomAccount> query = getIdentityManager().createIdentityQuery(MyCustomAccount.class);

        query.setParameter(MyCustomAccount.LOGIN_NAME, LOGIN_NAME);

        List<MyCustomAccount> result = query.getResultList();

        if (result.isEmpty()) {
            return null;
        }

        return result.get(0);
    }

    public static class MyCustomAccount extends AbstractIdentityType implements Account {

        public static final QueryParameter LOGIN_NAME = QUERY_ATTRIBUTE.byName("loginName");

        private String loginName;

        private Integer loginAttempts;

        @AttributeProperty
        @Unique
        public String getLoginName() {
            return loginName;
        }

        public void setLoginName(String loginName) {
            this.loginName = loginName;
        }

        @AttributeProperty
        public Integer getLoginAttempts() {
            return this.loginAttempts;
        }

        public void setLoginAttempts(Integer loginAttempts) {
            this.loginAttempts = loginAttempts;
        }
    }

}

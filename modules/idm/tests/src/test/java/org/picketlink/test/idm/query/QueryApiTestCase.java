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
package org.picketlink.test.idm.query;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.Condition;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.IdentityQueryBuilder;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;

import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * @author Pedro Igor
 */
@Configuration(include = {JPAStoreConfigurationTester.class, FileStoreConfigurationTester.class})
public class QueryApiTestCase extends AbstractPartitionManagerTestCase {

    public QueryApiTestCase(IdentityConfigurationTester visitor) {
        super(visitor);
    }

    @Test
    public void testEqualCondition() {
        User john = createUser("john");

        IdentityManager identityManager = getIdentityManager();
        IdentityQueryBuilder builder = identityManager.getQueryBuilder();
        Condition condition = builder.equal(User.LOGIN_NAME, "john");

        IdentityQuery query = builder
            .createIdentityQuery(User.class)
            .where(condition);

        List<User> result = query.getResultList();

        assertEquals(1, result.size());
        assertEquals(john, result.get(0));

        john.setAttribute(new Attribute<Integer>("loginAttempts", 1));

        identityManager.update(john);

        query = builder
            .createIdentityQuery(User.class)
            .where(builder.equal(AttributedType.QUERY_ATTRIBUTE.byName("loginAttempts"), 1));

        result = query.getResultList();
        assertEquals(1, result.size());
        assertEquals(john, result.get(0));

        query = builder
            .createIdentityQuery(User.class)
            .where(builder.equal(AttributedType.QUERY_ATTRIBUTE.byName("loginAttempts"), 2));

        result = query.getResultList();
        assertEquals(0, result.size());
    }

    @Test
    public void testGreaterThanCondition() {
        User john = createUser("john");
        User mary = createUser("mary");

        IdentityManager identityManager = getIdentityManager();
        IdentityQueryBuilder builder = identityManager.getQueryBuilder();
        IdentityQuery query = builder
            .createIdentityQuery(User.class)
            .where(builder.greaterThan(User.LOGIN_NAME, "john"));

        List<User> result = query.getResultList();

        assertEquals(1, result.size());
        assertEquals(mary, result.get(0));

        query = builder
            .createIdentityQuery(User.class)
            .where(builder.lessThan(User.LOGIN_NAME, "mary"));

        result = query.getResultList();

        assertEquals(1, result.size());
        assertEquals(john, result.get(0));

        query = builder
            .createIdentityQuery(User.class)
            .where(builder.lessThan(User.LOGIN_NAME, "john"));

        result = query.getResultList();

        assertEquals(0, result.size());

        john.setAttribute(new Attribute<Integer>("loginAttempts", 1));

        identityManager.update(john);

        query = builder
            .createIdentityQuery(User.class)
            .where(builder.equal(AttributedType.QUERY_ATTRIBUTE.byName("loginAttempts"), 1));

        result = query.getResultList();
        assertEquals(1, result.size());
        assertEquals(john, result.get(0));

        mary.setAttribute(new Attribute<Integer>("loginAttempts", 5));

        identityManager.update(mary);

        query = builder
            .createIdentityQuery(User.class)
            .where(builder.greaterThanOrEqualTo(AttributedType.QUERY_ATTRIBUTE.byName("loginAttempts"), 1));

        result = query.getResultList();
        assertEquals(2, result.size());

        query = builder
            .createIdentityQuery(User.class)
            .where(builder.greaterThanOrEqualTo(AttributedType.QUERY_ATTRIBUTE.byName("loginAttempts"), 2));

        result = query.getResultList();
        assertEquals(1, result.size());
        assertEquals(mary, result.get(0));
    }

    @Test
    public void testLessThanCondition() {
        User john = createUser("john");
        User mary = createUser("mary");

        IdentityManager identityManager = getIdentityManager();
        IdentityQueryBuilder builder = identityManager.getQueryBuilder();
        IdentityQuery query = builder
            .createIdentityQuery(User.class)
            .where(builder.lessThan(User.LOGIN_NAME, "mary"));

        List<User> result = query.getResultList();

        assertEquals(1, result.size());
        assertEquals(john, result.get(0));

        query = builder
            .createIdentityQuery(User.class)
            .where(builder.lessThan(User.LOGIN_NAME, "john"));

        result = query.getResultList();

        assertEquals(0, result.size());

        john.setAttribute(new Attribute<Integer>("loginAttempts", 1));

        identityManager.update(john);

        mary.setAttribute(new Attribute<Integer>("loginAttempts", 5));

        identityManager.update(mary);

        query = builder
            .createIdentityQuery(User.class)
            .where(builder.lessThanOrEqualTo(AttributedType.QUERY_ATTRIBUTE.byName("loginAttempts"), 5));

        result = query.getResultList();
        assertEquals(2, result.size());

        query = builder
            .createIdentityQuery(User.class)
            .where(builder.lessThan(AttributedType.QUERY_ATTRIBUTE.byName("loginAttempts"), 2));

        result = query.getResultList();
        assertEquals(1, result.size());
        assertEquals(john, result.get(0));
    }

    @Test
    public void testBetweenCondition() {
        User john = createUser("john");
        User mary = createUser("mary");

        IdentityManager identityManager = getIdentityManager();

        IdentityQueryBuilder builder = identityManager.getQueryBuilder();
        IdentityQuery<User> query = builder
            .createIdentityQuery(User.class)
            .where(builder.between(AttributedType.QUERY_ATTRIBUTE.byName("loginAttempts"), 1, 5));

        List<User> result = query.getResultList();

        assertEquals(0, result.size());

        john.setAttribute(new Attribute<Integer>("loginAttempts", 1));

        identityManager.update(john);

        mary.setAttribute(new Attribute<Integer>("loginAttempts", 5));

        identityManager.update(mary);

        result = query.getResultList();

        assertEquals(2, result.size());

        query = builder
            .createIdentityQuery(User.class)
            .where(builder.between(AttributedType.QUERY_ATTRIBUTE.byName("loginAttempts"), 3, 6));

        result = query.getResultList();
        assertEquals(1, result.size());
    }

    @Test
    public void testInCondition() {
        User john = createUser("john");
        IdentityManager identityManager = getIdentityManager();
        IdentityQueryBuilder queryBuilder = identityManager.getQueryBuilder();

        john.setAttribute(new Attribute<String>("someAttribute2", "someAttributeValue2"));

        identityManager.update(john);

        IdentityQuery query = queryBuilder.createIdentityQuery(john.getClass());

        query.where(queryBuilder.in(IdentityType.QUERY_ATTRIBUTE.byName("someAttribute2"), "someAttributeValue2"));

        List result = query.getResultList();

        assertEquals(1, result.size());
        assertTrue(contains(result, john.getId()));

        query = queryBuilder.createIdentityQuery(john.getClass());

        query.where(queryBuilder.in(IdentityType.QUERY_ATTRIBUTE.byName("someAttribute2"), "someAttributeValue23"));

        result = query.getResultList();

        assertEquals(0, result.size());

        query = queryBuilder.createIdentityQuery(john.getClass());

        john.setAttribute(new Attribute<String>("anotherAttribute", "anotherAttributeValue"));

        identityManager.update(john);

        query.where(queryBuilder.in(IdentityType.QUERY_ATTRIBUTE.byName("anotherAttribute"), "someAttributeValue2"));

        result = query.getResultList();

        assertEquals(0, result.size());

        query = queryBuilder.createIdentityQuery(john.getClass());

        query.where(queryBuilder.in(IdentityType.QUERY_ATTRIBUTE.byName("anotherAttribute"), "anotherAttributeValue"));
        query.where(queryBuilder.in(IdentityType.QUERY_ATTRIBUTE.byName("someAttribute2"), "someAttributeValue2"));

        result = query.getResultList();

        assertEquals(1, result.size());

        john.setAttribute(new Attribute<String[]>("multiValuedAttribute", new String[] {"value1", "value2", "value3"}));

        identityManager.update(john);

        query = queryBuilder.createIdentityQuery(john.getClass());

        query.where(queryBuilder.in(IdentityType.QUERY_ATTRIBUTE.byName("multiValuedAttribute"), "value2"));

        result = query.getResultList();

        assertEquals(1, result.size());

        query.where(queryBuilder.in(IdentityType.QUERY_ATTRIBUTE.byName("multiValuedAttribute"), "value2", "value3"));

        result = query.getResultList();

        assertEquals(1, result.size());

        query.where(queryBuilder.in(IdentityType.QUERY_ATTRIBUTE.byName("multiValuedAttribute"), "value2", "value4"));

        result = query.getResultList();

        assertEquals(0, result.size());

        john.setEmail("john@picketlink.org");

        identityManager.update(john);

        query = queryBuilder.createIdentityQuery(john.getClass());

        query.where(queryBuilder.in(User.EMAIL, john.getEmail()));

        result = query.getResultList();

        assertEquals(1, result.size());

        query.where(queryBuilder.in(User.LOGIN_NAME, john.getLoginName()));

        result = query.getResultList();

        assertEquals(1, result.size());
    }

    @Test
    public void testSorting() {
        User john = createUser("john");
        User mary = createUser("mary");
        User manuela = createUser("manuela");
        User zack = createUser("zack");

        IdentityManager identityManager = getIdentityManager();

        IdentityQueryBuilder builder = identityManager.getQueryBuilder();
        IdentityQuery<User> query = builder
            .createIdentityQuery(User.class)
            .sortBy(builder.asc(User.LOGIN_NAME));

        List<User> result = query.getResultList();

        assertEquals(4, result.size());
        assertEquals(john.getId(), result.get(0).getId());
        assertEquals(manuela.getId(), result.get(1).getId());
        assertEquals(mary.getId(), result.get(2).getId());
        assertEquals(zack.getId(), result.get(3).getId());

        query = builder
            .createIdentityQuery(User.class)
            .sortBy(builder.desc(User.LOGIN_NAME));

        result = query.getResultList();

        assertEquals(4, result.size());
        assertEquals(john.getId(), result.get(3).getId());
        assertEquals(manuela.getId(), result.get(2).getId());
        assertEquals(mary.getId(), result.get(1).getId());
        assertEquals(zack.getId(), result.get(0).getId());

        identityManager.remove(john);
        identityManager.remove(manuela);
        identityManager.remove(mary);
        identityManager.remove(zack);

        User johnyb = createUser("johnyb");

        johnyb.setFirstName("John");
        johnyb.setLastName("Bianchi");

        identityManager.update(johnyb);

        User johnyd = createUser("johnyd");

        johnyd.setFirstName("John");
        johnyd.setLastName("Damon");

        identityManager.update(johnyd);

        User anthonyh = createUser("anthonyh");

        anthonyh.setFirstName("Anthony");
        anthonyh.setLastName("Hopkins");

        identityManager.update(anthonyh);

        User travisd = createUser("travisd");

        travisd.setFirstName("Travis");
        travisd.setLastName("Damon");

        identityManager.update(travisd);

        query = builder
            .createIdentityQuery(User.class)
            .sortBy(builder.asc(User.FIRST_NAME), builder.asc(User.LAST_NAME));

        result = query.getResultList();

        assertEquals(4, result.size());
        assertEquals(anthonyh.getId(), result.get(0).getId());
        assertEquals(johnyb.getId(), result.get(1).getId());
        assertEquals(johnyd.getId(), result.get(2).getId());
        assertEquals(travisd.getId(), result.get(3).getId());

        query = builder
            .createIdentityQuery(User.class)
            .sortBy(builder.asc(User.FIRST_NAME), builder.desc(User.LAST_NAME));

        result = query.getResultList();

        assertEquals(4, result.size());
        assertEquals(anthonyh.getId(), result.get(0).getId());
        assertEquals(johnyd.getId(), result.get(1).getId());
        assertEquals(johnyb.getId(), result.get(2).getId());
        assertEquals(travisd.getId(), result.get(3).getId());

        query = builder
            .createIdentityQuery(User.class)
            .where(builder.like(User.FIRST_NAME, "%J%"))
            .sortBy(builder.desc(User.FIRST_NAME), builder.desc(User.LAST_NAME));

        result = query.getResultList();

        assertEquals(2, result.size());
        assertEquals(johnyd.getId(), result.get(0).getId());
        assertEquals(johnyb.getId(), result.get(1).getId());
    }

    protected boolean contains(List<IdentityType> result, String id) {
        for (IdentityType identityType : result) {
            if (identityType.getId().equals(id)) {
                return true;
            }
        }

        return false;
    }

}

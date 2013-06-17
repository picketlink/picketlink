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

package org.picketlink.idm.jpa.internal;

import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import org.picketlink.idm.config.JPAIdentityStoreConfigurationOld;
import org.picketlink.idm.config.JPAIdentityStoreConfigurationOld.PropertyType;
import org.picketlink.idm.model.sample.User;
import org.picketlink.idm.spi.SecurityContext;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class UserHandler extends IdentityTypeHandler<User>{

    public UserHandler() {
        getSortParametersMapping().put(User.LOGIN_NAME, PropertyType.AGENT_LOGIN_NAME);
        getSortParametersMapping().put(User.FIRST_NAME, PropertyType.USER_FIRST_NAME);
        getSortParametersMapping().put(User.LAST_NAME,  PropertyType.USER_LAST_NAME);
        getSortParametersMapping().put(User.EMAIL, PropertyType.USER_EMAIL);
    }

    @Override
    protected void doPopulateIdentityInstance(SecurityContext context, Object toIdentity, User fromUser, JPAIdentityStore store) {
        JPAIdentityStoreConfigurationOld jpaConfig = store.getConfig();

        jpaConfig.setModelPropertyValue(toIdentity, PropertyType.IDENTITY_PARTITION, store.lookupAndCreatePartitionObject(
                context, context.getPartition()), true);
        jpaConfig.setModelPropertyValue(toIdentity, PropertyType.IDENTITY_ID, fromUser.getId(), true);
        jpaConfig.setModelPropertyValue(toIdentity, PropertyType.AGENT_LOGIN_NAME, fromUser.getLoginName(), true);
        jpaConfig.setModelPropertyValue(toIdentity, PropertyType.USER_FIRST_NAME, fromUser.getFirstName());
        jpaConfig.setModelPropertyValue(toIdentity, PropertyType.USER_LAST_NAME, fromUser.getLastName());
        jpaConfig.setModelPropertyValue(toIdentity, PropertyType.USER_EMAIL, fromUser.getEmail());
    }

    @Override
    public List<Predicate> getPredicate(SecurityContext context, JPACriteriaQueryBuilder criteria, JPAIdentityStore store) {
        List<Predicate> predicates = super.getPredicate(context, criteria, store);
        CriteriaBuilder builder = criteria.getBuilder();
        JPAIdentityStoreConfigurationOld jpaConfig = store.getConfig();

        Object[] parameterValues = criteria.getIdentityQuery().getParameter(User.LOGIN_NAME);

        if (parameterValues != null) {
            predicates.add(builder.equal(
                    criteria.getRoot().get(jpaConfig.getModelProperty(PropertyType.AGENT_LOGIN_NAME).getName()),
                    parameterValues[0]));
        }

        parameterValues = criteria.getIdentityQuery().getParameter(User.FIRST_NAME);

        if (parameterValues != null) {
            predicates.add(builder.equal(
                    criteria.getRoot().get(jpaConfig.getModelProperty(PropertyType.USER_FIRST_NAME).getName()),
                    parameterValues[0]));
        }

        parameterValues = criteria.getIdentityQuery().getParameter(User.LAST_NAME);

        if (parameterValues != null) {
            predicates.add(builder.equal(
                    criteria.getRoot().get(jpaConfig.getModelProperty(PropertyType.USER_LAST_NAME).getName()),
                    parameterValues[0]));
        }

        parameterValues = criteria.getIdentityQuery().getParameter(User.EMAIL);

        if (parameterValues != null) {
            predicates.add(builder.equal(
                    criteria.getRoot().get(jpaConfig.getModelProperty(PropertyType.USER_EMAIL).getName()),
                    parameterValues[0]));
        }

        return predicates;
    }

    @Override
    protected User doCreateIdentityType(SecurityContext context, Object identity, JPAIdentityStore store) {
        JPAIdentityStoreConfigurationOld jpaConfig = store.getConfig();

        String loginName = jpaConfig.getModelProperty(PropertyType.AGENT_LOGIN_NAME).getValue(identity).toString();

        User user = new User(loginName);

        user.setFirstName(jpaConfig.getModelPropertyValue(String.class, identity, PropertyType.USER_FIRST_NAME));
        user.setLastName(jpaConfig.getModelPropertyValue(String.class, identity, PropertyType.USER_LAST_NAME));
        user.setEmail(jpaConfig.getModelPropertyValue(String.class, identity, PropertyType.USER_EMAIL));

        return user;
    }
}

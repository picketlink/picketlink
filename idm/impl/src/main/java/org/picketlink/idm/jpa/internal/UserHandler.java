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

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.event.AbstractBaseEvent;
import org.picketlink.idm.event.UserCreatedEvent;
import org.picketlink.idm.event.UserDeletedEvent;
import org.picketlink.idm.event.UserUpdatedEvent;
import org.picketlink.idm.jpa.annotations.PropertyType;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.QueryParameter;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class UserHandler extends IdentityTypeHandler<User>{

    public UserHandler(JPAIdentityStoreConfiguration config) {
        super(config);

        getSortParametersMapping().put(User.LOGIN_NAME, PropertyType.AGENT_LOGIN_NAME);
        getSortParametersMapping().put(User.FIRST_NAME, PropertyType.USER_FIRST_NAME);
        getSortParametersMapping().put(User.LAST_NAME,  PropertyType.USER_LAST_NAME);
        getSortParametersMapping().put(User.EMAIL, PropertyType.USER_EMAIL);
    }

    @Override
    protected void doPopulateIdentityInstance(Object toIdentity, User fromUser, JPAIdentityStore store) {
        getConfig().setModelPropertyValue(toIdentity, PropertyType.IDENTITY_PARTITION, store.lookupPartitionObject(store.getCurrentRealm()), true);
        getConfig().setModelPropertyValue(toIdentity, PropertyType.IDENTITY_ID, fromUser.getId(), true);
        getConfig().setModelPropertyValue(toIdentity, PropertyType.AGENT_LOGIN_NAME, fromUser.getLoginName(), true);
        getConfig().setModelPropertyValue(toIdentity, PropertyType.USER_FIRST_NAME, fromUser.getFirstName());
        getConfig().setModelPropertyValue(toIdentity, PropertyType.USER_LAST_NAME, fromUser.getLastName());
        getConfig().setModelPropertyValue(toIdentity, PropertyType.USER_EMAIL, fromUser.getEmail());
    }

    @Override
    protected AbstractBaseEvent raiseCreatedEvent(User fromIdentityType) {
        return new UserCreatedEvent(fromIdentityType);
    }

    @Override
    protected AbstractBaseEvent raiseUpdatedEvent(User fromIdentityType) {
        return new UserUpdatedEvent(fromIdentityType);
    }

    @Override
    protected AbstractBaseEvent raiseDeletedEvent(User fromIdentityType) {
        return new UserDeletedEvent(fromIdentityType);
    }
    
    @Override
    public List<Predicate> getPredicate(JPACriteriaQueryBuilder criteria, JPAIdentityStore store) {
        List<Predicate> predicates = super.getPredicate(criteria, store);
        CriteriaBuilder builder = criteria.getBuilder();
        
        Object[] parameterValues = criteria.getIdentityQuery().getParameter(User.LOGIN_NAME);

        if (parameterValues != null) {
            predicates.add(builder.equal(
                    criteria.getRoot().get(getConfig().getModelProperty(PropertyType.AGENT_LOGIN_NAME).getName()),
                    parameterValues[0]));
        }
        
        parameterValues = criteria.getIdentityQuery().getParameter(User.FIRST_NAME);

        if (parameterValues != null) {
            predicates.add(builder.equal(
                    criteria.getRoot().get(getConfig().getModelProperty(PropertyType.USER_FIRST_NAME).getName()),
                    parameterValues[0]));
        }

        parameterValues = criteria.getIdentityQuery().getParameter(User.LAST_NAME);

        if (parameterValues != null) {
            predicates.add(builder.equal(
                    criteria.getRoot().get(getConfig().getModelProperty(PropertyType.USER_LAST_NAME).getName()),
                    parameterValues[0]));
        }

        parameterValues = criteria.getIdentityQuery().getParameter(User.EMAIL);

        if (parameterValues != null) {
            predicates.add(builder.equal(
                    criteria.getRoot().get(getConfig().getModelProperty(PropertyType.USER_EMAIL).getName()),
                    parameterValues[0]));
        }
        
        return predicates;
    }

    @Override
    protected User doCreateIdentityType(Object identity, JPAIdentityStore store) {
        String loginName = getConfig().getModelProperty(PropertyType.AGENT_LOGIN_NAME).getValue(identity).toString();
        
        User user = new SimpleUser(loginName);

        user.setFirstName(getConfig().getModelPropertyValue(String.class, identity, PropertyType.USER_FIRST_NAME));
        user.setLastName(getConfig().getModelPropertyValue(String.class, identity, PropertyType.USER_LAST_NAME));
        user.setEmail(getConfig().getModelPropertyValue(String.class, identity, PropertyType.USER_EMAIL));
        
        return user;
    }
    
    @Override
    public void validate(User user, JPAIdentityStore store) {
        if (user.getLoginName() == null) {
            throw new IdentityManagementException("No login name was provided.");
        }
        
        if (store.getUser(user.getLoginName()) != null) {
            throw new IdentityManagementException("User already exists with the given loginName [" + user.getLoginName() + "] for the given Realm [" + store.getCurrentRealm().getName() + "]");
        }
    }
}

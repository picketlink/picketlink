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

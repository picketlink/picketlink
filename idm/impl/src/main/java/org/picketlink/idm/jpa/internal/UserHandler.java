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
import javax.persistence.criteria.Root;

import org.picketlink.idm.event.AbstractBaseEvent;
import org.picketlink.idm.event.UserCreatedEvent;
import org.picketlink.idm.event.UserDeletedEvent;
import org.picketlink.idm.event.UserUpdatedEvent;
import org.picketlink.idm.jpa.annotations.PropertyType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class UserHandler extends IdentityTypeHandler<User>{

    public UserHandler(JPAIdentityStoreConfiguration config) {
        super(config);
    }

    @Override
    protected void doPopulateIdentityInstance(Object toIdentity, User fromUser, JPAIdentityStore store) {
        setModelPropertyValue(toIdentity, PropertyType.IDENTITY_PARTITION, store.lookupPartitionObject(store.getCurrentRealm()), true);
        setModelPropertyValue(toIdentity, PropertyType.IDENTITY_ID, fromUser.getId(), true);
        setModelPropertyValue(toIdentity, PropertyType.AGENT_LOGIN_NAME, fromUser.getLoginName(), true);
        setModelPropertyValue(toIdentity, PropertyType.USER_FIRST_NAME, fromUser.getFirstName());
        setModelPropertyValue(toIdentity, PropertyType.USER_LAST_NAME, fromUser.getLastName());
        setModelPropertyValue(toIdentity, PropertyType.USER_EMAIL, fromUser.getEmail());
    }

    @Override
    protected AbstractBaseEvent raiseCreatedEvent(User fromIdentityType, JPAIdentityStore store) {
        return new UserCreatedEvent(fromIdentityType);
    }

    @Override
    protected AbstractBaseEvent raiseUpdatedEvent(User fromIdentityType, JPAIdentityStore store) {
        return new UserUpdatedEvent(fromIdentityType);
    }

    @Override
    protected AbstractBaseEvent raiseDeletedEvent(User fromIdentityType, JPAIdentityStore store) {
        return new UserDeletedEvent(fromIdentityType);
    }
    
    @Override
    public List<Predicate> getPredicate(JPACriteriaQueryBuilder criteria, JPAIdentityStore store) {
        List<Predicate> predicates = super.getPredicate(criteria, store);
        CriteriaBuilder builder = criteria.getBuilder();
        Root<?> root = criteria.getRoot();
        
        if (criteria.getIdentityQuery().getParameter(IdentityType.PARTITION) == null) {
            predicates.add(builder.equal(root.get(getConfig().getModelProperty(PropertyType.IDENTITY_PARTITION).getName()),
                    store.lookupPartitionObject(store.getCurrentRealm())));
        }
        
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

        user.setFirstName(getModelPropertyValue(String.class, identity, PropertyType.USER_FIRST_NAME));
        user.setLastName(getModelPropertyValue(String.class, identity, PropertyType.USER_LAST_NAME));
        user.setEmail(getModelPropertyValue(String.class, identity, PropertyType.USER_EMAIL));
        
        return user;
    }

}

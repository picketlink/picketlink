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

import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_ID;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_USER_EMAIL;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_USER_FIRST_NAME;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_USER_LAST_NAME;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.picketlink.idm.event.AbstractBaseEvent;
import org.picketlink.idm.event.UserCreatedEvent;
import org.picketlink.idm.event.UserDeletedEvent;
import org.picketlink.idm.event.UserUpdatedEvent;
import org.picketlink.idm.internal.util.properties.Property;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.QueryParameter;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class UserTypeManager extends IdentityTypeManager<User>{

    public UserTypeManager(JPAIdentityStore store) {
        super(store);
    }

    @Override
    protected void fromIdentityType(Object toIdentity, User fromUser) {
        getStore().setModelProperty(toIdentity, PROPERTY_IDENTITY_ID, fromUser.getId(), true);
        getStore().setModelProperty(toIdentity, PROPERTY_USER_FIRST_NAME, fromUser.getFirstName());
        getStore().setModelProperty(toIdentity, PROPERTY_USER_LAST_NAME, fromUser.getLastName());
        getStore().setModelProperty(toIdentity, PROPERTY_USER_EMAIL, fromUser.getEmail());
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
    protected List<Predicate> getPredicate(QueryParameter queryParameter, Object[] parameterValues,
            JPACriteriaQueryBuilder criteria) {
        List<Predicate> predicates = super.getPredicate(queryParameter, parameterValues, criteria);
        CriteriaBuilder builder = criteria.getBuilder();
        Root<?> root = criteria.getRoot();
        
        if (queryParameter.equals(User.ID)) {
            predicates.add(builder.equal(
                    criteria.getRoot().get(getConfig().getModelProperty(PROPERTY_IDENTITY_ID).getName()),
                    parameterValues[0]));
        }
        
        if (queryParameter.equals(User.FIRST_NAME)) {
            predicates.add(builder.equal(
                    criteria.getRoot().get(getConfig().getModelProperty(PROPERTY_USER_FIRST_NAME).getName()),
                    parameterValues[0]));
        }
        
        if (queryParameter.equals(User.LAST_NAME)) {
            predicates.add(builder.equal(
                    criteria.getRoot().get(getConfig().getModelProperty(PROPERTY_USER_LAST_NAME).getName()),
                    parameterValues[0]));
        }
        
        if (queryParameter.equals(User.EMAIL)) {
            predicates.add(builder.equal(
                    criteria.getRoot().get(getConfig().getModelProperty(PROPERTY_USER_EMAIL).getName()),
                    parameterValues[0]));
        }
        
        if (queryParameter.equals(IdentityType.HAS_GROUP_ROLE)) {
            for (Object object : parameterValues) {
                GroupRole groupRole = (GroupRole) object;

                Property<Object> memberModelProperty = getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_MEMBER);
                Property<Object> roleModelProperty = getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_ROLE);
                Property<Object> groupModelProperty = getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_GROUP);


                Subquery<?> subquery = criteria.getCriteria().subquery(getConfig().getMembershipClass());
                Root fromProject = subquery.from(getConfig().getMembershipClass());
                Subquery<?> select = subquery.select(fromProject.get(memberModelProperty.getName()));

                Predicate conjunction = builder.conjunction();

                conjunction.getExpressions().add(
                        builder.equal(fromProject.get(memberModelProperty.getName()), root));
                
                if (groupRole.getMember() != null) {
                    conjunction.getExpressions().add(
                            builder.equal(fromProject.get(memberModelProperty.getName()), getStore().lookupIdentityObjectById(groupRole.getMember())));
                }
                
                if (groupRole.getRole() != null) {
                    conjunction.getExpressions().add(
                            builder.equal(fromProject.get(roleModelProperty.getName()), getStore().lookupIdentityObjectById(groupRole.getRole())));
                    
                }
                
                if (groupRole.getGroup() != null) {
                    conjunction.getExpressions().add(
                            builder.equal(fromProject.get(groupModelProperty.getName()), getStore().lookupIdentityObjectById(groupRole.getGroup())));
                }

                subquery.where(conjunction);

                predicates.add(builder.in(root).value(subquery));
            }
        }
        
        if (queryParameter.equals(IdentityType.MEMBER_OF)) {
            for (Object object : parameterValues) {
                Property<Object> memberModelProperty = getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_MEMBER);
                Property<Object> groupModelProperty = getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_GROUP);


                Subquery<?> subquery = criteria.getCriteria().subquery(getConfig().getMembershipClass());
                Root fromProject = subquery.from(getConfig().getMembershipClass());
                Subquery<?> select = subquery.select(fromProject.get(memberModelProperty.getName()));

                Predicate conjunction = builder.conjunction();

                conjunction.getExpressions().add(
                        builder.equal(fromProject.get(memberModelProperty.getName()), root));
                conjunction.getExpressions().add(
                        builder.equal(fromProject.get(groupModelProperty.getName()), getStore().lookupIdentityObjectById(new SimpleGroup(object.toString()))));

                subquery.where(conjunction);
                
                predicates.add(builder.in(root).value(subquery));
            }
        }
        
        if (queryParameter.equals(IdentityType.HAS_ROLE)) {
            for (Object object : parameterValues) {
                String roleName = (String) parameterValues[0];

                Property<Object> memberModelProperty = getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_MEMBER);
                Property<Object> roleModelProperty = getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_ROLE);


                Subquery<?> subquery = criteria.getCriteria().subquery(getConfig().getMembershipClass());
                Root fromProject = subquery.from(getConfig().getMembershipClass());
                Subquery<?> select = subquery.select(fromProject.get(memberModelProperty.getName()));

                Predicate conjunction = builder.conjunction();

                conjunction.getExpressions().add(
                        builder.equal(fromProject.get(memberModelProperty.getName()), root));
                conjunction.getExpressions().add(
                        builder.equal(fromProject.get(roleModelProperty.getName()), getStore().lookupIdentityObjectById(new SimpleRole(object.toString()))));

                subquery.where(conjunction);

                predicates.add(builder.in(root).value(subquery));
            }
        }
        
        return predicates;
    }

    @Override
    protected User createIdentityType(Object identity) {
        String idValue = getConfig().getModelProperty(PROPERTY_IDENTITY_ID).getValue(identity).toString();
        
        User user = new SimpleUser(idValue);

        user.setFirstName(getStore().getModelProperty(String.class, identity, PROPERTY_USER_FIRST_NAME));
        user.setLastName(getStore().getModelProperty(String.class, identity, PROPERTY_USER_LAST_NAME));
        user.setEmail(getStore().getModelProperty(String.class, identity, PROPERTY_USER_EMAIL));
        
        return user;
    }

}

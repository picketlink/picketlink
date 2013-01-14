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
import javax.persistence.criteria.Subquery;

import org.picketlink.idm.event.AbstractBaseEvent;
import org.picketlink.idm.event.AgentCreatedEvent;
import org.picketlink.idm.event.AgentDeletedEvent;
import org.picketlink.idm.event.AgentUpdatedEvent;
import org.picketlink.idm.internal.util.properties.Property;
import org.picketlink.idm.jpa.annotations.PropertyType;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.SimpleAgent;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.query.QueryParameter;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class AgentHandler extends IdentityTypeHandler<Agent>{

    public AgentHandler(JPAIdentityStoreConfiguration config) {
        super(config);
    }

    @Override
    protected void doPopulateIdentityInstance(Object toIdentity, Agent fromUser, JPAIdentityStore store) {

    }

    @Override
    protected AbstractBaseEvent raiseCreatedEvent(Agent fromIdentityType, JPAIdentityStore store) {
        return new AgentCreatedEvent(fromIdentityType);
    }

    @Override
    protected AbstractBaseEvent raiseUpdatedEvent(Agent fromIdentityType, JPAIdentityStore store) {
        return new AgentUpdatedEvent(fromIdentityType);
    }

    @Override
    protected AbstractBaseEvent raiseDeletedEvent(Agent fromIdentityType, JPAIdentityStore store) {
        return new AgentDeletedEvent(fromIdentityType);
    }

    @Override
    protected Agent doCreateIdentityType(Object identity, JPAIdentityStore store) {
        String idValue = getConfig().getModelProperty(PropertyType.IDENTITY_ID).getValue(identity).toString();

        Agent agent = new SimpleAgent(idValue);
        
        return agent;
    }
    
    @Override
    public List<Predicate> getPredicate(QueryParameter queryParameter, Object[] parameterValues,
            JPACriteriaQueryBuilder criteria, JPAIdentityStore store) {

        List<Predicate> predicates = super.getPredicate(queryParameter, parameterValues, criteria, store);
        CriteriaBuilder builder = criteria.getBuilder();
        Root<?> root = criteria.getRoot();
        
        
        if (queryParameter.equals(Agent.ID)) {
            predicates.add(builder.equal(
                    criteria.getRoot().get(getConfig().getModelProperty(PropertyType.IDENTITY_ID).getName()),
                    parameterValues[0]));
        }
        
        if (queryParameter.equals(IdentityType.HAS_GROUP_ROLE)) {
            for (Object object : parameterValues) {
                GroupRole groupRole = (GroupRole) object;

                // TODO rewrite this to work with relationships instead
                /**
                Property<Object> memberModelProperty = getConfig().getModelProperty(getConfigJPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_MEMBER);
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
                            builder.equal(fromProject.get(memberModelProperty.getName()), store.lookupIdentityObjectById(groupRole.getMember())));
                }
                
                if (groupRole.getRole() != null) {
                    conjunction.getExpressions().add(
                            builder.equal(fromProject.get(roleModelProperty.getName()), store.lookupIdentityObjectById(groupRole.getRole())));
                    
                }
                
                if (groupRole.getGroup() != null) {
                    conjunction.getExpressions().add(
                            builder.equal(fromProject.get(groupModelProperty.getName()), store.lookupIdentityObjectById(groupRole.getGroup())));
                }

                subquery.where(conjunction);

                predicates.add(builder.in(root).value(subquery));*/
            }
        }
        
        if (queryParameter.equals(IdentityType.MEMBER_OF)) {
            for (Object object : parameterValues) {
                // TODO rewrite this to use relationships
                /*
                Property<Object> memberModelProperty = getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_MEMBER);
                Property<Object> groupModelProperty = storeConfig.getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_GROUP);


                Subquery<?> subquery = criteria.getCriteria().subquery(storeConfig.getMembershipClass());
                Root fromProject = subquery.from(storeConfig.getMembershipClass());
                Subquery<?> select = subquery.select(fromProject.get(memberModelProperty.getName()));

                Predicate conjunction = builder.conjunction();

                conjunction.getExpressions().add(
                        builder.equal(fromProject.get(memberModelProperty.getName()), root));
                conjunction.getExpressions().add(
                        builder.equal(fromProject.get(groupModelProperty.getName()), store.lookupIdentityObjectById(new SimpleGroup(object.toString()))));

                subquery.where(conjunction);
                
                predicates.add(builder.in(root).value(subquery));*/
            }
        }
        
        if (queryParameter.equals(IdentityType.HAS_ROLE)) {
            for (Object object : parameterValues) {
                String roleName = (String) parameterValues[0];
                // TODO rewrite this to use relationships
                /*
                Property<Object> memberModelProperty = storeConfig.getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_MEMBER);
                Property<Object> roleModelProperty = storeConfig.getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_ROLE);


                Subquery<?> subquery = criteria.getCriteria().subquery(storeConfig.getMembershipClass());
                Root fromProject = subquery.from(storeConfig.getMembershipClass());
                Subquery<?> select = subquery.select(fromProject.get(memberModelProperty.getName()));

                Predicate conjunction = builder.conjunction();

                conjunction.getExpressions().add(
                        builder.equal(fromProject.get(memberModelProperty.getName()), root));
                conjunction.getExpressions().add(
                        builder.equal(fromProject.get(roleModelProperty.getName()), store.lookupIdentityObjectById(new SimpleRole(object.toString()))));

                subquery.where(conjunction);

                predicates.add(builder.in(root).value(subquery));*/
            }
        }
        
        return predicates;
    }
    
}

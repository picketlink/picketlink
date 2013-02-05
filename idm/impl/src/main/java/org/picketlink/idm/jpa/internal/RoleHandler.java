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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.event.AbstractBaseEvent;
import org.picketlink.idm.event.RoleCreatedEvent;
import org.picketlink.idm.event.RoleDeletedEvent;
import org.picketlink.idm.event.RoleUpdatedEvent;
import org.picketlink.idm.jpa.annotations.PropertyType;
import org.picketlink.idm.model.Grant;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.query.internal.DefaultRelationshipQuery;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class RoleHandler extends IdentityTypeHandler<Role> {

    public RoleHandler(JPAIdentityStoreConfiguration config) {
        super(config);
    }

    @Override
    protected void doPopulateIdentityInstance(Object toIdentity, Role fromRole, JPAIdentityStore store) {
        getConfig().setModelPropertyValue(toIdentity, PropertyType.IDENTITY_PARTITION,
                store.lookupPartitionObject(store.getCurrentPartition()), true);
        getConfig().setModelPropertyValue(toIdentity, PropertyType.IDENTITY_NAME, fromRole.getName(), true);
    }

    @Override
    protected AbstractBaseEvent raiseCreatedEvent(Role fromIdentityType) {
        return new RoleCreatedEvent(fromIdentityType);
    }

    @Override
    protected AbstractBaseEvent raiseUpdatedEvent(Role fromIdentityType) {
        return new RoleUpdatedEvent(fromIdentityType);
    }

    @Override
    protected AbstractBaseEvent raiseDeletedEvent(Role fromIdentityType) {
        return new RoleDeletedEvent(fromIdentityType);
    }

    @Override
    protected Role doCreateIdentityType(Object identity, JPAIdentityStore store) {
        String name = getConfig().getModelPropertyValue(String.class, identity, PropertyType.IDENTITY_NAME);

        SimpleRole role = new SimpleRole(name);

        return role;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List<Predicate> getPredicate(JPACriteriaQueryBuilder criteria, JPAIdentityStore store) {
        List<Predicate> predicates = super.getPredicate(criteria, store);

        Object[] parameterValues = criteria.getIdentityQuery().getParameter(Role.NAME);

        if (parameterValues != null) {
            predicates.add(criteria.getBuilder().equal(
                    criteria.getRoot().get(getConfig().getModelProperty(PropertyType.IDENTITY_NAME).getName()),
                    parameterValues[0]));
        }
        
        parameterValues = criteria.getIdentityQuery().getParameter(Role.ROLE_OF);

        if (parameterValues != null) {
            for (Object object : parameterValues) {
                DefaultRelationshipQuery<Grant> query = new DefaultRelationshipQuery<Grant>(Grant.class, store);

                query.setParameter(Grant.ASSIGNEE, object);

                List<Grant> resultList = query.getResultList();

                if (!resultList.isEmpty()) {
                    List<String> relIds = new ArrayList<String>();

                    for (Grant grant : resultList) {
                        relIds.add(grant.getId());
                    }

                    Subquery<?> subquery = criteria.getCriteria().subquery(store.getConfig().getRelationshipIdentityClass());
                    Root fromProject = subquery.from(store.getConfig().getRelationshipIdentityClass());
                    subquery.select(fromProject.get(getConfig().getModelProperty(
                            PropertyType.RELATIONSHIP_IDENTITY).getName()));
                    Join<Object, Object> join = fromProject.join(getConfig().getModelProperty(
                            PropertyType.RELATIONSHIP_IDENTITY_RELATIONSHIP).getName());

                    List<Predicate> subqueryPredicates = new ArrayList<Predicate>();

                    subqueryPredicates.add(criteria.getBuilder().equal(
                            fromProject.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_DESCRIPTOR).getName()),
                            Grant.ROLE.getName()));
                    subqueryPredicates.add(criteria.getBuilder().equal(
                            fromProject.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY).getName()),
                            criteria.getRoot()));
                    subqueryPredicates.add(criteria.getBuilder()
                            .in(join.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_ID).getName())).value(relIds));

                    subquery.where(subqueryPredicates.toArray(new Predicate[subqueryPredicates.size()]));

                    predicates.add(criteria.getBuilder().in(criteria.getRoot()).value(subquery));
                } else {
                    predicates.add(criteria.getBuilder().equal(
                            criteria.getRoot().get(getConfig().getModelProperty(PropertyType.IDENTITY_ID).getName()), "-1"));
                }
            }
        }

        return predicates;
    }


    
    @Override
    public void validate(Role role, JPAIdentityStore store) {
        if (role.getName() == null) {
            throw new IdentityManagementException("No name was provided.");
        }
        
        if (store.getRole(role.getName()) != null) {
            throw new IdentityManagementException("Role already exists with the given loginName [" + role.getName() + "] for the given Partition [" + store.getCurrentPartition().getName() + "]");
        }
    }
}

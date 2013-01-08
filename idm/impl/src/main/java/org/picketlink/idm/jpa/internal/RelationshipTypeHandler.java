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

import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_DISCRIMINATOR;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_NAME;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_RELATED_TO;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_RELATES_TO;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;

import org.picketlink.idm.event.AbstractBaseEvent;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.SimpleRelationship;
import org.picketlink.idm.query.QueryParameter;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class RelationshipTypeHandler extends IdentityTypeHandler<Relationship> {

    @Override
    protected void doPopulateIdentityInstance(Object toIdentity, Relationship fromRelationship, JPAIdentityStore store) {
        store.setModelProperty(toIdentity, PROPERTY_IDENTITY_NAME, fromRelationship.getName(), true);

        Object relatedFromIdentity = store.lookupIdentityObjectById(fromRelationship.from());
        
        store.setModelProperty(toIdentity, JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_RELATED_TO, relatedFromIdentity, true);
        
        Object relatesToIdentity = store.lookupIdentityObjectById(fromRelationship.to());
        
        store.setModelProperty(toIdentity, JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_RELATES_TO, relatesToIdentity, true);
    }
    
    @Override
    void remove(Object identity, Relationship identityType, JPAIdentityStore store) {
    }

    @Override
    protected AbstractBaseEvent raiseCreatedEvent(Relationship fromIdentityType, JPAIdentityStore store) {
        return new AbstractBaseEvent() {
        };
    }

    @Override
    protected AbstractBaseEvent raiseUpdatedEvent(Relationship fromIdentityType, JPAIdentityStore store) {
        return new AbstractBaseEvent() {
        };
    }

    @Override
    protected AbstractBaseEvent raiseDeletedEvent(Relationship fromIdentityType, JPAIdentityStore store) {
        return new AbstractBaseEvent() {
        };
    }
    
    @Override
    protected Relationship doCreateIdentityType(Object identity, JPAIdentityStore store) {
        String name = store.getModelProperty(String.class, identity, PROPERTY_IDENTITY_NAME);

        Object relatesToInstance = store.getModelProperty(Object.class, identity, PROPERTY_IDENTITY_RELATES_TO);
        
        String relatesToDiscriminator = store.getConfig().getModelProperty(PROPERTY_IDENTITY_DISCRIMINATOR).getValue(relatesToInstance)
                .toString();
        IdentityTypeHandler<? extends IdentityType> relatesToIdentityTypeManager = store.getConfig().getIdentityTypeManager(relatesToDiscriminator);
        
        IdentityType relatesToType = relatesToIdentityTypeManager.createIdentityType(null, relatesToInstance, store);
        
        Object relatedToInstance = store.getModelProperty(Object.class, identity, PROPERTY_IDENTITY_RELATED_TO);

        String relatedToDiscriminator = store.getConfig().getModelProperty(PROPERTY_IDENTITY_DISCRIMINATOR).getValue(relatesToInstance)
                .toString();
        IdentityTypeHandler<? extends IdentityType> relatedToTypeManager = store.getConfig().getIdentityTypeManager(relatedToDiscriminator);
        
        IdentityType relatedToType = relatedToTypeManager.createIdentityType(null, relatedToInstance, store);

        SimpleRelationship relationship = new SimpleRelationship(name);
        
        relationship.setFrom(relatedToType);
        relationship.setTo(relatesToType);
        
        return relationship;
    }
    
    @Override
    public List<Predicate> getPredicate(QueryParameter queryParameter, Object[] parameterValues,
            JPACriteriaQueryBuilder criteria, JPAIdentityStore store) {
        List<Predicate> predicates = super.getPredicate(queryParameter, parameterValues, criteria, store);
        CriteriaBuilder builder = criteria.getBuilder();
        
        if (queryParameter.equals(Relationship.NAME)) {
            predicates.add(builder.equal(
                    criteria.getRoot().get(store.getConfig().getModelProperty(PROPERTY_IDENTITY_NAME).getName()),
                    parameterValues[0]));
        }
        
        if (queryParameter.equals(Relationship.TO)) {
            Object relatesFromIdentity = store.lookupIdentityObjectById((IdentityType) parameterValues[0]);
            
            predicates.add(builder.equal(criteria.getRoot().get(store.getConfig().getModelProperty(PROPERTY_IDENTITY_RELATES_TO).getName()),
                    relatesFromIdentity));
        }

        if (queryParameter.equals(Relationship.FROM)) {
            Object relatedFromIdentity = store.lookupIdentityObjectById((IdentityType) parameterValues[0]);

            predicates.add(builder.equal(criteria.getRoot().get(store.getConfig().getModelProperty(PROPERTY_IDENTITY_RELATED_TO).getName()),
                    relatedFromIdentity));
        }

        return predicates;
    }

}

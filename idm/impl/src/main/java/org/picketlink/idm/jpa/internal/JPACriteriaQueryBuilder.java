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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.picketlink.idm.event.AbstractBaseEvent;
import org.picketlink.idm.jpa.annotations.PropertyType;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class JPACriteriaQueryBuilder {

    private IdentityQuery<?> identityQuery;
    private CriteriaBuilder builder;
    private Root<?> root;
    private CriteriaQuery<?> criteria;
    private JPAIdentityStore identityStore;

    public JPACriteriaQueryBuilder(JPAIdentityStore identityStore, IdentityQuery<?> identityQuery) {
        this.identityStore = identityStore;
        this.identityQuery = identityQuery;
        
        this.builder = getEntityManager().getCriteriaBuilder();
        this.criteria = builder.createQuery(getConfig().getIdentityClass());
        this.root = criteria.from(getConfig().getIdentityClass());
    }

    public List<Predicate> getPredicates() {
        List<Predicate> predicates = new ArrayList<Predicate>();

        if (!IdentityType.class.equals(getIdentityQuery().getIdentityType())) {
            String discriminator = getConfig().getIdentityTypeDiscriminator(identityQuery.getIdentityType());

            predicates.add(builder.equal(root.get(getConfig().getModelProperty(PropertyType.IDENTITY_DISCRIMINATOR).getName()),
                    discriminator));

            IdentityTypeHandler<?> identityTypeManager = getConfig().getHandler(this.identityQuery.getIdentityType());

            predicates.addAll(identityTypeManager.getPredicate(this, identityStore));
        } else {
            IdentityTypeHandler<IdentityType> identityTypeManager = new DefaultIdentityTypeHandler(getConfig());

            predicates.addAll(identityTypeManager.getPredicate(this, identityStore));
        }

        return predicates;
    }

    public List<Order> getOrders() {
        IdentityTypeHandler<?> identityTypeManager;
        if (!IdentityType.class.equals(getIdentityQuery().getIdentityType())) {
            identityTypeManager = getConfig().getHandler(this.identityQuery.getIdentityType());
        } else {
            identityTypeManager = new DefaultIdentityTypeHandler(getConfig());
        }

        return identityTypeManager.getOrders(this);
    }

    protected CriteriaQuery<?> getCriteria() {
        return this.criteria;
    }

    protected CriteriaBuilder getBuilder() {
        return builder;
    }

    protected Root<?> getRoot() {
        return root;
    }

    protected IdentityQuery<?> getIdentityQuery() {
        return this.identityQuery;
    }
    
    private JPAIdentityStoreConfiguration getConfig() {
        return this.identityStore.getConfig();
    }

    private EntityManager getEntityManager() {
        return this.identityStore.getEntityManager();
    }

    private class DefaultIdentityTypeHandler extends IdentityTypeHandler<IdentityType> {

        public DefaultIdentityTypeHandler(JPAIdentityStoreConfiguration config) {
            super(config);
        }

        @Override
        protected IdentityType doCreateIdentityType(Object identity, JPAIdentityStore store) {
            return null;
        }

        @Override
        protected void doPopulateIdentityInstance(Object toIdentity, IdentityType fromIdentityType,
                                                  JPAIdentityStore store) {

        }

        @Override
        protected AbstractBaseEvent raiseCreatedEvent(IdentityType fromIdentityType) {
            return null;
        }

        @Override
        protected AbstractBaseEvent raiseUpdatedEvent(IdentityType fromIdentityType) {
            return null;
        }

        @Override
        protected AbstractBaseEvent raiseDeletedEvent(IdentityType fromIdentityType) {
            return null;
        }
    }
}

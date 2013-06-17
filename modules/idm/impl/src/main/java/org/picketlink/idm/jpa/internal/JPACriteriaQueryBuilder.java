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
import org.picketlink.idm.config.JPAIdentityStoreConfigurationOld;
import org.picketlink.idm.config.JPAIdentityStoreConfigurationOld.PropertyType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.spi.SecurityContext;

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

    public JPACriteriaQueryBuilder(SecurityContext context, JPAIdentityStore identityStore, IdentityQuery<?> identityQuery) {
        this.identityStore = identityStore;
        this.identityQuery = identityQuery;

        this.builder = getEntityManager(context).getCriteriaBuilder();
        this.criteria = builder.createQuery(getConfig().getIdentityClass());
        this.root = criteria.from(getConfig().getIdentityClass());
    }

    public List<Predicate> getPredicates(SecurityContext context) {
        List<Predicate> predicates = new ArrayList<Predicate>();

        if (!IdentityType.class.equals(getIdentityQuery().getIdentityType())) {
            String discriminator = getConfig().getIdentityTypeDiscriminator(identityQuery.getIdentityType());

            predicates.add(builder.equal(root.get(getConfig().getModelProperty(PropertyType.IDENTITY_DISCRIMINATOR).getName()),
                    discriminator));

            IdentityTypeHandler<?> identityTypeManager = IdentityTypeHandlerFactory.getHandler(this.identityQuery.getIdentityType());

            predicates.addAll(identityTypeManager.getPredicate(context, this, identityStore));
        } else {
            IdentityTypeHandler<IdentityType> identityTypeManager = new DefaultIdentityTypeHandler();

            predicates.addAll(identityTypeManager.getPredicate(context, this, this.identityStore));
        }

        return predicates;
    }

    public List<Order> getOrders() {
        IdentityTypeHandler<?> identityTypeManager;
        if (!IdentityType.class.equals(getIdentityQuery().getIdentityType())) {
            identityTypeManager = IdentityTypeHandlerFactory.getHandler(this.identityQuery.getIdentityType());
        } else {
            identityTypeManager = new DefaultIdentityTypeHandler();
        }

        return identityTypeManager.getOrders(this, this.identityStore);
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

    private JPAIdentityStoreConfigurationOld getConfig() {
        return this.identityStore.getConfig();
    }

    private EntityManager getEntityManager(SecurityContext context) {
        return this.identityStore.getEntityManager(context);
    }

    private class DefaultIdentityTypeHandler extends IdentityTypeHandler<IdentityType> {

        @Override
        protected IdentityType doCreateIdentityType(SecurityContext context, Object identity, JPAIdentityStore store) {
            return null;
        }

        @Override
        protected void doPopulateIdentityInstance(SecurityContext context, Object toIdentity, IdentityType fromIdentityType,
                                                  JPAIdentityStore store) {

        }

    }
}

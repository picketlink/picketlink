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
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.picketlink.idm.event.AbstractBaseEvent;
import org.picketlink.idm.jpa.annotations.PropertyType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class JPACriteriaQueryBuilder {

    private JPAIdentityStoreConfiguration config;
    private IdentityQuery<?> identityQuery;
    private EntityManager entityManager;
    private CriteriaBuilder builder;
    private Root<?> root;
    private CriteriaQuery<?> criteria;
    private JPAIdentityStore identityStore;

    public JPACriteriaQueryBuilder(JPAIdentityStore identityStore, IdentityQuery<?> identityQuery) {
        this.identityStore = identityStore;
        this.identityQuery = identityQuery;
        this.config = identityStore.getConfig();
        this.entityManager = identityStore.getEntityManager();

        if (entityManager == null) {
            throw new IllegalStateException("Entity Manager is null");
        }
        this.builder = this.entityManager.getCriteriaBuilder();

        if (builder == null) {
            throw new IllegalStateException("Criteria Builder is null");
        }

        Class<?> identityClass = this.config.getIdentityClass();

        this.criteria = builder.createQuery(identityClass);
        this.root = criteria.from(identityClass);
    }

    public List<Predicate> getPredicates() {
        List<Predicate> predicates = new ArrayList<Predicate>();

        this.builder = this.entityManager.getCriteriaBuilder();

        if (!IdentityType.class.equals(identityQuery.getIdentityType())) {
            String discriminator = this.config.getIdentityTypeDiscriminator(identityQuery.getIdentityType());

            predicates.add(builder.equal(root.get(this.config.getModelProperty(PropertyType.IDENTITY_DISCRIMINATOR).getName()),
                    discriminator));

            IdentityTypeHandler identityTypeManager = this.config.getHandler(this.identityQuery.getIdentityType());

            for (Entry<QueryParameter, Object[]> entry : this.identityQuery.getParameters().entrySet()) {
                QueryParameter queryParameter = entry.getKey();
                Object[] parameterValues = entry.getValue();

                predicates.addAll(identityTypeManager.getPredicate(queryParameter, parameterValues, this, identityStore));
            }
        } else {
            IdentityTypeHandler identityTypeManager = new IdentityTypeHandler<IdentityType>(this.config) {

                @Override
                protected IdentityType doCreateIdentityType(Object identity, JPAIdentityStore store) {
                    return null;
                }

                @Override
                protected void doPopulateIdentityInstance(Object toIdentity, IdentityType fromIdentityType,
                        JPAIdentityStore store) {
                    
                }

                @Override
                protected AbstractBaseEvent raiseCreatedEvent(IdentityType fromIdentityType, JPAIdentityStore store) {
                    return null;
                }

                @Override
                protected AbstractBaseEvent raiseUpdatedEvent(IdentityType fromIdentityType, JPAIdentityStore store) {
                    return null;
                }

                @Override
                protected AbstractBaseEvent raiseDeletedEvent(IdentityType fromIdentityType, JPAIdentityStore store) {
                    return null;
                }
            };

            for (Entry<QueryParameter, Object[]> entry : this.identityQuery.getParameters().entrySet()) {
                QueryParameter queryParameter = entry.getKey();
                Object[] parameterValues = entry.getValue();

                predicates.addAll(identityTypeManager.getPredicate(queryParameter, parameterValues, this, identityStore));
            }
        }

        return predicates;
    }

    public CriteriaQuery<?> getCriteria() {
        return this.criteria;
    }

    public CriteriaBuilder getBuilder() {
        return builder;
    }

    public Root<?> getRoot() {
        return root;
    }
    
    public IdentityQuery<?> getIdentityQuery() {
        return this.identityQuery;
    }
}

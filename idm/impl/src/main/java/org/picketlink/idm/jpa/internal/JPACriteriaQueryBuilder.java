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

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.picketlink.idm.event.AbstractBaseEvent;
import org.picketlink.idm.jpa.annotations.PropertyType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.query.IdentityQuery;

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
            IdentityTypeHandler<IdentityType> identityTypeManager = new IdentityTypeHandler<IdentityType>(getConfig()) {

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
            };

            predicates.addAll(identityTypeManager.getPredicate(this, identityStore));
        }

        return predicates;
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
}

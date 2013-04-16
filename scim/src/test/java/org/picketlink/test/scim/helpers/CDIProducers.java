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
package org.picketlink.test.scim.helpers;

import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.jpa.schema.CredentialObject;
import org.picketlink.idm.jpa.schema.CredentialObjectAttribute;
import org.picketlink.idm.jpa.schema.IdentityObject;
import org.picketlink.idm.jpa.schema.IdentityObjectAttribute;
import org.picketlink.idm.jpa.schema.PartitionObject;
import org.picketlink.idm.jpa.schema.RelationshipIdentityObject;
import org.picketlink.idm.jpa.schema.RelationshipObject;
import org.picketlink.idm.jpa.schema.RelationshipObjectAttribute;
import org.picketlink.idm.model.Realm;
import org.picketlink.scim.DataProvider;
import org.picketlink.scim.DataProviderAnnotation;
import org.picketlink.scim.providers.PicketLinkIDMDataProvider;

/**
 * Class that contains all the CDI Producers
 *
 * @author anil saldhana
 * @since Apr 10, 2013
 */
public class CDIProducers {
    @Produces
    @PersistenceContext(unitName = "picketlink-scim-testing-pu")
    private EntityManager em;

    @Produces
    public IdentityManager configure() {
        IdentityConfiguration configuration = new IdentityConfiguration();

        configuration.jpaStore().addRealm(Realm.DEFAULT_REALM, "Testing").addTier("Application")
                .setIdentityClass(IdentityObject.class).setAttributeClass(IdentityObjectAttribute.class)
                .setRelationshipClass(RelationshipObject.class).setRelationshipIdentityClass(RelationshipIdentityObject.class)
                .setRelationshipAttributeClass(RelationshipObjectAttribute.class).setCredentialClass(CredentialObject.class)
                .setCredentialAttributeClass(CredentialObjectAttribute.class).setPartitionClass(PartitionObject.class)
                .supportAllFeatures();

        return configuration.buildIdentityManagerFactory().createIdentityManager();
    }

    @Produces
    @DataProviderAnnotation
    public DataProvider createDataProvider() {
        return new PicketLinkIDMDataProvider();
    }
}
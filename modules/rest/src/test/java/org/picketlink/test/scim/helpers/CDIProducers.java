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
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.scim.DataProvider;
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
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .jpa()
//                        .identityClass(IdentityObject.class)
//                        .attributeClass(IdentityObjectAttribute.class)
//                        .relationshipClass(RelationshipObject.class)
//                        .relationshipIdentityClass(RelationshipIdentityObject.class)
//                        .relationshipAttributeClass(RelationshipObjectAttribute.class)
//                        .credentialClass(CredentialObject.class)
//                        .credentialAttributeClass(CredentialObjectAttribute.class)
//                        .partitionClass(PartitionObject.class)
//                        .supportAllFeatures();
        ;

        DefaultPartitionManager partitionManager = new DefaultPartitionManager(builder.build());

        partitionManager.add(new Realm(Realm.DEFAULT_REALM));

        return partitionManager.createIdentityManager();
    }

    @Produces
    public DataProvider createDataProvider() {
        return new PicketLinkIDMDataProvider();
    }
}
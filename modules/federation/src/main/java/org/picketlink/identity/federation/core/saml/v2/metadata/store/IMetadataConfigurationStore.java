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
package org.picketlink.identity.federation.core.saml.v2.metadata.store;

import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Configuration Store for the metadata
 *
 * @author Anil.Saldhana@redhat.com
 * @since Apr 27, 2009
 */
public interface IMetadataConfigurationStore {

    /**
     * <p>
     * Perform any bootstrap or initialization the store needs.
     * </p>
     */
    void bootstrap();

    /**
     * Get a set of the service provider ID, which can individually be drilled down to get additional trusted provider
     * information
     *
     * @return
     */
    Set<String> getServiceProviderID();

    /**
     * Get a set of the identity provider ID, which can individually be drilled down to get additional trusted provider
     * information
     *
     * @return
     */
    Set<String> getIdentityProviderID();

    /**
     * Get the Trusted Providers
     *
     * @param id
     *
     * @return a map of name of provider, metadata urls
     *
     * @throws {@link IOException}
     * @throws {@link ClassNotFoundException}
     */
    Map<String, String> loadTrustedProviders(String id) throws IOException, ClassNotFoundException;

    /**
     * Persist the map of trusted providers
     *
     * @param id
     * @param trusted
     *
     * @throws {@link IOException}
     */
    void persistTrustedProviders(String id, Map<String, String> trusted) throws IOException;

    /**
     * Persist into an external sink (file system, ldap, db etc)
     *
     * @param entity
     * @param id An unique identifier useful for retrieval
     *
     * @throws {@link IOException}
     */
    void persist(EntityDescriptorType entity, String id) throws IOException;

    /**
     * Load the descriptor from the external data sink
     *
     * @param id unique identifier used during persistence
     *
     * @return
     *
     * @throws {@link IOException}
     */
    EntityDescriptorType load(String id) throws IOException;

    /**
     * Delete the descriptor from the external data sink
     *
     * @param id
     */
    void delete(String id);

    /**
     * Delete the trusted providers from the external data sink
     *
     * @param id
     */
    void deleteTrustedProviders(String id);

    /**
     * <p>
     * Perform final cleanup if needed.
     * </p>
     */
    void cleanup();
}
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

package org.picketlink.idm.spi;

import java.util.List;

import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.model.Agent;

/**
 * An optional interface typically implemented by an IdentityStore that supports the storage of credential related state 
 * 
 * @author Shane Bryzak
 *
 */
public interface CredentialStore {
    
    /**
     * Store the specified credential state
     * 
     * @param storage
     */
    void storeCredential(Agent agent, CredentialStorage storage);

    /**
     * Return the currently active credential state of the specified class, for the specified Agent
     * 
     * @param storageClass
     * @return
     */
    <T extends CredentialStorage> T retrieveCurrentCredential(Agent agent, Class<T> storageClass);

    /**
     * Returns a List of all credential state of the specified class, for the specified Agent
     * 
     * @param agent
     * @param storageClass
     * @return
     */
    <T extends CredentialStorage> List<T> retrieveCredentials(Agent agent, Class<T> storageClass);
}
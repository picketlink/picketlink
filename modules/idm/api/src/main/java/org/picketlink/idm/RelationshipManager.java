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
package org.picketlink.idm;

import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.query.RelationshipQuery;

/**
 * Defines relationship management operations
 *
 * @author Shane Bryzak
 */
public interface RelationshipManager {

    /**
     * <p>
     * Adds the given {@link Relationship} instance to the configured identity store.
     * </p>
     *
     * @param relationship
     * @throws IdentityManagementException If cannot add the provided {@link Relationship} instance.
     */
    void add(Relationship relationship) throws IdentityManagementException;

    /**
     * <p>
     * Updates the given {@link Relationship} instance. The instance must have an identifier that references a
     * previously stored instance, otherwise a exception will be thrown.
     * </p>
     *
     * @param relationship
     * @throws IdentityManagementException If cannot update the provided {@link Relationship} instance.
     */
    void update(Relationship relationship) throws IdentityManagementException;

    /**
     * <p>
     * Removes the given {@link Relationship} instance. The instance must have an identifier that references a
     * previously stored instance, otherwise a exception will be thrown.
     * </p>
     *
     * @param relationship
     * @throws IdentityManagementException If cannot remove the provided {@link Relationship} instance.
     */
    void remove(Relationship relationship) throws IdentityManagementException;

    /**
     * <p>
     * Creates an {@link RelationshipQuery} that can be used to query for {@link Relationship} instances.
     * </p>
     * <p>
     * The first argument tells which {@link Relationship} type should be returned. If you provide the {@link Relationship} base
     * interface any {@link Relationship} instance that matches the provided query parameters will be returned.
     * </p>
     *
     * @param identityType
     * @return
     */
    <T extends Relationship> RelationshipQuery<T> createRelationshipQuery(Class<T> relationshipType);

    /**
     * Returns true if the specified identity inherits privileges assigned to the specified assignee,
     * either via a relationship or a direct reference from the identity (or a combination of these)
     *
     * @param identity
     * @param assignee
     * @return
     */
    boolean inheritsPrivileges(IdentityType identity, IdentityType assignee);
}

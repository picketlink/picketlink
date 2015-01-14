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

import org.picketlink.idm.model.AttributedType;

/**
 * @author pedroigor
 */
public interface AttributedTypeManager<T extends AttributedType> {

    void add(T attributedType) throws IdentityManagementException;

    void update(T attributedType) throws IdentityManagementException;

    void remove(T attributedType) throws IdentityManagementException;

    /**
     * <p>
     * Retrieves an {@link org.picketlink.idm.model.AttributedType} with the given identifier.
     * </p>
     * <p>
     * The first argument tells which {@link org.picketlink.idm.model.AttributedType} type should be returned. If you provide the {@link org.picketlink.idm.model.AttributedType} base
     * interface any {@link org.picketlink.idm.model.AttributedType} instance that matches the given identifier will be returned.
     * </p>
     *
     * @param attributedType
     * @param id
     * @return If no {@link org.picketlink.idm.model.AttributedType} is found with the given identifier this method returns null.
     */
    <C extends T> C lookupById(Class<C> attributedType, String id) throws IdentityManagementException;
}

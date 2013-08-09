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

import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;

import java.io.Serializable;

/**
 * <p>A special type of IdentityStore that is also capable of providing attribute management functionality.</p>
 *
 * @author Pedro Igor
 * @author Shane Bryzak
 */
public interface AttributeStore<T extends IdentityStoreConfiguration> extends IdentityStore<T> {

    /**
     * Sets the specified Attribute value for the specified IdentityType
     *
     * @param ctx
     * @param identityType
     * @param attribute
     */
    void setAttribute(IdentityContext context, AttributedType type,
                      Attribute<? extends Serializable> attribute);

    /**
     * Returns the Attribute value with the specified name, for the specified IdentityType
     * @param ctx
     * @param identityType
     * @param attributeName
     * @return
     */
    <V extends Serializable> Attribute<V> getAttribute(IdentityContext context, AttributedType type,
                                                       String attributeName);

    /**
     * Removes the specified Attribute value, for the specified IdentityType
     *
     * @param ctx
     * @param identityType
     * @param attributeName
     */
    void removeAttribute(IdentityContext context, AttributedType type, String attributeName);

    /**
     * Loads all attributes for the given {@link AttributedType}.
     *
     * @param context
     * @param attributedType
     */
    void loadAttributes(IdentityContext context, AttributedType attributedType);
}

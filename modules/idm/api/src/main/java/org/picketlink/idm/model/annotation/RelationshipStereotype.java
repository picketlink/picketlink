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
package org.picketlink.idm.model.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>
 *     In many security systems, use of architectural patterns produces a set of recurring concepts. A stereotype represents
 *     those concepts, helping developers to declare some metadata for their own types in order to get them properly recognized,
 *     managed and associated with those common concepts.
 * </p>
 *
 * <p>
 *     This annotation provides all stereotypes that can be defined by {@link org.picketlink.idm.model.Relationship} types, such as
 *     grant and group memberships. Each stereotype has some common properties that should be defined using the
 *     {@link org.picketlink.idm.model.annotation.StereotypeProperty} annotation.
 * </p>
 *
 * <p>
 *     A {@link org.picketlink.idm.model.Relationship} must declare only a single stereotype.
 * </p>
 *
 * @author Shane Bryzak
 * @author Pedro Igor
 *
 * @see org.picketlink.idm.model.annotation.StereotypeProperty
 */
@Target({TYPE})
@Documented
@Retention(RUNTIME)
@Inherited
public @interface RelationshipStereotype {

    Stereotype value();

    public enum Stereotype {

        /**
         * <p>Should be used by {@link org.picketlink.idm.model.Relationship} types that represent an association between a
         * {@link org.picketlink.idm.model.IdentityType} and a role type.</p>
         */
        GRANT,

        /**
         * <p>Should be used by {@link org.picketlink.idm.model.Relationship} types that represent an association between a
         * {@link org.picketlink.idm.model.IdentityType} and a group type. Usually, the associated identity type is an user that is member
         * of a group.</p>
         */
        GROUP_MEMBERSHIP
    }

}

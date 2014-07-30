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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>
 *     Marks a property of an IdentityType or Relationship as being an attribute of a specific stereotype.
 * </p>
 *
 * @author Shane Bryzak
 * @author Pedro Igor
 *
 * @see org.picketlink.idm.model.annotation.IdentityStereotype
 * @see org.picketlink.idm.model.annotation.RelationshipStereotype
 */
@Target({METHOD, FIELD})
@Documented
@Retention(RUNTIME)
@Inherited
public @interface StereotypeProperty {

    Property value();

    public enum Property {
        /**
         * <p>
         *     Should be used to mark a property of an {@link org.picketlink.idm.model.IdentityType} as the unique and never reassigned idenfifier.
         * </p>
         */
        IDENTITY_ID,

        /**
         * <p>
         *     Should be used to mark a property of an {@link org.picketlink.idm.model.IdentityType} as the name of a {@link org.picketlink.idm.model.annotation.IdentityStereotype.Stereotype#ROLE} stereotype.
         * </p>
         */
        IDENTITY_ROLE_NAME,

        /**
         * <p>
         *     Should be used to mark a property of an {@link org.picketlink.idm.model.IdentityType} as the name of a {@link org.picketlink.idm.model.annotation.IdentityStereotype.Stereotype#GROUP} stereotype.
         * </p>
         */
        IDENTITY_GROUP_NAME,

        /**
         * <p>
         *     Should be used to mark a property of an {@link org.picketlink.idm.model.IdentityType} as the name of a {@link org.picketlink.idm.model.annotation.IdentityStereotype.Stereotype#USER} stereotype.
         * </p>
         */
        IDENTITY_USER_NAME,

        /**
         * <p>
         *     Should be used to mark a property of an {@link org.picketlink.idm.model.Relationship} as related with the role of a {@link org.picketlink.idm.model.annotation.RelationshipStereotype.Stereotype#GRANT} stereotype.
         * </p>
         */
        RELATIONSHIP_GRANT_ROLE,

        /**
         * <p>
         *     Should be used to mark a property of an {@link org.picketlink.idm.model.Relationship} as related with the assignee of a {@link org.picketlink.idm.model.annotation.RelationshipStereotype.Stereotype#GRANT} stereotype.
         * </p>
         */
        RELATIONSHIP_GRANT_ASSIGNEE,

        /**
         * <p>
         *     Should be used to mark a property of an {@link org.picketlink.idm.model.Relationship} as related with the group of a {@link org.picketlink.idm.model.annotation.RelationshipStereotype.Stereotype#GROUP_MEMBERSHIP} stereotype.
         * </p>
         */
        RELATIONSHIP_GROUP_MEMBERSHIP_GROUP,

        /**
         * <p>
         *     Should be used to mark a property of an {@link org.picketlink.idm.model.Relationship} as related with the member of a {@link org.picketlink.idm.model.annotation.RelationshipStereotype.Stereotype#GROUP_MEMBERSHIP} stereotype.
         * </p>
         */
        RELATIONSHIP_GROUP_MEMBERSHIP_MEMBER,
    }
}

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
package org.picketlink.idm.model;

import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.RelationshipQueryParameter;

/**
 * Represents a relationship between two or more {@link IdentityType}, which is also
 * capable of supporting multiple attribute values.
 *
 * @author anil saldhana
 * @author Shane Bryzak
 * @since Dec 18, 2012
 */
public interface Relationship extends AttributedType {

    /**
     * A query parameter that can be used to obtain all relationships for a given {@link IdentityType}.
     */
    QueryParameter IDENTITY = new QueryParameter() {};

    public final class RELATIONSHIP_QUERY_ATTRIBUTE {
        public static RelationshipQueryParameter byName(final String name) {
            return new RelationshipQueryParameter() {

                @Override
                public String getName() {
                    return name;
                }
            };
        }
    }

}
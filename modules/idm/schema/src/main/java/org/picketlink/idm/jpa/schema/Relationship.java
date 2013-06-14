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

package org.picketlink.idm.jpa.schema;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.picketlink.idm.jpa.annotations.Identifier;
import org.picketlink.idm.jpa.annotations.RelationshipClass;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@Entity
public class Relationship implements Serializable {

    private static final long serialVersionUID = -7482143409681874546L;

    @Id
    @Identifier
    private String id;

    @RelationshipClass
    private String relationshipClass;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRelationshipClass() {
        return relationshipClass;
    }

    public void setRelationshipClass(String relationshipClass) {
        this.relationshipClass = relationshipClass;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!getClass().isInstance(obj)) {
            return false;
        }

        Relationship other = (Relationship) obj;

        return getId() != null && other.getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getId() != null ? getId().hashCode() : 0);
        return result;
    }
}

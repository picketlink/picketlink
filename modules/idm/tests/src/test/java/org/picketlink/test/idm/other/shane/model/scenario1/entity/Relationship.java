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
package org.picketlink.test.idm.other.shane.model.scenario1.entity;

import org.picketlink.idm.jpa.annotations.Identifier;
import org.picketlink.idm.jpa.annotations.RelationshipClass;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Root entity bean that defines each identity object
 *
 * @author Shane Bryzak
 */
@IdentityManaged (org.picketlink.idm.model.Relationship.class)
@Entity
public class Relationship implements Serializable {
    private static final long serialVersionUID = 3756417796986661942L;

    @Id @Identifier private String relationshipId;
    @RelationshipClass private String relationshipClass;

    public String getRelationshipId() {
        return relationshipId;
    }

    public void setRelationshipId(String relationshipId) {
        this.relationshipId = relationshipId;
    }

    public String getRelationshipClass() {
        return relationshipClass;
    }

    public void setRelationshipClass(String relationshipClass) {
        this.relationshipClass = relationshipClass;
    }
}

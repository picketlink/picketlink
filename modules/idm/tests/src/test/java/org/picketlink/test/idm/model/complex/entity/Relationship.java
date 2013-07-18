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

package org.picketlink.test.idm.model.complex.entity;

import javax.persistence.Entity;
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.RelationshipClass;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@Entity
public class Relationship extends AttributedTypeObject {

    private static final long serialVersionUID = -7482143409681874546L;

    @RelationshipClass
    private String relationshipClass;

    @AttributeValue
    private String attributeA;

    @AttributeValue
    private String attributeB;

    @AttributeValue
    private String attributeC;

    public String getRelationshipClass() {
        return relationshipClass;
    }

    public void setRelationshipClass(String relationshipClass) {
        this.relationshipClass = relationshipClass;
    }

    public String getAttributeA() {
        return attributeA;
    }

    public void setAttributeA(String attributeA) {
        this.attributeA = attributeA;
    }

    public String getAttributeB() {
        return attributeB;
    }

    public void setAttributeB(String attributeB) {
        this.attributeB = attributeB;
    }

    public String getAttributeC() {
        return attributeC;
    }

    public void setAttributeC(String attributeC) {
        this.attributeC = attributeC;
    }
}

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

package org.picketlink.test.idm.relationship;

import org.picketlink.idm.model.AbstractAttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.annotation.RelationshipAttribute;
import org.picketlink.idm.model.annotation.RelationshipIdentity;
import org.picketlink.idm.query.RelationshipQueryParameter;

/**
 * @author Pedro Silva
 * 
 */
public class CustomRelationship extends AbstractAttributedType implements Relationship {

    private static final long serialVersionUID = 1030652086550754965L;
    
    public static final RelationshipQueryParameter IDENTITY_TYPE_A = new RelationshipQueryParameter() {
        
        @Override
        public String getName() {
            return "identityTypeA";
        }
    };

    public static final RelationshipQueryParameter IDENTITY_TYPE_B = new RelationshipQueryParameter() {
        
        @Override
        public String getName() {
            return "identityTypeB";
        }
    };

    public static final RelationshipQueryParameter IDENTITY_TYPE_C = new RelationshipQueryParameter() {
        
        @Override
        public String getName() {
            return "identityTypeC";
        }
    };

    private IdentityType identityTypeA;
    private IdentityType identityTypeB;
    private IdentityType identityTypeC;

    private String attributeA;
    private String attributeB;
    private String attributeC;

    @RelationshipIdentity
    public IdentityType getIdentityTypeA() {
        return this.identityTypeA;
    }

    public void setIdentityTypeA(IdentityType identityTypeA) {
        this.identityTypeA = identityTypeA;
    }

    @RelationshipIdentity
    public IdentityType getIdentityTypeB() {
        return this.identityTypeB;
    }

    public void setIdentityTypeB(IdentityType identityTypeB) {
        this.identityTypeB = identityTypeB;
    }

    @RelationshipIdentity
    public IdentityType getIdentityTypeC() {
        return this.identityTypeC;
    }

    public void setIdentityTypeC(IdentityType identityTypeC) {
        this.identityTypeC = identityTypeC;
    }

    @RelationshipAttribute
    public String getAttributeA() {
        return this.attributeA;
    }

    public void setAttributeA(String attributeA) {
        this.attributeA = attributeA;
    }

    @RelationshipAttribute
    public String getAttributeB() {
        return this.attributeB;
    }

    public void setAttributeB(String attributeB) {
        this.attributeB = attributeB;
    }

    @RelationshipAttribute
    public String getAttributeC() {
        return this.attributeC;
    }

    public void setAttributeC(String attributeC) {
        this.attributeC = attributeC;
    }

}
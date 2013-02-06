/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
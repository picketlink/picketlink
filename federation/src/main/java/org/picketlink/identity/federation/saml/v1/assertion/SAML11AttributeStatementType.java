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
package org.picketlink.identity.federation.saml.v1.assertion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <complexType name="AttributeStatementType"> <complexContent> <extension base="saml:SubjectStatementAbstractType"> <sequence>
 * <element ref="saml:Attribute" maxOccurs="unbounded"/>
 *
 * </sequence> </extension> </complexContent> </complexType>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11AttributeStatementType extends SAML11SubjectStatementType {
    private static final long serialVersionUID = 1L;

    protected List<SAML11AttributeType> attribute = new ArrayList<SAML11AttributeType>();

    public void add(SAML11AttributeType aAttribute) {
        this.attribute.add(aAttribute);
    }

    public void addAllAttributes(List<SAML11AttributeType> attribList) {
        this.attribute.addAll(attribList);
    }

    public boolean remove(SAML11AttributeType anAttrib) {
        return this.attribute.remove(anAttrib);
    }

    public List<SAML11AttributeType> get() {
        return Collections.unmodifiableList(attribute);
    }
}
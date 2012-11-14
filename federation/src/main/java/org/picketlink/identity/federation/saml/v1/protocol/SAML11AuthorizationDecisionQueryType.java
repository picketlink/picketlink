/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.identity.federation.saml.v1.protocol;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.picketlink.identity.federation.saml.v1.assertion.SAML11ActionType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11EvidenceType;

/**
 * <complexType name="AuthorizationDecisionQueryType"> <complexContent> <extension base="samlp:SubjectQueryAbstractType">
 * <sequence>
 *
 * <element ref="saml:Action" maxOccurs="unbounded"/> <element ref="saml:Evidence" minOccurs="0"/> </sequence> <attribute
 * name="Resource" type="anyURI" use="required"/> </extension> </complexContent> </complexType>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11AuthorizationDecisionQueryType extends SAML11SubjectQueryAbstractType {
    private static final long serialVersionUID = 1L;

    protected List<SAML11ActionType> action = new ArrayList<SAML11ActionType>();

    protected SAML11EvidenceType evidence;

    protected URI resource;

    public URI getResource() {
        return resource;
    }

    public void setResource(URI resource) {
        this.resource = resource;
    }

    public SAML11EvidenceType getEvidence() {
        return evidence;
    }

    public void setEvidence(SAML11EvidenceType evidence) {
        this.evidence = evidence;
    }

    public void add(SAML11ActionType sadt) {
        this.action.add(sadt);
    }

    public boolean remove(SAML11ActionType sadt) {
        return this.action.remove(sadt);
    }

    public List<SAML11ActionType> get() {
        return Collections.unmodifiableList(action);
    }
}
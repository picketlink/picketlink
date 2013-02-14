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

package org.picketlink.idm.ldap.internal;

import static org.picketlink.idm.ldap.internal.LDAPConstants.CN;
import static org.picketlink.idm.ldap.internal.LDAPConstants.COMMA;
import static org.picketlink.idm.ldap.internal.LDAPConstants.EQUAL;
import static org.picketlink.idm.ldap.internal.LDAPConstants.MEMBER;
import static org.picketlink.idm.ldap.internal.LDAPConstants.SPACE_STRING;

import java.io.Serializable;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

/**
 * @author Pedro Silva
 * 
 */
public class LDAPEntry implements Serializable {

    private static final long serialVersionUID = -6220260371962877017L;

    private Attributes attributes = new BasicAttributes(true);

    private String dnSuffix;

    public LDAPEntry() {
        
    }
    
    public LDAPEntry(String dnSuffix) {
        this.dnSuffix = dnSuffix;
    }

    public String getDN() {
        try {
            return getDN(getLDAPAttributes().get(getAttributeForBinding()).get().toString());
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getBidingName() {
        try {
            return getAttributeForBinding() + EQUAL + getLDAPAttributes().get(getAttributeForBinding()).get().toString();
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getAttributeForBinding() {
        return CN;
    }

    public String getDN(String name) {
        return getAttributeForBinding() + EQUAL + name + COMMA + this.dnSuffix;
    }

    public String getDnSuffix() {
        return this.dnSuffix;
    }

    public void setDnSuffix(String dnSuffix) {
        this.dnSuffix = dnSuffix;
    }

    public void addMember(LDAPEntry childEntry) {
        Attribute memberAttribute = getLDAPAttributes().get(MEMBER);
        if (memberAttribute != null) {
            if (memberAttribute.contains(SPACE_STRING)) {
                memberAttribute.remove(SPACE_STRING);
            }
        } else {
            memberAttribute = new BasicAttribute(MEMBER);
        }

        memberAttribute.add(childEntry.getDN());
        getLDAPAttributes().put(memberAttribute);
    }

    public void removeMember(LDAPEntry childEntry) {
        Attribute memberAttribute = getLDAPAttributes().get(MEMBER);

        if (memberAttribute != null) {
            memberAttribute.remove(childEntry.getDN());

            if (memberAttribute.size() == 0) {
                memberAttribute.add(SPACE_STRING);
            }
        }
    }

    protected void addAllLDAPAttributes(Attributes theAttributes) {
        if (theAttributes != null) {
            NamingEnumeration<? extends Attribute> ne = theAttributes.getAll();
            try {
                while (ne.hasMore()) {
                    Attribute att = ne.next();
                    attributes.put(att);
                }
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected Attributes getLDAPAttributes() {
        return this.attributes;
    }

    protected void setLDAPAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public boolean isMember(LDAPEntry member) {
        Attribute memberAttribute = getLDAPAttributes().get(MEMBER);

        return memberAttribute != null && memberAttribute.contains(member.getDN());
    }
}

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

package org.picketlink.idm.ldap.internal;

import static org.picketlink.idm.ldap.internal.LDAPConstants.OBJECT_CLASS;
import static org.picketlink.idm.ldap.internal.LDAPConstants.UID;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;

import org.picketlink.idm.model.Agent;

/**
 * @author Pedro Silva
 *
 */
public class LDAPAgent extends LDAPIdentityType implements Agent {

    private static final long serialVersionUID = -8314904094352933682L;

    public LDAPAgent(String dnSuffix) {
        super(dnSuffix);
        Attribute oc = new BasicAttribute(OBJECT_CLASS);

        oc.add("inetOrgPerson");
        oc.add("organizationalPerson");
        oc.add("person");
        oc.add("top");
        oc.add("extensibleObject");

        getLDAPAttributes().put(oc);
        
        getLDAPAttributes().put(LDAPConstants.CN, " ");
        getLDAPAttributes().put(LDAPConstants.SN, " ");
        getLDAPAttributes().put(LDAPConstants.GIVENNAME, " ");
    }

    public LDAPAgent(String loginName, String agentDNSuffix) {
        this(agentDNSuffix);
        
        if (loginName == null) {
            throw new IllegalArgumentException("You must provide a name.");
        }
        
        setLoginName(loginName);
    }

    @Override
    protected String getAttributeForBinding() {
        return UID;
    }
    
    @Override
    public String getLoginName() {
        Attribute theAttribute = getLDAPAttributes().get(UID);
        
        if (theAttribute != null) {
            try {
                return (String) theAttribute.get();
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        }
        
        return null;
    }
    
    @Override
    public void setLoginName(String loginName) {
        Attribute theAttribute = getLDAPAttributes().get(UID);
        
        if (theAttribute == null) {
            getLDAPAttributes().put(UID, loginName);
        } else {
            theAttribute.remove(0);
            theAttribute.add(0, loginName);
        }
    }
    
}

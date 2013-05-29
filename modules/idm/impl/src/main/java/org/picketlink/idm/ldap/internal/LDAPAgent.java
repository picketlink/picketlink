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

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;

import static org.picketlink.idm.IDMMessages.MESSAGES;
import static org.picketlink.idm.ldap.internal.LDAPConstants.OBJECT_CLASS;
import static org.picketlink.idm.ldap.internal.LDAPConstants.UID;

/**
 * @author Pedro Silva
 *
 */
public class LDAPAgent extends LDAPIdentityType {

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
            throw MESSAGES.nullArgument("Login name.");
        }

        setLoginName(loginName);
    }

    @Override
    protected String getAttributeForBinding() {
        return UID;
    }

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

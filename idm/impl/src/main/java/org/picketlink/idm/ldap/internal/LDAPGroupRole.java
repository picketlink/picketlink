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

import static org.picketlink.idm.ldap.internal.LDAPConstants.OBJECT_CLASS;

import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;

import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Role;

/**
 * Attributes of an {@link LDAPUser} that does not map to LDAP managed attributes
 *
 * @author anil saldhana
 * @since Sep 7, 2012
 */
public class LDAPGroupRole extends LDAPIdentityType /*implements GroupRole*/ {

    private static final long serialVersionUID = 1L;
    
    private LDAPAgent agent;
    private LDAPGroup group;
    private LDAPRole role;

    public LDAPGroupRole(LDAPAgent agent, LDAPGroup group, LDAPRole role) {
        super(agent.getDN());
        this.agent = agent;
        this.group = group;
        this.role = role;
        Attribute oc = new BasicAttribute(OBJECT_CLASS);
        oc.add("top");
        oc.add(LDAPConstants.GROUP_OF_NAMES);
        
        getLDAPAttributes().put(oc);
        
        getLDAPAttributes().put(LDAPConstants.MEMBER, this.role.getDN());
        
        Attribute theAttribute = getLDAPAttributes().get(LDAPConstants.CN);
        
        if (theAttribute == null) {
            getLDAPAttributes().put(LDAPConstants.CN, this.group.getName());
        } else {
            theAttribute.set(0, this.group.getName());
        }

    }
    
    @Override
    public String getDN() {
        return "cn=" + this.group.getName() + LDAPConstants.COMMA + this.agent.getDN(); 
    }
    
    //@Override
    public IdentityType getMember() {
        return this.agent;
    }

    //@Override
    public Group getGroup() {
        return this.group;
    }

    //@Override
    public Role getRole() {
        return this.role;
    }

}
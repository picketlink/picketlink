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

import static org.picketlink.idm.ldap.internal.LDAPConstants.CN;
import static org.picketlink.idm.ldap.internal.LDAPConstants.COMMA;
import static org.picketlink.idm.ldap.internal.LDAPConstants.CUSTOM_ATTRIBUTE_CREATE_DATE;
import static org.picketlink.idm.ldap.internal.LDAPConstants.CUSTOM_ATTRIBUTE_ENABLED;
import static org.picketlink.idm.ldap.internal.LDAPConstants.CUSTOM_ATTRIBUTE_EXPIRY_DATE;
import static org.picketlink.idm.ldap.internal.LDAPConstants.EQUAL;
import static org.picketlink.idm.ldap.internal.LDAPConstants.MEMBER;
import static org.picketlink.idm.ldap.internal.LDAPConstants.SPACE_STRING;

import java.io.Serializable;
import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 * @author Pedro Silva
 * 
 */
public class LDAPEntry implements DirContext, Serializable {

    private static final long serialVersionUID = 1L;

    private Attributes attributes = new BasicAttributes(true);
    private LDAPCustomAttributes customAttributes = new LDAPCustomAttributes();

    private String dnSuffix;

    public LDAPEntry(String dnSuffix) {
        if (dnSuffix == null) {
            throw new IllegalArgumentException("You must provide a base dn.");
        }

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

    public LDAPCustomAttributes getCustomAttributes() {
        if (this.customAttributes == null) {
            this.customAttributes = new LDAPCustomAttributes();
        }

        // this.customAttributes.addAttribute(CUSTOM_ATTRIBUTE_ENABLED, String.valueOf(isEnabled()));
        // this.customAttributes.addAttribute(CUSTOM_ATTRIBUTE_CREATE_DATE, String.valueOf(getCreatedDate().getTime()));
        //
        
        return this.customAttributes;
    }

    public void setCustomAttributes(LDAPCustomAttributes customAttributes) {
        this.customAttributes = customAttributes;

        if (this.customAttributes != null) {
            Object enabledAttribute = this.customAttributes.getAttribute(CUSTOM_ATTRIBUTE_ENABLED);
            Object createDateAttribute = this.customAttributes.getAttribute(CUSTOM_ATTRIBUTE_CREATE_DATE);
            Object expiryDateAttribute = this.customAttributes.getAttribute(CUSTOM_ATTRIBUTE_EXPIRY_DATE);

            // if (enabledAttribute != null) {
            // this.enabled = Boolean.valueOf(enabledAttribute.toString());
            // }
            //
            // if (createDateAttribute != null) {
            // this.createDate = new Date(Long.valueOf(createDateAttribute.toString()));
            // }
            //
            // if (expiryDateAttribute != null) {
            // this.expirationDate = new Date(Long.valueOf(expiryDateAttribute.toString()));
            // }
        }
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
        }

        try {
            if (!memberAttribute.getAll().hasMoreElements()) {
                memberAttribute.add(SPACE_STRING);
            }
        } catch (NamingException e) {
            e.printStackTrace();
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

    /**
     * <p>
     * Returns the LDAP attributes.
     * </p>
     * 
     * @return
     */
    protected Attributes getLDAPAttributes() {
        return this.attributes;
    }

    protected void setLDAPAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    @Override
    public Attributes getAttributes(String name, String[] ids) throws NamingException {
        if (!name.equals(""))
            throw new NameNotFoundException();
        Attributes answer = new BasicAttributes(true);
        Attribute target;
        for (int i = 0; i < ids.length; i++) {
            target = attributes.get(ids[i]);
            if (target != null) {
                answer.put(target);
            }
        }
        return answer;
    }

    @Override
    public Object lookup(Name name) throws NamingException {
        return null;
    }

    @Override
    public Object lookup(String name) throws NamingException {
        return null;
    }

    @Override
    public void bind(Name name, Object obj) throws NamingException {
    }

    @Override
    public void bind(String name, Object obj) throws NamingException {
    }

    @Override
    public void rebind(Name name, Object obj) throws NamingException {
    }

    @Override
    public void rebind(String name, Object obj) throws NamingException {
    }

    @Override
    public void unbind(Name name) throws NamingException {
    }

    @Override
    public void unbind(String name) throws NamingException {
    }

    @Override
    public void rename(Name oldName, Name newName) throws NamingException {
    }

    @Override
    public void rename(String oldName, String newName) throws NamingException {
    }

    @Override
    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        return null;
    }

    @Override
    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        return null;
    }

    @Override
    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        return null;
    }

    @Override
    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        return null;
    }

    @Override
    public void destroySubcontext(Name name) throws NamingException {
    }

    @Override
    public void destroySubcontext(String name) throws NamingException {
    }

    @Override
    public Context createSubcontext(Name name) throws NamingException {
        return null;
    }

    @Override
    public Context createSubcontext(String name) throws NamingException {
        return null;
    }

    @Override
    public Object lookupLink(Name name) throws NamingException {
        return null;
    }

    @Override
    public Object lookupLink(String name) throws NamingException {
        return null;
    }

    @Override
    public NameParser getNameParser(Name name) throws NamingException {
        return null;
    }

    @Override
    public NameParser getNameParser(String name) throws NamingException {
        return null;
    }

    @Override
    public Name composeName(Name name, Name prefix) throws NamingException {
        return null;
    }

    @Override
    public String composeName(String name, String prefix) throws NamingException {
        return null;
    }

    @Override
    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        return null;
    }

    @Override
    public Object removeFromEnvironment(String propName) throws NamingException {
        return null;
    }

    @Override
    public Hashtable<?, ?> getEnvironment() throws NamingException {
        return null;
    }

    @Override
    public void close() throws NamingException {
    }

    @Override
    public String getNameInNamespace() throws NamingException {
        return null;
    }

    @Override
    public Attributes getAttributes(Name name) throws NamingException {
        return getAttributes(name.toString());
    }

    @Override
    public Attributes getAttributes(String name) throws NamingException {
        return attributes;
    }

    @Override
    public void modifyAttributes(Name name, int mod_op, Attributes attrs) throws NamingException {
    }

    @Override
    public void modifyAttributes(String name, int mod_op, Attributes attrs) throws NamingException {
    }

    @Override
    public void modifyAttributes(Name name, ModificationItem[] mods) throws NamingException {
    }

    @Override
    public void modifyAttributes(String name, ModificationItem[] mods) throws NamingException {
    }

    @Override
    public void bind(Name name, Object obj, Attributes attrs) throws NamingException {
    }

    @Override
    public void bind(String name, Object obj, Attributes attrs) throws NamingException {
    }

    @Override
    public void rebind(Name name, Object obj, Attributes attrs) throws NamingException {
    }

    @Override
    public void rebind(String name, Object obj, Attributes attrs) throws NamingException {
    }

    @Override
    public DirContext createSubcontext(Name name, Attributes attrs) throws NamingException {
        return null;
    }

    @Override
    public DirContext createSubcontext(String name, Attributes attrs) throws NamingException {
        return null;
    }

    @Override
    public DirContext getSchema(Name name) throws NamingException {
        return null;
    }

    @Override
    public DirContext getSchema(String name) throws NamingException {
        return null;
    }

    @Override
    public DirContext getSchemaClassDefinition(Name name) throws NamingException {
        return null;
    }

    @Override
    public DirContext getSchemaClassDefinition(String name) throws NamingException {
        return null;
    }

    @Override
    public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes, String[] attributesToReturn)
            throws NamingException {
        return null;
    }

    @Override
    public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes, String[] attributesToReturn)
            throws NamingException {
        return null;
    }

    @Override
    public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes) throws NamingException {
        return null;
    }

    @Override
    public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes) throws NamingException {
        return null;
    }

    @Override
    public NamingEnumeration<SearchResult> search(Name name, String filter, SearchControls cons) throws NamingException {
        return null;
    }

    @Override
    public NamingEnumeration<SearchResult> search(String name, String filter, SearchControls cons) throws NamingException {
        return null;
    }

    @Override
    public NamingEnumeration<SearchResult> search(Name name, String filterExpr, Object[] filterArgs, SearchControls cons)
            throws NamingException {
        return null;
    }

    @Override
    public NamingEnumeration<SearchResult> search(String name, String filterExpr, Object[] filterArgs, SearchControls cons)
            throws NamingException {
        return null;
    }

    @Override
    public Attributes getAttributes(Name name, String[] attrIds) throws NamingException {
        return null;
    }

}

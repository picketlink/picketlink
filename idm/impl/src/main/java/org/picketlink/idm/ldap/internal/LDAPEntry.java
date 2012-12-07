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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.picketlink.idm.internal.util.Base64;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;

/**
 * <p>An adaptor class that provides barebones implementation of the {@link DirContext}.</p>
 * 
 * @author anil saldhana
 * @since Aug 30, 2012
 */
public abstract class LDAPEntry implements DirContext, IdentityType {

    private static final long serialVersionUID = 1L;

    private Attributes attributes = new BasicAttributes(true);
    private LDAPCustomAttributes customAttributes = new LDAPCustomAttributes();
    private String dnSuffix;
    
    private boolean enabled = true;
    private Date expirationDate;
    private Date createDate = new Date();
    
    public LDAPEntry(String dnSuffix) {
        this.dnSuffix = dnSuffix;
    }

    public String getDN() {
        try {
            return getDN(getLDAPAttributes().get(doGetAttributeForBinding()).get().toString());
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected String doGetAttributeForBinding() {
        return CN;
    }

    public String getDN(String name) {
        return doGetAttributeForBinding() + EQUAL + name + COMMA + this.dnSuffix;
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
    public Attributes getAttributes(Name name, String[] ids) throws NamingException {
        return getAttributes(name.toString(), ids);
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
    public boolean isEnabled() {
        return this.enabled;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public Date getExpirationDate() {
        return this.expirationDate;
    }
    
    @Override
    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public Date getCreatedDate() {
        return this.createDate;
    }

    @Override
    public void setAttribute(org.picketlink.idm.model.Attribute<? extends Serializable> attribute) {
        Serializable value = attribute.getValue();
        
        getLDAPAttributes().put(attribute.getName(), value);
        getCustomAttributes().addAttribute(attribute.getName(), value);
    }

    @Override
    public void removeAttribute(String name) {
        getLDAPAttributes().remove(name);
        getCustomAttributes().removeAttribute(name);
    }

    public LDAPCustomAttributes getCustomAttributes() {
        if (this.customAttributes == null) {
            this.customAttributes = new LDAPCustomAttributes();
        }
        
        this.customAttributes.addAttribute(CUSTOM_ATTRIBUTE_ENABLED, String.valueOf(isEnabled()));
        this.customAttributes.addAttribute(CUSTOM_ATTRIBUTE_CREATE_DATE, String.valueOf(getCreatedDate().getTime()));
        
        if (this.expirationDate != null) {
            this.customAttributes.addAttribute(CUSTOM_ATTRIBUTE_EXPIRY_DATE, String.valueOf(getExpirationDate().getTime()));            
        }

        return this.customAttributes;
    }

    public void setCustomAttributes(LDAPCustomAttributes customAttributes) {
        this.customAttributes = customAttributes;
        
        if (this.customAttributes != null) {
            Object enabledAttribute = this.customAttributes.getAttribute(CUSTOM_ATTRIBUTE_ENABLED);
            Object createDateAttribute = this.customAttributes.getAttribute(CUSTOM_ATTRIBUTE_CREATE_DATE);
            Object expiryDateAttribute = this.customAttributes.getAttribute(CUSTOM_ATTRIBUTE_EXPIRY_DATE);
            
            if (enabledAttribute != null) {
                this.enabled = Boolean.valueOf(enabledAttribute.toString());    
            }
            
            if (createDateAttribute != null) {
                this.createDate = new Date(Long.valueOf(createDateAttribute.toString()));            
            }

            if (expiryDateAttribute != null) {
                this.expirationDate = new Date(Long.valueOf(expiryDateAttribute.toString()));            
            }
}
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Serializable> org.picketlink.idm.model.Attribute<T> getAttribute(String name) {
        try {
            Attribute theAttribute = attributes.get(name);
            Object value = null;

            if (theAttribute != null) {
                value = theAttribute.get();
            } else if (this.customAttributes.getAttributes().containsKey(name)) {
                value = this.customAttributes.getAttribute(name);
            } else {
                return null;
            }

            return new org.picketlink.idm.model.Attribute<T>(name, (T) value);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<org.picketlink.idm.model.Attribute<? extends Serializable>> getAttributes() {
        try {
            Collection<org.picketlink.idm.model.Attribute<? extends Serializable>> attribs = new ArrayList<org.picketlink.idm.model.Attribute<? extends Serializable>>();

            // retrieve all ldap attributes
            NamingEnumeration<? extends Attribute> theAttributes = getLDAPAttributes().getAll();

            while (theAttributes.hasMore()) {
                Attribute anAttribute = theAttributes.next();
                NamingEnumeration<Object> ne = (NamingEnumeration<Object>) anAttribute.getAll();

                List<String> theList = new ArrayList<String>();
                while (ne.hasMoreElements()) {
                    String val = null;
                    Object obj = ne.next();
                    if (obj instanceof byte[]) {
                        val = new String(Base64.encodeBytes((byte[]) obj));
                    } else {
                        val = (String) obj;
                    }
                    theList.add(val);
                }
                String[] valuesArr = new String[theList.size()];
                theList.toArray(valuesArr);

                attribs.add(new org.picketlink.idm.model.Attribute<Serializable>(anAttribute.getID(), valuesArr));
            }

            // retrieve all custom attributes
            Map<String, Object> customAttributes = getCustomAttributes().getAttributes();
            Set<Entry<String, Object>> entrySet = customAttributes.entrySet();

            for (Entry<String, Object> entry : entrySet) {
                attribs.add(new org.picketlink.idm.model.Attribute<Serializable>(entry.getKey(), (Serializable) entry
                        .getValue()));
            }

            return attribs;
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Partition getPartition() {
        return null;
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
    
    public void setDnSuffix(String dnSuffix) {
        this.dnSuffix = dnSuffix;
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
}
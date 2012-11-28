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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

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
import org.picketlink.idm.ldap.internal.LDAPObjectChangedNotification.NType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;

/**
 * An adaptor class that provides barebones implementation of the {@link DirContext}
 *
 * @author anil saldhana
 * @since Aug 30, 2012
 */
public class DirContextAdaptor implements DirContext, IdentityType {

    public static final String COMMA = ",";
    public static final String EQUAL = "=";
    public static final String SPACE_STRING = " ";
    private Attributes attributes = new BasicAttributes(true);

    protected LDAPChangeNotificationHandler handler = null;
    
    protected LDAPUserCustomAttributes customAttributes = new LDAPUserCustomAttributes();

    public void addAllLDAPAttributes(Attributes theAttributes) {
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

    @Override
    public Object lookup(Name name) throws NamingException {
        return null;
    }

//    public void setLDAPChangeNotificationHandler(LDAPChangeNotificationHandler lh) {
//        this.handler = lh;
//    }

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

    /**
     * <p>Returns the LDAP attributes.</p>
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
    public String getKey() {
        return null;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
    
    @Override
    public Date getExpiryDate() {
        return null;
    }
    
    @Override
    public Date getCreatedDate() {
        return null;
    }

    /**
     * <p>Replaces an attribute and reflects the change in the LDAP tree.</p>
     *
     * @param name
     * @param value
     */
    protected void replaceAttribute(String name, String value) {
        attributes.put(name, value);
        Attribute anAttribute = attributes.get(name);

        if (handler != null) {
            handler.handle(new LDAPObjectChangedNotification(this, NType.REPLACE_ATTRIBUTE, anAttribute));
        }
    }

    @Override
    public void setAttribute(org.picketlink.idm.model.Attribute<? extends Serializable> attribute) {
        attributes.put(attribute.getName(), attribute.getValue());
        getCustomAttributes().addAttribute(attribute.getName(), attribute.getValue());
        Attribute anAttribute = attributes.get(attribute.getName());
        if (handler != null) {
            handler.handle(new LDAPObjectChangedNotification(this, NType.ADD_ATTRIBUTE, anAttribute));
        }
    }

    @Override
    public void removeAttribute(String name) {
        Attribute anAttribute = attributes.get(name);
        attributes.remove(name);
        this.customAttributes.removeAttribute(name);
        if (handler != null) {
            handler.handle(new LDAPObjectChangedNotification(this, NType.REMOVE_ATTRIBUTE, anAttribute));
        }
    }
    
    public void setCustomAttribute(String name, String value) {
        // Add into the custom attributes also
        customAttributes.addAttribute(name, value);
//        if (handler != null) {
//            handler.handle(new LDAPObjectChangedNotification(this, NType.CUSTOM_ATTRIBUTE, null));
//        }
    }

    public void setCustomAttribute(String name, String[] values) {
        // Add into the custom attributes also
        customAttributes.addAttribute(name, values);
//        if (handler != null) {
//            handler.handle(new LDAPObjectChangedNotification(this, NType.CUSTOM_ATTRIBUTE, null));
//        }
    }

    public LDAPUserCustomAttributes getCustomAttributes() {
        if (this.customAttributes == null) {
            this.customAttributes = new LDAPUserCustomAttributes();
        }

        return this.customAttributes;
    }

    public void setCustomAttributes(LDAPUserCustomAttributes customAttributes) {
        this.customAttributes = customAttributes;
    }

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
            
            // FIXME need to update this for new attributes API
            /*
            String val = null;
            if (obj instanceof byte[]) {
                val = new String(Base64.encodeBytes((byte[]) obj));
            } else {
                val = (String) obj;
            }
            return val;*/

            return new org.picketlink.idm.model.Attribute<T>(name, (T) value);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO method no longer required?
    /*
    @Override
    public String[] getAttributeValues(String name) {
        try {
            Attribute theAttribute = attributes.get(name);
            if (theAttribute != null) {
                return (String[]) theAttribute.get();
            }
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        return null;
    }*/

    @SuppressWarnings("unchecked")
    @Override
    public Collection<org.picketlink.idm.model.Attribute<? extends Serializable>> getAttributes() {
        try {
            Collection<org.picketlink.idm.model.Attribute<? extends Serializable>> attribs =
                    new ArrayList<org.picketlink.idm.model.Attribute<? extends Serializable>>();

            NamingEnumeration<? extends Attribute> theAttributes = attributes.getAll();

            // FIXME need to fix this to populate attribs variable
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

                //map.put(anAttribute.getID(), valuesArr);
            }
            return attribs;
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Partition getPartition() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
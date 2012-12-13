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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.naming.Binding;
import javax.naming.CommunicationException;
import javax.naming.Context;
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
import javax.naming.ldap.InitialLdapContext;

/**
 * <p>
 * This class provides a set of operations to manage LDAP trees.
 * </p>
 * <p>
 * A different {@link DirContext} is used to perform authentication. The reason is that while managing the ldap tree information
 * bindings are not allowed. Also, instead of creating a new {@link DirContext} each time we reuse it.
 * </p>
 * 
 * @author Anil Saldhana
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class LDAPOperationManager {

    private List<String> managedAttributes = new ArrayList<String>();

    private Properties properties;
    private DirContext context;
    private DirContext authenticationContext;

    public LDAPOperationManager(Properties properties) throws NamingException {
        this.context = new InitialLdapContext(properties, null);
        this.authenticationContext = new InitialLdapContext(properties, null);
        this.properties = properties;
    }

    /**
     * <p>
     * Binds a {@link Object} to the LDAP tree.
     * </p>
     * 
     * @param ldapUser
     */
    public void bind(String dn, Object object) {
        try {
            context.bind(dn, object);
        } catch (NamingException e) {
            if(e instanceof CommunicationException){
                //Discard context and try to recover from LDAP server communication breakage
                try {
                    context.close();
                    context = new InitialLdapContext(properties,null);
                    context.bind(dn, object);
                } catch (NamingException e1) {
                    throw new RuntimeException(e1);
                }
            }else {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * <p>
     * Modifies the given {@link Attribute} instance using the given DN. This method performs a REPLACE_ATTRIBUTE operation.
     * </p>
     * 
     * @param dn
     * @param attribute
     */
    public void modifyAttribute(String dn, Attribute attribute) {
        ModificationItem[] mods = new ModificationItem[] { new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attribute) };
        modifyAttributes(dn, mods);
    }

    /**
     * <p>
     * Removes the given {@link Attribute} instance using the given DN. This method performs a REMOVE_ATTRIBUTE operation.
     * </p>
     * 
     * @param dn
     * @param attribute
     */
    public void removeAttribute(String dn, Attribute attribute) {
        ModificationItem[] mods = new ModificationItem[] { new ModificationItem(DirContext.REMOVE_ATTRIBUTE, attribute) };
        modifyAttributes(dn, mods);
    }

    /**
     * <p>
     * Adds the given {@link Attribute} instance using the given DN. This method performs a ADD_ATTRIBUTE operation.
     * </p>
     * 
     * @param dn
     * @param attribute
     */
    public void addAttribute(String dn, Attribute attribute) {
            ModificationItem[] mods = new ModificationItem[] { new ModificationItem(DirContext.ADD_ATTRIBUTE, attribute) };
            modifyAttributes(dn, mods);
    }

    /**
     * <p>
     * Re-binds a {@link Object} to the LDAP tree.
     * </p>
     * 
     * @param dn
     * @param object
     */
    public void rebind(String dn, Object object) {
        try {
            context.rebind(dn, object);
        } catch (NamingException e) {
            if(e instanceof CommunicationException){
                //Discard context and try to recover from LDAP server communication breakage
                try {
                    context.close();
                    context = new InitialLdapContext(properties,null);
                    context.rebind(dn, object);
                } catch (NamingException e1) {
                    throw new RuntimeException(e1);
                }
            }else {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * <p>
     * Looks up a entry on the LDAP tree with the given DN.
     * </p>
     * 
     * @param dn
     * @return
     * @throws NamingException
     */
    @SuppressWarnings("unchecked")
    public <T> T lookup(String dn) {
        try {
            return (T) context.lookup(dn);
        } catch (NamingException e) {
            return null;
        }
    }

    /**
     * <p>
     * Searches the LDAP tree.
     * </p>
     * 
     * @param baseDN
     * @param attributesToSearch
     * @return
     */
    public <T extends Object> List<T> searchByAttribute(String baseDN, String attributeName, String attributeValue,
            LDAPSearchCallback<T> searchCallback) {
        List<T> result = new ArrayList<T>();

        NamingEnumeration<SearchResult> answer = null;
        
        try {
            Attributes attributesToSearch = new BasicAttributes(true);

            attributesToSearch.put(new BasicAttribute(attributeName, attributeValue));

            answer = this.context.search(baseDN, attributesToSearch);

            while (answer.hasMore()) {
                result.add(searchCallback.processResult(answer.next()));
            }
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            if (answer != null) {
                try {
                    answer.close();
                } catch (NamingException e) {
                }
            }
        }

        return result;
    }

    /**
     * <p>
     * Searches the LDAP tree.
     * </p>
     * 
     * @param baseDN
     * @param attributesToSearch
     * @return
     */
    public NamingEnumeration<SearchResult> search(String baseDN, Attributes attributesToSearch, String[] attributesToReturn) {
        try {
            return context.search(baseDN, attributesToSearch, attributesToReturn);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>
     * Searches the LDAP tree.
     * </p>
     * 
     * @param baseDN
     * @param filter
     * @param attributesToReturn
     * @param searchControls
     * @return
     */
    public NamingEnumeration<SearchResult> search(String baseDN, String filter, String[] attributesToReturn,
            SearchControls searchControls) {
        try {
            return this.context.search(baseDN, filter, attributesToReturn, searchControls);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    public NamingEnumeration<SearchResult> search(String baseDN, String filter) {
        try {
            SearchControls cons = new SearchControls();

            cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
            cons.setReturningObjFlag(true);

            return this.context.search(baseDN, filter, cons);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>
     * Destroys a subcontext with the given DN from the LDAP tree.
     * </p>
     * 
     * @param dn
     */
    public void destroySubcontext(String dn) {
        try {
            destroyRecursively(dn);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void destroyRecursively(String dn) {
        NamingEnumeration<Binding> enumeration = null;
        
        try {
            enumeration = this.context.listBindings(dn);
            
            while (enumeration.hasMore()) {
                Binding binding = enumeration.next();
                String name = binding.getNameInNamespace();
                
                destroyRecursively(name);
            }
            this.context.unbind(dn);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                enumeration.close();
            } catch (Exception e) {
            }
        }        
    }

    /**
     * <p>
     * Checks if the attribute with the given name is a managed attributes. Managed attributes are the ones defined in the
     * underlying schema or those defined in the managed attribute list.
     * </p>
     * 
     * @param attributeName
     * @return
     */
    public boolean isManagedAttribute(String attributeName) {
        if (this.managedAttributes.contains(attributeName)) {
            return true;
        }

        if (checkAttributePresence(attributeName)) {
            this.managedAttributes.add(attributeName);
            return true;
        }

        return false;
    }

    /**
     * <p>
     * Ask the ldap server for the schema for the attribute.
     * </p>
     * 
     * @param attributeName
     * @return
     */
    public boolean checkAttributePresence(String attributeName) {
        try {
            DirContext schema = context.getSchema("");

            DirContext cnSchema = (DirContext) schema.lookup("AttributeDefinition/" + attributeName);
            if (cnSchema != null) {
                return true;
            }
        } catch (Exception e) {
            return false; // Probably an unmanaged attribute
        }

        return false;
    }

    /**
     * <p>
     * Performs a simple authentication using the ginve DN and password to bind to the authentication context.
     * </p>
     * 
     * @param dn
     * @param password
     * @return
     */
    public boolean authenticate(String dn, String password) {
        try {
            this.authenticationContext.addToEnvironment(Context.SECURITY_PRINCIPAL, dn);
            this.authenticationContext.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
            this.authenticationContext.lookup(dn);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private void modifyAttributes(String dn, ModificationItem[] mods){
        try{
            context.modifyAttributes(dn, mods);
        } catch (NamingException e) {
            if(e instanceof CommunicationException){
                //Discard context and try to recover from LDAP server communication breakage
                try {
                    context.close();
                    context = new InitialLdapContext(properties,null);
                    context.modifyAttributes(dn, mods);
                } catch (NamingException e1) {
                    throw new RuntimeException(e1);
                }
            }else {
                throw new RuntimeException(e);
            }
        }
    }
}
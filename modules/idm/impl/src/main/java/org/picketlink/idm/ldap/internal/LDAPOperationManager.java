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

import org.picketlink.common.constants.LDAPConstants;
import org.picketlink.idm.config.LDAPIdentityStoreConfiguration;

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
import javax.naming.ldap.LdapContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static javax.naming.directory.SearchControls.SUBTREE_SCOPE;
import static org.picketlink.common.constants.LDAPConstants.CREATE_TIMESTAMP;
import static org.picketlink.common.constants.LDAPConstants.EQUAL;
import static org.picketlink.common.util.LDAPUtil.convertObjectGUIToByteString;
import static org.picketlink.idm.IDMInternalLog.LDAP_STORE_LOGGER;

/**
 * <p>
 * This class provides a set of operations to manage LDAP trees.
 * </p>
 * <p>
 * A different {@link DirContext} is used to perform authentication. The reason is that while managing the ldap tree
 * information
 * bindings are not allowed. Also, instead of creating a new {@link DirContext} each time we reuse it.
 * </p>
 *
 * @author Anil Saldhana
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class LDAPOperationManager {

    private List<String> managedAttributes = new ArrayList<String>();

    private LdapContext context;
    private DirContext authenticationContext;

    private LDAPIdentityStoreConfiguration config;

    public LDAPOperationManager(LDAPIdentityStoreConfiguration config) throws NamingException {
        this.config = config;
        this.context = constructContext();
        this.authenticationContext = constructContext();
    }

    private LdapContext constructContext() throws NamingException {
        Properties env = new Properties();
        env.setProperty(Context.INITIAL_CONTEXT_FACTORY, this.config.getFactoryName());
        env.setProperty(Context.SECURITY_AUTHENTICATION, this.config.getAuthType());

        String protocol = this.config.getProtocol();

        if (protocol != null) {
            env.setProperty(Context.SECURITY_PROTOCOL, protocol);
        }

        String bindDN = this.config.getBindDN();

        char[] bindCredential = null;

        if (this.config.getBindCredential() != null) {
            bindCredential = this.config.getBindCredential().toCharArray();
        }

        if (bindDN != null) {
            env.setProperty(Context.SECURITY_PRINCIPAL, bindDN);
            env.put(Context.SECURITY_CREDENTIALS, bindCredential);
        }

        String url = this.config.getLdapURL();

        if (url == null) {
            throw new RuntimeException("url");
        }

        env.setProperty(Context.PROVIDER_URL, url);

        // Just dump the additional properties
        Properties additionalProperties = this.config.getAdditionalProperties();
        Set<Object> keys = additionalProperties.keySet();

        for (Object key : keys) {
            env.setProperty((String) key, additionalProperties.getProperty((String) key));
        }

        if (config.isActiveDirectory()) {
            env.put("java.naming.ldap.attributes.binary", LDAPConstants.OBJECT_GUID);
        }

        if (LDAP_STORE_LOGGER.isDebugEnabled()) {
            LDAP_STORE_LOGGER.debugf("Creating LdapContext using properties: [%s]", env);
        }

        return new InitialLdapContext(env, null);
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
            LDAP_STORE_LOGGER.errorf(e, "Could not bind object [%s] using DN [%s]", object, dn);

            if (e instanceof CommunicationException) {
                // Discard context and try to recover from LDAP server communication breakage
                try {
                    context.close();
                    context = constructContext();
                    context.bind(dn, object);
                } catch (NamingException e1) {
                    throw new RuntimeException(e1);
                }
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * <p>
     * Modifies the given {@link Attribute} instance using the given DN. This method performs a REPLACE_ATTRIBUTE
     * operation.
     * </p>
     *
     * @param dn
     * @param attribute
     */
    public void modifyAttribute(String dn, Attribute attribute) {
        ModificationItem[] mods = new ModificationItem[]{new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attribute)};
        modifyAttributes(dn, mods);
    }

    /**
     * <p>
     * Removes the given {@link Attribute} instance using the given DN. This method performs a REMOVE_ATTRIBUTE
     * operation.
     * </p>
     *
     * @param dn
     * @param attribute
     */
    public void removeAttribute(String dn, Attribute attribute) {
        ModificationItem[] mods = new ModificationItem[]{new ModificationItem(DirContext.REMOVE_ATTRIBUTE, attribute)};
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
        ModificationItem[] mods = new ModificationItem[]{new ModificationItem(DirContext.ADD_ATTRIBUTE, attribute)};
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
            LDAP_STORE_LOGGER.errorf("Could not re-bind object [%s] using DN [%s]", object, dn, e);

            if (e instanceof CommunicationException) {
                // Discard context and try to recover from LDAP server communication breakage
                try {
                    context.close();
                    context = constructContext();
                    context.rebind(dn, object);
                } catch (NamingException e1) {
                    throw new RuntimeException(e1);
                }
            } else {
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
     *
     * @return
     *
     * @throws NamingException
     */
    @SuppressWarnings("unchecked")
    public <T> T lookup(String dn) {
        try {
            return (T) context.lookup(dn);
        } catch (NamingException e) {
            LDAP_STORE_LOGGER.errorf(e, "Could not lookup entry using DN [%s]", dn);
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
     *
     * @return
     */
    public <T extends Object> List<T> removeEntryById(String baseDN, String id) {
        List<T> result = new ArrayList<T>();

        NamingEnumeration<SearchResult> answer = null;

        try {
            Attributes attributesToSearch = new BasicAttributes(true);

            attributesToSearch.put(new BasicAttribute(getUniqueIdentifierAttributeName(), id));

            answer = getContext().search(baseDN, attributesToSearch);

            if (answer.hasMore()) {
                SearchResult sr = answer.next();
                destroySubcontext(sr.getNameInNamespace());
            }
        } catch (NamingException e) {
            LDAP_STORE_LOGGER.errorf(e, "Could not remove entry from DN [%s] and id [%s]", baseDN, id);
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

    public NamingEnumeration<SearchResult> search(String baseDN, String filter) throws NamingException {
        SearchControls cons = new SearchControls();

        cons.setSearchScope(SUBTREE_SCOPE);
        cons.setReturningObjFlag(true);
        cons.setReturningAttributes(new String[]{"*", getUniqueIdentifierAttributeName(), CREATE_TIMESTAMP});

        try {
            return getContext().search(baseDN, filter, cons);
        } catch (NamingException e) {
            LDAP_STORE_LOGGER.errorf(e, "Could not query server using DN [%s] and filter [%s]", baseDN, filter);
            throw e;
        }
    }

    public NamingEnumeration<SearchResult> lookupById(String baseDN, String id) {
        String filter = null;

        if (this.config.isActiveDirectory()) {
            String strObjectGUID = "<GUID=" + id + ">";

            try {
                Attributes attributes = this.context.getAttributes(strObjectGUID);
                byte[] objectGUID = (byte[]) attributes.get(LDAPConstants.OBJECT_GUID).get();

                filter = "(&(objectClass=*)(" + getUniqueIdentifierAttributeName() + EQUAL + convertObjectGUIToByteString(objectGUID) + "))";
            } catch (NamingException ne) {
                return createEmptyEnumeration();
            }
        }

        if (filter == null) {
            filter = "(&(objectClass=*)(" + getUniqueIdentifierAttributeName() + EQUAL + id + "))";
        }

        try {
            SearchControls cons = new SearchControls();

            cons.setSearchScope(SUBTREE_SCOPE);
            cons.setReturningObjFlag(false);
            cons.setCountLimit(1);
            cons.setReturningAttributes(new String[]{"*", getUniqueIdentifierAttributeName(), CREATE_TIMESTAMP});

            return getContext().search(baseDN, filter, cons);
        } catch (NamingException e) {
            LDAP_STORE_LOGGER.errorf(e, "Could not query server using DN [%s] and filter [%s]", baseDN, filter);
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
            NamingEnumeration<Binding> enumeration = null;

            try {
                enumeration = getContext().listBindings(dn);

                while (enumeration.hasMore()) {
                    Binding binding = enumeration.next();
                    String name = binding.getNameInNamespace();

                    destroySubcontext(name);
                }

                getContext().unbind(dn);
            } catch (NamingException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    enumeration.close();
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            LDAP_STORE_LOGGER.errorf(e, "Could not unbind DN [%s]", dn);
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>
     * Checks if the attribute with the given name is a managed attributes. Managed attributes are the ones defined in
     * the
     * underlying schema or those defined in the managed attribute list.
     * </p>
     *
     * @param attributeName
     *
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
     *
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
     *
     * @return
     */
    public boolean authenticate(String dn, String password) {
        try {
            this.authenticationContext.addToEnvironment(Context.SECURITY_PRINCIPAL, dn);
            this.authenticationContext.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
            this.authenticationContext.lookup(dn);
        } catch (Exception e) {
            if (LDAP_STORE_LOGGER.isDebugEnabled()) {
                LDAP_STORE_LOGGER.debugf(e, "Authentication failed for DN [%s]", dn);
            }

            return false;
        }

        return true;
    }

    private void modifyAttributes(String dn, ModificationItem[] mods) {
        try {
            if (LDAP_STORE_LOGGER.isDebugEnabled()) {
                LDAP_STORE_LOGGER.debugf("Modifying attributes for entry [%s]: [", dn);

                for (ModificationItem item: mods) {
                    LDAP_STORE_LOGGER.debugf("  Op [%s]: %s = %s", item.getModificationOp(), item.getAttribute().getID(), item.getAttribute().get());
                }

                LDAP_STORE_LOGGER.debugf("]");
            }

            context.modifyAttributes(dn, mods);
        } catch (NamingException e) {
            LDAP_STORE_LOGGER.errorf(e, "Could not modify attribute for DN [%s].", dn);
            if (e instanceof CommunicationException) {
                // Discard context and try to recover from LDAP server communication breakage
                try {
                    context.close();
                    context = constructContext();
                    context.modifyAttributes(dn, mods);
                } catch (NamingException e1) {
                    throw new RuntimeException(e1);
                }
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public void createSubContext(String name, Attributes attributes) {
        try {
            if (LDAP_STORE_LOGGER.isDebugEnabled()) {
                LDAP_STORE_LOGGER.debugf("Creating entry [%s] with attributes: [", name);

                NamingEnumeration<? extends Attribute> all = attributes.getAll();

                while (all.hasMore()) {
                    Attribute attribute = all.next();

                    LDAP_STORE_LOGGER.debugf("  %s = %s", attribute.getID(), attribute.get());
                }

                LDAP_STORE_LOGGER.debugf("]");
            }

            getContext().createSubcontext(name, attributes);
        } catch (NamingException e) {
            LDAP_STORE_LOGGER.errorf(e, "Could not create entry [%s].", name);
            throw new RuntimeException("Error creating subcontext [" + name + "]", e);
        }
    }

    private LdapContext getContext() {
        return this.context;
    }

    private String getUniqueIdentifierAttributeName() {
        return this.config.getUniqueIdentifierAttributeName();
    }

    private NamingEnumeration<SearchResult> createEmptyEnumeration() {
        return new NamingEnumeration<SearchResult>() {
            @Override
            public SearchResult next() throws NamingException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean hasMore() throws NamingException {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void close() throws NamingException {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public boolean hasMoreElements() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public SearchResult nextElement() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
    }

}
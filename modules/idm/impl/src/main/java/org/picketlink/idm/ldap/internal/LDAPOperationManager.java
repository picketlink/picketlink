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
import org.picketlink.common.util.LDAPUtil;
import org.picketlink.idm.IDMLog;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.config.LDAPIdentityStoreConfiguration;
import org.picketlink.idm.config.LDAPMappingConfiguration;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static javax.naming.directory.SearchControls.SUBTREE_SCOPE;
import static org.picketlink.common.constants.LDAPConstants.CREATE_TIMESTAMP;
import static org.picketlink.common.constants.LDAPConstants.EQUAL;
import static org.picketlink.common.util.LDAPUtil.convertObjectGUIToByteString;
import static org.picketlink.idm.IDMInternalLog.LDAP_STORE_LOGGER;
import static org.picketlink.idm.IDMInternalMessages.MESSAGES;

/**
 * <p>This class provides a set of operations to manage LDAP trees.</p>
 *
 * @author Anil Saldhana
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class LDAPOperationManager {

    private final LDAPIdentityStoreConfiguration config;
    private final Map<String, Object> connectionProperties;

    public LDAPOperationManager(LDAPIdentityStoreConfiguration config) throws NamingException {
        this.config = config;
        this.connectionProperties = Collections.unmodifiableMap(createConnectionProperties());
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
     * Searches the LDAP tree.
     * </p>
     *
     * @param baseDN
     * @param id
     *
     * @return
     */
    public void removeEntryById(final String baseDN, final String id) {
        try {
            final Attributes attributesToSearch = new BasicAttributes(true);

            attributesToSearch.put(new BasicAttribute(getUniqueIdentifierAttributeName(), id));

            execute(new LdapOperation<Void>() {
                @Override
                public Void execute(LdapContext context) throws NamingException {
                    NamingEnumeration<SearchResult> result = context.search(baseDN, attributesToSearch);

                    if (result.hasMore()) {
                        SearchResult sr = result.next();
                        destroySubcontext(context, sr.getNameInNamespace());
                    }

                    result.close();

                    return null;
                }
            });
        } catch (NamingException e) {
            LDAP_STORE_LOGGER.errorf(e, "Could not remove entry from DN [%s] and id [%s]", baseDN, id);
            throw new RuntimeException(e);
        }
    }

    public List<SearchResult> search(final String baseDN, final String filter, LDAPMappingConfiguration mappingConfiguration) throws NamingException {
        final List<SearchResult> result = new ArrayList<SearchResult>();
        final SearchControls cons = new SearchControls();

        cons.setSearchScope(SUBTREE_SCOPE);
        cons.setReturningObjFlag(false);

        List<String> returningAttributes = getReturningAttributes(mappingConfiguration);

        cons.setReturningAttributes(returningAttributes.toArray(new String[returningAttributes.size()]));

        try {
            return execute(new LdapOperation<List<SearchResult>>() {
                @Override
                public List<SearchResult> execute(LdapContext context) throws NamingException {
                    NamingEnumeration<SearchResult> search = context.search(baseDN, filter, cons);

                    while (search.hasMoreElements()) {
                        result.add(search.nextElement());
                    }

                    search.close();

                    return result;
                }
            });
        } catch (NamingException e) {
            LDAP_STORE_LOGGER.errorf(e, "Could not query server using DN [%s] and filter [%s]", baseDN, filter);
            throw e;
        }
    }

    public String getFilterById(String baseDN, String id) {
        String filter = null;

        if (this.config.isActiveDirectory()) {
            final String strObjectGUID = "<GUID=" + id + ">";

            try {
                Attributes attributes = execute(new LdapOperation<Attributes>() {
                    @Override
                    public Attributes execute(LdapContext context) throws NamingException {
                        return context.getAttributes(strObjectGUID);
                    }
                });

                byte[] objectGUID = (byte[]) attributes.get(LDAPConstants.OBJECT_GUID).get();

                filter = "(&(objectClass=*)(" + getUniqueIdentifierAttributeName() + EQUAL + convertObjectGUIToByteString(objectGUID) + "))";
            } catch (NamingException ne) {
                return filter;
            }
        }

        if (filter == null) {
            filter = "(&(objectClass=*)(" + getUniqueIdentifierAttributeName() + EQUAL + id + "))";
        }

        return filter;
    }

    public SearchResult lookupById(final String baseDN, final String id, final LDAPMappingConfiguration mappingConfiguration) {
        final String filter = getFilterById(baseDN, id);

        if (filter != null) {
            try {
                final SearchControls cons = new SearchControls();

                cons.setSearchScope(SUBTREE_SCOPE);
                cons.setReturningObjFlag(false);
                cons.setCountLimit(1);

                List<String> returningAttributes = getReturningAttributes(mappingConfiguration);

                cons.setReturningAttributes(returningAttributes.toArray(new String[returningAttributes.size()]));

                return execute(new LdapOperation<SearchResult>() {
                    @Override
                    public SearchResult execute(LdapContext context) throws NamingException {
                        NamingEnumeration<SearchResult> search = context.search(baseDN, filter, cons);

                        try {
                            if (search.hasMoreElements()) {
                                return search.next();
                            }
                        } finally {
                            if (search != null) {
                                search.close();
                            }
                        }

                        return null;
                    }
                });
            } catch (NamingException e) {
                LDAP_STORE_LOGGER.errorf(e, "Could not query server using DN [%s] and filter [%s]", baseDN, filter);
                throw new RuntimeException(e);
            }
        }

        return null;
    }

    /**
     * <p>
     * Destroys a subcontext with the given DN from the LDAP tree.
     * </p>
     *
     * @param dn
     */
    private void destroySubcontext(LdapContext context, final String dn) {
        try {
            NamingEnumeration<Binding> enumeration = null;

            try {
                enumeration = context.listBindings(dn);

                while (enumeration.hasMore()) {
                    Binding binding = enumeration.next();
                    String name = binding.getNameInNamespace();

                    destroySubcontext(context, name);
                }

                context.unbind(dn);
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
     * Performs a simple authentication using the ginve DN and password to bind to the authentication context.
     * </p>
     *
     * @param dn
     * @param password
     *
     * @return
     */
    public boolean authenticate(String dn, String password) {
        InitialContext authCtx = null;

        try {
            Hashtable<String, Object> env = new Hashtable<String, Object>(this.connectionProperties);

            env.put(Context.SECURITY_PRINCIPAL, dn);
            env.put(Context.SECURITY_CREDENTIALS, password);

            // Never use connection pool to prevent password caching
            env.put("com.sun.jndi.ldap.connect.pool", "false");

            authCtx = new InitialLdapContext(env, null);

            return true;
        } catch (Exception e) {
            if (LDAP_STORE_LOGGER.isDebugEnabled()) {
                LDAP_STORE_LOGGER.debugf(e, "Authentication failed for DN [%s]", dn);
            }

            return false;
        } finally {
            if (authCtx != null) {
                try {
                    authCtx.close();
                } catch (NamingException e) {

                }
            }
        }
    }

    private void modifyAttributes(final String dn, final ModificationItem[] mods) {
        try {
            if (LDAP_STORE_LOGGER.isDebugEnabled()) {
                LDAP_STORE_LOGGER.debugf("Modifying attributes for entry [%s]: [", dn);

                for (ModificationItem item : mods) {
                    LDAP_STORE_LOGGER.debugf("  Op [%s]: %s = %s", item.getModificationOp(), item.getAttribute().getID(), item.getAttribute().get());
                }

                LDAP_STORE_LOGGER.debugf("]");
            }

            execute(new LdapOperation<Void>() {
                @Override
                public Void execute(LdapContext context) throws NamingException {
                    context.modifyAttributes(dn, mods);
                    return null;
                }
            });
        } catch (NamingException e) {
            LDAP_STORE_LOGGER.errorf(e, "Could not modify attribute for DN [%s].", dn);
            throw new IdentityManagementException("Could not modify attribute for DN [" + dn + "]", e);
        }
    }

    public void createSubContext(final String name, final Attributes attributes) {
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

            execute(new LdapOperation<Void>() {
                @Override
                public Void execute(LdapContext context) throws NamingException {
                    DirContext subcontext = context.createSubcontext(name, attributes);

                    subcontext.close();

                    return null;
                }
            });
        } catch (NamingException e) {
            LDAP_STORE_LOGGER.errorf(e, "Could not create entry [%s].", name);
            throw new IdentityManagementException("Error creating subcontext [" + name + "]", e);
        }
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

    public Attributes getAttributes(final String entryUUID, final String baseDN, LDAPMappingConfiguration mappingConfiguration) {
        SearchResult search = lookupById(baseDN, entryUUID, mappingConfiguration);

        if (search == null) {
            throw MESSAGES.storeLdapEntryNotFoundWithId(entryUUID, baseDN);
        }

        return search.getAttributes();
    }

    public String decodeEntryUUID(final Object entryUUID) {
        String id;

        if (this.config.isActiveDirectory()) {
            id = LDAPUtil.decodeObjectGUID((byte[]) entryUUID);
        } else {
            id = entryUUID.toString();
        }

        return id;
    }

    private LdapContext createLdapContext() throws NamingException {
        return new InitialLdapContext(new Hashtable<Object, Object>(this.connectionProperties), null);
    }

    private Map<String, Object> createConnectionProperties() {
        HashMap<String, Object> env = new HashMap<String, Object>();

        env.put(Context.INITIAL_CONTEXT_FACTORY, this.config.getFactoryName());
        env.put(Context.SECURITY_AUTHENTICATION, this.config.getAuthType());

        String protocol = this.config.getProtocol();

        if (protocol != null) {
            env.put(Context.SECURITY_PROTOCOL, protocol);
        }

        String bindDN = this.config.getBindDN();

        char[] bindCredential = null;

        if (this.config.getBindCredential() != null) {
            bindCredential = this.config.getBindCredential().toCharArray();
        }

        if (bindDN != null) {
            env.put(Context.SECURITY_PRINCIPAL, bindDN);
            env.put(Context.SECURITY_CREDENTIALS, bindCredential);
        }

        String url = this.config.getLdapURL();

        if (url == null) {
            throw new RuntimeException("url");
        }

        env.put(Context.PROVIDER_URL, url);

        // Just dump the additional properties
        Properties additionalProperties = this.config.getConnectionProperties();

        if (additionalProperties != null) {
            for (Object key : additionalProperties.keySet()) {
                env.put(key.toString(), additionalProperties.getProperty(key.toString()));
            }
        }

        if (config.isActiveDirectory()) {
            env.put("java.naming.ldap.attributes.binary", LDAPConstants.OBJECT_GUID);
        }

        if (LDAP_STORE_LOGGER.isDebugEnabled()) {
            LDAP_STORE_LOGGER.debugf("Creating LdapContext using properties: [%s]", env);
        }

        return env;
    }

    private <R> R execute(LdapOperation<R> operation) throws NamingException {
        LdapContext context = null;

        try {
            context = createLdapContext();
            return operation.execute(context);
        } catch (NamingException ne) {
            IDMLog.IDENTITY_STORE_LOGGER.error("Could not create Ldap context.", ne);
            throw ne;
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException ne) {
                    IDMLog.IDENTITY_STORE_LOGGER.error("Could not close Ldap context.", ne);
                }
            }
        }
    }

    private interface LdapOperation<R> {
        R execute(LdapContext context) throws NamingException;
    }

    private List<String> getReturningAttributes(final LDAPMappingConfiguration mappingConfiguration) {
        List<String> returningAttributes = new ArrayList<String>();

        if (mappingConfiguration != null) {
            returningAttributes.addAll(mappingConfiguration.getMappedProperties().values());

            returningAttributes.add(mappingConfiguration.getParentMembershipAttributeName());

            for (LDAPMappingConfiguration relationshipConfig : this.config.getRelationshipConfigs()) {
                if (relationshipConfig.getRelatedAttributedType().equals(mappingConfiguration.getMappedClass())) {
                    returningAttributes.addAll(relationshipConfig.getMappedProperties().values());
                }
            }
        } else {
            returningAttributes.add("*");
        }

        returningAttributes.add(getUniqueIdentifierAttributeName());
        returningAttributes.add(CREATE_TIMESTAMP);
        returningAttributes.add(LDAPConstants.OBJECT_CLASS);

        return returningAttributes;
    }
}
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

import static javax.naming.directory.DirContext.REPLACE_ATTRIBUTE;
import static org.picketlink.idm.ldap.internal.LDAPConstants.CN;
import static org.picketlink.idm.ldap.internal.LDAPConstants.MEMBER;
import static org.picketlink.idm.ldap.internal.LDAPConstants.OBJECT_CLASS;
import static org.picketlink.idm.ldap.internal.LDAPConstants.UID;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
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

import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.credential.Credential;
import org.picketlink.idm.credential.PasswordCredential;
import org.picketlink.idm.credential.X509CertificateCredential;
import org.picketlink.idm.internal.util.Base64;
import org.picketlink.idm.model.DefaultMembership;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Membership;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;

/**
 * An IdentityStore implementation backed by an LDAP directory
 * 
 * @author Shane Bryzak
 * @author Anil Saldhana
 */
public class LDAPIdentityStore implements IdentityStore<LDAPConfiguration> {
//, LDAPChangeNotificationHandler,
//        ManagedAttributeLookup 

    private static final String USER_CERTIFICATE_ATTRIBUTE = "usercertificate";
    private static final String USER_PASSWORD_ATTRIBUTE = "userpassword";
    public final String COMMA = ",";
    public final String EQUAL = "=";

    protected DirContext ctx = null;
    protected String userDNSuffix, roleDNSuffix, groupDNSuffix;
    protected boolean isActiveDirectory = false;

    protected List<String> managedAttributes = new ArrayList<String>();

    protected LDAPConfiguration ldapConfiguration = null;
    
    @Override
    public void setup(LDAPConfiguration config, IdentityStoreInvocationContext context) {
        configure(config);
    }

    @Override
    public LDAPConfiguration getConfig() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IdentityStoreInvocationContext getContext() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public void configure(IdentityStoreConfiguration configuration) throws SecurityConfigurationException {
        if (!(configuration instanceof LDAPConfiguration)) {
            throw new IllegalArgumentException("Can only pass instance of LDAPConfiguration to LDAPIdentityStore");
        }

        LDAPConfiguration config = (LDAPConfiguration) configuration;

        this.ldapConfiguration = config;
        userDNSuffix = config.getUserDNSuffix();
        roleDNSuffix = config.getRoleDNSuffix();
        groupDNSuffix = config.getGroupDNSuffix();
        isActiveDirectory = config.isActiveDirectory();

        constructContext();
    }

    @Override
    public void createUser(User user) {
        if (user.getId() == null) {
            throw new RuntimeException("No identifier was provided. You should provide one before storing the user.");
        }

        LDAPUser ldapUser = null;

        if (!(user instanceof LDAPUser)) {
            ldapUser = convert(user);
        } else {
            ldapUser = (LDAPUser) user;
        }

        // ldapUser.setLookup(this);
        // ldapUser.setLDAPChangeNotificationHandler(this);

        bind(getUserDN(ldapUser), ldapUser);
        bind(getCustomAttributesDN(ldapUser), ldapUser.getCustomAttributes());
    }

    @Override
    public void removeUser(User user) {
        // Look for custom attributes
        LDAPUser ldapUser = (LDAPUser) getUser(user.getId());

        String customDN = getCustomAttributesDN(ldapUser);

        try {
            lookup(customDN);
            destroySubcontext(customDN);
        } catch (Exception ignore) {
        }

        destroySubcontext(getUserDN(ldapUser));
    }

    @Override
    public User getUser(String name) {
        LDAPUser user = null;

        try {
            Attributes matchAttrs = new BasicAttributes(true); // ignore attribute name case

            matchAttrs.put(new BasicAttribute(LDAPConstants.UID, name));

            NamingEnumeration<SearchResult> answer = ctx.search(userDNSuffix, matchAttrs);

            if (answer.hasMore()) {
                SearchResult sr = answer.next();

                // user.setLookup(this);

                // user.setLDAPChangeNotificationHandler(this);

                // Get the custom attributes

                user = new LDAPUser(sr.getAttributes());
                
                user.setCustomAttributes(getCustomAttributes(user));
            }
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }

        return user;
    }

    @Override
    public void createGroup(Group group) {
        ensureGroupDNExists();
        LDAPGroup ldapGroup = new LDAPGroup();
        // ldapGroup.setLDAPChangeNotificationHandler(this);

        ldapGroup.setName(group.getName());
        ldapGroup.setGroupDNSuffix(groupDNSuffix);

        try {
            ctx.bind(ldapGroup.getDN(), ldapGroup);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }

        if (group.getParentGroup() != null) {
            ldapGroup.setParentGroup(group.getParentGroup());

            LDAPGroup parentGroup = (LDAPGroup) getGroup(group.getParentGroup().getName());
            ldapGroup.setParentGroup(parentGroup);
            parentGroup.addChildGroup(ldapGroup);
            try {
                ctx.rebind(parentGroup.getDN(), parentGroup);
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void removeGroup(Group group) {
        LDAPGroup ldapGroup = (LDAPGroup) getGroup(group.getName());
        
        if (ldapGroup == null) {
            throw new RuntimeException("There is no group with name [" + group.getName() + "]");
        }
        
        try {
            ctx.destroySubcontext(ldapGroup.getDN());
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Group getGroup(String name) {
        LDAPGroup ldapGroup = null;
        try {
            Attributes matchAttrs = new BasicAttributes(true); // ignore attribute name case
            matchAttrs.put(new BasicAttribute(CN, name));

            NamingEnumeration<SearchResult> answer = ctx.search(groupDNSuffix, matchAttrs);
            while (answer.hasMore()) {
                SearchResult sr = answer.next();
                Attributes attributes = sr.getAttributes();
                ldapGroup = new LDAPGroup();
                ldapGroup.setGroupDNSuffix(groupDNSuffix);
                ldapGroup.addAllLDAPAttributes(attributes);
                // Let us work out any parent groups for this group exist
                Group parentGroup = getParentGroup(ldapGroup);
                if (parentGroup != null) {
                    ldapGroup.setParentGroup(parentGroup);
                }
                // ldapGroup.setLDAPChangeNotificationHandler(this);
            }
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        return ldapGroup;
    }

    @Override
    public void createRole(Role role) {
        LDAPRole ldapRole = new LDAPRole();
        // ldapRole.setLDAPChangeNotificationHandler(this);

        ldapRole.setName(role.getName());
        ldapRole.setRoleDNSuffix(roleDNSuffix);

        try {
            ctx.bind(ldapRole.getDN(), role);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeRole(Role role) {
        try {
            LDAPRole ldapRole = (LDAPRole) getRole(role.getName());

            ctx.destroySubcontext(ldapRole.getDN());
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Role getRole(String role) {
        try {
            Attributes matchAttrs = new BasicAttributes(true); // ignore attribute name case

            matchAttrs.put(new BasicAttribute(CN, role));

            NamingEnumeration<SearchResult> searchResult = ctx.search(roleDNSuffix, matchAttrs);

            while (searchResult.hasMore()) {
                SearchResult result = searchResult.next();

                return new LDAPRole(result.getAttributes(), this.roleDNSuffix);
            }
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public Membership createMembership(IdentityType member, Group group, Role role) {
        if (member instanceof User) {
            final LDAPRole ldapRole = (LDAPRole) getRole(role.getName());
            final LDAPUser ldapUser = (LDAPUser) getUser(((User) member).getId());
            final LDAPGroup ldapGroup = (LDAPGroup) getGroup(group.getName());

            ldapRole.addUser(getUserDN(ldapUser));
            ldapGroup.addRole(ldapRole);
            ldapGroup.addUser(getUserDN(ldapUser));

            try {
                ctx.modifyAttributes(ldapRole.getDN(), REPLACE_ATTRIBUTE, ldapRole.getAttributes(MEMBER));
            } catch (NamingException e) {
                throw new RuntimeException("Error while modifying members of role [" + ldapRole.getName() + "].", e);
            }

            try {
                ctx.modifyAttributes(ldapGroup.getDN(), REPLACE_ATTRIBUTE, ldapGroup.getAttributes(MEMBER));
            } catch (NamingException e) {
                throw new RuntimeException("Error while modifying members of group [" + ldapGroup.getName() + "].", e);
            }

            return new DefaultMembership(ldapUser, ldapRole, ldapGroup);
        } else if (member instanceof Group) {
            // FIXME implement Group membership, or return null
            return null;
        } else {
            throw new IllegalArgumentException("The member parameter must be an instance of User or Group");
        }
    }

    @Override
    public void removeMembership(IdentityType member, Group group, Role role) {
        if (member instanceof User) {
            final LDAPRole ldapRole = (LDAPRole) getRole(role.getName());
            final LDAPUser ldapUser = (LDAPUser) getUser(((User) member).getId());
            final LDAPGroup ldapGroup = (LDAPGroup) getGroup(group.getName());

            ldapRole.removeUser(getUserDN(ldapUser));
            ldapGroup.removeRole(ldapRole);
        } else if (member instanceof Group) {
            // FIXME implement Group membership if supported
        }
    }

    @Override
    public Membership getMembership(IdentityType member, Group group, Role role) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.picketlink.idm.spi.IdentityStore#setAttribute(org.picketlink.idm.model.User, java.lang.String,
     * java.lang.String[])
     */
    @Override
    public void setAttribute(IdentityType identity, org.picketlink.idm.model.Attribute<? extends Serializable> attribute) {
        if (identity instanceof User) {
            LDAPUser ldapUser = null;

            if (identity instanceof LDAPUser) {
                ldapUser = (LDAPUser) identity;
            } else {
                ldapUser = (LDAPUser) getUser(((User) identity).getId());
            }
            if (isManaged(attribute.getName())) {
                ldapUser.setAttribute(attribute);
            } else {
                // FIXME
                // ldapUser.setCustomAttribute(attribute.getName(), attribute.getValue());
            }
        } else if (identity instanceof Group) {
            LDAPGroup ldapGroup = null;
            if (identity instanceof LDAPGroup) {
                ldapGroup = (LDAPGroup) identity;
            } else {
                ldapGroup = (LDAPGroup) getGroup(((Group) identity).getName());
            }
            ldapGroup.setAttribute(attribute);
        } else if (identity instanceof Role) {
            LDAPRole ldapRole = null;
            if (identity instanceof LDAPGroup) {
                ldapRole = (LDAPRole) identity;
            } else {
                ldapRole = (LDAPRole) getRole(((Role) identity).getName());
            }
            ldapRole.setAttribute(attribute);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.picketlink.idm.spi.IdentityStore#removeAttribute(org.picketlink.idm.model.User, java.lang.String)
     */
    @Override
    public void removeAttribute(IdentityType identity, String name) {
        if (identity instanceof User) {
            if (identity instanceof LDAPUser == false) {
                throw new RuntimeException("Wrong type:" + identity);
            }
            LDAPUser ldapUser = (LDAPUser) identity;
            ldapUser.removeAttribute(name);
        } else if (identity instanceof Group) {
            LDAPGroup ldapGroup = null;
            if (identity instanceof LDAPGroup) {
                ldapGroup = (LDAPGroup) identity;
            } else {
                ldapGroup = (LDAPGroup) getGroup(((Group) identity).getName());
            }
            ldapGroup.removeAttribute(name);
        } else if (identity instanceof Role) {
            LDAPRole ldapRole = null;
            if (identity instanceof LDAPGroup) {
                ldapRole = (LDAPRole) identity;
            } else {
                ldapRole = (LDAPRole) getRole(((Role) identity).getName());
            }
            ldapRole.removeAttribute(name);
        }
    }

    protected void ensureGroupDNExists() {
        try {
            Object obj = ctx.lookup(groupDNSuffix);

            if (obj == null) {
                createGroupDN();
            }

            return; // exists
        } catch (NamingException e) {
            if (e instanceof NameNotFoundException) {
                createGroupDN();
                return;
            }

            throw new RuntimeException(e);
        }
    }

    protected void createGroupDN() {
        try {
            Attributes attributes = new BasicAttributes(true);

            Attribute oc = new BasicAttribute(OBJECT_CLASS);
            oc.add("top");
            oc.add("organizationalUnit");
            attributes.put(oc);
            ctx.createSubcontext(groupDNSuffix, attributes);
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }

    /**
     * <p>
     * Returns the parent group for the given child group.
     * </p>
     * 
     * @param childGroup
     * @return
     */
    protected Group getParentGroup(LDAPGroup childGroup) {
        Attributes matchAttrs = new BasicAttributes(true);
        matchAttrs.put(new BasicAttribute(MEMBER, CN + EQUAL + childGroup.getName() + COMMA + groupDNSuffix));
        // Search for objects with these matching attributes
        try {
            NamingEnumeration<SearchResult> answer = ctx.search(groupDNSuffix, matchAttrs, new String[] { CN });
            while (answer.hasMoreElements()) {
                SearchResult sr = (SearchResult) answer.nextElement();
                Attributes attributes = sr.getAttributes();
                String cn = (String) attributes.get(CN).get();
                return getGroup(cn);
            }
        } catch (NamingException e) {
            throw new RuntimeException("Error looking parent group for [" + childGroup.getDN() + "]", e);
        }

        return null;
    }

    public boolean isManaged(String attributeName) {
        if (managedAttributes.contains(attributeName)) {
            return true;
        } else {
            if (checkDirectoryServerForAttributePresence(attributeName)) {
                managedAttributes.add(attributeName);
                return true;
            }
        }
        return false;
    }

    /**
     * Ask the ldap server for the schema for the attribute
     * 
     * @param attributeName
     * @return
     */
    private boolean checkDirectoryServerForAttributePresence(String attributeName) {

        try {
            DirContext schema = ctx.getSchema("");

            DirContext cnSchema = (DirContext) schema.lookup("AttributeDefinition/" + attributeName);
            if (cnSchema != null) {
                return true;
            }
        } catch (Exception e) {
            return false; // Probably an unmanaged attribute
        }

        return false;
    }

    @Override
    public boolean validateCredential(User user, Credential credential) {
        if (credential instanceof PasswordCredential) {
            PasswordCredential pc = (PasswordCredential) credential;
            boolean valid = false;
            // We have to bind
            try {
                LDAPUser ldapUser = null;
                if (user instanceof LDAPUser == false) {
                    ldapUser = convert(user);
                } else {
                    ldapUser = (LDAPUser) user;
                }

                String filter = "(&(objectClass=inetOrgPerson)(uid={0}))";
                SearchControls ctls = new SearchControls();
                ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                ctls.setReturningAttributes(new String[0]);
                ctls.setReturningObjFlag(true);
                NamingEnumeration<SearchResult> enm = ctx.search(userDNSuffix, filter, new String[] { ldapUser.getId() }, ctls);

                String dn = null;
                if (enm.hasMore()) {
                    SearchResult result = enm.next();
                    dn = result.getNameInNamespace();

                    System.out.println("dn: " + dn);
                }

                if (dn == null || enm.hasMore()) {
                    // uid not found or not unique
                    throw new NamingException("Authentication failed");
                }

                // Step 3: Bind with found DN and given password
                ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, dn);
                ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, pc.getPassword());
                lookup(dn);
                valid = true;
            } catch (NamingException e) {
                // Ignore
            }

            constructContext();
            return valid;
        } else {
            throwsNotSupportedCredentialType(credential);
        }

        return false;
    }

    @Override
    public void updateCredential(User user, Credential credential) {
        if (credential instanceof PasswordCredential) {
            PasswordCredential pc = (PasswordCredential) credential;
            if (isActiveDirectory) {
                updateADPassword((LDAPUser) user, pc.getPassword());
            } else {
                LDAPUser ldapuser = null;
                if (user instanceof LDAPUser == false) {
                    ldapuser = convert(user);
                } else {
                    ldapuser = (LDAPUser) user;
                }

                ModificationItem[] mods = new ModificationItem[1];

                Attribute mod0 = new BasicAttribute(USER_PASSWORD_ATTRIBUTE, pc.getPassword());

                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, mod0);

                try {
                    ctx.modifyAttributes(getUserDN(ldapuser), mods);
                } catch (NamingException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (credential instanceof X509CertificateCredential) {
            X509CertificateCredential cc = (X509CertificateCredential) credential;
            try {
                LDAPUser ldapUser = (LDAPUser) user;
                ldapUser.setAttribute(new org.picketlink.idm.model.Attribute<String>(USER_CERTIFICATE_ATTRIBUTE, new String(
                        Base64.encodeBytes(cc.getCertificate().getEncoded()))));
                ModificationItem[] mods = new ModificationItem[1];

                byte[] certbytes = cc.getCertificate().getEncoded();

                mods[0] = new ModificationItem(REPLACE_ATTRIBUTE, new BasicAttribute(USER_CERTIFICATE_ATTRIBUTE, certbytes));

                // Perform the update
                ctx.modifyAttributes(getUserDN(ldapUser), mods);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throwsNotSupportedCredentialType(credential);
        }
    }
    
    @Override
    public List<IdentityType> fetchQueryResults(Map<QueryParameter, Object> parameters) {
        // TODO implement this
        return null;
    }

    @Override
    public void updateUser(User user) {
        LDAPUser ldapUser = (LDAPUser) user;

        try {
            String fullName = ldapUser.getFirstName();

            if (ldapUser.getLastName() != null) {
                fullName = fullName + " " + ldapUser.getLastName();
            }

            ldapUser.setFullName(fullName);

            LDAPUser storedUser = (LDAPUser) getUser(user.getId());

            NamingEnumeration<? extends Attribute> storedAttributes = storedUser.getLDAPAttributes().getAll();

            // check for attributes to replace or remove
            while (storedAttributes.hasMore()) {
                Attribute storedAttribute = storedAttributes.next();
                Attribute updatedAttribute = ldapUser.getLDAPAttributes().get(storedAttribute.getID());

                // if the stored attribute exists in the updated attributes list, replace it. Otherwise remove it from the store.
                if (updatedAttribute != null) {
                    ModificationItem[] mods = new ModificationItem[] { new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                            updatedAttribute) };
                    ctx.modifyAttributes(getUserDN(ldapUser), mods);
                } else {
                    ModificationItem[] mods = new ModificationItem[] { new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                            storedAttribute) };
                    ctx.modifyAttributes(getUserDN(ldapUser), mods);
                }
            }

            NamingEnumeration<? extends Attribute> enumUpdatedAttributes = ldapUser.getLDAPAttributes().getAll();

            while (enumUpdatedAttributes.hasMore()) {
                Attribute updatedAttribute = enumUpdatedAttributes.next();
                Attribute storedAttribute = storedUser.getLDAPAttributes().get(updatedAttribute.getID());
                
                // if the attribute is not stored and is a managed attribute add it to the store.
                if (storedAttribute == null && isManaged(updatedAttribute.getID())) {
                    ModificationItem[] mods = new ModificationItem[] { new ModificationItem(DirContext.ADD_ATTRIBUTE,
                            updatedAttribute) };
                    ctx.modifyAttributes(getUserDN(ldapUser), mods);
                }
            }

            LDAPUserCustomAttributes attributes = ldapUser.getCustomAttributes();

            Set<Entry<String, Object>> entrySet = new HashMap<String, Object>(attributes.getAttributes()).entrySet();
            
            for (Entry<String, Object> entry : entrySet) {
                // if the custom attribute is managed, add it to the LDAP managed attributes list. Otherwise remove it from the list of LDAP managed attributes.
                if (isManaged(entry.getKey())) {
                    ldapUser.getLDAPAttributes().put(entry.getKey(), entry.getValue());
                    attributes.removeAttribute(entry.getKey());
                } else {
                    ldapUser.getLDAPAttributes().remove(entry.getKey());
                }
            }

            ctx.rebind(getCustomAttributesDN(ldapUser), attributes);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Group getGroup(String name, Group parent) {
        // TODO implement this
        return null;
    }

    @Override
    public <T extends Serializable> org.picketlink.idm.model.Attribute<T> getAttribute(IdentityType identityType,
            String attributeName) {
        // TODO Auto-generated method stub
        return null;
    }

    private void constructContext() {
        if (ctx != null) {
            try {
                ctx.close();
            } catch (NamingException ignore) {

            }
        }
        // Construct the dir ctx
        Properties env = new Properties();
        env.setProperty(Context.INITIAL_CONTEXT_FACTORY, ldapConfiguration.getFactoryName());
        env.setProperty(Context.SECURITY_AUTHENTICATION, ldapConfiguration.getAuthType());

        String protocol = ldapConfiguration.getProtocol();
        if (protocol != null) {
            env.setProperty(Context.SECURITY_PROTOCOL, protocol);
        }
        String bindDN = ldapConfiguration.getBindDN();
        char[] bindCredential = null;

        if (ldapConfiguration.getBindCredential() != null) {
            bindCredential = ldapConfiguration.getBindCredential().toCharArray();
        }

        if (bindDN != null) {
            env.setProperty(Context.SECURITY_PRINCIPAL, bindDN);
            env.put(Context.SECURITY_CREDENTIALS, bindCredential);
        }

        String url = ldapConfiguration.getLdapURL();
        if (url == null) {
            throw new RuntimeException("url");
        }

        env.setProperty(Context.PROVIDER_URL, url);

        // Just dump the additional properties
        Properties additionalProperties = ldapConfiguration.getAdditionalProperties();
        Set<Object> keys = additionalProperties.keySet();
        for (Object key : keys) {
            env.setProperty((String) key, additionalProperties.getProperty((String) key));
        }

        try {
            ctx = new InitialLdapContext(env, null);
        } catch (NamingException e1) {
            throw new RuntimeException(e1);
        }
    }

    // Remember the updation has to happen over SSL. That is handled by the JNDI Ctx Parameters
    private void updateADPassword(LDAPUser user, String password) {
        try {
            // set password is a ldap modfy operation
            ModificationItem[] mods = new ModificationItem[1];

            // Replace the "unicdodePwd" attribute with a new value
            // Password must be both Unicode and a quoted string
            String newQuotedPassword = "\"" + password + "\"";
            byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");

            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("unicodePwd", newUnicodePassword));

            // Perform the update
            ctx.modifyAttributes(getUserDN(user), mods);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>
     * Helper method to throws a {@link IllegalArgumentException} when the specified {@link Credential} is not supported.
     * </p>
     * TODO: when using JBoss Logging this method should be removed.
     * 
     * @param credential
     * @return
     */
    private void throwsNotSupportedCredentialType(Credential credential) throws IllegalArgumentException {
        throw new IllegalArgumentException("Credential type not supported: " + credential.getClass());
    }

    private LDAPUser convert(User user) {
        LDAPUser ldapuser = new LDAPUser();

        // ldapuser.setLookup(this);
        // ldapuser.setLDAPChangeNotificationHandler(this);
        // ldapuser.setUserDNSuffix(userDNSuffix);

        ldapuser.setId(user.getId());
        ldapuser.setFirstName(" ");
        ldapuser.setLastName(" ");

        if (user.getFirstName() != null) {
            ldapuser.setFirstName(user.getFirstName());
            ldapuser.setFullName(user.getFirstName());
        }

        if (user.getLastName() != null) {
            ldapuser.setLastName(user.getLastName());

            if (ldapuser.getFullName() != null) {
                ldapuser.setFullName(ldapuser.getFullName() + " " + user.getLastName());
            } else {
                ldapuser.setFullName(user.getLastName());
            }
        }

        if (user.getEmail() != null) {
            ldapuser.setEmail(user.getEmail());
        }

        for (org.picketlink.idm.model.Attribute<? extends Serializable> attrib : user.getAttributes()) {
            ldapuser.setAttribute(attrib);
        }

        return ldapuser;
    }

    /**
     * <p>Returns the custom attributes for the given {@link LDAPUser}.</p>
     * 
     * @param user
     * @return
     */
    private LDAPUserCustomAttributes getCustomAttributes(LDAPUser user) {
        String customDN = getCustomAttributesDN(user);

        LDAPUserCustomAttributes customAttributes = null;

        try {
            customAttributes = lookup(customDN);
        } catch (Exception ignore) {
        }
        
        return customAttributes;
    }
    
    /**
     * <p>
     * Returns a DN for the {@link LDAPUser} custom attributes entry.
     * </p>
     * 
     * @param ldapUser
     * @return
     */
    private String getCustomAttributesDN(LDAPUser ldapUser) {
        return "cn=custom-attributes" + COMMA + getUserDN(ldapUser);
    }
    
    /**
     * <p>Returns a DN for the given {@link LDAPUser}.</p>
     * 
     * @param user
     * @return
     */
    private String getUserDN(LDAPUser user) {
        String uid = user.getId();
        
        try {
            if (uid != null) {
                uid = (String) user.getLDAPAttributes().get(UID).get();
            }
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        
        return UID + EQUAL + uid + COMMA + userDNSuffix;
    }

    /**
     * <p>
     * Binds a {@link Object} to the LDAP tree.
     * </p>
     * 
     * @param ldapUser
     */
    private void bind(String dn, Object object) {
        try {
            ctx.bind(dn, object);
        } catch (NamingException e) {
            throw new RuntimeException(e);
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
    private <T> T lookup(String dn) {
        try {
            return (T) ctx.lookup(dn);
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
    private void destroySubcontext(String dn) {
        try {
            ctx.destroySubcontext(dn);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.picketlink.idm.spi.IdentityStore#executeQuery(org.picketlink.idm.query.UserQuery,
     * org.picketlink.idm.query.Range)
     */
    /*
     * @Override public List<User> executeQuery(IdentityStoreInvocationContext invocationContext, UserQuery query, Range range)
     * { // TODO: Deal with range List<User> users = new ArrayList<User>(); Map<String, String[]> filters =
     * query.getAttributeFilters(); if (filters != null) { // we are dealing with attributes // Get all the managed attributes
     * first to do the search Attributes matchAttrs = getManagedAttributes(filters); if (matchAttrs.size() == 0) { // go for
     * custom attributes List<User> allUsers = getAllUsers(); for (User theUser : allUsers) { if
     * (userHasRequiredAttributes((LDAPUser) theUser, filters)) { users.add(theUser); } } return users; }
     * 
     * // Perform the search try { NamingEnumeration<SearchResult> answer = ctx.search(userDNSuffix, matchAttrs); while
     * (answer.hasMore()) { SearchResult sr = answer.next(); Attributes attributes = sr.getAttributes();
     * 
     * LDAPUser user = new LDAPUser(); user.setLookup(this); user.setUserDNSuffix(userDNSuffix);
     * user.addAllLDAPAttributes(attributes);
     * 
     * user.setLDAPChangeNotificationHandler(this);
     * 
     * // Get the custom attributes String customDN = user.getCustomAttributes().getDN() + COMMA + user.getDN(); try {
     * LDAPUserCustomAttributes lca = (LDAPUserCustomAttributes) ctx.lookup(customDN); if (lca != null) {
     * user.setCustomAttributes(lca); } } catch (Exception ignore) { } if (userHasRequiredAttributes(user, filters)) {
     * users.add(user); } } } catch (NamingException e) { throw new RuntimeException("Error executing user query.", e); } }
     * 
     * return users; }
     */

    /*
     * (non-Javadoc)
     * 
     * @see org.picketlink.idm.spi.IdentityStore#executeQuery(org.picketlink.idm.query.GroupQuery,
     * org.picketlink.idm.query.Range)
     */
    /*
     * @Override public List<Group> executeQuery(IdentityStoreInvocationContext invocationContext, GroupQuery query, Range
     * range) { List<Group> groups = new ArrayList<Group>();
     * 
     * try { BasicAttributes groupAttributeFilter = new BasicAttributes(true);
     * 
     * if (query.getId() != null) { groupAttributeFilter.put(CN, query.getId()); }
     * 
     * if (query.getName() != null) { groupAttributeFilter.put(CN, query.getName()); }
     * 
     * if (query.getRelatedUser() != null) { LDAPUser ldapUser = (LDAPUser) getUser(invocationContext,
     * query.getRelatedUser().getId());
     * 
     * groupAttributeFilter.put(MEMBER, ldapUser.getDN()); }
     * 
     * if (query.getRole() != null) { LDAPRole ldapRole = (LDAPRole) getRole(invocationContext, query.getRole().getName());
     * 
     * groupAttributeFilter.put(MEMBER, ldapRole.getDN()); }
     * 
     * NamingEnumeration<SearchResult> groupSearchResult = ctx.search(groupDNSuffix, groupAttributeFilter);
     * 
     * // iterate over the returned roles while (groupSearchResult.hasMore()) { boolean isGroupSelected = true;
     * 
     * SearchResult groupResult = groupSearchResult.next(); Attributes groupAttributes = groupResult.getAttributes();
     * 
     * LDAPGroup childGroup = new LDAPGroup(groupAttributes, groupDNSuffix);
     * 
     * if (query.getParentGroup() != null) { Group parentGroup = getParentGroup(invocationContext, childGroup);
     * 
     * if (parentGroup == null || !query.getParentGroup().getId().equals(parentGroup.getId())) { isGroupSelected = false; }
     * 
     * }
     * 
     * if (isGroupSelected) { groups.add(childGroup); } } } catch (NamingException e) { throw new
     * RuntimeException("Error executing group query.", e); }
     * 
     * return groups; }
     */

    /*
     * (non-Javadoc)
     * 
     * @see org.picketlink.idm.spi.IdentityStore#executeQuery(org.picketlink.idm.query.RoleQuery,
     * org.picketlink.idm.query.Range)
     */
    /*
     * @Override public List<Role> executeQuery(IdentityStoreInvocationContext invocationContext, RoleQuery query, Range range)
     * { List<Role> roles = new ArrayList<Role>();
     * 
     * try { BasicAttributes roleAttributeFilter = new BasicAttributes(true);
     * 
     * if (query.getName() != null) { roleAttributeFilter.put(CN, query.getName()); }
     * 
     * NamingEnumeration<SearchResult> roleSearchResult = ctx.search(roleDNSuffix, roleAttributeFilter);
     * 
     * // iterate over the returned roles while (roleSearchResult.hasMore()) { boolean isRoleSelected = true;
     * 
     * SearchResult roleResult = roleSearchResult.next(); Attributes roleAttributes = roleResult.getAttributes();
     * 
     * LDAPRole ldapRole = new LDAPRole(roleAttributes, roleDNSuffix);
     * 
     * // checks if the role has a member mapped to the owner if (query.getOwner() != null) { Attribute memberAttribute =
     * roleAttributes.get(MEMBER);
     * 
     * LDAPUser ldapUser = (LDAPUser) query.getOwner();
     * 
     * if (!(memberAttribute != null && memberAttribute.contains(ldapUser.getDN()))) { isRoleSelected = false; } }
     * 
     * // checks if the role is a member of the group if (query.getGroup() != null) { LDAPGroup ldapGroup = (LDAPGroup)
     * getGroup(invocationContext, query.getGroup().getName());
     * 
     * Attributes groupAttributes = ldapGroup.getLDAPAttributes(); Attribute memberAttribute = groupAttributes.get(MEMBER);
     * 
     * // if the role is a group member then select it. Otherwise the role is not a member of the provided group. if
     * (!(memberAttribute != null && memberAttribute.contains(ldapRole.getDN()))) { isRoleSelected = false; } }
     * 
     * if (isRoleSelected) { roles.add(ldapRole); } } } catch (NamingException e) { throw new
     * RuntimeException("Error executing role query.", e); }
     * 
     * return roles; }
     */
    
//  private Attributes getManagedAttributes(Map<String, String[]> filters) {
//  Attributes attr = new BasicAttributes(true);
//  Set<String> keys = filters.keySet();
//  for (String key : keys) {
//      if (isManaged(key)) {
//          attr.put(key, filters.get(key));
//      }
//  }
//  return attr;
//}
//
//private boolean userHasRequiredAttributes(LDAPUser user, Map<String, String[]> filters) {
//  Set<String> keys = filters.keySet();
//
//  for (String key : keys) {
//      String[] values = filters.get(key);
//      String[] attValues = user.<String[]> getAttribute(key).getValue();
//      if (IDMUtil.arraysEqual(values, attValues) == false) {
//          return false;
//      }
//  }
//  return true;
//}
//
//private List<User> getAllUsers() {
//  List<User> users = new ArrayList<User>();
//  // Perform the search
//  try {
//      Attributes attr = new BasicAttributes(true);
//      NamingEnumeration<SearchResult> answer = ctx.search(userDNSuffix, attr);
//      
//      while (answer.hasMore()) {
//          SearchResult sr = answer.next();
//          Attributes attributes = sr.getAttributes();
//          LDAPUser user = new LDAPUser(attributes);
//          
//          LDAPUserCustomAttributes customAttributes = getCustomAttributes(user);
//          // user.setLookup(this);
//          
//          user.setCustomAttributes(customAttributes);
//          
//          // user.setLDAPChangeNotificationHandler(this);
//
//          users.add(user);
//      }
//  } catch (NamingException e) {
//      throw new RuntimeException(e);
//  }
//  
//  return users;
//}
    
    // TODO method no longer required?
    /*
     * @Override public String[] getAttributeValues(IdentityStoreInvocationContext invocationContext, IdentityType identity,
     * String name) { if (identity instanceof User) { if (identity instanceof LDAPUser == false) { throw new
     * RuntimeException("Wrong type:" + identity); } LDAPUser ldapUser = (LDAPUser) identity; return
     * ldapUser.getAttributeValues(name); } else if (identity instanceof Group) { LDAPGroup ldapGroup = null; if (identity
     * instanceof LDAPGroup) { ldapGroup = (LDAPGroup) identity; } else { ldapGroup = (LDAPGroup) getGroup(invocationContext,
     * ((Group) identity).getName()); } return ldapGroup.getAttributeValues(name); } else if (identity instanceof Role) {
     * LDAPRole ldapRole = null; if (identity instanceof LDAPGroup) { ldapRole = (LDAPRole) identity; } else { ldapRole =
     * (LDAPRole) getRole(invocationContext, ((Role) identity).getName()); } return ldapRole.getAttributeValues(name); } else {
     * throw new IllegalArgumentException("identity parameter must be an instance of User, Group or Role"); } }
     */

    /*
     * (non-Javadoc)
     * 
     * @see org.picketlink.idm.spi.IdentityStore#getAttributes(org.picketlink.idm.model.User)
     */

    // TODO method no longer required?
    /*
     * @Override public Map<String, String[]> getAttributes(IdentityStoreInvocationContext invocationContext, IdentityType
     * identity) { if (identity instanceof User) { if (identity instanceof LDAPUser == false) { throw new
     * RuntimeException("Wrong type:" + identity); } LDAPUser ldapUser = (LDAPUser) identity; return ldapUser.getAttributes(); }
     * else if (identity instanceof Group) { LDAPGroup ldapGroup = null; if (identity instanceof LDAPGroup) { ldapGroup =
     * (LDAPGroup) identity; } else { ldapGroup = (LDAPGroup) getGroup(invocationContext, ((Group) identity).getName()); }
     * return ldapGroup.getAttributes(); } else if (identity instanceof Role) { LDAPRole ldapRole = null; if (ldapRole
     * instanceof LDAPRole) { ldapRole = (LDAPRole) identity; } else { ldapRole = (LDAPRole) getRole(invocationContext, ((Role)
     * identity).getName()); } return ldapRole.getAttributes(); } else { throw new
     * IllegalArgumentException("identity parameter must be an instance of User, Group or Role"); } }
     */
    
//  @Override
//  public void handle(LDAPObjectChangedNotification notification) {
//      DirContext object = notification.getLDAPObject();
//      if (object instanceof LDAPUser) {
//          LDAPUser user = (LDAPUser) object;
//          LDAPUserCustomAttributes ldapUserCustomAttributes = user.getCustomAttributes();
//          try {
//              String userDN = getUserDN(user);
//              if (notification.getNtype() == NType.ADD_ATTRIBUTE) {
//                  Attribute attrib = notification.getAttribute();
//                  if (attrib == null)
//                      throw new RuntimeException("attrib is null");
//                  ModificationItem[] mods = new ModificationItem[] { new ModificationItem(DirContext.ADD_ATTRIBUTE, attrib) };
//                  ctx.modifyAttributes(userDN, mods);
//              }
//              if (notification.getNtype() == NType.REPLACE_ATTRIBUTE) {
//                  Attribute attrib = notification.getAttribute();
//                  if (attrib == null)
//                      throw new RuntimeException("attrib is null");
//                  ModificationItem[] mods = new ModificationItem[] { new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
//                          attrib) };
//                  ctx.modifyAttributes(userDN, mods);
//              }
//              if (notification.getNtype() == NType.REMOVE_ATTRIBUTE) {
//                  Attribute attrib = notification.getAttribute();
//                  if (attrib == null)
//                      throw new RuntimeException("attrib is null");
//                  ModificationItem[] mods = new ModificationItem[] { new ModificationItem(DirContext.REMOVE_ATTRIBUTE, attrib) };
//                  ctx.modifyAttributes(userDN, mods);
//              }
//              // ctx.rebind(userDN, object);
//              ctx.rebind(getCustomAttributesDN(user), ldapUserCustomAttributes);
//          } catch (NamingException e) {
//              throw new RuntimeException(e);
//          }
//      }
//  }

}
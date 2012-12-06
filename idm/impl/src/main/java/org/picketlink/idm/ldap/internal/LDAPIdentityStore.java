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
import static org.picketlink.idm.ldap.internal.LDAPConstants.EQUAL;
import static org.picketlink.idm.ldap.internal.LDAPConstants.MEMBER;
import static org.picketlink.idm.ldap.internal.LDAPConstants.UID;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;

import org.picketlink.idm.credential.PlainTextPassword;
import org.picketlink.idm.credential.X509CertificateCredential;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.internal.util.Base64;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.IdentityType.AttributeParameter;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroupRole;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;

/**
 * An IdentityStore implementation backed by an LDAP directory
 * 
 * @author Shane Bryzak
 * @author Anil Saldhana
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class LDAPIdentityStore implements IdentityStore<LDAPConfiguration> {

    private static final String USER_CERTIFICATE_ATTRIBUTE = "usercertificate";
    private static final String USER_PASSWORD_ATTRIBUTE = "userpassword";

    private LDAPConfiguration configuration;
    private IdentityStoreInvocationContext context;

    @Override
    public void setup(LDAPConfiguration config, IdentityStoreInvocationContext context) {
        this.configuration = config;
        this.context = context;
    }

    @Override
    public LDAPConfiguration getConfig() {
        return this.configuration;
    }

    @Override
    public IdentityStoreInvocationContext getContext() {
        return this.context;
    }

    @Override
    public void add(IdentityType identityType) {
        if (User.class.isInstance(identityType)) {
            User user = (User) identityType;

            if (user.getId() == null) {
                throw new RuntimeException("No identifier was provided. You should provide one before storing the user.");
            }

            LDAPUser ldapUser = null;

            if (!(user instanceof LDAPUser)) {
                ldapUser = convert(user);
            } else {
                ldapUser = (LDAPUser) user;
            }

            ldapUser.setFullName(getUserCN(ldapUser));

            store(ldapUser);
        } else if (Group.class.isInstance(identityType)) {
            Group group = (Group) identityType;

            LDAPGroup ldapGroup = new LDAPGroup(this.configuration.getGroupDNSuffix());

            ldapGroup.setName(group.getName());

            store(ldapGroup);

            if (group.getParentGroup() != null) {
                String parentName = group.getParentGroup().getName();
                LDAPGroup parentGroup = (LDAPGroup) getGroup(parentName);

                if (parentGroup == null) {
                    throw new RuntimeException("Parent group [" + parentName + "] does not exists.");
                }

                parentGroup.addChildGroup(ldapGroup);

                ldapGroup.setParentGroup(parentGroup);

                getLdapManager().modifyAttribute(parentGroup.getDN(), parentGroup.getLDAPAttributes().get(MEMBER));
            }

        } else if (Role.class.isInstance(identityType)) {
            Role role = (Role) identityType;

            LDAPRole ldapRole = new LDAPRole(this.configuration.getRoleDNSuffix());

            ldapRole.setName(role.getName());

            store(ldapRole);
        }
    }

    @Override
    public void update(IdentityType identityType) {
        if (User.class.isInstance(identityType)) {
            User user = (User) identityType;

            LDAPUser updatedUser = (LDAPUser) user;

            try {
                LDAPUser storedUser = (LDAPUser) getUser(user.getId());

                if (storedUser == null) {
                    throw new RuntimeException("User [" + user.getId() + "] does not exists.");
                }

                updatedUser.setFullName(getUserCN(updatedUser));

                NamingEnumeration<? extends Attribute> storedAttributes = storedUser.getLDAPAttributes().getAll();

                // check for attributes to replace or remove
                while (storedAttributes.hasMore()) {
                    Attribute storedAttribute = storedAttributes.next();
                    Attribute updatedAttribute = updatedUser.getLDAPAttributes().get(storedAttribute.getID());

                    // if the stored attribute exists in the updated attributes list, replace it. Otherwise remove it from the
                    // store.
                    if (updatedAttribute != null) {
                        getLdapManager().modifyAttribute(storedUser.getDN(), updatedAttribute);
                    } else {
                        getLdapManager().removeAttribute(storedUser.getDN(), storedAttribute);
                    }
                }

                NamingEnumeration<? extends Attribute> enumUpdatedAttributes = updatedUser.getLDAPAttributes().getAll();

                // check for attributes to add
                while (enumUpdatedAttributes.hasMore()) {
                    Attribute updatedAttribute = enumUpdatedAttributes.next();
                    Attribute storedAttribute = storedUser.getLDAPAttributes().get(updatedAttribute.getID());

                    // if the attribute is not stored and is a managed attribute add it to the store.
                    if (storedAttribute == null && getLdapManager().isManagedAttribute(updatedAttribute.getID())) {
                        getLdapManager().addAttribute(storedUser.getDN(), updatedAttribute);
                    }
                }

                updateCustomAttributes(updatedUser);
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void remove(IdentityType identityType) {
        if (User.class.isInstance(identityType)) {
            User user = (User) identityType;

            LDAPUser ldapUser = (LDAPUser) getUser(user.getId());

            if (ldapUser == null) {
                throw new RuntimeException("User [" + user.getId() + "] does not exists.");
            }

            remove(ldapUser);
        } else if (Group.class.isInstance(identityType)) {
            Group group = (Group) identityType;

            LDAPGroup ldapGroup = (LDAPGroup) getGroup(group.getName());

            if (ldapGroup == null) {
                throw new RuntimeException("Group [" + group.getName() + "] doest not exists.");
            }

            remove(ldapGroup);

        } else if (Role.class.isInstance(identityType)) {
            Role role = (Role) identityType;

            LDAPRole ldapRole = (LDAPRole) getRole(role.getName());

            if (ldapRole == null) {
                throw new RuntimeException("Role [" + role.getName() + "] doest not exists.");
            }

            remove(ldapRole);
        }
    }

    @Override
    public User getUser(String name) {
        final String baseDN = this.configuration.getUserDNSuffix();

        List<User> answer = getLdapManager().searchByAttribute(baseDN, UID, name, new LDAPSearchCallback<User>() {

            @Override
            public User processResult(SearchResult sr) {
                LDAPUser user = new LDAPUser(baseDN, sr.getAttributes());

                user.setCustomAttributes(getCustomAttributes(user.getDN()));

                return user;
            }

        });

        return answer.isEmpty() ? null : answer.get(0);
    }

    @Override
    public Group getGroup(String name) {
        final String baseDN = this.configuration.getGroupDNSuffix();

        List<Group> answer = getLdapManager().searchByAttribute(baseDN, CN, name, new LDAPSearchCallback<Group>() {

            @Override
            public Group processResult(SearchResult sr) {
                LDAPGroup ldapGroup = new LDAPGroup(sr.getAttributes(), baseDN);

                ldapGroup.setCustomAttributes(getCustomAttributes(ldapGroup.getDN()));

                Group parentGroup = getParentGroup(ldapGroup);

                if (parentGroup != null) {
                    ldapGroup.setParentGroup(parentGroup);
                }

                return ldapGroup;
            }

        });

        return answer.isEmpty() ? null : answer.get(0);
    }

    @Override
    public Role getRole(String name) {
        final String baseDN = this.configuration.getRoleDNSuffix();

        List<Role> answer = getLdapManager().searchByAttribute(baseDN, CN, name, new LDAPSearchCallback<Role>() {

            @Override
            public Role processResult(SearchResult sr) {
                LDAPRole ldapRole = new LDAPRole(sr.getAttributes(), baseDN);

                ldapRole.setCustomAttributes(getCustomAttributes(ldapRole.getDN()));

                return ldapRole;
            }

        });

        return answer.isEmpty() ? null : answer.get(0);
    }

    @Override
    public Group getGroup(String name, Group parent) {
        Group ldapGroup = getGroup(name);
        Group ldapGroupParent = ldapGroup.getParentGroup();

        if (parent != null && ldapGroup != null && ldapGroupParent != null
                && ldapGroupParent.getName().equals(parent.getName())) {
            return ldapGroup;
        }

        return null;
    }

    @Override
    public GroupRole createMembership(IdentityType member, Group group, Role role) {
        if (member instanceof User) {
            final LDAPRole ldapRole = (LDAPRole) getRole(role.getName());
            final LDAPUser ldapUser = (LDAPUser) getUser(((User) member).getId());
            final LDAPGroup ldapGroup = (LDAPGroup) getGroup(group.getName());

            ldapRole.addUser(ldapUser.getDN());
            ldapGroup.addRole(ldapRole);
            ldapGroup.addUser(ldapUser.getDN());

            getLdapManager().modifyAttribute(ldapRole.getDN(), ldapRole.getLDAPAttributes().get(MEMBER));
            getLdapManager().modifyAttribute(ldapGroup.getDN(), ldapGroup.getLDAPAttributes().get(MEMBER));

            return new SimpleGroupRole(ldapUser, ldapRole, ldapGroup);
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

            ldapRole.removeUser(ldapUser.getDN());
            ldapGroup.removeRole(ldapRole);
        } else if (member instanceof Group) {
            // FIXME implement Group membership if supported
        }
    }

    @Override
    public GroupRole getMembership(IdentityType member, Group group, Role role) {
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
            if (getLdapManager().isManagedAttribute(attribute.getName())) {
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

    // @Override
    public boolean validateCredential(User user, Object credential) {
        LDAPUser ldapUser = (LDAPUser) getUser(user.getId());

        if (ldapUser == null) {
            return false;
        }

        boolean valid = false;

        if (credential instanceof PlainTextPassword) {
            PlainTextPassword pc = (PlainTextPassword) credential;

            valid = getLdapManager().authenticate(ldapUser.getDN(), new String(pc.getPassword()));
        } else if (credential instanceof X509CertificateCredential) {
            X509CertificateCredential clientCert = (X509CertificateCredential) credential;

            org.picketlink.idm.model.Attribute<Serializable> certAttribute = ldapUser.getAttribute(USER_CERTIFICATE_ATTRIBUTE);

            byte[] certBytes = Base64.decode(new String((byte[]) certAttribute.getValue()));

            try {
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                X509Certificate storedCert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(
                        certBytes));
                X509Certificate providedCert = clientCert.getCertificate();

                valid = storedCert.equals(providedCert);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return valid;
    }

    // @Override
    public void updateCredential(User user, Object credential) {
        if (credential instanceof PlainTextPassword) {
            PlainTextPassword pc = (PlainTextPassword) credential;
            if (this.configuration.isActiveDirectory()) {
                updateADPassword((LDAPUser) user, new String(pc.getPassword()));
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

                getLdapManager().modifyAttribute(ldapuser.getDN(), mod0);
            }
        } else if (credential instanceof X509CertificateCredential) {
            X509CertificateCredential cc = (X509CertificateCredential) credential;
            try {
                LDAPUser ldapUser = (LDAPUser) user;

                String certbytes = Base64.encodeBytes(cc.getCertificate().getEncoded());

                ldapUser.setAttribute(new org.picketlink.idm.model.Attribute<String>(USER_CERTIFICATE_ATTRIBUTE, certbytes));

                BasicAttribute certAttribute = new BasicAttribute(USER_CERTIFICATE_ATTRIBUTE, certbytes);

                // Perform the update
                getLdapManager().modifyAttribute(ldapUser.getDN(), certAttribute);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throwsNotSupportedCredentialType(credential);
        }
    }

    @Override
    public <T extends IdentityType> List<T> fetchQueryResults(Class<T> typeClass, Map<QueryParameter, Object[]> parameters) {
        // TODO: make this code more simple
        List<T> result = new ArrayList<T>();

        Set<Entry<QueryParameter, Object[]>> parametersEntries = parameters.entrySet();
        Attributes attributesTosearch = new BasicAttributes(true);
        Map<QueryParameter, Object[]> customAttributesToSearch = new HashMap<QueryParameter, Object[]>();

        for (Entry<QueryParameter, Object[]> entry : parametersEntries) {
            QueryParameter queryParameter = entry.getKey();
            Object[] values = entry.getValue();

            Attribute mappedAttribute = null;

            if (queryParameter instanceof IdentityType.AttributeParameter) {
                IdentityType.AttributeParameter attrParameter = (AttributeParameter) queryParameter;

                if (getLdapManager().isManagedAttribute(attrParameter.getName())) {
                    mappedAttribute = new BasicAttribute(attrParameter.getName());
                    mappedAttribute.add(values[0]);
                    attributesTosearch.put(mappedAttribute);
                }
            } else {
                mappedAttribute = LDAPAttributeMapper.map(queryParameter);

                if (mappedAttribute != null) {
                    mappedAttribute.add(values[0]);
                    attributesTosearch.put(mappedAttribute);
                }
            }

            if (mappedAttribute == null) {
                customAttributesToSearch.put(queryParameter, values);
            }
        }

        if (User.class.isAssignableFrom(typeClass)) {
            NamingEnumeration<SearchResult> answer = getLdapManager().search(this.configuration.getUserDNSuffix(),
                    attributesTosearch, new String[] { UID });

            while (answer.hasMoreElements()) {
                SearchResult sr = (SearchResult) answer.nextElement();
                Attributes attributes = sr.getAttributes();
                String uid = null;

                try {
                    uid = (String) attributes.get(UID).get();
                } catch (NamingException e) {
                    e.printStackTrace();
                }

                LDAPUser transientUser = new LDAPUser(this.configuration.getUserDNSuffix());

                transientUser.setId(uid);

                if (customAttributesToSearch.size() > 0) {
                    Set<Entry<QueryParameter, Object[]>> customQueryParameters = customAttributesToSearch.entrySet();

                    for (Entry<QueryParameter, Object[]> customQueryParameter : customQueryParameters) {
                        QueryParameter queryParameter = customQueryParameter.getKey();
                        Object[] values = customQueryParameter.getValue();

                        LDAPCustomAttributes customAttributes = getCustomAttributes(transientUser.getDN());

                        if (customAttributes != null) {
                            Set<Entry<String, Object>> customAttr = customAttributes.getAttributes().entrySet();

                            boolean hasAttribute = false;

                            for (Entry<String, Object> customAttribute : customAttr) {
                                String id = customAttribute.getKey().toString();
                                Object entryValue = customAttribute.getValue();
                                Attribute mapCustom = LDAPAttributeMapper.mapCustom(queryParameter);

                                if (mapCustom != null) {
                                    if (mapCustom.getID().equals(id)) {
                                        hasAttribute = true;

                                        if (id.equals(LDAPConstants.CUSTOM_ATTRIBUTE_CREATE_DATE)
                                                || id.equals(LDAPConstants.CUSTOM_ATTRIBUTE_EXPIRY_DATE)) {
                                            long providedTimeInMillis = ((Date) values[0]).getTime();
                                            long storedTimeInMillis = Long.valueOf(entryValue.toString());

                                            if (queryParameter.equals(User.CREATED_DATE)
                                                    || queryParameter.equals(User.EXPIRY_DATE)) {
                                                if (providedTimeInMillis != storedTimeInMillis) {
                                                    uid = null;
                                                    break;
                                                }
                                            } else if (id.equals(LDAPConstants.CUSTOM_ATTRIBUTE_CREATE_DATE)) {
                                                if (queryParameter.equals(User.CREATED_AFTER)) {
                                                    if (storedTimeInMillis < providedTimeInMillis) {
                                                        uid = null;
                                                        break;
                                                    }
                                                }

                                                if (queryParameter.equals(User.CREATED_BEFORE)) {
                                                    if (storedTimeInMillis > providedTimeInMillis) {
                                                        uid = null;
                                                        break;
                                                    }
                                                }
                                            } else if (id.equals(LDAPConstants.CUSTOM_ATTRIBUTE_EXPIRY_DATE)) {
                                                if (queryParameter.equals(User.EXPIRY_AFTER)) {
                                                    if (storedTimeInMillis < providedTimeInMillis) {
                                                        uid = null;
                                                        break;
                                                    }
                                                }

                                                if (queryParameter.equals(User.EXPIRY_BEFORE)) {
                                                    if (storedTimeInMillis > providedTimeInMillis) {
                                                        uid = null;
                                                        break;
                                                    }
                                                }
                                            }
                                        } else {
                                            if (queryParameter instanceof IdentityType.AttributeParameter) {
                                                IdentityType.AttributeParameter attrParameter = (AttributeParameter) queryParameter;

                                                if (id.equals(attrParameter.getName())) {
                                                    hasAttribute = true;

                                                    if (!values[0].toString().equals(entryValue.toString())) {
                                                        uid = null;
                                                        break;
                                                    }
                                                }
                                            } else {
                                                if (!(values[0].toString().equals(entryValue.toString()))) {
                                                    uid = null;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    if (queryParameter instanceof IdentityType.AttributeParameter) {
                                        IdentityType.AttributeParameter attrParameter = (AttributeParameter) queryParameter;

                                        if (id.equals(attrParameter.getName())) {
                                            hasAttribute = true;
                                            
                                            if (entryValue.getClass().isArray()) {
                                                Object[] attributeValues = (Object[]) entryValue;
                                                int matchCount = 0;
                                                
                                                for (Object object : attributeValues) {
                                                    for (Object parameterValue : values) {
                                                        if (object.toString().equals(parameterValue.toString())) {
                                                            matchCount++;
                                                        }
                                                    }
                                                }
                                                
                                                if (matchCount != attributeValues.length) {
                                                    uid = null;
                                                    break;
                                                }
                                            } else {
                                                if (!values[0].toString().equals(entryValue.toString())) {
                                                    uid = null;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (!hasAttribute) {
                                uid = null;
                            }
                        } else {
                            uid = null;
                        }
                    }

                    if (uid != null) {
                        result.add((T) getUser(uid));
                    }
                } else {
                    result.add((T) getUser(uid));
                }
            }
        }

        return result;
    }

    @Override
    public void updateRole(Role role) {
        LDAPRole storedRole = (LDAPRole) getRole(role.getName());

        if (storedRole == null) {
            throw new RuntimeException("No role found with the given name [" + role.getName() + "].");
        }

        LDAPRole updatedRole = (LDAPRole) role;

        updateCustomAttributes(updatedRole);
    }

    @Override
    public void updateGroup(Group group) {
        LDAPGroup storedGroup = (LDAPGroup) getGroup(group.getName());

        if (storedGroup == null) {
            throw new RuntimeException("No group found with the given name [" + group.getName() + "].");
        }

        LDAPGroup updatedGroup = (LDAPGroup) group;

        updateCustomAttributes(updatedGroup);
    }

    @Override
    public <T extends Serializable> org.picketlink.idm.model.Attribute<T> getAttribute(IdentityType identityType,
            String attributeName) {
        // TODO Auto-generated method stub
        return null;
    }

    // Remember the updation has to happen over SSL. That is handled by the JNDI Ctx Parameters
    private void updateADPassword(LDAPUser user, String password) {
        try {
            // Replace the "unicdodePwd" attribute with a new value
            // Password must be both Unicode and a quoted string
            String newQuotedPassword = "\"" + password + "\"";
            byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");

            BasicAttribute unicodePwd = new BasicAttribute("unicodePwd", newUnicodePassword);

            getLdapManager().modifyAttribute(user.getDN(), unicodePwd);
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
    private void throwsNotSupportedCredentialType(Object credential) throws IllegalArgumentException {
        throw new IllegalArgumentException("Credential type not supported: " + credential.getClass());
    }

    /**
     * <p>
     * Converts the given {@link User} instance to a {@link LDAPUser} instance.
     * </p>
     * 
     * @param user
     * @return
     */
    private LDAPUser convert(User user) {
        LDAPUser ldapUser = new LDAPUser(this.configuration.getUserDNSuffix());

        ldapUser.setId(user.getId());
        ldapUser.setFirstName(" ");
        ldapUser.setLastName(" ");

        if (user.getFirstName() != null) {
            ldapUser.setFirstName(user.getFirstName());
        }

        if (user.getLastName() != null) {
            ldapUser.setLastName(user.getLastName());
        }

        if (user.getEmail() != null) {
            ldapUser.setEmail(user.getEmail());
        }

        if (user.getExpirationDate() != null) {
            ldapUser.setExpirationDate(user.getExpirationDate());
        }

        for (org.picketlink.idm.model.Attribute<? extends Serializable> attrib : user.getAttributes()) {
            ldapUser.setAttribute(attrib);
        }

        return ldapUser;
    }

    /**
     * <p>
     * Returns the custom attributes for the given parent DN.
     * </p>
     * 
     * @param parentDN
     * @return
     */
    private LDAPCustomAttributes getCustomAttributes(String parentDN) {
        String customDN = getCustomAttributesDN(parentDN);

        LDAPCustomAttributes customAttributes = null;

        try {
            customAttributes = getLdapManager().lookup(customDN);
        } catch (Exception ignore) {
        }

        return customAttributes;
    }

    /**
     * <p>
     * Returns a DN for the custom attributes entry.
     * </p>
     * 
     * @param parentDN
     * @return
     */
    private String getCustomAttributesDN(String parentDN) {
        return "cn=custom-attributes" + COMMA + parentDN;
    }

    /**
     * <p>
     * Returns the user CN attribute value. The CN is composed of user's first and last name.
     * </p>
     * 
     * @param ldapUser
     * @return
     */
    private String getUserCN(LDAPUser ldapUser) {
        String fullName = ldapUser.getFirstName();

        if (ldapUser.getLastName() != null) {
            fullName = fullName + " " + ldapUser.getLastName();
        }
        return fullName;
    }

    /**
     * <p>
     * Stores the given {@link LDAPEntry} instance in the LDAP tree. This method performs a bind for both {@link LDAPEntry}
     * instance and its {@link LDAPCustomAttributes}.
     * </p>
     * 
     * @param ldapEntry
     */
    private void store(LDAPEntry ldapEntry) {
        getLdapManager().bind(ldapEntry.getDN(), ldapEntry);
        getLdapManager().bind(getCustomAttributesDN(ldapEntry.getDN()), ldapEntry.getCustomAttributes());
    }

    /**
     * <p>
     * Removes the given {@link LDAPEntry} entry from the LDAP tree. This method also remove the custom attribute entry for the
     * given parent instance.
     * </p>
     * 
     * @param ldapEntry
     */
    private void remove(LDAPEntry ldapEntry) {
        String customDN = getCustomAttributesDN(ldapEntry.getDN());

        try {
            getLdapManager().lookup(customDN);
            getLdapManager().destroySubcontext(customDN);
        } catch (Exception ignore) {
        }

        getLdapManager().destroySubcontext(ldapEntry.getDN());
    }

    /**
     * <p>
     * Returns the parent group for the given child group.
     * </p>
     * 
     * @param childGroup
     * @return
     */
    private Group getParentGroup(LDAPGroup childGroup) {
        Attributes matchAttrs = new BasicAttributes(true);
        matchAttrs.put(new BasicAttribute(MEMBER, CN + EQUAL + childGroup.getName() + COMMA
                + this.configuration.getGroupDNSuffix()));
        // Search for objects with these matching attributes
        try {
            NamingEnumeration<SearchResult> answer = getLdapManager().search(this.configuration.getGroupDNSuffix(), matchAttrs,
                    new String[] { CN });
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

    /**
     * <p>
     * Updates the custom attributes for the given {@link LDAPEntry} instance.
     * </p>
     * 
     * @param ldapEntry
     */
    private void updateCustomAttributes(LDAPEntry ldapEntry) {
        LDAPCustomAttributes attributes = ldapEntry.getCustomAttributes();

        Set<Entry<String, Object>> entrySet = new HashMap<String, Object>(attributes.getAttributes()).entrySet();

        for (Entry<String, Object> entry : entrySet) {
            // if the custom attribute is managed, add it to the LDAP managed attributes list. Otherwise remove it from the
            // list of LDAP managed attributes.
            if (getLdapManager().isManagedAttribute(entry.getKey())) {
                ldapEntry.getLDAPAttributes().put(entry.getKey(), entry.getValue());
                attributes.removeAttribute(entry.getKey());
            } else {
                ldapEntry.getLDAPAttributes().remove(entry.getKey());
            }
        }

        getLdapManager().rebind(getCustomAttributesDN(ldapEntry.getDN()), attributes);
    }

    public LDAPOperationManager getLdapManager() {
        return this.configuration.getLdapManager();
    }
}
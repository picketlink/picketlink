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
import static org.picketlink.idm.ldap.internal.LDAPConstants.SPACE_STRING;
import static org.picketlink.idm.ldap.internal.LDAPConstants.UID;

import java.io.Serializable;
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
import javax.naming.directory.SearchResult;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.internal.X509CertificateCredentialHandler;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.credential.spi.annotations.CredentialHandlers;
import org.picketlink.idm.credential.spi.annotations.Stored;
import org.picketlink.idm.event.GroupCreatedEvent;
import org.picketlink.idm.event.GroupDeletedEvent;
import org.picketlink.idm.event.GroupUpdatedEvent;
import org.picketlink.idm.event.RoleCreatedEvent;
import org.picketlink.idm.event.RoleDeletedEvent;
import org.picketlink.idm.event.RoleUpdatedEvent;
import org.picketlink.idm.event.UserCreatedEvent;
import org.picketlink.idm.event.UserDeletedEvent;
import org.picketlink.idm.event.UserUpdatedEvent;
import org.picketlink.idm.internal.util.IDMUtil;
import org.picketlink.idm.internal.util.properties.Property;
import org.picketlink.idm.internal.util.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.idm.internal.util.properties.query.PropertyQueries;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroupRole;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;

/**
 * An IdentityStore implementation backed by an LDAP directory
 * 
 * @author Shane Bryzak
 * @author Anil Saldhana
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@CredentialHandlers({LDAPPlainTextPasswordCredentialHandler.class, X509CertificateCredentialHandler.class})
public class LDAPIdentityStore implements IdentityStore<LDAPConfiguration>, CredentialStore {

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
        Class<? extends IdentityType> identityTypeClass = identityType.getClass();

        if (IDMUtil.isUserType(identityTypeClass)) {
            User storedUser = addUser((User) identityType);

            UserCreatedEvent event = new UserCreatedEvent(storedUser);
           // event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, storedUser);
            getContext().getEventBridge().raiseEvent(event);
        } else if (IDMUtil.isGroupType(identityTypeClass)) {
            Group storedGroup = addGroup((Group) identityType);

            GroupCreatedEvent event = new GroupCreatedEvent(storedGroup);
            //event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, storedGroup);
            getContext().getEventBridge().raiseEvent(event);
        } else if (IDMUtil.isRoleType(identityTypeClass)) {
            Role storedRole = addRole((Role) identityType);

            RoleCreatedEvent event = new RoleCreatedEvent(storedRole);
           // event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, storedRole);
            getContext().getEventBridge().raiseEvent(event);
        }
    }

    @Override
    public void update(IdentityType identityType) {
        Class<? extends IdentityType> identityTypeClass = identityType.getClass();

        if (IDMUtil.isUserType(identityTypeClass)) {
            User updatedUser = (User) identityType;

            if (updatedUser.getId() == null) {
                throw new IdentityManagementException("No identifier was provided.");
            }

            User storedUser = getUser(updatedUser.getId());

            if (storedUser == null) {
                throw new RuntimeException("User [" + updatedUser.getId() + "] does not exists.");
            }

            updateUser(updatedUser, storedUser);

            UserUpdatedEvent event = new UserUpdatedEvent(storedUser);
           // event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, storedUser);
            getContext().getEventBridge().raiseEvent(event);
        } else if (IDMUtil.isGroupType(identityTypeClass)) {
            Group updatedGroup = (Group) identityType;

            if (updatedGroup.getName() == null) {
                throw new IdentityManagementException("No identifier was provided.");
            }

            Group storedGroup = getGroup(updatedGroup.getName());

            if (storedGroup == null) {
                throw new RuntimeException("No group found with the given name [" + updatedGroup.getName() + "].");
            }

            updateGroup(updatedGroup, storedGroup);

            GroupUpdatedEvent event = new GroupUpdatedEvent(storedGroup);
            //event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, storedGroup);
            getContext().getEventBridge().raiseEvent(event);
        } else if (IDMUtil.isRoleType(identityTypeClass)) {
            Role updatedRole = (Role) identityType;

            if (updatedRole.getName() == null) {
                throw new IdentityManagementException("No identifier was provided.");
            }

            Role storedRole = getRole(updatedRole.getName());

            if (storedRole == null) {
                throw new RuntimeException("No role found with the given name [" + updatedRole.getName() + "].");
            }

            updateRole(updatedRole, storedRole);

            RoleUpdatedEvent event = new RoleUpdatedEvent(storedRole);
            //event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, storedRole);
            getContext().getEventBridge().raiseEvent(event);
        }
    }

    @Override
    public void remove(IdentityType identityType) {
        Class<? extends IdentityType> identityTypeClass = identityType.getClass();

        if (IDMUtil.isUserType(identityTypeClass)) {
            User user = (User) identityType;

            if (user.getId() == null) {
                throw new IdentityManagementException("No identifier was provided.");
            }

            User storedUser = getUser(user.getId());

            if (storedUser == null) {
                throw new RuntimeException("User [" + user.getId() + "] doest not exists.");
            }

            removeUser(storedUser);
            
            UserDeletedEvent event = new UserDeletedEvent(storedUser);
           // event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, storedUser);
            getContext().getEventBridge().raiseEvent(event);
        } else if (IDMUtil.isGroupType(identityTypeClass)) {
            Group group = (Group) identityType;

            if (group.getName() == null) {
                throw new IdentityManagementException("No identifier was provided.");
            }

            Group storedGroup = getGroup(group.getName());

            if (storedGroup == null) {
                throw new RuntimeException("Group [" + group.getName() + "] doest not exists.");
            }

            removeGroup(storedGroup);
            
            GroupDeletedEvent event = new GroupDeletedEvent(storedGroup);
            //event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, storedGroup);
            getContext().getEventBridge().raiseEvent(event);
        } else if (IDMUtil.isRoleType(identityTypeClass)) {
            Role role = (Role) identityType;
            
            if (role.getName() == null) {
                throw new IdentityManagementException("No identifier was provided.");
            }

            Role storedRole = getRole(role.getName());

            if (storedRole == null) {
                throw new RuntimeException("Role [" + role.getName() + "] doest not exists.");
            }

            removeRole(storedRole);
            
            RoleDeletedEvent event = new RoleDeletedEvent(storedRole);
            //event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, storedRole);
            getContext().getEventBridge().raiseEvent(event);
        }
    }

    @Override
    public Agent getAgent(String id) {
        // TODO: need to handle pure Agent instances. For now let's only consider User instances.
        return getUser(id);
    }

    @Override
    public User getUser(String id) {
        final String baseDN = this.configuration.getUserDNSuffix();

        List<User> answer = getLdapManager().searchByAttribute(baseDN, UID, id, new LDAPSearchCallback<User>() {

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
            User user = getUser(((User) member).getId());

            LDAPRole ldapRole = null;

            if (role != null) {
                ldapRole = (LDAPRole) getRole(role.getName());
            }

            LDAPUser ldapUser = null;

            if (user != null) {
                ldapUser = (LDAPUser) getUser(user.getId());
            }

            LDAPGroup ldapGroup = null;

            if (group != null) {
                ldapGroup = (LDAPGroup) getGroup(group.getName());
            }

            if (ldapRole != null && ldapGroup != null) {
                LDAPGroupRole groupRole = new LDAPGroupRole(ldapUser, ldapGroup, ldapRole);
                storeMembershipEntry(groupRole, ldapRole);
            } else {
                if (ldapUser != null && ldapRole != null) {
                    addMember(ldapRole, ldapUser);
                }

                if (ldapGroup != null && ldapRole != null) {
                    addMember(ldapGroup, ldapRole);
                }

                if (ldapGroup != null && ldapUser != null) {
                    addMember(ldapGroup, ldapUser);
                }
            }

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
            LDAPUser ldapUser = (LDAPUser) getUser(((User) member).getId());

            LDAPRole ldapRole = null;

            if (role != null) {
                ldapRole = (LDAPRole) getRole(role.getName());
            }

            LDAPGroup ldapGroup = null;

            if (group != null) {
                ldapGroup = (LDAPGroup) getGroup(group.getName());
            }

            if (group != null && role != null) {
                LDAPGroupRole groupRole = new LDAPGroupRole(ldapUser, ldapGroup, ldapRole);
                removeMemberShipEntry(groupRole, ldapRole);
            } else {
                if (ldapRole != null) {
                    removeMember(ldapRole, ldapUser);
                }

                if (ldapGroup != null) {
                    removeMember(ldapGroup, ldapUser);
                }
            }
        } else if (member instanceof Group) {
            // FIXME implement Group membership if supported
        }
    }

    @Override
    public GroupRole getMembership(IdentityType member, Group group, Role role) {
        GroupRole groupRole = null;

        LDAPUser ldapUser = (LDAPUser) getUser(((User) member).getId());

        if (group != null && role != null) {
            LDAPRole ldapRole = (LDAPRole) getRole(role.getName());
            LDAPGroup ldapGroup = (LDAPGroup) getGroup(group.getName());

            String dn = new LDAPGroupRole(ldapUser, ldapGroup, ldapRole).getDN();

            groupRole = getLdapManager().lookup(dn);

            LDAPGroupRole ldapGroupRole = (LDAPGroupRole) groupRole;

            if (groupRole == null || !ldapGroupRole.isMember(ldapRole)) {
                groupRole = null;
            }
        } else {
            if (role != null) {
                LDAPRole ldapRole = (LDAPRole) getRole(role.getName());

                if (ldapRole.isMember(ldapUser)) {
                    groupRole = new SimpleGroupRole(ldapUser, getRole(role.getName()), null);
                }
            }

            if (group != null) {
                LDAPGroup ldapGroup = (LDAPGroup) getGroup(group.getName());

                if (ldapGroup.isMember(ldapUser)) {
                    groupRole = new SimpleGroupRole(ldapUser, null, group);
                }
            }
        }

        return groupRole;
    }

    @Override
    public <T extends IdentityType> List<T> fetchQueryResults(IdentityQuery<T> identityQuery) {
        // TODO: pagination of query results needs to be implemented
        List<T> result = new ArrayList<T>();
        String filter = getSearchFilter((IdentityQuery<IdentityType>) identityQuery);

        Class<T> typeClass = identityQuery.getIdentityType();

        NamingEnumeration<SearchResult> answer = null;

        if (filter == null) {
            return result;
        }

        LDAPQuery ldapQuery = new LDAPQuery(identityQuery.getParameters());
        String idAttribute = getIdAttribute(typeClass);
        String dnSuffix = getBaseDN(typeClass);

        try {

            answer = getLdapManager().search(dnSuffix, filter);

            while (answer.hasMoreElements()) {
                SearchResult sr = (SearchResult) answer.nextElement();
                Attributes attributes = sr.getAttributes();
                String uid = (String) attributes.get(idAttribute).get();

                LDAPCustomAttributes customAttributes = getCustomAttributes(idAttribute + "=" + uid + COMMA + dnSuffix);

                if (ldapQuery.hasCustomAttributes() && customAttributes == null) {
                    continue;
                }

                if (identityQuery.getParameters().containsKey(IdentityType.ENABLED)) {
                    Object[] values = identityQuery.getParameters().get(IdentityType.ENABLED);
                    String enabled = String.valueOf(customAttributes.getAttribute(LDAPConstants.CUSTOM_ATTRIBUTE_ENABLED));

                    if (!enabled.equals(values[0].toString())) {
                        continue;
                    }
                }

                if (identityQuery.getParameters().containsKey(IdentityType.CREATED_DATE)) {
                    Object[] values = identityQuery.getParameters().get(IdentityType.CREATED_DATE);
                    long storedDateInMillis = Long.valueOf(customAttributes.getAttribute(
                            LDAPConstants.CUSTOM_ATTRIBUTE_CREATE_DATE).toString());
                    long providedDateInMillis = ((Date) values[0]).getTime();

                    if (storedDateInMillis != providedDateInMillis) {
                        continue;
                    }
                }

                if (identityQuery.getParameters().containsKey(IdentityType.CREATED_BEFORE)) {
                    Object[] values = identityQuery.getParameters().get(IdentityType.CREATED_BEFORE);
                    long storedDateInMillis = Long.valueOf(customAttributes.getAttribute(
                            LDAPConstants.CUSTOM_ATTRIBUTE_CREATE_DATE).toString());
                    long providedDateInMillis = ((Date) values[0]).getTime();

                    if (storedDateInMillis > providedDateInMillis) {
                        continue;
                    }
                }

                if (identityQuery.getParameters().containsKey(IdentityType.CREATED_AFTER)) {
                    Object[] values = identityQuery.getParameters().get(IdentityType.CREATED_AFTER);
                    long storedDateInMillis = Long.valueOf(customAttributes.getAttribute(
                            LDAPConstants.CUSTOM_ATTRIBUTE_CREATE_DATE).toString());
                    long providedDateInMillis = ((Date) values[0]).getTime();

                    if (storedDateInMillis < providedDateInMillis) {
                        continue;
                    }
                }

                if (identityQuery.getParameters().containsKey(IdentityType.EXPIRY_DATE)
                        || identityQuery.getParameters().containsKey(IdentityType.EXPIRY_BEFORE)
                        || identityQuery.getParameters().containsKey(IdentityType.EXPIRY_AFTER)) {

                    Object expiryAttribute = customAttributes.getAttribute(LDAPConstants.CUSTOM_ATTRIBUTE_EXPIRY_DATE);

                    if (expiryAttribute == null) {
                        continue;
                    }

                    if (identityQuery.getParameters().containsKey(IdentityType.EXPIRY_DATE)) {
                        Object[] values = identityQuery.getParameters().get(IdentityType.EXPIRY_DATE);
                        long storedDateInMillis = Long.valueOf(expiryAttribute.toString());
                        long providedDateInMillis = ((Date) values[0]).getTime();

                        if (storedDateInMillis != providedDateInMillis) {
                            continue;
                        }
                    }

                    if (identityQuery.getParameters().containsKey(IdentityType.EXPIRY_BEFORE)) {
                        Object[] values = identityQuery.getParameters().get(IdentityType.EXPIRY_BEFORE);
                        long storedDateInMillis = Long.valueOf(expiryAttribute.toString());
                        long providedDateInMillis = ((Date) values[0]).getTime();

                        if (storedDateInMillis > providedDateInMillis) {
                            continue;
                        }
                    }

                    if (identityQuery.getParameters().containsKey(IdentityType.EXPIRY_AFTER)) {
                        Object[] values = identityQuery.getParameters().get(IdentityType.EXPIRY_AFTER);
                        long storedDateInMillis = Long.valueOf(expiryAttribute.toString());
                        long providedDateInMillis = ((Date) values[0]).getTime();

                        if (storedDateInMillis < providedDateInMillis) {
                            continue;
                        }
                    }
                }

                // let's restrict the result by looking the provided custom attributes.
                boolean match = true;

                for (Entry<QueryParameter, Object[]> ldapQueryParameter : identityQuery.getParameters().entrySet()) {
                    QueryParameter queryParameter = ldapQueryParameter.getKey();
                    Object[] values = ldapQueryParameter.getValue();

                    if (queryParameter instanceof IdentityType.AttributeParameter) {
                        match = false;

                        Object[] queryParameterValues = values;
                        IdentityType.AttributeParameter customParameter = (IdentityType.AttributeParameter) queryParameter;
                        Object customParameterValue = customAttributes.getAttribute(customParameter.getName());

                        if (customParameterValue != null) {
                            int count = queryParameterValues.length;

                            for (Object parameterValue : queryParameterValues) {
                                if (customParameterValue.getClass().isArray()) {
                                    Object[] customParameterValues = (Object[]) customParameterValue;

                                    for (Object value : customParameterValues) {
                                        if (value.equals(parameterValue)) {
                                            count--;
                                        }
                                    }
                                } else {
                                    if (parameterValue.equals(customParameterValue)) {
                                        count--;
                                    }
                                }
                            }

                            if (count <= 0) {
                                match = true;
                            }
                        }
                    }
                }

                if (match) {
                    if (IDMUtil.isUserType(typeClass)) {
                        result.add((T) getUser(uid));
                    } else if (IDMUtil.isRoleType(typeClass)) {
                        result.add((T) getRole(uid));
                    } else if (IDMUtil.isGroupType(typeClass)) {
                        result.add((T) getGroup(uid));
                    }
                }
            }
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
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
    
    @Override
    public void validateCredentials(Credentials credentials) {
        CredentialHandler handler = getContext().getCredentialValidator(credentials.getClass(), this);
        if (handler == null) {
            throw new SecurityConfigurationException(
                    "No suitable CredentialHandler available for validating Credentials of type [" + credentials.getClass()
                            + "] for IdentityStore [" + this.getClass() + "]");
        }
        handler.validate(credentials, this);
    }

    @Override
    public void updateCredential(Agent agent, Object credential, Date effectiveDate, Date expiryDate) {
        CredentialHandler handler = getContext().getCredentialUpdater(credential.getClass(), this);
        if (handler == null) {
            throw new SecurityConfigurationException(
                    "No suitable CredentialHandler available for updating Credentials of type [" + credential.getClass()
                            + "] for IdentityStore [" + this.getClass() + "]");
        }
        handler.update(agent, credential, this, effectiveDate, expiryDate);
    }

    @Override
    public void storeCredential(Agent agent, CredentialStorage storage) {
        List<Property<Object>> annotatedTypes = PropertyQueries.createQuery(storage.getClass())
                .addCriteria(new AnnotatedPropertyCriteria(Stored.class)).getResultList();

        if (annotatedTypes.isEmpty()) {
            throw new IdentityManagementException("Could not find any @Stored annotated method for CredentialStorage type ["
                    + storage.getClass().getName() + "].");
        } else {
            Property<Object> storedProperty = annotatedTypes.get(0);
            Object credential = storedProperty.getValue(storage);

            if (Serializable.class.isInstance(credential)) {
                org.picketlink.idm.model.Attribute<Serializable> credentialAttribute = new org.picketlink.idm.model.Attribute<Serializable>(
                        storage.getClass().getName(), (Serializable) credential);

                agent.setAttribute(credentialAttribute);

                update(agent);
            } else {
                throw new IdentityManagementException(
                        "Credential storage property [" + storedProperty.getName() + "] in class [" + 
                        storage.getClass().getName() + "] must implement Serializable");
            }
        }
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(Agent agent, Class<T> storageClass) {
        T storage = null;
        List<Property<Object>> annotatedTypes = PropertyQueries.createQuery(storageClass)
                .addCriteria(new AnnotatedPropertyCriteria(Stored.class)).getResultList();

        if (annotatedTypes.isEmpty()) {
            throw new IdentityManagementException("Could not find any @Stored annotated method for CredentialStorage type ["
                    + storageClass.getName() + "].");
        } else {
            Property<Object> storedProperty = annotatedTypes.get(0);
            org.picketlink.idm.model.Attribute<Serializable> credentialAttribute = agent.getAttribute(storageClass.getName());

            if (credentialAttribute != null) {
                try {
                    storage = storageClass.newInstance();
                } catch (Exception e) {
                    throw new IdentityManagementException("Error while creating a " + storageClass.getName()
                            + " storage instance.", e);
                }

                storedProperty.setValue(storage, credentialAttribute.getValue());
            } else {
                throw new IdentityManagementException(
                        "Methods annotated with @Stored should aways return a serializable object.");
            }
        }

        return storage;
    }

    @Override
    public <T extends IdentityType> int countQueryResults(IdentityQuery<T> identityQuery) {
        throw createNotImplementedYetException();
    }

    @Override
    public <T extends Serializable> org.picketlink.idm.model.Attribute<T> getAttribute(IdentityType identityType,
            String attributeName) {
        throw createNotImplementedYetException();
    }

    @Override
    public void setAttribute(IdentityType identity, org.picketlink.idm.model.Attribute<? extends Serializable> attribute) {
        throw createNotImplementedYetException();
    }

    @Override
    public void removeAttribute(IdentityType identity, String name) {
        throw createNotImplementedYetException();
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
        LDAPUser ldapUser = null;

        if (user instanceof LDAPUser) {
            ldapUser = (LDAPUser) user;
        } else {
            ldapUser = new LDAPUser(this.configuration.getUserDNSuffix());

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
        return CN + "=custom-attributes" + COMMA + parentDN;
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

    private void addMember(LDAPEntry parentEntry, LDAPEntry childEntry) {
        parentEntry.addMember(childEntry);
        getLdapManager().modifyAttribute(parentEntry.getDN(), parentEntry.getLDAPAttributes().get(MEMBER));
    }

    private void removeMember(LDAPEntry parentEntry, LDAPEntry childEntry) {
        parentEntry.removeMember(childEntry);
        getLdapManager().modifyAttribute(parentEntry.getDN(), parentEntry.getLDAPAttributes().get(MEMBER));
    }

    /**
     * <p>
     * Removes the given {@link LDAPEntry} entry from the LDAP tree. This method also remove the custom attribute entry for the
     * given parent instance.
     * </p>
     * 
     * @param ldapEntry
     */
    private void removeEntry(LDAPEntry ldapEntry) {
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

        NamingEnumeration<SearchResult> answer = null;

        // Search for objects with these matching attributes
        try {
            answer = getLdapManager().search(this.configuration.getGroupDNSuffix(), matchAttrs, new String[] { CN });
            while (answer.hasMoreElements()) {
                SearchResult sr = (SearchResult) answer.nextElement();
                Attributes attributes = sr.getAttributes();
                String cn = (String) attributes.get(CN).get();
                return getGroup(cn);
            }
        } catch (NamingException e) {
            throw new RuntimeException("Error looking parent group for [" + childGroup.getDN() + "]", e);
        } finally {
            if (answer != null) {
                try {
                    answer.close();
                } catch (NamingException e) {
                }
            }
        }

        return null;
    }

    /**
     * <p>
     * Updates the attributes for the given {@link LDAPEntry} instance.
     * </p>
     * 
     * @param updatedEntryEntry
     */
    private void updateAttributes(LDAPEntry updatedEntryEntry, LDAPEntry storedEntry) {
        try {
            NamingEnumeration<? extends Attribute> storedAttributes = storedEntry.getLDAPAttributes().getAll();

            // check for attributes to replace or remove
            while (storedAttributes.hasMore()) {
                Attribute storedAttribute = storedAttributes.next();
                Attribute updatedAttribute = updatedEntryEntry.getLDAPAttributes().get(storedAttribute.getID());

                // if the stored attribute exists in the updated attributes list, replace it. Otherwise remove it from the
                // store.
                if (updatedAttribute != null) {
                    getLdapManager().modifyAttribute(storedEntry.getDN(), updatedAttribute);
                } else {
                    getLdapManager().removeAttribute(storedEntry.getDN(), storedAttribute);
                }
            }

            NamingEnumeration<? extends Attribute> enumUpdatedAttributes = updatedEntryEntry.getLDAPAttributes().getAll();

            // check for attributes to add
            while (enumUpdatedAttributes.hasMore()) {
                Attribute updatedAttribute = enumUpdatedAttributes.next();
                Attribute storedAttribute = storedEntry.getLDAPAttributes().get(updatedAttribute.getID());

                // if the attribute is not stored and is a managed attribute add it to the store.
                if (storedAttribute == null && getLdapManager().isManagedAttribute(updatedAttribute.getID())) {
                    getLdapManager().addAttribute(storedEntry.getDN(), updatedAttribute);
                }
            }

            LDAPCustomAttributes attributes = updatedEntryEntry.getCustomAttributes();

            getLdapManager().rebind(getCustomAttributesDN(updatedEntryEntry.getDN()), attributes);
        } catch (NamingException e) {
            throw new IdentityManagementException("Error updating custom attributes for IdentityType [" + storedEntry + "].", e);
        }
    }

    /**
     * <p>
     * Finds all parent entries where the specified {@link LDAPEntry} is configured as a member.
     * </p>
     * 
     * @param ldapUser
     * @return
     */
    private NamingEnumeration<SearchResult> findParentEntries(String dnSuffix, LDAPEntry member) {
        String filter = "(member=" + member.getDN() + ")";

        return getLdapManager().search(dnSuffix, filter);
    }

    /**
     * <p>
     * Remove from parent entries inside the given <code>dnSuffix</code> the specified {@link LDAPEntry}.
     * </p>
     * 
     * @param dnSuffix
     * @param member
     */
    private void removeFromParent(String dnSuffix, LDAPEntry member) {
        NamingEnumeration<SearchResult> results = null;

        try {
            results = findParentEntries(dnSuffix, member);

            while (results.hasMoreElements()) {
                SearchResult searchResult = (SearchResult) results.nextElement();
                Attribute memberAttribute = searchResult.getAttributes().get(MEMBER);

                if (memberAttribute != null) {
                    memberAttribute.remove(member.getDN());
                }

                if (!memberAttribute.getAll().hasMoreElements()) {
                    memberAttribute.add(SPACE_STRING);
                }
            }
        } catch (NamingException ne) {
            throw new IdentityManagementException(ne);
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (NamingException e) {
                }
            }
        }

    }

    private void storeMembershipEntry(LDAPEntry ldapEntry, LDAPEntry member) {
        String dn = ldapEntry.getDN();

        LDAPEntry storedGroupRole = getLdapManager().lookup(dn);

        if (storedGroupRole == null) {
            storedGroupRole = ldapEntry;
            getLdapManager().bind(dn, storedGroupRole);
        } else {
            Attribute memberAttribute = storedGroupRole.getLDAPAttributes().get(MEMBER);

            if (!memberAttribute.contains(member.getDN())) {
                memberAttribute.add(member.getDN());
                getLdapManager().modifyAttribute(dn, memberAttribute);
                getLdapManager().rebind(dn, storedGroupRole);
            }
        }
    }

    private void removeMemberShipEntry(LDAPEntry ldapEntry, LDAPEntry member) {
        String dn = ldapEntry.getDN();

        LDAPEntry storedGroupRole = getLdapManager().lookup(dn);

        if (storedGroupRole != null) {
            Attribute memberAttribute = storedGroupRole.getLDAPAttributes().get(MEMBER);

            if (memberAttribute.contains(member.getDN())) {
                memberAttribute.remove(member.getDN());
                memberAttribute.add(SPACE_STRING);
                getLdapManager().modifyAttribute(dn, memberAttribute);
                getLdapManager().rebind(dn, storedGroupRole);
            }
        }
    }

    public LDAPOperationManager getLdapManager() {
        return this.configuration.getLdapManager();
    }

    protected Role addRole(Role role) {
        if (role.getName() == null) {
            throw new IdentityManagementException("No identifier was provided.");
        }

        LDAPRole ldapRole = new LDAPRole(this.configuration.getRoleDNSuffix());

        ldapRole.setName(role.getName());

        store(ldapRole);

        return ldapRole;
    }

    protected Group addGroup(Group group) {
        if (group.getName() == null) {
            throw new IdentityManagementException("No identifier was provided.");
        }

        LDAPGroup ldapGroup = new LDAPGroup(this.configuration.getGroupDNSuffix());

        ldapGroup.setName(group.getName());

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

        store(ldapGroup);

        return ldapGroup;
    }

    protected User addUser(User user) {
        if (user.getId() == null) {
            throw new IdentityManagementException("No identifier was provided.");
        }

        LDAPUser ldapUser = null;

        if (!(user instanceof LDAPUser)) {
            ldapUser = convert(user);
        } else {
            ldapUser = (LDAPUser) user;
        }

        ldapUser.setFullName(ldapUser.getUserCN());

        store(ldapUser);

        return ldapUser;
    }

    protected Group updateGroup(Group updatedGroup, Group storedGroup) {
        updateAttributes((LDAPGroup) updatedGroup, (LDAPGroup) storedGroup);

        return updatedGroup;
    }

    protected Role updateRole(Role updatedRole, Role storedRole) {
        updateAttributes((LDAPRole) updatedRole, (LDAPRole) storedRole);

        return updatedRole;
    }

    protected User updateUser(User user, User storedUser) {
        LDAPUser updatedUser = convert(user);

        updatedUser.setFullName(updatedUser.getUserCN());

        updateAttributes(updatedUser, (LDAPEntry) storedUser);

        return updatedUser;
    }

    protected Role removeRole(Role role) {
        removeEntry((LDAPEntry) role);
        removeFromParent(this.configuration.getGroupDNSuffix(), (LDAPEntry) role);
        return role;
    }

    protected Group removeGroup(Group group) {
        // removes the custom grouprole entry from inside the user entries
        NamingEnumeration<SearchResult> results = null;

        try {
            results = getLdapManager().search(this.configuration.getUserDNSuffix(), "(&(cn= " + group.getName() + "*))");

            while (results.hasMoreElements()) {
                SearchResult searchResult = (SearchResult) results.nextElement();
                String dn = searchResult.getNameInNamespace();
                getLdapManager().destroySubcontext(dn);
            }
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (NamingException e) {
                }
            }
        }

        removeEntry((LDAPEntry) group);

        return group;
    }

    protected User removeUser(User user) {
        removeFromParent(this.configuration.getRoleDNSuffix(), (LDAPEntry) user);
        removeFromParent(this.configuration.getGroupDNSuffix(), (LDAPEntry) user);

        removeEntry((LDAPEntry) user);

        return user;
    }

    /**
     * <p>
     * Returns a LDAP search filter that restricts the results to only those that match one of the membership query parameters,
     * if provided.
     * </p>
     * 
     * @param identityQuery
     * @return
     */
    private String getSearchFilter(IdentityQuery<IdentityType> identityQuery) {
        Class<IdentityType> typeClass = identityQuery.getIdentityType();

        StringBuffer additionalFilter = new StringBuffer();

        if (IDMUtil.isUserType(typeClass)) {
            // add to the filter only the users that have the specified roles
            if (identityQuery.getParameters().containsKey(User.HAS_ROLE)) {
                Object[] roleNames = identityQuery.getParameters().get(User.HAS_ROLE);
                LDAPEntry[] roles = new LDAPEntry[roleNames.length];

                for (int i = 0; i < roleNames.length; i++) {
                    Object name = roleNames[i];
                    roles[i] = (LDAPEntry) getRole(name.toString());
                }

                String usersFilterMemberOf = getUsersFilterMemberOf(roles);

                if (usersFilterMemberOf.length() == 0) {
                    return null;
                }

                additionalFilter.append(usersFilterMemberOf);
            }

            // add to the filter only the users member of the specified groups
            if (identityQuery.getParameters().containsKey(User.MEMBER_OF)) {
                Object[] groupNames = identityQuery.getParameters().get(User.MEMBER_OF);
                LDAPEntry[] groups = new LDAPEntry[groupNames.length];

                for (int i = 0; i < groupNames.length; i++) {
                    Object name = groupNames[i];
                    groups[i] = (LDAPEntry) getGroup(name.toString());
                }

                String usersFilterMemberOf = getUsersFilterMemberOf(groups);

                if (usersFilterMemberOf.length() == 0) {
                    return null;
                }

                additionalFilter.append(usersFilterMemberOf);
            }

            // add to the filter only users with the specified group and role combination
            if (identityQuery.getParameters().containsKey(IdentityType.HAS_GROUP_ROLE)) {
                Object[] groupRoles = identityQuery.getParameters().get(User.HAS_GROUP_ROLE);

                NamingEnumeration<SearchResult> search = null;

                try {
                    for (Object group : groupRoles) {
                        GroupRole groupRole = (GroupRole) group;

                        search = getLdapManager().search(this.configuration.getUserDNSuffix(),
                                "(" + CN + "=" + groupRole.getGroup().getName() + ")");

                        if (search.hasMoreElements()) {
                            while (search.hasMoreElements()) {
                                SearchResult searchResult = search.next();
                                String[] nameInNamespace = searchResult.getNameInNamespace().split(",");
                                String userId = nameInNamespace[1];

                                Attribute member = searchResult.getAttributes().get(MEMBER);

                                if (member.contains(CN + "=" + groupRole.getRole().getName() + COMMA
                                        + this.configuration.getRoleDNSuffix())) {
                                    additionalFilter.append("(").append(userId).append(")");
                                }
                            }
                        }
                    }

                    if (additionalFilter.length() == 0) {
                        return null;
                    }
                } catch (Exception e) {
                    throw new IdentityManagementException(e);
                } finally {
                    if (search != null) {
                        try {
                            search.close();
                        } catch (NamingException e) {
                        }
                    }
                }
            }
        } else if (IDMUtil.isRoleType(typeClass)) {
            // add to the filter only the roles where the specified agents are member of
            if (identityQuery.getParameters().containsKey(Role.ROLE_OF)) {
                Object[] values = identityQuery.getParameters().get(Role.ROLE_OF);
                Agent[] agents = new Agent[values.length];

                for (int j = 0; j < values.length; j++) {
                    Object value = values[j];
                    agents[j] = (Agent) value;
                }

                String filter = getEntryFilterForMembers(agents, this.configuration.getRoleDNSuffix());

                if (filter.length() == 0) {
                    return null;
                }

                additionalFilter.append(filter);
            }
        } else if (IDMUtil.isGroupType(typeClass)) {
            // add to the filter only the groups where the specified agents are member of
            if (identityQuery.getParameters().containsKey(Group.HAS_MEMBER)) {
                Object[] values = identityQuery.getParameters().get(Group.HAS_MEMBER);
                Agent[] agents = new Agent[values.length];

                for (int j = 0; j < values.length; j++) {
                    Object value = values[j];
                    agents[j] = (Agent) value;
                }

                String filter = getEntryFilterForMembers(agents, this.configuration.getGroupDNSuffix());

                if (filter.length() == 0) {
                    return null;
                }

                additionalFilter.append(filter);
            }

            // add to the filter only the groups with the specified parent
            if (identityQuery.getParameters().containsKey(Group.PARENT)) {
                String parentName = identityQuery.getParameters().get(Group.PARENT)[0].toString();
                LDAPGroup parentGroup = (LDAPGroup) getGroup(parentName);

                NamingEnumeration<?> members = null;

                try {
                    members = parentGroup.getLDAPAttributes().get(MEMBER).getAll();

                    while (members.hasMoreElements()) {
                        String groupDN = (String) members.nextElement();

                        if (groupDN.toString().trim().isEmpty()) {
                            continue;
                        }

                        String groupName = groupDN.split(",")[0];

                        additionalFilter.append("(").append(groupName).append(")");
                    }
                } catch (NamingException e) {
                    throw new IdentityManagementException(e);
                } finally {
                    if (members != null) {
                        try {
                            members.close();
                        } catch (NamingException e) {
                        }
                    }
                }
            }
        }

        if (additionalFilter.length() > 0) {
            additionalFilter.insert(0, "(|");
            additionalFilter.insert(additionalFilter.length() - 1, ")");
        }

        LDAPQuery ldapQuery = new LDAPQuery(identityQuery.getParameters());

        StringBuffer filter = ldapQuery.createManagedAttributesFilter();

        if (filter == null) {
            filter = new StringBuffer("(&(objectClass=*)(" + getIdAttribute(typeClass) + "=*)(!(cn=custom-attributes)))");
        }

        filter.insert(filter.length() - 1, additionalFilter.toString());

        return filter.toString();
    }

    private String getIdAttribute(Class<? extends IdentityType> identityTypeClass) {
        String idAttribute = null;

        if (IDMUtil.isUserType(identityTypeClass)) {
            idAttribute = UID;
        } else if (IDMUtil.isRoleType(identityTypeClass)) {
            idAttribute = CN;
        } else if (IDMUtil.isGroupType(identityTypeClass)) {
            idAttribute = CN;
        }

        return idAttribute;
    }

    private String getBaseDN(Class<? extends IdentityType> identityTypeClass) {
        String baseDN = null;

        if (IDMUtil.isUserType(identityTypeClass)) {
            baseDN = this.configuration.getUserDNSuffix();
        } else if (IDMUtil.isRoleType(identityTypeClass)) {
            baseDN = this.configuration.getRoleDNSuffix();
        } else if (IDMUtil.isGroupType(identityTypeClass)) {
            baseDN = this.configuration.getGroupDNSuffix();
        }

        return baseDN;
    }

    /**
     * <p>
     * Returns a filter where only the specified {@link Agent} are member of.
     * </p>
     * 
     * @param members
     * @param baseDN
     * @return
     */
    private String getEntryFilterForMembers(Agent[] members, String baseDN) {
        StringBuffer additionalFilter = new StringBuffer();
        String hasMemberFilter = "";

        for (Agent agent : members) {
            LDAPUser ldapUser = (LDAPUser) getUser(agent.getId());

            hasMemberFilter = hasMemberFilter + "(member=" + ldapUser.getDN() + ")";
        }

        NamingEnumeration<SearchResult> search = null;

        try {
            search = getLdapManager().search(baseDN, hasMemberFilter.toString());

            while (search.hasMoreElements()) {
                SearchResult searchResult = search.next();
                String entryCN = searchResult.getAttributes().get(CN).get().toString();

                additionalFilter.append("(").append(CN).append("=").append(entryCN).append(")");
            }
        } catch (Exception e) {
            throw new IdentityManagementException(e);
        } finally {
            if (search != null) {
                try {
                    search.close();
                } catch (NamingException e) {
                }
            }
        }

        return additionalFilter.toString();
    }

    private String getUsersFilterMemberOf(LDAPEntry[] parents) {
        StringBuffer additionalFilter = new StringBuffer();
        Map<String, Integer> userCount = new HashMap<String, Integer>();

        for (LDAPEntry ldapEntry : parents) {
            Attribute memberAttribute = null;

            memberAttribute = ldapEntry.getLDAPAttributes().get(MEMBER);

            NamingEnumeration<?> members = null;

            try {
                members = memberAttribute.getAll();

                while (members.hasMoreElements()) {
                    String memberDN = (String) members.nextElement();

                    if (!memberDN.trim().isEmpty()) {
                        String userId = memberDN.split(",")[0];

                        if (!userCount.containsKey(userId)) {
                            userCount.put(userId, 1);
                        } else {
                            Integer count = userCount.get(userId);
                            userCount.put(userId, count + 1);
                        }

                        additionalFilter.append("(").append(userId).append(")");
                    }
                }
            } catch (NamingException e) {
                throw new IdentityManagementException(e);
            } finally {
                if (members != null) {
                    try {
                        members.close();
                    } catch (NamingException e) {
                    }
                }
            }
        }

        Set<Entry<String, Integer>> entrySet = userCount.entrySet();

        for (Entry<String, Integer> entry : entrySet) {
            if (!entry.getValue().equals(parents.length)) {
                String filterTmp = additionalFilter.toString();

                filterTmp = filterTmp.replaceAll("\\(" + entry.getKey() + "\\)", "");

                additionalFilter = new StringBuffer(filterTmp);
            }
        }

        return additionalFilter.toString();
    }
    
    private IdentityManagementException createNotImplementedYetException() {
        return new IdentityManagementException("Not implemented yet.");
    }
}
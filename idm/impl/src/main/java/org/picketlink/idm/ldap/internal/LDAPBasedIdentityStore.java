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

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.internal.X509CertificateCredentialHandler;
import org.picketlink.idm.credential.spi.annotations.CredentialHandlers;
import org.picketlink.idm.internal.util.IDMUtil;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.Grant;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupMembership;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleAgent;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;

/**
 * An IdentityStore implementation backed by an LDAP directory
 * 
 * @author Shane Bryzak
 * @author Anil Saldhana
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@CredentialHandlers({ LDAPPlainTextPasswordCredentialHandler.class, X509CertificateCredentialHandler.class })
public class LDAPBasedIdentityStore implements IdentityStore<LDAPConfiguration> {

    private LDAPConfiguration configuration;
    private IdentityStoreInvocationContext context;

    @Override
    public void setup(LDAPConfiguration config, IdentityStoreInvocationContext context) {
        this.configuration = config;
        this.context = context;

        if (this.context.getRealm() == null) {
            this.context.setRealm(new Realm(Realm.DEFAULT_REALM));
        }
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
    public void add(AttributedType attributedType) {
        attributedType.setId(getContext().getIdGenerator().generate());

        if (IdentityType.class.isInstance(attributedType)) {
            IdentityType identityType = (IdentityType) attributedType;
            identityType.setPartition(getContext().getRealm());

            if (Agent.class.isInstance(attributedType)) {
                if (User.class.isInstance(attributedType)) {
                    User newUser = (User) attributedType;
                    addUser(newUser);
                } else {
                    Agent newAgent = (Agent) attributedType;
                    addAgent(newAgent);
                }
            } else if (Role.class.isInstance(attributedType)) {
                Role newRole = (Role) attributedType;
                addRole(newRole);
            } else if (Group.class.isInstance(attributedType)) {
                Group newGroup = (Group) attributedType;
                addGroup(newGroup);
            } else {
                throw createUnsupportedIdentityTypeException(identityType.getClass());
            }
        } else if (Relationship.class.isInstance(attributedType)) {
            Relationship relationship = (Relationship) attributedType;

            if (Grant.class.isInstance(relationship)) {
                Grant grant = (Grant) relationship;

                LDAPRole roleEntry = lookupEntry(new LDAPRole(grant.getRole().getName(), getConfig().getRoleDNSuffix()));
                LDAPAgent agentEntry = null;

                if (Agent.class.isInstance(grant.getAssignee())) {
                    Agent agent = (Agent) grant.getAssignee();

                    if (User.class.isInstance(grant.getAssignee())) {
                        agentEntry = lookupEntry(new LDAPUser(agent.getLoginName(), getConfig().getUserDNSuffix()));
                    } else {
                        agentEntry = lookupEntry(new LDAPAgent(agent.getLoginName(), getConfig().getAgentDNSuffix()));
                    }
                }

                addMember(roleEntry, agentEntry);
            } else if (GroupMembership.class.isInstance(relationship)) {
                GroupMembership groupMembership = (GroupMembership) relationship;

                LDAPGroup groupEntry = lookupEntry(new LDAPGroup(groupMembership.getGroup().getName(), getConfig()
                        .getGroupDNSuffix()));
                LDAPAgent agentEntry = null;

                if (Agent.class.isInstance(groupMembership.getMember())) {
                    Agent agent = (Agent) groupMembership.getMember();

                    if (User.class.isInstance(groupMembership.getMember())) {
                        agentEntry = lookupEntry(new LDAPUser(agent.getLoginName(), getConfig().getUserDNSuffix()));
                    } else {
                        agentEntry = lookupEntry(new LDAPAgent(agent.getLoginName(), getConfig().getAgentDNSuffix()));
                    }
                }

                addMember(groupEntry, agentEntry);

                if (GroupRole.class.isInstance(relationship)) {
                    GroupRole groupRole = (GroupRole) relationship;

                    LDAPRole roleEntry = lookupEntry(new LDAPRole(groupRole.getRole().getName(), getConfig().getRoleDNSuffix()));

                    LDAPGroupRole groupRoleEntry = new LDAPGroupRole(agentEntry, groupEntry, roleEntry);

                    NamingEnumeration<SearchResult> search = getConfig().getLdapManager().search(agentEntry.getDN(),
                            groupRoleEntry.getBidingName());

                    try {
                        if (!search.hasMore()) {
                            getConfig().getLdapManager().createSubContext(groupRoleEntry.getDN(),
                                    groupRoleEntry.getLDAPAttributes());
                        }
                        addMember(groupRoleEntry, roleEntry);
                    } catch (Exception e) {
                        throw new IdentityManagementException("Error creating GroupRole relationship.", e);
                    }
                }
            }
        } else {
            throw createUnsupportedAttributedType(attributedType.getClass());
        }
    }

    private void addMember(LDAPEntry parentEntry, LDAPEntry childEntry) {
        parentEntry.addMember(childEntry);
        getConfig().getLdapManager().modifyAttribute(parentEntry.getDN(), parentEntry.getLDAPAttributes().get(MEMBER));
    }

    private IdentityManagementException createUnsupportedIdentityTypeException(Class<? extends IdentityType> identityTypeClass) {
        return new IdentityManagementException("Unsupported IdentityType [" + identityTypeClass.getName() + "].");
    }

    private IdentityManagementException createUnsupportedAttributedType(Class<? extends AttributedType> type) {
        return new IdentityManagementException("Unsupported AttributedType [" + type.getName() + "].");
    }

    @Override
    public void update(AttributedType attributedType) {
        if (Agent.class.isInstance(attributedType)) {
            if (User.class.isInstance(attributedType)) {
                User updatedUser = (User) attributedType;
                updateUser(updatedUser);
            } else {
                Agent updatedAgent = (Agent) attributedType;
                updateAgent(updatedAgent);
            }
        } else if (Role.class.isInstance(attributedType)) {
            Role updatedRole = (Role) attributedType;
            updateRole(updatedRole);
        } else if (Group.class.isInstance(attributedType)) {
            Group updatedGroup = (Group) attributedType;
            updateGroup(updatedGroup);
        }
    }

    @Override
    public void remove(AttributedType attributedType) {
        if (IdentityType.class.isInstance(attributedType)) {
            if (attributedType.getId() == null) {
                throw new IdentityManagementException("No identifier provided.");
            }

            String dnSuffix = getBaseDN((Class<? extends IdentityType>) attributedType.getClass());

            if (dnSuffix == null) {
                throw new IdentityManagementException("No DN suffix found for the given type ["
                        + attributedType.getClass().getName() + "].");
            }

            getConfig().getLdapManager().searchByAttribute(dnSuffix, LDAPConstants.ENTRY_UUID, attributedType.getId(),
                    new LDAPSearchCallback<User>() {

                        @Override
                        public User processResult(SearchResult sr) {
                            String name = sr.getNameInNamespace();
                            getConfig().getLdapManager().destroySubcontext(name);
                            return null;
                        }
                    });
        } else if (Relationship.class.isInstance(attributedType)) {
            Relationship relationship = (Relationship) attributedType;

            if (Grant.class.isInstance(relationship)) {
                Grant grant = (Grant) relationship;

                LDAPRole roleEntry = lookupEntry(new LDAPRole(grant.getRole().getName(), getConfig().getRoleDNSuffix()));
                LDAPAgent agentEntry = null;

                if (Agent.class.isInstance(grant.getAssignee())) {
                    Agent agent = (Agent) grant.getAssignee();

                    if (User.class.isInstance(grant.getAssignee())) {
                        agentEntry = lookupEntry(new LDAPUser(agent.getLoginName(), getConfig().getUserDNSuffix()));
                    } else {
                        agentEntry = lookupEntry(new LDAPAgent(agent.getLoginName(), getConfig().getAgentDNSuffix()));
                    }
                }

                removeMember(roleEntry, agentEntry);
            } else if (GroupMembership.class.isInstance(relationship)) {
                GroupMembership groupMembership = (GroupMembership) relationship;

                LDAPGroup groupEntry = lookupEntry(new LDAPGroup(groupMembership.getGroup().getName(), getConfig()
                        .getGroupDNSuffix()));
                LDAPAgent agentEntry = null;

                if (Agent.class.isInstance(groupMembership.getMember())) {
                    Agent agent = (Agent) groupMembership.getMember();

                    if (User.class.isInstance(groupMembership.getMember())) {
                        agentEntry = lookupEntry(new LDAPUser(agent.getLoginName(), getConfig().getUserDNSuffix()));
                    } else {
                        agentEntry = lookupEntry(new LDAPAgent(agent.getLoginName(), getConfig().getAgentDNSuffix()));
                    }
                }

                removeMember(groupEntry, agentEntry);

                if (GroupRole.class.isInstance(groupMembership)) {
                    GroupRole groupRole = (GroupRole) groupMembership;

                    LDAPRole roleEntry = lookupEntry(new LDAPRole(groupRole.getRole().getName(), getConfig().getRoleDNSuffix()));
                    LDAPGroupRole groupRoleEntry = new LDAPGroupRole(agentEntry, groupEntry, roleEntry);

                    NamingEnumeration<SearchResult> search = getConfig().getLdapManager().search(agentEntry.getDN(),
                            groupRoleEntry.getBidingName());

                    try {
                        if (search.hasMore()) {
                            getConfig().getLdapManager().destroySubcontext(groupRoleEntry.getDN());
                        }

                    } catch (Exception e) {
                        throw new IdentityManagementException("Error removing GroupRole relationship.", e);
                    }
                }
            }

        }
    }

    private void removeMember(LDAPEntry parentEntry, LDAPEntry childEntry) {
        parentEntry.removeMember(childEntry);
        getConfig().getLdapManager().modifyAttribute(parentEntry.getDN(), parentEntry.getLDAPAttributes().get(MEMBER));
    }

    @Override
    public Agent getAgent(String loginName) {
        if (loginName != null) {
            LDAPAgent ldapAgent = lookupEntry(new LDAPAgent(loginName, getConfig().getAgentDNSuffix()));

            if (ldapAgent != null) {
                Agent agent = new SimpleAgent(ldapAgent.getLoginName());

                agent.setLoginName(ldapAgent.getLoginName());

                populateIdentityType(ldapAgent, agent);

                return agent;
            }
        }

        return null;
    }

    @Override
    public User getUser(String loginName) {
        if (loginName != null) {
            LDAPUser ldapUser = lookupEntry(new LDAPUser(loginName, getConfig().getUserDNSuffix()));

            if (ldapUser != null) {
                User user = new SimpleUser(ldapUser.getLoginName());

                user.setLoginName(ldapUser.getLoginName());
                user.setFirstName(ldapUser.getFirstName());
                user.setLastName(ldapUser.getLastName());
                user.setEmail(ldapUser.getEmail());

                populateIdentityType(ldapUser, user);

                return user;
            }
        }

        return null;
    }

    @Override
    public Group getGroup(String name) {
        if (name != null) {
            LDAPGroup ldapGroup = lookupEntry(new LDAPGroup(name, getConfig().getGroupDNSuffix()));

            if (ldapGroup != null) {
                Group group = new SimpleGroup(ldapGroup.getName(), getParentGroup(ldapGroup));

                populateIdentityType(ldapGroup, group);

                return group;
            }
        }

        return null;
    }

    @Override
    public Group getGroup(String name, Group parent) {
        Group group = getGroup(name);

        if (group.getParentGroup() == null || !group.getParentGroup().getName().equals(parent.getName())) {
            group = null;
        }

        return group;
    }

    @Override
    public Role getRole(String name) {
        if (name != null) {
            LDAPRole ldapRole = lookupEntry(new LDAPRole(name, getConfig().getRoleDNSuffix()));

            if (ldapRole != null) {
                Role role = new SimpleRole(ldapRole.getName());

                populateIdentityType(ldapRole, role);

                return role;
            }
        }

        return null;
    }

    @Override
    public <T extends IdentityType> List<T> fetchQueryResults(IdentityQuery<T> identityQuery) {
        LDAPQuery ldapQuery = new LDAPQuery(identityQuery.getParameters());

        String idAttribute = getIdAttribute(identityQuery.getIdentityType());

        StringBuffer filter = ldapQuery.createManagedAttributesFilter();

        if (filter == null) {
            filter = new StringBuffer("(&(objectClass=*))");
        }

        StringBuffer additionalFilter = new StringBuffer();

        if (IDMUtil.isAgentType(identityQuery.getIdentityType())) {
            // add to the filter only the users that have the specified roles
            if (identityQuery.getParameters().containsKey(User.HAS_ROLE)) {
                Object[] roleNames = identityQuery.getParameters().get(User.HAS_ROLE);
                LDAPEntry[] roles = new LDAPEntry[roleNames.length];

                for (int i = 0; i < roleNames.length; i++) {
                    Object name = roleNames[i];
                    roles[i] = (LDAPEntry) lookupEntry(new LDAPRole(name.toString(), getConfig().getRoleDNSuffix()));
                }

                String usersFilterMemberOf = getUsersFilterMemberOf(roles);

                if (usersFilterMemberOf.length() == 0) {
                    return Collections.emptyList();
                }

                additionalFilter.append(usersFilterMemberOf);
            }

            // add to the filter only the users member of the specified groups
            if (identityQuery.getParameters().containsKey(User.MEMBER_OF)) {
                Object[] groupNames = identityQuery.getParameters().get(User.MEMBER_OF);
                LDAPEntry[] groups = new LDAPEntry[groupNames.length];

                for (int i = 0; i < groupNames.length; i++) {
                    Object name = groupNames[i];
                    groups[i] = lookupEntry(new LDAPGroup(name.toString(), getConfig().getGroupDNSuffix()));
                }

                String usersFilterMemberOf = getUsersFilterMemberOf(groups);

                if (usersFilterMemberOf.length() == 0) {
                    return Collections.emptyList();
                }

                additionalFilter.append(usersFilterMemberOf);
            }

            // add to the filter only users with the specified group and role
            // combination
            if (identityQuery.getParameters().containsKey(IdentityType.HAS_GROUP_ROLE)) {
                Object[] groupRoles = identityQuery.getParameters().get(User.HAS_GROUP_ROLE);

                NamingEnumeration<SearchResult> search = null;

                try {
                    for (Object group : groupRoles) {
                        GroupRole groupRole = (GroupRole) group;
                        Agent agent = (Agent) groupRole.getMember();

                        String agentDNSuffix = getConfig().getAgentDNSuffix();

                        if (User.class.isInstance(agent)) {
                            agentDNSuffix = getConfig().getUserDNSuffix();
                        }

                        search = getConfig().getLdapManager().search(agentDNSuffix,
                                "(" + CN + "=" + groupRole.getGroup().getName() + ")");

                        if (search.hasMoreElements()) {
                            while (search.hasMoreElements()) {
                                SearchResult searchResult = search.next();
                                String[] nameInNamespace = searchResult.getNameInNamespace().split(",");
                                String userId = nameInNamespace[1];

                                javax.naming.directory.Attribute member = searchResult.getAttributes().get(MEMBER);

                                if (member.contains(CN + "=" + groupRole.getRole().getName() + COMMA
                                        + this.configuration.getRoleDNSuffix())) {
                                    additionalFilter.append("(").append(userId).append(")");
                                }
                            }
                        }
                    }

                    if (additionalFilter.length() == 0) {
                        return Collections.emptyList();
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
        } else if (IDMUtil.isRoleType(identityQuery.getIdentityType())) {
            // add to the filter only the roles where the specified agents are
            // member of
            if (identityQuery.getParameters().containsKey(Role.ROLE_OF)) {
                Object[] values = identityQuery.getParameters().get(Role.ROLE_OF);
                Agent[] agents = new Agent[values.length];

                for (int j = 0; j < values.length; j++) {
                    Object value = values[j];
                    agents[j] = (Agent) value;
                }

                String entryFilter = getEntryFilterForMembers(agents, this.configuration.getRoleDNSuffix());

                if (entryFilter.length() == 0) {
                    return Collections.emptyList();
                }

                additionalFilter.append(entryFilter);
            }
        } else if (IDMUtil.isGroupType(identityQuery.getIdentityType())) {
            // add to the filter only the groups where the specified agents are
            // member of
            if (identityQuery.getParameters().containsKey(Group.HAS_MEMBER)) {
                Object[] values = identityQuery.getParameters().get(Group.HAS_MEMBER);
                Agent[] agents = new Agent[values.length];

                for (int j = 0; j < values.length; j++) {
                    Object value = values[j];
                    agents[j] = (Agent) value;
                }

                String entryFilter = getEntryFilterForMembers(agents, this.configuration.getGroupDNSuffix());

                if (entryFilter.length() == 0) {
                    return Collections.emptyList();
                }

                additionalFilter.append(entryFilter);
            }

            // add to the filter only the groups with the specified parent
            if (identityQuery.getParameters().containsKey(Group.PARENT)) {
                String parentName = identityQuery.getParameters().get(Group.PARENT)[0].toString();
                LDAPGroup parentGroup = lookupEntry(new LDAPGroup(parentName, getConfig().getGroupDNSuffix()));

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

        if (idAttribute != null) {
            filter.insert(filter.length() - 1, "(" + idAttribute + "=*)");
        }

        filter.insert(filter.length() - 1, "(!(cn=custom-attributes))");
        filter.insert(filter.length() - 1, additionalFilter.toString());

        NamingEnumeration<SearchResult> answer = null;
        List<T> results = new ArrayList<T>();

        try {

            SearchControls controls = new SearchControls();
            LdapContext context = null;

            if (identityQuery.getLimit() > 0) {
                if (identityQuery.getContext() == null) {
                    context = getConfig().getLdapManager().createContext();
                    context.setRequestControls(new Control[] { new PagedResultsControl(identityQuery.getLimit(),
                            Control.CRITICAL) });
                } else {
                    if (identityQuery.getCookie() == null) {
                        return Collections.emptyList();
                    }
                    
                    context = identityQuery.getContext();
                    context.setRequestControls(new Control[] { new PagedResultsControl(identityQuery.getLimit(), identityQuery.getCookie(),
                            Control.CRITICAL) });
                }
                
                answer = context.search(getBaseDN(identityQuery.getIdentityType()), filter.toString(), controls);
            } else {
                answer = getConfig().getLdapManager().search(getBaseDN(identityQuery.getIdentityType()), filter.toString(),
                        controls);
            }

            while (answer.hasMore()) {
                SearchResult sr = (SearchResult) answer.next();
                String nameInNamespace = sr.getNameInNamespace();
                String names[] = nameInNamespace.split(LDAPConstants.COMMA);
                String uid = names[0].split(LDAPConstants.EQUAL)[1];

                T ldapEntry = null;

                if (nameInNamespace.endsWith(getConfig().getUserDNSuffix())) {
                    ldapEntry = (T) getUser(uid);
                } else if (nameInNamespace.endsWith(getConfig().getAgentDNSuffix())) {
                    ldapEntry = (T) getAgent(uid);
                } else if (nameInNamespace.endsWith(getConfig().getRoleDNSuffix())) {
                    ldapEntry = (T) getRole(uid);
                } else if (nameInNamespace.endsWith(getConfig().getGroupDNSuffix())) {
                    ldapEntry = (T) getGroup(uid);
                }

                if (identityQuery.getParameters().containsKey(IdentityType.ENABLED)) {
                    Object[] values = identityQuery.getParameters().get(IdentityType.ENABLED);

                    if (!String.valueOf(ldapEntry.isEnabled()).equals(values[0].toString())) {
                        continue;
                    }
                }

                if (identityQuery.getParameters().containsKey(IdentityType.EXPIRY_DATE)
                        || identityQuery.getParameters().containsKey(IdentityType.EXPIRY_BEFORE)
                        || identityQuery.getParameters().containsKey(IdentityType.EXPIRY_AFTER)) {

                    if (ldapEntry.getExpirationDate() == null) {
                        continue;
                    }

                    if (identityQuery.getParameters().containsKey(IdentityType.EXPIRY_DATE)) {
                        Object[] values = identityQuery.getParameters().get(IdentityType.EXPIRY_DATE);

                        long storedDateInMillis = ldapEntry.getExpirationDate().getTime();
                        long providedDateInMillis = ((Date) values[0]).getTime();

                        if (storedDateInMillis != providedDateInMillis) {
                            continue;
                        }
                    }

                    if (identityQuery.getParameters().containsKey(IdentityType.EXPIRY_BEFORE)) {
                        Object[] values = identityQuery.getParameters().get(IdentityType.EXPIRY_BEFORE);

                        long storedDateInMillis = ldapEntry.getExpirationDate().getTime();
                        long providedDateInMillis = ((Date) values[0]).getTime();

                        if (storedDateInMillis > providedDateInMillis) {
                            continue;
                        }
                    }

                    if (identityQuery.getParameters().containsKey(IdentityType.EXPIRY_AFTER)) {
                        Object[] values = identityQuery.getParameters().get(IdentityType.EXPIRY_AFTER);

                        long storedDateInMillis = ldapEntry.getExpirationDate().getTime();
                        long providedDateInMillis = ((Date) values[0]).getTime();

                        if (storedDateInMillis < providedDateInMillis) {
                            continue;
                        }
                    }
                }

                boolean match = true;

                Set<Entry<QueryParameter, Object[]>> parameters = identityQuery.getParameters(
                        IdentityType.AttributeParameter.class).entrySet();

                for (Entry<QueryParameter, Object[]> ldapQueryParameter : parameters) {
                    QueryParameter queryParameter = ldapQueryParameter.getKey();
                    Object[] values = ldapQueryParameter.getValue();

                    match = false;

                    IdentityType.AttributeParameter customParameter = (IdentityType.AttributeParameter) queryParameter;
                    Attribute<Serializable> customParameterValue = ldapEntry.getAttribute(customParameter.getName());

                    if (ldapEntry.getAttribute(customParameter.getName()) != null) {
                        int count = values.length;

                        for (Object parameterValue : values) {
                            if (customParameterValue.getValue().getClass().isArray()) {
                                Object[] customParameterValues = (Object[]) customParameterValue.getValue();

                                for (Object value : customParameterValues) {
                                    if (value.equals(parameterValue)) {
                                        count--;
                                    }
                                }
                            } else {
                                if (parameterValue.equals(customParameterValue.getValue())) {
                                    count--;
                                }
                            }
                        }

                        match = count <= 0;

                        if (!match) {
                            break;
                        }
                    }
                }

                if (match && ldapEntry != null) {
                    results.add(ldapEntry);
                }
            }

            if (context != null) {
                // Examine the paged results control response
                Control[] controls1 = context.getResponseControls();
                long total = 0;
                byte[] cookie = null;
                if (controls1 != null) {
                    for (int i = 0; i < controls1.length; i++) {
                        if (controls1[i] instanceof PagedResultsResponseControl) {
                            PagedResultsResponseControl prrc = (PagedResultsResponseControl) controls1[i];
                            total = prrc.getResultSize();
                            if (total != 0) {
                                System.out.println("***************** END-OF-PAGE " + "(total : " + total
                                        + ") *****************\n");
                            } else {
                                System.out.println("***************** END-OF-PAGE " + "(total: unknown) ***************\n");
                            }
                            cookie = prrc.getCookie();
                        }
                    }
                } else {
                    System.out.println("No controls were sent from the server");
                }

                // Re-activate paged results
                context.setRequestControls(new Control[] { new PagedResultsControl(identityQuery.getLimit(), cookie, Control.CRITICAL) });
                identityQuery.setContext(context);
                identityQuery.setCookie(cookie);
            }
        } catch (Exception e) {
            throw new IdentityManagementException("Error during query execution.", e);
        } finally {
//            if (answer != null) {
//                try {
//                    answer.close();
//                } catch (NamingException e) {
//                }
//            }
        }

        return results;
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
            LDAPUser ldapUser = lookupEntry(new LDAPUser(agent.getLoginName(), getConfig().getUserDNSuffix()));

            hasMemberFilter = hasMemberFilter + "(member=" + ldapUser.getDN() + ")";
        }

        NamingEnumeration<SearchResult> search = null;

        try {
            search = getConfig().getLdapManager().search(baseDN, hasMemberFilter.toString());

            while (search.hasMoreElements()) {
                SearchResult searchResult = search.next();
                String entryCN = searchResult.getAttributes().get(CN).get().toString();

                additionalFilter.append("(").append(CN).append(LDAPConstants.EQUAL).append(entryCN).append(")");
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

        if (additionalFilter.length() > 0) {
            additionalFilter.insert(0, "(|");
            additionalFilter.append(")");
        }

        return additionalFilter.toString();
    }

    private String getUsersFilterMemberOf(LDAPEntry[] parents) {
        StringBuffer additionalFilter = new StringBuffer();

        Map<String, Integer> userCount = new HashMap<String, Integer>();

        for (LDAPEntry ldapEntry : parents) {
            javax.naming.directory.Attribute memberAttribute = null;

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

        if (additionalFilter.length() > 0) {
            additionalFilter.insert(0, "(|");
            additionalFilter.append(")");
        }

        return additionalFilter.toString();
    }

    private String getIdAttribute(Class<? extends IdentityType> identityTypeClass) {
        String idAttribute = null;

        if (IDMUtil.isAgentType(identityTypeClass)) {
            idAttribute = UID;
        } else if (IDMUtil.isRoleType(identityTypeClass)) {
            idAttribute = CN;
        } else if (IDMUtil.isGroupType(identityTypeClass)) {
            idAttribute = CN;
        }

        return idAttribute;
    }

    @Override
    public <T extends IdentityType> int countQueryResults(IdentityQuery<T> identityQuery) {
        int limit = identityQuery.getLimit();
        int offset = identityQuery.getOffset();

        identityQuery.setLimit(0);
        identityQuery.setOffset(0);

        int resultCount = identityQuery.getResultList().size();

        identityQuery.setLimit(limit);
        identityQuery.setOffset(offset);

        return resultCount;
    }

    @Override
    public <T extends Relationship> List<T> fetchQueryResults(RelationshipQuery<T> query) {
        List<T> results = new ArrayList<T>();
        Class<T> relationshipType = query.getRelationshipType();

        if (Grant.class.equals(relationshipType)) {
            Agent agent = (Agent) query.getParameter(Grant.ASSIGNEE)[0];
            Role role = (Role) query.getParameter(Grant.ROLE)[0];

            LDAPRole roleEntry = lookupEntry(new LDAPRole(role.getName(), getConfig().getRoleDNSuffix()));
            LDAPAgent agentEntry = null;

            if (Agent.class.isInstance(agent)) {
                if (User.class.isInstance(agent)) {
                    agentEntry = lookupEntry(new LDAPUser(agent.getLoginName(), getConfig().getUserDNSuffix()));
                } else {
                    agentEntry = lookupEntry(new LDAPAgent(agent.getLoginName(), getConfig().getAgentDNSuffix()));
                }
            }

            if (roleEntry.isMember(agentEntry)) {
                results.add((T) new Grant(agent, role));
            }
        } else if (GroupMembership.class.equals(relationshipType)) {
            Agent agent = (Agent) query.getParameter(GroupMembership.MEMBER)[0];
            Group group = (Group) query.getParameter(GroupMembership.GROUP)[0];

            LDAPGroup groupEntry = lookupEntry(new LDAPGroup(group.getName(), getConfig().getGroupDNSuffix()));
            LDAPAgent agentEntry = null;

            if (Agent.class.isInstance(agent)) {
                if (User.class.isInstance(agent)) {
                    agentEntry = lookupEntry(new LDAPUser(agent.getLoginName(), getConfig().getUserDNSuffix()));
                } else {
                    agentEntry = lookupEntry(new LDAPAgent(agent.getLoginName(), getConfig().getAgentDNSuffix()));
                }
            }

            if (groupEntry.isMember(agentEntry)) {
                results.add((T) new GroupMembership(agent, group));
            }
        } else if (GroupRole.class.equals(relationshipType)) {
            Agent agent = (Agent) query.getParameter(GroupRole.MEMBER)[0];
            Group group = (Group) query.getParameter(GroupRole.GROUP)[0];
            Role role = (Role) query.getParameter(GroupRole.ROLE)[0];

            LDAPGroup groupEntry = lookupEntry(new LDAPGroup(group.getName(), getConfig().getGroupDNSuffix()));
            LDAPRole roleEntry = lookupEntry(new LDAPRole(role.getName(), getConfig().getRoleDNSuffix()));
            LDAPAgent agentEntry = null;

            if (Agent.class.isInstance(agent)) {
                if (User.class.isInstance(agent)) {
                    agentEntry = lookupEntry(new LDAPUser(agent.getLoginName(), getConfig().getUserDNSuffix()));
                } else {
                    agentEntry = lookupEntry(new LDAPAgent(agent.getLoginName(), getConfig().getAgentDNSuffix()));
                }
            }

            LDAPGroupRole groupRoleEntry = new LDAPGroupRole(agentEntry, groupEntry, roleEntry);

            NamingEnumeration<SearchResult> search = getConfig().getLdapManager().search(agentEntry.getDN(),
                    groupRoleEntry.getBidingName());

            try {
                if (search.hasMore()) {
                    groupRoleEntry.setLDAPAttributes(search.next().getAttributes());

                    if (groupRoleEntry.isMember(roleEntry)) {
                        results.add((T) new GroupRole(agent, group, role));
                    }
                }
            } catch (Exception e) {
                throw new IdentityManagementException("Error looking up GroupRole relationship.", e);
            }
        }

        return results;
    }

    @Override
    public <T extends Relationship> int countQueryResults(RelationshipQuery<T> query) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setAttribute(IdentityType identityType, Attribute<? extends Serializable> attribute) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T extends Serializable> Attribute<T> getAttribute(IdentityType identityType, String attributeName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeAttribute(IdentityType identityType, String attributeName) {
        // TODO Auto-generated method stub

    }

    @Override
    public void validateCredentials(Credentials credentials) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateCredential(Agent agent, Object credential, Date effectiveDate, Date expiryDate) {
        // TODO Auto-generated method stub

    }

    private void addIdentityType(IdentityType newIdentityType, LDAPIdentityType ldapIdentityType) {
        ldapIdentityType.setId(newIdentityType.getId());
        ldapIdentityType.setEnabled(newIdentityType.isEnabled());
        ldapIdentityType.setCreatedDate(newIdentityType.getCreatedDate());
        ldapIdentityType.setExpirationDate(newIdentityType.getExpirationDate());

        String name = ldapIdentityType.getDN();

        getConfig().getLdapManager().createSubContext(name, ldapIdentityType.getLDAPAttributes());
        getConfig().getLdapManager().rebind(getCustomAttributesDN(ldapIdentityType.getDN()),
                ldapIdentityType.getCustomAttributes());
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

    private void updateIdentityType(IdentityType updatedIdentityType, LDAPIdentityType identityTypeEntry) {
        identityTypeEntry.setEnabled(updatedIdentityType.isEnabled());
        identityTypeEntry.setExpirationDate(updatedIdentityType.getExpirationDate());

        Attributes ldapAttributes = identityTypeEntry.getLDAPAttributes();

        NamingEnumeration<? extends javax.naming.directory.Attribute> all = ldapAttributes.getAll();
        Attributes clonedAttributes = (Attributes) identityTypeEntry.getLDAPAttributes().clone();

        while (all.hasMoreElements()) {
            javax.naming.directory.Attribute attribute = (javax.naming.directory.Attribute) all.nextElement();

            if (clonedAttributes.get(attribute.getID()) != null) {
                if (!attribute.getID().equalsIgnoreCase(LDAPConstants.ENTRY_UUID)
                        && !attribute.getID().equalsIgnoreCase(LDAPConstants.CREATE_TIMESTAMP)) {
                    getConfig().getLdapManager().modifyAttribute(identityTypeEntry.getDN(), attribute);
                }
            } else {
                getConfig().getLdapManager().addAttribute(identityTypeEntry.getDN(), attribute);
            }
        }

        identityTypeEntry.getCustomAttributes().clear();

        Collection<Attribute<? extends Serializable>> updatedAttributes = updatedIdentityType.getAttributes();

        for (Attribute<? extends Serializable> attribute : updatedAttributes) {
            identityTypeEntry.getCustomAttributes().addAttribute(attribute.getName(), attribute.getValue());
        }

        getConfig().getLdapManager().rebind(getCustomAttributesDN(identityTypeEntry.getDN()),
                identityTypeEntry.getCustomAttributes());
    }

    private void populateIdentityType(LDAPIdentityType ldapIdentityType, IdentityType identityType) {
        identityType.setId(ldapIdentityType.getId());
        identityType.setEnabled(ldapIdentityType.isEnabled());
        identityType.setCreatedDate(ldapIdentityType.getCreatedDate());
        identityType.setExpirationDate(ldapIdentityType.getExpirationDate());
        identityType.setPartition(ldapIdentityType.getPartition());

        Set<Entry<String, Serializable>> entrySet = ldapIdentityType.getCustomAttributes().getAttributes().entrySet();

        for (Entry<String, Serializable> entry : entrySet) {
            if (!entry.getKey().equals(LDAPConstants.CUSTOM_ATTRIBUTE_ENABLED)
                    && !entry.getKey().equals(LDAPConstants.CUSTOM_ATTRIBUTE_EXPIRY_DATE)) {
                identityType.setAttribute(new Attribute<Serializable>(entry.getKey(), entry.getValue()));
            }
        }
    }

    private <T extends LDAPIdentityType> T lookupEntry(T identityType) {
        String filter = "(&(objectClass=*)(" + identityType.getBidingName() + "))";

        NamingEnumeration<SearchResult> search = getConfig().getLdapManager().search(identityType.getDnSuffix(), filter);

        try {
            if (search.hasMore()) {
                SearchResult sr = search.next();

                identityType.setLDAPAttributes(sr.getAttributes());
                identityType.setCustomAttributes(getCustomAttributes(identityType));

                Attributes operationalAttributes = getConfig().getLdapManager().lookupOperationalAttributes(
                        identityType.getDnSuffix(), filter);

                identityType.setId(operationalAttributes.get(LDAPConstants.ENTRY_UUID).get().toString());

                String createdTimeStamp = operationalAttributes.get(LDAPConstants.CREATE_TIMESTAMP).get().toString();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

                try {
                    identityType.setCreatedDate(sdf.parse(createdTimeStamp));
                } catch (ParseException e) {
                    throw new IdentityManagementException("Error parsing created date.", e);
                }

                // for now, the store is not supporting partitions. ldap does not pprovide a good attribute to hold such
                // information.
                // maybe in this case we should mix stores.
                identityType.setPartition(new Realm(Realm.DEFAULT_REALM));
            } else {
                identityType = null;
            }
        } catch (NamingException e) {
            throw new IdentityManagementException("Error looking up entry.", e);
        } finally {
            if (search != null) {
                try {
                    search.close();
                } catch (NamingException e) {
                }
            }
        }

        return identityType;
    }

    /**
     * <p>
     * Returns the custom attributes for the given parent DN.
     * </p>
     * 
     * @param parentDN
     * @return
     */
    private LDAPCustomAttributes getCustomAttributes(LDAPAttributedType attributedType) {
        String customDN = getCustomAttributesDN(attributedType.getDN());

        LDAPCustomAttributes customAttributes = null;

        try {
            customAttributes = getConfig().getLdapManager().lookup(customDN);
        } catch (Exception ignore) {
        }

        if (customAttributes == null) {
            getConfig().getLdapManager().bind(customDN, attributedType.getCustomAttributes());
        }

        return customAttributes;
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
            answer = getConfig().getLdapManager()
                    .search(this.configuration.getGroupDNSuffix(), matchAttrs, new String[] { CN });
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

    private void addGroup(Group newGroup) {
        LDAPGroup ldapGroup = new LDAPGroup(getConfig().getGroupDNSuffix());

        ldapGroup.setName(newGroup.getName());

        addIdentityType(newGroup, ldapGroup);

        if (newGroup.getParentGroup() != null) {
            String parentName = newGroup.getParentGroup().getName();
            LDAPGroup parentGroup = lookupEntry(new LDAPGroup(parentName, getConfig().getGroupDNSuffix()));

            if (parentGroup == null) {
                throw new RuntimeException("Parent group [" + parentName + "] does not exists.");
            }

            parentGroup.addChildGroup(ldapGroup);

            ldapGroup.setParentGroup(parentGroup);

            getConfig().getLdapManager().modifyAttribute(parentGroup.getDN(), parentGroup.getLDAPAttributes().get(MEMBER));
        }
    }

    private void addRole(Role newRole) {
        LDAPRole ldapRole = new LDAPRole(getConfig().getRoleDNSuffix());

        ldapRole.setName(newRole.getName());

        addIdentityType(newRole, ldapRole);
    }

    private void addAgent(Agent newAgent) {
        LDAPAgent ldapAgent = new LDAPAgent(getConfig().getAgentDNSuffix());

        ldapAgent.setLoginName(newAgent.getLoginName());

        addIdentityType(newAgent, ldapAgent);
    }

    private void addUser(User newUser) {
        LDAPUser ldapUser = new LDAPUser(getConfig().getUserDNSuffix());

        ldapUser.setLoginName(newUser.getLoginName());
        ldapUser.setFirstName(newUser.getFirstName());
        ldapUser.setLastName(newUser.getLastName());
        ldapUser.setFullName(ldapUser.getUserCN());
        ldapUser.setEmail(newUser.getEmail());

        addIdentityType(newUser, ldapUser);
    }

    private void updateGroup(Group updatedGroup) {
        LDAPGroup groupEntry = lookupEntry(new LDAPGroup(updatedGroup.getName(), getConfig().getGroupDNSuffix()));

        updateIdentityType(updatedGroup, groupEntry);
    }

    private void updateRole(Role updatedRole) {
        LDAPRole roleEntry = lookupEntry(new LDAPRole(updatedRole.getName(), getConfig().getRoleDNSuffix()));

        updateIdentityType(updatedRole, roleEntry);
    }

    private void updateAgent(Agent updatedAgent) {
        LDAPAgent agentEntry = lookupEntry(new LDAPAgent(updatedAgent.getLoginName(), getConfig().getAgentDNSuffix()));

        updateIdentityType(updatedAgent, agentEntry);
    }

    private void updateUser(User updatedUser) {
        LDAPUser userEntry = lookupEntry(new LDAPUser(updatedUser.getLoginName(), getConfig().getUserDNSuffix()));

        userEntry.setFirstName(updatedUser.getFirstName());
        userEntry.setLastName(updatedUser.getLastName());
        userEntry.setFullName(userEntry.getUserCN());
        userEntry.setEmail(updatedUser.getEmail());

        updateIdentityType(updatedUser, userEntry);
    }

    private String getBaseDN(Class<? extends IdentityType> identityTypeClass) {
        String baseDN = null;

        if (IDMUtil.isUserType(identityTypeClass)) {
            baseDN = getConfig().getUserDNSuffix();
        } else if (IDMUtil.isRoleType(identityTypeClass)) {
            baseDN = getConfig().getRoleDNSuffix();
        } else if (IDMUtil.isGroupType(identityTypeClass)) {
            baseDN = getConfig().getGroupDNSuffix();
        } else if (IDMUtil.isAgentType(identityTypeClass)) {
            baseDN = getConfig().getAgentDNSuffix();
        } else {
            baseDN = getConfig().getBaseDN();
        }

        return baseDN;
    }
}
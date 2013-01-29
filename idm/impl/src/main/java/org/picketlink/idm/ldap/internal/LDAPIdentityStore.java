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
import javax.naming.directory.SearchResult;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.spi.CredentialHandler;
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
import org.picketlink.idm.query.internal.DefaultIdentityQuery;
import org.picketlink.idm.query.internal.DefaultRelationshipQuery;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;

/**
 * An IdentityStore implementation backed by an LDAP directory
 * 
 * @author Shane Bryzak
 * @author Anil Saldhana
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@CredentialHandlers({ LDAPPlainTextPasswordCredentialHandler.class })
public class LDAPIdentityStore implements IdentityStore<LDAPIdentityStoreConfiguration> {

    private LDAPIdentityStoreConfiguration configuration;
    private IdentityStoreInvocationContext context;

    @Override
    public void setup(LDAPIdentityStoreConfiguration config, IdentityStoreInvocationContext context) {
        this.configuration = config;
        this.context = context;

        if (this.context.getRealm() == null) {
            this.context.setRealm(new Realm(Realm.DEFAULT_REALM));
        }
    }

    @Override
    public LDAPIdentityStoreConfiguration getConfig() {
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
                addGrantRelationship(grant);
            } else if (GroupMembership.class.isInstance(relationship)) {
                GroupMembership groupMembership = (GroupMembership) relationship;

                addGroupMembership(groupMembership);

                if (GroupRole.class.isInstance(relationship)) {
                    GroupRole groupRole = (GroupRole) relationship;
                    addGroupRoleRelationship(groupRole);
                }
            } else {
                throw createUnsupportedRelationshipType(relationship.getClass());
            }
        } else {
            throw createUnsupportedAttributedType(attributedType.getClass());
        }
    }

    @Override
    public void update(AttributedType attributedType) {
        if (IdentityType.class.isInstance(attributedType)) {
            IdentityType identityType = (IdentityType) attributedType;

            if (Agent.class.isInstance(identityType)) {
                if (User.class.isInstance(identityType)) {
                    User updatedUser = (User) identityType;
                    updateUser(updatedUser);
                } else {
                    Agent updatedAgent = (Agent) identityType;
                    updateAgent(updatedAgent);
                }
            } else if (Role.class.isInstance(identityType)) {
                Role updatedRole = (Role) identityType;
                updateRole(updatedRole);
            } else if (Group.class.isInstance(identityType)) {
                Group updatedGroup = (Group) identityType;
                updateGroup(updatedGroup);
            } else {
                throw createUnsupportedIdentityTypeException(identityType.getClass());
            }
        } else {
            throw createUnsupportedAttributedType(attributedType.getClass());
        }
    }

    @Override
    public void remove(AttributedType attributedType) {
        if (IdentityType.class.isInstance(attributedType)) {
            IdentityType identityType = (IdentityType) attributedType;

            if (identityType.getId() == null) {
                throw new IdentityManagementException("No identifier provided.");
            }

            if (Agent.class.isInstance(identityType)) {
                removeAgentRelationships((Agent) identityType);
            }

            String baseDN = getBaseDN(identityType.getClass());

            getLDAPManager().removeEntryById(baseDN, identityType.getId());
        } else if (Relationship.class.isInstance(attributedType)) {
            Relationship relationship = (Relationship) attributedType;

            if (Grant.class.isInstance(relationship)) {
                Grant grant = (Grant) relationship;

                LDAPRole roleEntry = lookupEntryByDN(new LDAPRole(grant.getRole().getName(), getConfig().getRoleDNSuffix()));
                LDAPAgent agentEntry = null;

                if (Agent.class.isInstance(grant.getAssignee())) {
                    Agent agent = (Agent) grant.getAssignee();

                    if (User.class.isInstance(grant.getAssignee())) {
                        agentEntry = lookupEntryByDN(new LDAPUser(agent.getLoginName(), getConfig().getUserDNSuffix()));
                    } else {
                        agentEntry = lookupEntryByDN(new LDAPAgent(agent.getLoginName(), getConfig().getAgentDNSuffix()));
                    }
                }

                removeMember(roleEntry, agentEntry);
            } else if (GroupMembership.class.isInstance(relationship)) {
                GroupMembership groupMembership = (GroupMembership) relationship;

                LDAPGroup groupEntry = lookupEntryByDN(new LDAPGroup(groupMembership.getGroup().getName(), getConfig()
                        .getGroupDNSuffix()));
                LDAPAgent agentEntry = null;

                if (Agent.class.isInstance(groupMembership.getMember())) {
                    Agent agent = (Agent) groupMembership.getMember();

                    if (User.class.isInstance(groupMembership.getMember())) {
                        agentEntry = lookupEntryByDN(new LDAPUser(agent.getLoginName(), getConfig().getUserDNSuffix()));
                    } else {
                        agentEntry = lookupEntryByDN(new LDAPAgent(agent.getLoginName(), getConfig().getAgentDNSuffix()));
                    }
                }

                removeMember(groupEntry, agentEntry);

                if (GroupRole.class.isInstance(groupMembership)) {
                    GroupRole groupRole = (GroupRole) groupMembership;

                    LDAPRole roleEntry = lookupEntryByDN(new LDAPRole(groupRole.getRole().getName(), getConfig()
                            .getRoleDNSuffix()));
                    LDAPGroupRole groupRoleEntry = new LDAPGroupRole(agentEntry, groupEntry, roleEntry);

                    NamingEnumeration<SearchResult> search = getLDAPManager().search(agentEntry.getDN(),
                            groupRoleEntry.getBidingName());

                    try {
                        if (search.hasMore()) {
                            getLDAPManager().destroySubcontext(groupRoleEntry.getDN());
                        }

                    } catch (Exception e) {
                        throw new IdentityManagementException("Error removing GroupRole relationship.", e);
                    }
                }
            }

        }
    }

    private void removeAgentRelationships(Agent agent) {
        DefaultRelationshipQuery<Grant> query = new DefaultRelationshipQuery<Grant>(Grant.class, this);

        query.setParameter(Grant.ASSIGNEE, agent);

        List<Grant> resultList = query.getResultList();

        for (Grant grant : resultList) {
            remove(grant);
        }

        DefaultRelationshipQuery<GroupMembership> groupQuery = new DefaultRelationshipQuery<GroupMembership>(
                GroupMembership.class, this);

        groupQuery.setParameter(GroupMembership.MEMBER, agent);

        List<GroupMembership> resultGroups = groupQuery.getResultList();

        for (GroupMembership groups : resultGroups) {
            remove(groups);
        }

        DefaultRelationshipQuery<GroupRole> groupRoleQuery = new DefaultRelationshipQuery<GroupRole>(GroupRole.class, this);

        groupRoleQuery.setParameter(GroupRole.MEMBER, agent);

        List<GroupRole> resultGroupRoless = groupRoleQuery.getResultList();

        for (GroupRole groupRoles : resultGroupRoless) {
            remove(groupRoles);
        }
    }

    private void removeMember(LDAPEntry parentEntry, LDAPEntry childEntry) {
        parentEntry.removeMember(childEntry);
        getLDAPManager().modifyAttribute(parentEntry.getDN(), parentEntry.getLDAPAttributes().get(MEMBER));
    }

    @Override
    public Agent getAgent(String loginName) {
        Agent agent = null;

        if (loginName != null) {
            LDAPAgent ldapAgent = lookupEntryByDN(new LDAPAgent(loginName, getConfig().getAgentDNSuffix()));

            if (ldapAgent != null) {
                agent = new SimpleAgent(ldapAgent.getLoginName());

                agent.setLoginName(ldapAgent.getLoginName());

                populateIdentityType(ldapAgent, agent);
            }
        }

        if (agent == null) {
            agent = getUser(loginName);
        }

        return agent;
    }

    @Override
    public User getUser(String loginName) {
        if (loginName != null) {
            LDAPUser ldapUser = lookupEntryByDN(new LDAPUser(loginName, getConfig().getUserDNSuffix()));

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
            LDAPGroup ldapGroup = lookupEntryByDN(new LDAPGroup(name, getConfig().getGroupDNSuffix()));

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
            LDAPRole ldapRole = lookupEntryByDN(new LDAPRole(name, getConfig().getRoleDNSuffix()));

            if (ldapRole != null) {
                Role role = new SimpleRole(ldapRole.getName());

                populateIdentityType(ldapRole, role);

                return role;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IdentityType> List<T> fetchQueryResults(IdentityQuery<T> identityQuery) {
        LDAPQuery ldapQuery = new LDAPQuery(identityQuery.getParameters());

        StringBuffer filter = ldapQuery.createManagedAttributesFilter();

        if (filter == null) {
            filter = new StringBuffer("(&(objectClass=*))");
        }

        StringBuffer relationshipFilter = new StringBuffer();

        if (IDMUtil.isAgentType(identityQuery.getIdentityType())) {
            // add to the filter only the users that have the specified roles
            if (identityQuery.getParameters().containsKey(User.HAS_ROLE)) {
                String roleOfFilter = createHasRoleFilter(identityQuery);

                if (roleOfFilter.isEmpty()) {
                    return Collections.emptyList();
                }

                relationshipFilter.append(roleOfFilter);
            }

            // add to the filter only the users member of the specified groups
            if (identityQuery.getParameters().containsKey(User.MEMBER_OF)) {
                String memberOfFilter = createMemberOfFilter(identityQuery);

                if (memberOfFilter.isEmpty()) {
                    return Collections.emptyList();
                }

                relationshipFilter.append(memberOfFilter);
            }

            // add to the filter only users with the specified group and role
            // combination
            if (identityQuery.getParameters().containsKey(IdentityType.HAS_GROUP_ROLE)) {
                String hasGroupRoleFilter = createGroupRoleFilter(identityQuery);

                if (hasGroupRoleFilter.isEmpty()) {
                    return Collections.emptyList();
                }

                relationshipFilter.append(hasGroupRoleFilter);
            }
        } else if (IDMUtil.isRoleType(identityQuery.getIdentityType())) {
            // add to the filter only the roles where the specified agents are
            // member of
            if (identityQuery.getParameters().containsKey(Role.ROLE_OF)) {
                String rolesOfFilter = createRoleOfFilter(identityQuery);

                if (rolesOfFilter.isEmpty()) {
                    return Collections.emptyList();
                }

                relationshipFilter.append(rolesOfFilter);
            }
        } else if (IDMUtil.isGroupType(identityQuery.getIdentityType())) {
            // add to the filter only the groups where the specified agents are
            // member of
            if (identityQuery.getParameters().containsKey(Group.HAS_MEMBER)) {
                String hasMemberFilter = createHasMemberFilter(identityQuery);

                if (hasMemberFilter.isEmpty()) {
                    return Collections.emptyList();
                }

                relationshipFilter.append(hasMemberFilter);
            }

            // add to the filter only the groups with the specified parent
            if (identityQuery.getParameters().containsKey(Group.PARENT)) {
                String parentGroupFilter = createParentGroupFilter(identityQuery);

                if (parentGroupFilter.isEmpty()) {
                    return Collections.emptyList();
                }

                relationshipFilter.append(parentGroupFilter);
            }
        }

        String idAttribute = getIdAttribute(identityQuery.getIdentityType());

        if (idAttribute != null) {
            filter.insert(filter.length() - 1, "(" + idAttribute + "=*)");
        }

        filter.insert(filter.length() - 1, "(!(cn=custom-attributes))");
        filter.insert(filter.length() - 1, relationshipFilter.toString());

        NamingEnumeration<SearchResult> answer = null;
        List<T> results = new ArrayList<T>();

        try {

            answer = getLDAPManager().search(getBaseDN(identityQuery.getIdentityType()), filter.toString());

            while (answer.hasMore()) {
                SearchResult sr = (SearchResult) answer.next();
                String nameInNamespace = sr.getNameInNamespace();
                String[] names = nameInNamespace.split(LDAPConstants.COMMA);
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
        } catch (Exception e) {
            throw new IdentityManagementException("Error during query execution.", e);
        } finally {
            if (answer != null) {
                try {
                    answer.close();
                } catch (NamingException e) {
                }
            }
        }

        return results;
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

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Relationship> List<T> fetchQueryResults(RelationshipQuery<T> query) {
        List<T> results = new ArrayList<T>();
        Class<T> relationshipType = query.getRelationshipType();

        if (Grant.class.equals(relationshipType)) {
            Agent agent = null;

            if (query.getParameter(Grant.ASSIGNEE) != null) {
                agent = (Agent) query.getParameter(Grant.ASSIGNEE)[0];
            }

            Role role = null;

            if (query.getParameter(Grant.ROLE) != null) {
                role = (Role) query.getParameter(Grant.ROLE)[0];
            }

            LDAPAgent agentEntry = null;

            if (Agent.class.isInstance(agent)) {
                if (User.class.isInstance(agent)) {
                    agentEntry = lookupEntryByDN(new LDAPUser(agent.getLoginName(), getConfig().getUserDNSuffix()));
                } else {
                    agentEntry = lookupEntryByDN(new LDAPAgent(agent.getLoginName(), getConfig().getAgentDNSuffix()));
                }
            }

            if (agent != null && role != null) {
                LDAPRole roleEntry = lookupEntryByDN(new LDAPRole(role.getName(), getConfig().getRoleDNSuffix()));

                if (roleEntry.isMember(agentEntry)) {
                    results.add((T) new Grant(agent, role));
                }
            } else if (agent != null) {
                IdentityQuery<Role> rolesOf = new DefaultIdentityQuery<Role>(Role.class, this);

                rolesOf.setParameter(Role.ROLE_OF, agent);

                List<Role> result = rolesOf.getResultList();

                for (Role grantedRole : result) {
                    results.add((T) new Grant(agent, grantedRole));
                }
            } else if (role != null) {
                IdentityQuery<User> rolesOf = new DefaultIdentityQuery<User>(User.class, this);

                rolesOf.setParameter(Role.HAS_ROLE, agent);

                List<User> result = rolesOf.getResultList();

                for (User user : result) {
                    results.add((T) new Grant(user, role));
                }
            }
        } else if (GroupMembership.class.equals(relationshipType)) {
            Agent agent = null;

            if (query.getParameter(GroupMembership.MEMBER) != null) {
                agent = (Agent) query.getParameter(GroupMembership.MEMBER)[0];
            }

            Group group = null;

            if (query.getParameter(GroupMembership.GROUP) != null) {
                group = (Group) query.getParameter(GroupMembership.GROUP)[0];
            }

            if (agent != null && group != null) {
                LDAPGroup groupEntry = lookupEntryByDN(new LDAPGroup(group.getName(), getConfig().getGroupDNSuffix()));
                LDAPAgent agentEntry = null;

                if (Agent.class.isInstance(agent)) {
                    if (User.class.isInstance(agent)) {
                        agentEntry = lookupEntryByDN(new LDAPUser(agent.getLoginName(), getConfig().getUserDNSuffix()));
                    } else {
                        agentEntry = lookupEntryByDN(new LDAPAgent(agent.getLoginName(), getConfig().getAgentDNSuffix()));
                    }
                }

                if (agentEntry != null && groupEntry.isMember(agentEntry)) {
                    results.add((T) new GroupMembership(agent, group));
                }
            } else if (agent != null) {
                IdentityQuery<Group> groupsOf = new DefaultIdentityQuery<Group>(Group.class, this);

                groupsOf.setParameter(Group.HAS_MEMBER, agent);

                List<Group> result = groupsOf.getResultList();

                for (Group grantedRole : result) {
                    results.add((T) new GroupMembership(agent, grantedRole));
                }
            } else if (group != null) {
                IdentityQuery<User> groupsOf = new DefaultIdentityQuery<User>(User.class, this);

                groupsOf.setParameter(User.MEMBER_OF, agent);

                List<User> result = groupsOf.getResultList();

                for (User user : result) {
                    results.add((T) new GroupMembership(user, group));
                }
            }
        } else if (GroupRole.class.equals(relationshipType)) {
            Agent agent = null;

            if (query.getParameter(GroupRole.MEMBER) != null) {
                agent = (Agent) query.getParameter(GroupRole.MEMBER)[0];
            }

            Role role = null;

            if (query.getParameter(GroupRole.ROLE) != null) {
                role = (Role) query.getParameter(GroupRole.ROLE)[0];
            }

            Group group = null;

            if (query.getParameter(GroupRole.ROLE) != null) {
                group = (Group) query.getParameter(GroupRole.GROUP)[0];
            }

            if (agent != null && group != null && role != null) {
                LDAPGroup groupEntry = lookupEntryByDN(new LDAPGroup(group.getName(), getConfig().getGroupDNSuffix()));
                LDAPRole roleEntry = lookupEntryByDN(new LDAPRole(role.getName(), getConfig().getRoleDNSuffix()));
                LDAPAgent agentEntry = null;

                if (Agent.class.isInstance(agent)) {
                    if (User.class.isInstance(agent)) {
                        agentEntry = lookupEntryByDN(new LDAPUser(agent.getLoginName(), getConfig().getUserDNSuffix()));
                    } else {
                        agentEntry = lookupEntryByDN(new LDAPAgent(agent.getLoginName(), getConfig().getAgentDNSuffix()));
                    }
                }
                
                if (agentEntry != null && groupEntry != null && roleEntry != null) {
                    LDAPGroupRole groupRoleEntry = new LDAPGroupRole(agentEntry, groupEntry, roleEntry);

                    NamingEnumeration<SearchResult> search = getLDAPManager().search(agentEntry.getDN(),
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
                    } finally {
                        try {
                            search.close();
                        } catch (NamingException e) {
                            
                        }
                    }
                }
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

    private void addIdentityType(IdentityType newIdentityType, LDAPIdentityType ldapIdentityType) {
        ldapIdentityType.setId(newIdentityType.getId());
        ldapIdentityType.setEnabled(newIdentityType.isEnabled());
        ldapIdentityType.setCreatedDate(newIdentityType.getCreatedDate());
        ldapIdentityType.setExpirationDate(newIdentityType.getExpirationDate());

        getLDAPManager().createSubContext(ldapIdentityType.getDN(), ldapIdentityType.getLDAPAttributes());
        getLDAPManager().rebind(getCustomAttributesDN(ldapIdentityType.getDN()), ldapIdentityType.getCustomAttributes());
    }

    private LDAPOperationManager getLDAPManager() {
        return getConfig().getLdapManager();
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
                    getLDAPManager().modifyAttribute(identityTypeEntry.getDN(), attribute);
                }
            } else {
                getLDAPManager().addAttribute(identityTypeEntry.getDN(), attribute);
            }
        }

        identityTypeEntry.getCustomAttributes().clear();

        Collection<Attribute<? extends Serializable>> updatedAttributes = updatedIdentityType.getAttributes();

        for (Attribute<? extends Serializable> attribute : updatedAttributes) {
            identityTypeEntry.getCustomAttributes().addAttribute(attribute.getName(), attribute.getValue());
        }

        getLDAPManager().rebind(getCustomAttributesDN(identityTypeEntry.getDN()), identityTypeEntry.getCustomAttributes());
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

    protected <T extends LDAPIdentityType> T lookupEntryById(Class<T> type, String id) {
        T identityType = null;

        NamingEnumeration<SearchResult> search = getLDAPManager().lookupById(getBaseDN(type), id);

        try {
            if (search.hasMore()) {
                SearchResult sr = search.next();

                identityType = type.getConstructor(String.class).newInstance(getBaseDN(type));

                populateLDAPEntry(identityType, sr);
            }

            if (search.hasMore()) {
                throw new IdentityManagementException("Ambiguous entry found with the given id [" + id + "]");
            }
        } catch (NamingException e) {
            throw new IdentityManagementException("Error looking up entry.", e);
        } catch (Exception e) {
            throw new IdentityManagementException("Error creating instance for type [" + type.getClass().getName() + "].", e);
        } finally {
            if (search != null) {
                try {
                    search.close();
                } catch (NamingException e) {
                }
            }
        }

        if (identityType == null) {
            throw new IdentityManagementException("No entry found for the given type [" + type.getClass().getName()
                    + "] and id [" + id + "]");
        }

        return identityType;
    }

    private <T extends LDAPIdentityType> T lookupEntryByDN(T identityType) {
        String filter = "(&(objectClass=*)(" + identityType.getBidingName() + "))";

        NamingEnumeration<SearchResult> search = getLDAPManager().search(identityType.getDnSuffix(), filter);

        try {
            if (search.hasMore()) {
                SearchResult sr = search.next();
                populateLDAPEntry(identityType, sr);
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

    private <T extends LDAPIdentityType> void populateLDAPEntry(T identityType, SearchResult sr) throws NamingException {
        identityType.setLDAPAttributes(sr.getAttributes());
        identityType.setCustomAttributes(getCustomAttributes(identityType));

        Attributes operationalAttributes = getLDAPManager().lookupOperationalAttributes(identityType.getDnSuffix(),
                identityType.getBidingName());

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

        identityType.setCustomAttributes(getCustomAttributes(identityType));
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
            customAttributes = getLDAPManager().lookup(customDN);
        } catch (Exception ignore) {
        }

        if (customAttributes == null) {
            getLDAPManager().bind(customDN, attributedType.getCustomAttributes());
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
            answer = getLDAPManager().search(this.configuration.getGroupDNSuffix(), matchAttrs, new String[] { CN });
            
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
            LDAPGroup parentGroup = lookupEntryByDN(new LDAPGroup(parentName, getConfig().getGroupDNSuffix()));

            if (parentGroup == null) {
                throw new RuntimeException("Parent group [" + parentName + "] does not exists.");
            }

            parentGroup.addChildGroup(ldapGroup);

            ldapGroup.setParentGroup(parentGroup);

            getLDAPManager().modifyAttribute(parentGroup.getDN(), parentGroup.getLDAPAttributes().get(MEMBER));
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
        LDAPGroup groupEntry = (LDAPGroup) lookupEntry(updatedGroup);

        updateIdentityType(updatedGroup, groupEntry);
    }

    private void updateRole(Role updatedRole) {
        LDAPRole roleEntry = (LDAPRole) lookupEntry(updatedRole);

        updateIdentityType(updatedRole, roleEntry);
    }

    private void updateAgent(Agent updatedAgent) {
        LDAPAgent agentEntry = (LDAPAgent) lookupEntry(updatedAgent);

        updateIdentityType(updatedAgent, agentEntry);
    }

    private void updateUser(User updatedUser) {
        LDAPUser userEntry = (LDAPUser) lookupEntry(updatedUser);

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

    private void addGroupRoleRelationship(GroupRole groupRole) {
        Group group = groupRole.getGroup();

        if (group == null) {
            throw new IdentityManagementException("You must specify a group for this relationship type.");
        }

        Role role = groupRole.getRole();

        if (role == null) {
            throw new IdentityManagementException("You must specify a role for this relationship type.");
        }

        IdentityType member = groupRole.getMember();

        if (Agent.class.isInstance(member)) {
            Agent agent = (Agent) member;

            if (agent == null) {
                throw new IdentityManagementException("You must assign a agent for this relationship type.");
            }

            LDAPAgent agentEntry = (LDAPAgent) lookupEntry(agent);
            LDAPGroup groupEntry = (LDAPGroup) lookupEntry(group);
            LDAPRole roleEntry = (LDAPRole) lookupEntry(role);

            LDAPGroupRole groupRoleEntry = new LDAPGroupRole(agentEntry, groupEntry, roleEntry);

            NamingEnumeration<SearchResult> search = getLDAPManager()
                    .search(agentEntry.getDN(), groupRoleEntry.getBidingName());

            try {
                // if the grouprole entry does not exists create it as a child of the agent entry.
                if (!search.hasMore()) {
                    getLDAPManager().createSubContext(groupRoleEntry.getDN(), groupRoleEntry.getLDAPAttributes());
                }
            } catch (Exception e) {
                throw new IdentityManagementException("Error creating GroupRole relationship.", e);
            } finally {
                if (search != null) {
                    try {
                        search.close();
                    } catch (NamingException e) {
                    }
                }
            }

            addMember(groupRoleEntry, roleEntry);
            
            addGrantRelationship(new Grant(agent, role));
            addGroupMembership(new GroupMembership(agent, group));
        } else {
            throw new IdentityManagementException("You must assign a valid Agent instance for this relationship type.");
        }
    }

    private void addGroupMembership(GroupMembership groupMembership) {
        Group group = groupMembership.getGroup();

        if (group == null) {
            throw new IdentityManagementException("You must specify a group for this relationship type.");
        }

        LDAPGroup groupEntry = (LDAPGroup) lookupEntry(group);

        IdentityType member = groupMembership.getMember();

        if (Agent.class.isInstance(member)) {
            LDAPAgent agentEntry = (LDAPAgent) lookupEntry(member);
            addMember(groupEntry, agentEntry);
        } else {
            throw new IdentityManagementException("Only Agent types are supported for this relationship type.");
        }
    }

    private void addGrantRelationship(Grant grant) {
        Role role = grant.getRole();

        if (role == null) {
            throw new IdentityManagementException("You must assign a role for this relationship type.");
        }

        LDAPRole roleEntry = (LDAPRole) lookupEntry(role);

        if (Agent.class.isInstance(grant.getAssignee())) {
            Agent agent = (Agent) grant.getAssignee();

            if (agent == null) {
                throw new IdentityManagementException("You must assign a agent for this relationship type.");
            }

            LDAPAgent agentEntry = (LDAPAgent) lookupEntry(agent);

            addMember(roleEntry, agentEntry);
        } else {
            throw new IdentityManagementException("You must assign a valid Agent instance for this relationship type.");
        }
    }

    private void addMember(LDAPEntry parentEntry, LDAPEntry childEntry) {
        parentEntry.addMember(childEntry);
        getLDAPManager().modifyAttribute(parentEntry.getDN(), parentEntry.getLDAPAttributes().get(MEMBER));
    }

    @SuppressWarnings("unchecked")
    private <T extends IdentityType> T lookupEntry(T identityType) {
        T identityTypeEntry = null;

        if (Agent.class.isInstance(identityType)) {
            Agent agent = (Agent) identityType;

            if (User.class.isInstance(agent)) {
                identityTypeEntry = (T) lookupEntryById(LDAPUser.class, agent.getId());
            } else {
                identityTypeEntry = (T) lookupEntryById(LDAPAgent.class, agent.getId());
            }
        } else if (Role.class.isInstance(identityType)) {
            identityTypeEntry = (T) lookupEntryById(LDAPRole.class, identityType.getId());
        } else if (Group.class.isInstance(identityType)) {
            identityTypeEntry = (T) lookupEntryById(LDAPGroup.class, identityType.getId());
        } else {
            throw new IdentityManagementException("Unsupported type [" + identityType.getClass().getName() + "].");
        }

        return identityTypeEntry;
    }

    private String createParentGroupFilter(IdentityQuery<?> identityQuery) {
        String parentName = identityQuery.getParameters().get(Group.PARENT)[0].toString();
        LDAPGroup parentGroup = lookupEntryByDN(new LDAPGroup(parentName, getConfig().getGroupDNSuffix()));

        NamingEnumeration<?> members = null;

        StringBuffer additionalFilter = new StringBuffer();

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

        return additionalFilter.toString();
    }

    private String createHasMemberFilter(IdentityQuery<?> identityQuery) {
        Object[] values = identityQuery.getParameters().get(Group.HAS_MEMBER);
        Agent[] agents = new Agent[values.length];

        for (int j = 0; j < values.length; j++) {
            Object value = values[j];
            agents[j] = (Agent) value;
        }

        return createMembersFilter(agents, this.configuration.getGroupDNSuffix());
    }

    private String createRoleOfFilter(IdentityQuery<?> identityQuery) {
        Object[] values = identityQuery.getParameters().get(Role.ROLE_OF);
        Agent[] agents = new Agent[values.length];

        for (int j = 0; j < values.length; j++) {
            Object value = values[j];
            agents[j] = (Agent) value;
        }

        return createMembersFilter(agents, this.configuration.getRoleDNSuffix());
    }

    private String createGroupRoleFilter(IdentityQuery<?> identityQuery) {
        Object[] groupRoles = identityQuery.getParameters().get(User.HAS_GROUP_ROLE);
        StringBuffer additionalFilter = new StringBuffer();

        NamingEnumeration<SearchResult> search = null;

        try {
            for (Object group : groupRoles) {
                GroupRole groupRole = (GroupRole) group;
                Agent agent = (Agent) groupRole.getMember();

                String agentDNSuffix = getConfig().getAgentDNSuffix();

                if (User.class.isInstance(agent)) {
                    agentDNSuffix = getConfig().getUserDNSuffix();
                }

                search = getLDAPManager().search(agentDNSuffix, "(" + CN + "=" + groupRole.getGroup().getName() + ")");

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

    private String createMemberOfFilter(IdentityQuery<?> identityQuery) {
        Object[] groupNames = identityQuery.getParameters().get(User.MEMBER_OF);
        LDAPEntry[] groups = new LDAPEntry[groupNames.length];

        for (int i = 0; i < groupNames.length; i++) {
            Object name = groupNames[i];
            groups[i] = lookupEntryByDN(new LDAPGroup(name.toString(), getConfig().getGroupDNSuffix()));
        }

        return createParentMembersFilter(groups);
    }

    private String createHasRoleFilter(IdentityQuery<?> identityQuery) {
        Object[] roleNames = identityQuery.getParameters().get(User.HAS_ROLE);
        LDAPEntry[] roles = new LDAPEntry[roleNames.length];

        for (int i = 0; i < roleNames.length; i++) {
            Object name = roleNames[i];
            roles[i] = (LDAPEntry) lookupEntryByDN(new LDAPRole(name.toString(), getConfig().getRoleDNSuffix()));
        }

        return createParentMembersFilter(roles);
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
    private String createMembersFilter(Agent[] members, String baseDN) {
        String hasMemberFilter = "";

        for (Agent agent : members) {
            LDAPAgent ldapAgent = null;

            if (User.class.isInstance(agent)) {
                ldapAgent = lookupEntryByDN(new LDAPUser(agent.getLoginName(), getConfig().getUserDNSuffix()));
            } else {
                ldapAgent = lookupEntryByDN(new LDAPAgent(agent.getLoginName(), getConfig().getAgentDNSuffix()));
            }

            hasMemberFilter = hasMemberFilter + "(member=" + ldapAgent.getDN() + ")";
        }

        NamingEnumeration<SearchResult> search = null;
        
        StringBuffer additionalFilter = new StringBuffer();
        
        try {
            search = getLDAPManager().search(baseDN, hasMemberFilter.toString());

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

    private String createParentMembersFilter(LDAPEntry[] parents) {
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

    private IdentityManagementException createUnsupportedIdentityTypeException(Class<? extends IdentityType> identityTypeClass) {
        return new IdentityManagementException("Unsupported IdentityType [" + identityTypeClass.getName() + "].");
    }

    private IdentityManagementException createUnsupportedAttributedType(Class<? extends AttributedType> type) {
        return new IdentityManagementException("Unsupported AttributedType [" + type.getName() + "].");
    }

    private IdentityManagementException createUnsupportedRelationshipType(Class<? extends Relationship> type) {
        return new IdentityManagementException("Unsupported Relationship type [" + type.getName() + "].");
    }
}
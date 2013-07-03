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

import java.io.Serializable;
import org.picketlink.idm.config.LDAPIdentityStoreConfiguration;
import org.picketlink.idm.credential.spi.annotations.CredentialHandlers;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;

/**
 * An IdentityStore implementation backed by an LDAP directory
 *
 * @author Shane Bryzak
 * @author Anil Saldhana
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@CredentialHandlers({ LDAPPlainTextPasswordCredentialHandler.class })
public class LDAPIdentityStore implements IdentityStore<LDAPIdentityStoreConfiguration> {

    @Override
    public void setup(org.picketlink.idm.config.LDAPIdentityStoreConfiguration config) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public org.picketlink.idm.config.LDAPIdentityStoreConfiguration getConfig() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void add(org.picketlink.idm.spi.IdentityContext context, org.picketlink.idm.model.AttributedType value) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void update(org.picketlink.idm.spi.IdentityContext context, org.picketlink.idm.model.AttributedType value) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void remove(org.picketlink.idm.spi.IdentityContext context, org.picketlink.idm.model.AttributedType value) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public org.picketlink.idm.model.sample.Agent getAgent(org.picketlink.idm.spi.IdentityContext context, String loginName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public org.picketlink.idm.model.sample.User getUser(org.picketlink.idm.spi.IdentityContext context, String loginName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public org.picketlink.idm.model.sample.Group getGroup(org.picketlink.idm.spi.IdentityContext context, String groupPath) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public org.picketlink.idm.model.sample.Group getGroup(org.picketlink.idm.spi.IdentityContext context, String name, org.picketlink.idm.model.sample.Group parent) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public org.picketlink.idm.model.sample.Role getRole(org.picketlink.idm.spi.IdentityContext context, String name) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <V extends org.picketlink.idm.model.IdentityType> java.util.List<V> fetchQueryResults(org.picketlink.idm.spi.IdentityContext context, org.picketlink.idm.query.IdentityQuery<V> identityQuery) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <V extends org.picketlink.idm.model.IdentityType> int countQueryResults(org.picketlink.idm.spi.IdentityContext context, org.picketlink.idm.query.IdentityQuery<V> identityQuery) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <V extends org.picketlink.idm.model.Relationship> java.util.List<V> fetchQueryResults(org.picketlink.idm.spi.IdentityContext context, org.picketlink.idm.query.RelationshipQuery<V> query) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <V extends org.picketlink.idm.model.Relationship> int countQueryResults(org.picketlink.idm.spi.IdentityContext context, org.picketlink.idm.query.RelationshipQuery<V> query) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void validateCredentials(org.picketlink.idm.spi.IdentityContext context, org.picketlink.idm.credential.Credentials credentials) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void updateCredential(org.picketlink.idm.spi.IdentityContext context, org.picketlink.idm.model.sample.Agent agent, Object credential, java.util.Date effectiveDate, java.util.Date expiryDate) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
//    private LDAPIdentityStoreConfiguration configuration;
//    private LDAPOperationManager operationManager;
//
//    @Override
//    public void setup(LDAPIdentityStoreConfiguration config) {
//        this.configuration = config;
//
//        try {
//            this.operationManager = new LDAPOperationManager(this.configuration);
//        } catch (NamingException e) {
//            throw MESSAGES.ldapCouldNotCreateContext(e);
//        }
//
//        try {
//            this.operationManager.search(this.configuration.getUserDNSuffix(), "(objectClass=initialCheck)");
//        } catch (NamingException e) {
//            throw MESSAGES.ldapCouldNotFindUsersBaseDN(this.configuration.getUserDNSuffix(), e);
//        }
//
//        try {
//            this.operationManager.search(this.configuration.getAgentDNSuffix(), "(objectClass=initialCheck)");
//        } catch (NamingException e) {
//            throw MESSAGES.ldapCouldNotFindAgentsBaseDN(this.configuration.getAgentDNSuffix(), e);
//        }
//
//        try {
//            this.operationManager.search(this.configuration.getRoleDNSuffix(), "(objectClass=initialCheck)");
//        } catch (NamingException e) {
//            throw MESSAGES.ldapCouldNotFindRolesBaseDN(this.configuration.getRoleDNSuffix(), e);
//        }
//
//        try {
//            this.operationManager.search(this.configuration.getGroupDNSuffix(), "(objectClass=initialCheck)");
//        } catch (NamingException e) {
//            throw MESSAGES.ldapCouldNotFindGroupsBaseDN(this.configuration.getGroupDNSuffix(), e);
//        }
//    }
//
//    @Override
//    public LDAPIdentityStoreConfiguration getConfig() {
//        return this.configuration;
//    }
//
//    @Override
//    public void add(SecurityContext context, AttributedType attributedType) {
//        if (IdentityType.class.isInstance(attributedType)) {
//            IdentityType identityType = (IdentityType) attributedType;
//
//            identityType.setPartition(context.get());
//
//            if (Agent.class.isInstance(attributedType)) {
//                Agent newAgent = (Agent) attributedType;
//
//                if (User.class.isInstance(newAgent)) {
//                    User newUser = (User) attributedType;
//                    addUser(context, newUser);
//                } else {
//                    addAgent(context, newAgent);
//                }
//            } else if (Role.class.isInstance(attributedType)) {
//                Role newRole = (Role) attributedType;
//                addRole(context, newRole);
//            } else if (Group.class.isInstance(attributedType)) {
//                Group newGroup = (Group) attributedType;
//                addGroup(context, newGroup);
//            } else {
//                throw MESSAGES.identityTypeUnsupportedType(identityType.getClass());
//            }
//        } else if (Relationship.class.isInstance(attributedType)) {
//            Relationship relationship = (Relationship) attributedType;
//
//            if (GroupRole.class.isInstance(relationship)) {
//                GroupRole groupRole = (GroupRole) relationship;
//                addGroupRoleRelationship(context, groupRole);
//            } else if (Grant.class.isInstance(relationship)) {
//                Grant grant = (Grant) relationship;
//                addGrantRelationship(context, grant);
//            } else if (GroupMembership.class.isInstance(relationship)) {
//                GroupMembership groupMembership = (GroupMembership) relationship;
//                addGroupMembership(context, groupMembership);
//            } else {
//                throw MESSAGES.storeConfigUnsupportedRelationshipType(relationship.getClass());
//            }
//        } else {
//            throw MESSAGES.attributedTypeUnsupportedType(attributedType.getClass());
//        }
//    }
//
//    @Override
//    public void update(SecurityContext context, AttributedType attributedType) {
//        if (IdentityType.class.isInstance(attributedType)) {
//            IdentityType identityType = (IdentityType) attributedType;
//
//            if (Agent.class.isInstance(identityType)) {
//                if (User.class.isInstance(identityType)) {
//                    User updatedUser = (User) identityType;
//                    updateUser(context, updatedUser);
//                } else {
//                    Agent updatedAgent = (Agent) identityType;
//                    updateAgent(context, updatedAgent);
//                }
//            } else if (Role.class.isInstance(identityType)) {
//                Role updatedRole = (Role) identityType;
//                updateRole(context, updatedRole);
//            } else if (Group.class.isInstance(identityType)) {
//                Group updatedGroup = (Group) identityType;
//                updateGroup(context, updatedGroup);
//            } else {
//                throw MESSAGES.identityTypeUnsupportedType(identityType.getClass());
//            }
//        } else {
//            throw MESSAGES.attributedTypeUnsupportedType(attributedType.getClass());
//        }
//    }
//
//    @Override
//    public void remove(SecurityContext context, AttributedType attributedType) {
//        if (IdentityType.class.isInstance(attributedType)) {
//            IdentityType identityType = (IdentityType) attributedType;
//            LDAPEntry ldapEntry = (LDAPEntry) lookupEntryById(context, identityType);
//
//            String baseDN = ldapEntry.getDnSuffix();
//
//            if (Group.class.isInstance(identityType)) {
//                LDAPGroup groupEntry = (LDAPGroup) ldapEntry;
//                Group parentGroup = getParentGroup(groupEntry, false);
//
//                if (parentGroup != null) {
//                    LDAPGroup parentGroupEntry = (LDAPGroup) lookupEntryById(context, parentGroup);
//                    removeMember(parentGroupEntry, groupEntry);
//                }
//            }
//
//            RelationshipQuery<Relationship> query = context.getIdentityManager().createRelationshipQuery(Relationship.class);
//
//            query.setParameter(Relationship.IDENTITY, identityType);
//
//            List<Relationship> relationships = query.getResultList();
//
//            for (Relationship relationship : relationships) {
//                context.getIdentityManager().remove(relationship);
//            }
//
//            getLDAPManager().removeEntryById(baseDN, identityType.getId());
//        } else if (Relationship.class.isInstance(attributedType)) {
//            Relationship relationship = (Relationship) attributedType;
//
//            if (GroupRole.class.isInstance(relationship)) {
//                GroupRole groupRole = (GroupRole) relationship;
//
//                removeGroupRoleRelationship(context, groupRole);
//            } else if (Grant.class.isInstance(relationship)) {
//                removeGrantRelationship(context, (Grant) relationship);
//            } else if (GroupMembership.class.isInstance(relationship)) {
//                GroupMembership groupMembership = (GroupMembership) relationship;
//
//                removeGroupMembership(context, groupMembership);
//            } else {
//                throw MESSAGES.storeConfigUnsupportedRelationshipType(relationship.getClass());
//            }
//        }
//    }
//
//    @Override
//    public Agent getAgent(SecurityContext context, String loginName) {
//        if (Realm.class.isInstance(context.get())) {
//            Agent agent = null;
//
//            if (loginName != null) {
//                LDAPAgent ldapAgent = lookupAgent(loginName);
//
//                if (ldapAgent == null) {
//                    agent = getUser(context, loginName);
//                } else {
//                    agent = new Agent(ldapAgent.getLoginName());
//
//                    agent.setLoginName(ldapAgent.getLoginName());
//
//                    populateIdentityType(ldapAgent, agent);
//                }
//            }
//
//            return agent;
//        } else {
//            // FIXME throw proper exception
//            throw new RuntimeException();
//        }
//
//    }
//
//    @Override
//    public User getUser(SecurityContext context, String loginName) {
//        if (Realm.class.isInstance(context.get())) {
//            User user = null;
//
//            if (loginName != null) {
//                if (user == null) {
//                    LDAPUser ldapUser = lookupUser(loginName);
//
//                    if (ldapUser != null) {
//                        user = new User(ldapUser.getLoginName());
//
//                        user.setLoginName(ldapUser.getLoginName());
//                        user.setFirstName(ldapUser.getFirstName());
//                        user.setLastName(ldapUser.getLastName());
//                        user.setEmail(ldapUser.getEmail());
//
//                        populateIdentityType(ldapUser, user);
//                    }
//                }
//            }
//
//            return user;
//        } else {
//            // FIXME throw proper exception
//            throw new RuntimeException();
//        }
//    }
//
//    @Override
//    public Group getGroup(SecurityContext context, String groupPath) {
//        if (groupPath == null) {
//            return null;
//        }
//
//        Group group = null; // getContext().getCache().lookupGroup(getContext().get(), groupPath);
//
//        if (group == null) {
//            group = getGroup(groupPath, getGroupBaseDN(groupPath));
//        }
//
//        return group;
//    }
//
//    @Override
//    public Group getGroup(SecurityContext context, String name, Group parent) {
//        Group group = getGroup(context, parent.getPath() + "/" + name);
//
//        if (group.getParentGroup() == null || !group.getParentGroup().getName().equals(parent.getName())) {
//            group = null;
//        }
//
//        return group;
//    }
//
//    @Override
//    public Role getRole(SecurityContext context, String name) {
//        Role role = null;
//
//        if (name != null) {
//            LDAPRole ldapRole = lookupRole(name);
//
//            if (ldapRole != null) {
//                role = new Role(ldapRole.getName());
//
//                populateIdentityType(ldapRole, role);
//            }
//        }
//
//        return role;
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public <T extends IdentityType> List<T> fetchQueryResults(SecurityContext context, IdentityQuery<T> identityQuery) {
//        LDAPQuery ldapQuery = new LDAPQuery(context, identityQuery, this);
//
//        StringBuffer searchFilter = ldapQuery.createManagedAttributesFilter();
//
//        if (searchFilter == null) {
//            searchFilter = new StringBuffer("(&(objectClass=*))");
//        }
//
//        String relationshipFilter = ldapQuery.createRelationshipFilter();
//
//        if (relationshipFilter.isEmpty() && ldapQuery.hasRelationshipParameters()) {
//            return Collections.emptyList();
//        }
//
//        String idAttribute = getIdAttribute(identityQuery.getIdentityType());
//
//        if (idAttribute != null) {
//            searchFilter.insert(searchFilter.length() - 1, "(" + idAttribute + "=*)");
//        }
//
//        searchFilter.insert(searchFilter.length() - 1, "(!(cn=custom-attributes))");
//        searchFilter.insert(searchFilter.length() - 1, relationshipFilter.toString());
//
//        NamingEnumeration<SearchResult> answer = null;
//        List<T> results = new ArrayList<T>();
//
//        try {
//            String searchBaseDN = getBaseDN(identityQuery.getIdentityType());
//
//            if (identityQuery.getParameter(AttributedType.ID) != null) {
//                searchBaseDN = getConfig().getBaseDN();
//            }
//
//            answer = getLDAPManager().search(searchBaseDN, searchFilter.toString());
//
//            while (answer.hasMore()) {
//                SearchResult sr = (SearchResult) answer.next();
//                String nameInNamespace = sr.getNameInNamespace();
//                String[] names = nameInNamespace.split(LDAPConstants.COMMA);
//                String uid = names[0].split(LDAPConstants.EQUAL)[1];
//
//                T ldapEntry = null;
//
//                if (nameInNamespace.endsWith(getConfig().getUserDNSuffix())) {
//                    ldapEntry = (T) getUser(context, uid);
//                } else if (nameInNamespace.endsWith(getConfig().getAgentDNSuffix())) {
//                    ldapEntry = (T) getAgent(context, uid);
//                } else if (nameInNamespace.endsWith(getConfig().getRoleDNSuffix())) {
//                    ldapEntry = (T) getRole(context, uid);
//                } else if (getConfig().isGroupNamespace(nameInNamespace)) {
//                    String groupDN = nameInNamespace.substring(nameInNamespace.indexOf(",") + 1);
//                    LDAPGroup ldapGroup = new LDAPGroup(groupDN);
//
//                    ldapGroup.setLDAPAttributes(sr.getAttributes());
//
//                    ldapEntry = (T) getGroupById(context, ldapGroup.getId());
//                } else {
//                    throw MESSAGES.ldapStoreUnknownBaseDNForIdentityType(nameInNamespace);
//                }
//
//                boolean match = true;
//
//                if (isCustomAttributesSupported()) {
//                    if (identityQuery.getParameters().containsKey(IdentityType.ENABLED)) {
//                        Object[] values = identityQuery.getParameters().get(IdentityType.ENABLED);
//
//                        if (!String.valueOf(ldapEntry.isEnabled()).equals(values[0].toString())) {
//                            continue;
//                        }
//                    }
//
//                    if (identityQuery.getParameters().containsKey(IdentityType.EXPIRY_DATE)
//                            || identityQuery.getParameters().containsKey(IdentityType.EXPIRY_BEFORE)
//                            || identityQuery.getParameters().containsKey(IdentityType.EXPIRY_AFTER)) {
//
//                        if (ldapEntry.getExpirationDate() == null) {
//                            continue;
//                        }
//
//                        if (identityQuery.getParameters().containsKey(IdentityType.EXPIRY_DATE)) {
//                            Object[] values = identityQuery.getParameters().get(IdentityType.EXPIRY_DATE);
//
//                            long storedDateInMillis = ldapEntry.getExpirationDate().getTime();
//                            long providedDateInMillis = ((Date) values[0]).getTime();
//
//                            if (storedDateInMillis != providedDateInMillis) {
//                                continue;
//                            }
//                        }
//
//                        if (identityQuery.getParameters().containsKey(IdentityType.EXPIRY_BEFORE)) {
//                            Object[] values = identityQuery.getParameters().get(IdentityType.EXPIRY_BEFORE);
//
//                            long storedDateInMillis = ldapEntry.getExpirationDate().getTime();
//                            long providedDateInMillis = ((Date) values[0]).getTime();
//
//                            if (storedDateInMillis > providedDateInMillis) {
//                                continue;
//                            }
//                        }
//
//                        if (identityQuery.getParameters().containsKey(IdentityType.EXPIRY_AFTER)) {
//                            Object[] values = identityQuery.getParameters().get(IdentityType.EXPIRY_AFTER);
//
//                            long storedDateInMillis = ldapEntry.getExpirationDate().getTime();
//                            long providedDateInMillis = ((Date) values[0]).getTime();
//
//                            if (storedDateInMillis < providedDateInMillis) {
//                                continue;
//                            }
//                        }
//                    }
//
//                    Set<Entry<QueryParameter, Object[]>> parameters = identityQuery.getParameters(
//                            IdentityType.AttributeParameter.class).entrySet();
//
//                    for (Entry<QueryParameter, Object[]> ldapQueryParameter : parameters) {
//                        QueryParameter queryParameter = ldapQueryParameter.getKey();
//                        Object[] values = ldapQueryParameter.getValue();
//
//                        match = false;
//
//                        IdentityType.AttributeParameter customParameter = (IdentityType.AttributeParameter) queryParameter;
//                        Attribute<Serializable> customParameterValue = ldapEntry.getAttribute(customParameter.getName());
//
//                        if (ldapEntry.getAttribute(customParameter.getName()) != null) {
//                            int count = values.length;
//
//                            for (Object parameterValue : values) {
//                                if (customParameterValue.getValue().getClass().isArray()) {
//                                    Object[] customParameterValues = (Object[]) customParameterValue.getValue();
//
//                                    for (Object value : customParameterValues) {
//                                        if (value.equals(parameterValue)) {
//                                            count--;
//                                        }
//                                    }
//                                } else {
//                                    if (parameterValue.equals(customParameterValue.getValue())) {
//                                        count--;
//                                    }
//                                }
//                            }
//
//                            match = count <= 0;
//
//                            if (!match) {
//                                break;
//                            }
//                        }
//                    }
//                }
//
//                if (match && ldapEntry != null) {
//                    results.add(ldapEntry);
//                }
//            }
//        } catch (NamingException nme) {
//            throw MESSAGES.ldapStoreSearchFailed(nme);
//        } finally {
//            if (answer != null) {
//                try {
//                    answer.close();
//                } catch (NamingException e) {
//                }
//            }
//        }
//
//        return results;
//    }
//
//    @Override
//    public <T extends IdentityType> int countQueryResults(SecurityContext context, IdentityQuery<T> identityQuery) {
//        throw MESSAGES.notImplentedYet();
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public <T extends Relationship> List<T> fetchQueryResults(SecurityContext context, RelationshipQuery<T> query) {
//        List<T> results = new ArrayList<T>();
//        Class<T> relationshipType = query.getRelationshipType();
//
//        Object[] identityQueryParameterValue = query.getParameter(Relationship.IDENTITY);
//
//        if (identityQueryParameterValue != null && identityQueryParameterValue.length > 0) {
//            Object identityParameterValue = identityQueryParameterValue[0];
//
//            IdentityType ldapEntry = null;
//
//            if (IdentityType.class.isInstance(identityParameterValue)) {
//                ldapEntry = lookupEntryById(context, (IdentityType) identityParameterValue);
//            } else if (String.class.isInstance(identityParameterValue)) {
//                IdentityQuery<IdentityType> identityQuery = context.getIdentityManager()
//                        .createIdentityQuery(IdentityType.class);
//
//                identityQuery.setParameter(IdentityType.ID, identityParameterValue.toString());
//
//                List<IdentityType> result = identityQuery.getResultList();
//
//                if (!result.isEmpty()) {
//                    ldapEntry = result.get(0);
//                }
//            }
//
//            if (ldapEntry == null) {
//                return results;
//            }
//
//            if (Agent.class.isInstance(ldapEntry)) {
//                Agent agent = (Agent) ldapEntry;
//
//                RelationshipQuery<Grant> grantQuery = context.getIdentityManager().createRelationshipQuery(Grant.class);
//
//                grantQuery.setParameter(Grant.ASSIGNEE, agent);
//
//                List<Grant> resultList = grantQuery.getResultList();
//
//                for (Grant grant : resultList) {
//                    results.add((T) grant);
//                }
//
//                RelationshipQuery<GroupMembership> groupQuery = context.getIdentityManager().createRelationshipQuery(
//                        GroupMembership.class);
//
//                groupQuery.setParameter(GroupMembership.MEMBER, agent);
//
//                List<GroupMembership> resultGroups = groupQuery.getResultList();
//
//                for (GroupMembership groups : resultGroups) {
//                    results.add((T) groups);
//                }
//
//                RelationshipQuery<GroupRole> groupRoleQuery = context.getIdentityManager().createRelationshipQuery(
//                        GroupRole.class);
//
//                groupRoleQuery.setParameter(GroupRole.ASSIGNEE, agent);
//
//                List<GroupRole> resultGroupRoless = groupRoleQuery.getResultList();
//
//                for (GroupRole groupRole : resultGroupRoless) {
//                    results.add((T) groupRole);
//                }
//            } else if (Role.class.isInstance(ldapEntry)) {
//                Role role = (Role) ldapEntry;
//                RelationshipQuery<Grant> grantQuery = context.getIdentityManager().createRelationshipQuery(Grant.class);
//
//                grantQuery.setParameter(Grant.ROLE, role);
//
//                List<Grant> resultList = grantQuery.getResultList();
//
//                for (Grant grant : resultList) {
//                    results.add((T) grant);
//                }
//
//                RelationshipQuery<GroupRole> groupRoleQuery = context.getIdentityManager().createRelationshipQuery(
//                        GroupRole.class);
//
//                groupRoleQuery.setParameter(GroupRole.ROLE, role);
//
//                List<GroupRole> resultGroupRoless = groupRoleQuery.getResultList();
//
//                for (GroupRole groupRole : resultGroupRoless) {
//                    results.add((T) groupRole);
//                }
//            } else if (Group.class.isInstance(ldapEntry)) {
//                Group group = (Group) ldapEntry;
//                RelationshipQuery<GroupMembership> groupMembershipQuery = context.getIdentityManager().createRelationshipQuery(
//                        GroupMembership.class);
//
//                groupMembershipQuery.setParameter(GroupMembership.GROUP, group);
//
//                List<GroupMembership> resultList = groupMembershipQuery.getResultList();
//
//                for (GroupMembership groupMembership : resultList) {
//                    results.add((T) groupMembership);
//                }
//
//                RelationshipQuery<GroupRole> groupRoleQuery = context.getIdentityManager().createRelationshipQuery(
//                        GroupRole.class);
//
//                groupRoleQuery.setParameter(GroupRole.GROUP, group);
//
//                List<GroupRole> resultGroupRoless = groupRoleQuery.getResultList();
//
//                for (GroupRole groupRole : resultGroupRoless) {
//                    results.add((T) groupRole);
//                }
//            }
//        } else {
//            if (Grant.class.equals(relationshipType)) {
//                IdentityType identityType = null;
//
//                if (query.getParameter(Grant.ASSIGNEE) != null) {
//                    identityType = (IdentityType) query.getParameter(Grant.ASSIGNEE)[0];
//                }
//
//                Role role = null;
//
//                if (query.getParameter(Grant.ROLE) != null) {
//                    role = (Role) query.getParameter(Grant.ROLE)[0];
//                }
//
//                if (identityType != null && role != null) {
//                    LDAPEntry agentEntry = (LDAPEntry) lookupEntryById(context, identityType);
//                    LDAPRole roleEntry = (LDAPRole) lookupEntryById(context, role);
//
//                    if (roleEntry.isMember(agentEntry)) {
//                        results.add((T) new Grant(identityType, role));
//                    }
//                } else if (identityType != null) {
//                    String membersFilter = "";
//
//                    LDAPEntry ldapEntry = null;
//
//                    try {
//                        ldapEntry = (LDAPEntry) lookupEntryById(context, identityType);
//                        membersFilter = "(member=" + ldapEntry.getDN() + ")";
//                    } catch (IdentityManagementException ime) {
//                        return results;
//                    }
//
//                    NamingEnumeration<SearchResult> search = null;
//
//                    try {
//                        search = getLDAPManager().search(getConfig().getRoleDNSuffix(), membersFilter.toString());
//
//                        while (search.hasMoreElements()) {
//                            SearchResult searchResult = search.next();
//                            String entryCN = searchResult.getAttributes().get(CN).get().toString();
//                            results.add((T) new Grant(identityType, getRole(context, entryCN)));
//                        }
//                    } catch (NamingException nme) {
//                        throw MESSAGES.ldapStoreSearchFailed(nme);
//                    } finally {
//                        if (search != null) {
//                            try {
//                                search.close();
//                            } catch (NamingException e) {
//                            }
//                        }
//                    }
//                } else if (role != null) {
//                    LDAPEntry ldapEntry = null;
//
//                    try {
//                        ldapEntry = (LDAPEntry) lookupEntryById(context, role);
//                    } catch (IdentityManagementException ime) {
//                        return results;
//                    }
//
//                    NamingEnumeration<?> members = null;
//
//                    try {
//                        members = ldapEntry.getLDAPAttributes().get(MEMBER).getAll();
//
//                        while (members.hasMoreElements()) {
//                            String memberDN = (String) members.nextElement();
//
//                            if (!memberDN.trim().isEmpty()) {
//                                String userBindingName = memberDN.split(COMMA)[0];
//                                String loginName = userBindingName.split(EQUAL)[1];
//
//                                Agent associatedAgent = getAgent(context, loginName);
//
//                                if (associatedAgent != null) {
//                                    results.add((T) new Grant(associatedAgent, role));
//                                }
//                            }
//                        }
//                    } catch (NamingException e) {
//                        throw MESSAGES.ldapStoreSearchFailed(e);
//                    } finally {
//                        if (members != null) {
//                            try {
//                                members.close();
//                            } catch (NamingException e) {
//                            }
//                        }
//                    }
//                }
//            } else if (GroupMembership.class.equals(relationshipType)) {
//                Agent agent = null;
//
//                if (query.getParameter(GroupMembership.MEMBER) != null) {
//                    agent = (Agent) query.getParameter(GroupMembership.MEMBER)[0];
//                }
//
//                Group group = null;
//
//                if (query.getParameter(GroupMembership.GROUP) != null) {
//                    group = (Group) query.getParameter(GroupMembership.GROUP)[0];
//                }
//
//                if (agent != null && group != null) {
//                    LDAPGroup groupEntry = null;
//                    LDAPAgent agentEntry = null;
//
//                    try {
//                        groupEntry = (LDAPGroup) lookupEntryById(context, group);
//                        agentEntry = (LDAPAgent) lookupEntryById(context, agent);
//                    } catch (IdentityManagementException ime) {
//                        return results;
//                    }
//
//                    boolean isMember = false;
//
//                    if (groupEntry.isMember(agentEntry)) {
//                        isMember = true;
//                    } else {
//                        List<Group> parentGroups = getParentGroups(context, groupEntry);
//
//                        for (Group parentGroup : parentGroups) {
//                            LDAPGroup parentGroupEntry = (LDAPGroup) lookupEntryById(context, parentGroup);
//
//                            if (parentGroupEntry.isMember(agentEntry)) {
//                                isMember = true;
//                            }
//                        }
//                    }
//
//                    if (isMember) {
//                        results.add((T) new GroupMembership(agent, group));
//                    }
//                } else if (agent != null) {
//                    String membersFilter = "";
//
//                    try {
//                        LDAPEntry ldapEntry = (LDAPEntry) lookupEntryById(context, agent);
//                        membersFilter = "(" + MEMBER + "=" + ldapEntry.getDN() + ")";
//                    } catch (IdentityManagementException ime) {
//                        return results;
//                    }
//
//                    NamingEnumeration<SearchResult> search = null;
//
//                    try {
//                        search = getLDAPManager().search(getConfig().getBaseDN(), membersFilter.toString());
//
//                        while (search.hasMoreElements()) {
//                            SearchResult searchResult = search.next();
//                            String entryCN = searchResult.getAttributes().get(CN).get().toString();
//                            String nameInNamespace = searchResult.getNameInNamespace();
//
//                            if (!getConfig().isGroupNamespace(nameInNamespace)) {
//                                continue;
//                            }
//
//                            String baseDN = nameInNamespace.substring(nameInNamespace.indexOf(LDAPConstants.COMMA) + 1);
//                            LDAPGroup ldapGroup = new LDAPGroup(baseDN);
//
//                            populateLDAPEntry(ldapGroup, searchResult);
//
//                            Group simpleGroup = new Group(entryCN, getParentGroup(ldapGroup, true));
//
//                            populateIdentityType(ldapGroup, simpleGroup);
//
//                            results.add((T) new GroupMembership(agent, simpleGroup));
//                        }
//                    } catch (NamingException e) {
//                        throw MESSAGES.ldapStoreSearchFailed(e);
//                    } finally {
//                        if (search != null) {
//                            try {
//                                search.close();
//                            } catch (NamingException e) {
//                            }
//                        }
//                    }
//                } else if (group != null) {
//                    LDAPEntry ldapEntry = null;
//
//                    try {
//                        ldapEntry = (LDAPEntry) lookupEntryById(context, group);
//                    } catch (IdentityManagementException ime) {
//                        return results;
//                    }
//
//                    NamingEnumeration<?> members = null;
//
//                    try {
//                        members = ldapEntry.getLDAPAttributes().get(MEMBER).getAll();
//
//                        while (members.hasMoreElements()) {
//                            String memberDN = (String) members.nextElement();
//
//                            if (memberDN.contains(getConfig().getUserDNSuffix())
//                                    || memberDN.contains(getConfig().getAgentDNSuffix())) {
//
//                                if (!memberDN.trim().isEmpty()) {
//                                    String agentBindingName = memberDN.split(COMMA)[0];
//                                    String loginName = agentBindingName.split(EQUAL)[1];
//
//                                    results.add((T) new GroupMembership(getAgent(context, loginName), group));
//                                }
//                            }
//                        }
//                    } catch (NamingException e) {
//                        throw MESSAGES.ldapStoreSearchFailed(e);
//                    } finally {
//                        if (members != null) {
//                            try {
//                                members.close();
//                            } catch (NamingException e) {
//                            }
//                        }
//                    }
//                }
//            } else if (GroupRole.class.equals(relationshipType)) {
//                Agent agent = null;
//
//                if (query.getParameter(GroupRole.ASSIGNEE) != null) {
//                    agent = (Agent) query.getParameter(GroupRole.ASSIGNEE)[0];
//                }
//
//                Role role = null;
//
//                if (query.getParameter(GroupRole.ROLE) != null) {
//                    role = (Role) query.getParameter(GroupRole.ROLE)[0];
//                }
//
//                Group group = null;
//
//                if (query.getParameter(GroupRole.GROUP) != null) {
//                    group = (Group) query.getParameter(GroupRole.GROUP)[0];
//                }
//
//                if (agent != null && group != null && role != null) {
//                    LDAPGroup groupEntry = (LDAPGroup) lookupEntryById(context, group);
//                    LDAPRole roleEntry = (LDAPRole) lookupEntryById(context, role);
//                    LDAPAgent agentEntry = (LDAPAgent) lookupEntryById(context, agent);
//
//                    if (hasGroupRole(groupEntry, roleEntry, agentEntry)) {
//                        results.add((T) new GroupRole(agent, group, role));
//                    } else {
//                        List<Group> parentGroups = getParentGroups(context, groupEntry);
//
//                        for (Group parentGroup : parentGroups) {
//                            LDAPGroup parentGroupEntry = (LDAPGroup) lookupEntryById(context, parentGroup);
//
//                            if (hasGroupRole(parentGroupEntry, roleEntry, agentEntry)) {
//                                results.add((T) new GroupRole(agent, group, role));
//                                break;
//                            }
//                        }
//                    }
//                } else if (agent != null && role == null && group == null) {
//                    LDAPAgent agentEntry = lookupAgent(agent);
//
//                    if (agentEntry != null) {
//                        NamingEnumeration<SearchResult> search = null;
//
//                        try {
//                            search = getLDAPManager().search(agentEntry.getDN(), "(&(objectClass=*)(cn=*)(member=*))");
//
//                            while (search.hasMore()) {
//                                SearchResult next = search.next();
//                                String groupName = (String) next.getAttributes().get(CN).get();
//
//                                javax.naming.directory.Attribute members = next.getAttributes().get(MEMBER);
//
//                                if (members != null && members.size() > 0) {
//                                    NamingEnumeration<?> allRoles = members.getAll();
//
//                                    while (allRoles.hasMoreElements()) {
//                                        String roleDN = (String) allRoles.nextElement();
//                                        String roleName = roleDN.substring(roleDN.indexOf(EQUAL) + 1, roleDN.indexOf(COMMA));
//
//                                        Role associatedRole = getRole(context, roleName);
//                                        Group associatedGroup = getGroup(context, groupName);
//
//                                        if (associatedRole != null && associatedGroup != null) {
//                                            results.add((T) new GroupRole(agent, associatedGroup, associatedRole));
//                                        }
//                                    }
//                                }
//                            }
//                        } catch (NamingException e) {
//                            throw MESSAGES.ldapStoreSearchFailed(e);
//                        } finally {
//                            try {
//                                search.close();
//                            } catch (NamingException e) {
//                            }
//                        }
//                    }
//                } else if (role != null) {
//                    LDAPRole roleEntry = null;
//
//                    try {
//                        roleEntry = (LDAPRole) lookupEntryById(context, role);
//                    } catch (IdentityManagementException ime) {
//                        return results;
//                    }
//
//                    if (roleEntry != null) {
//                        NamingEnumeration<SearchResult> search = null;
//
//                        try {
//                            search = getLDAPManager().search(getConfig().getUserDNSuffix(),
//                                    "(&(objectClass=*)(" + CN + EQUAL + "*)(" + MEMBER + EQUAL + roleEntry.getDN() + "))");
//
//                            while (search.hasMore()) {
//                                SearchResult next = search.next();
//                                String nameInNamespace = next.getNameInNamespace();
//                                String userDN = nameInNamespace.substring(nameInNamespace.indexOf(UID));
//                                String userName = userDN.substring(userDN.indexOf(EQUAL) + 1, userDN.indexOf(COMMA));
//                                String groupName = (String) next.getAttributes().get(CN).get();
//
//                                Role associatedRole = getRole(context, roleEntry.getName());
//                                Group associatedGroup = getGroup(context, groupName);
//                                Agent associatedAgent = getAgent(context, userName);
//
//                                if (associatedRole != null && associatedGroup != null && associatedAgent != null) {
//                                    results.add((T) new GroupRole(associatedAgent, associatedGroup, associatedRole));
//                                }
//                            }
//                        } catch (NamingException e) {
//                            throw MESSAGES.ldapStoreSearchFailed(e);
//                        } finally {
//                            try {
//                                search.close();
//                            } catch (NamingException e) {
//                            }
//                        }
//                    }
//                } else if (group != null) {
//                    LDAPGroup groupEntry = null;
//
//                    try {
//                        groupEntry = (LDAPGroup) lookupEntryById(context, group);
//                    } catch (IdentityManagementException ime) {
//                        return results;
//                    }
//
//                    String filter = "(&(objectClass=*)(" + groupEntry.getBidingName() + ")(" + MEMBER + EQUAL + "*))";
//                    NamingEnumeration<SearchResult> search = null;
//
//                    try {
//                        search = getLDAPManager().search(getConfig().getUserDNSuffix(), filter);
//
//                        while (search.hasMore()) {
//                            SearchResult next = search.next();
//                            String nameInNamespace = next.getNameInNamespace();
//                            String userDN = nameInNamespace.substring(nameInNamespace.indexOf(UID));
//                            String userName = userDN.substring(userDN.indexOf(EQUAL) + 1, userDN.indexOf(COMMA));
//                            String groupName = (String) next.getAttributes().get(CN).get();
//
//                            javax.naming.directory.Attribute members = next.getAttributes().get(MEMBER);
//
//                            if (members != null && members.size() > 0) {
//                                NamingEnumeration<?> allRoles = members.getAll();
//
//                                while (allRoles.hasMoreElements()) {
//                                    String roleDN = (String) allRoles.nextElement();
//                                    String roleName = roleDN.substring(roleDN.indexOf(EQUAL) + 1, roleDN.indexOf(COMMA));
//
//                                    Role associatedRole = getRole(context, roleName);
//                                    Group associatedGroup = getGroup(context, groupName);
//                                    Agent associatedAgent = getAgent(context, userName);
//
//                                    if (associatedRole != null && associatedGroup != null && associatedAgent != null) {
//                                        results.add((T) new GroupRole(associatedAgent, associatedGroup, associatedRole));
//                                    }
//                                }
//                            }
//                        }
//                    } catch (NamingException e) {
//                        throw MESSAGES.ldapStoreSearchFailed(e);
//                    } finally {
//                        try {
//                            search.close();
//                        } catch (NamingException e) {
//                        }
//                    }
//                }
//            }
//        }
//
//        return results;
//    }
//
//    private boolean hasGroupRole(LDAPGroup groupEntry, LDAPRole roleEntry, LDAPAgent agentEntry) {
//        NamingEnumeration<SearchResult> groupRoleAttributes = null;
//
//        try {
//            groupRoleAttributes = lookupGroupRoleEntry(agentEntry, groupEntry);
//
//            if (groupRoleAttributes.hasMore()) {
//                LDAPGroupRole groupRoleEntry = new LDAPGroupRole(agentEntry, groupEntry, roleEntry);
//
//                groupRoleEntry.setLDAPAttributes(groupRoleAttributes.next().getAttributes());
//
//                if (groupRoleEntry.isMember(roleEntry)) {
//                    return true;
//                }
//            }
//        } catch (NamingException e) {
//            throw MESSAGES.ldapStoreSearchFailed(e);
//        } finally {
//            try {
//                groupRoleAttributes.close();
//            } catch (NamingException e) {
//            }
//        }
//
//        return false;
//    }
//
//    @Override
//    public <T extends Relationship> int countQueryResults(SecurityContext context, RelationshipQuery<T> query) {
//        throw MESSAGES.notImplentedYet();
//    }
//
//    @Override
//    public void setAttribute(SecurityContext context, IdentityType identityType, Attribute<? extends Serializable> attribute) {
//        throw MESSAGES.notImplentedYet();
//    }
//
//    @Override
//    public <T extends Serializable> Attribute<T> getAttribute(SecurityContext context, IdentityType identityType,
//            String attributeName) {
//        throw MESSAGES.notImplentedYet();
//    }
//
//    @Override
//    public void removeAttribute(SecurityContext context, IdentityType identityType, String attributeName) {
//        throw MESSAGES.notImplentedYet();
//    }
//
//    @Override
//    public void validateCredentials(SecurityContext context, Credentials credentials) {
//        CredentialHandler handler = context.getCredentialValidator(credentials.getClass(), this);
//
//        if (handler == null) {
//            throw MESSAGES.credentialHandlerNotFoundForCredentialType(credentials.getClass());
//        }
//
//        handler.validate(context, credentials, this);
//    }
//
//    @Override
//    public void updateCredential(SecurityContext context, Agent agent, Object credential, Date effectiveDate, Date expiryDate) {
//        CredentialHandler handler = context.getCredentialUpdater(credential.getClass(), this);
//
//        if (handler == null) {
//            throw MESSAGES.credentialHandlerNotFoundForCredentialType(credential.getClass());
//        }
//
//        handler.update(context, agent, credential, this, effectiveDate, expiryDate);
//    }
//
//    private void addIdentityType(SecurityContext context, IdentityType newIdentityType, LDAPIdentityType ldapIdentityType) {
//        ldapIdentityType.setEnabled(newIdentityType.isEnabled());
//        ldapIdentityType.setExpirationDate(newIdentityType.getExpirationDate());
//
//        getLDAPManager().createSubContext(ldapIdentityType.getDN(), ldapIdentityType.getLDAPAttributes());
//
//        if (isCustomAttributesSupported()) {
//            getLDAPManager().rebind(getCustomAttributesDN(ldapIdentityType.getDN()), ldapIdentityType.getCustomAttributes());
//        }
//
//        NamingEnumeration<SearchResult> search = null;
//
//        try {
//            search = getLDAPManager().search(ldapIdentityType.getDnSuffix(),
//                    "(&(objectClass=*)(" + ldapIdentityType.getBidingName() + "))");
//
//            ldapIdentityType.setLDAPAttributes(search.next().getAttributes());
//        } catch (NamingException ne) {
//            throw MESSAGES.ldapStoreSearchFailed(ne);
//        } finally {
//            try {
//                search.close();
//            } catch (NamingException e) {
//            }
//        }
//
//        newIdentityType.setId(ldapIdentityType.getId());
//    }
//
//    protected LDAPOperationManager getLDAPManager() {
//        return this.operationManager;
//    }
//
//    /**
//     * <p>
//     * Returns a DN for the custom attributes entry.
//     * </p>
//     *
//     * @param parentDN
//     * @return
//     */
//    private String getCustomAttributesDN(String parentDN) {
//        return CN + "=custom-attributes" + COMMA + parentDN;
//    }
//
//    private void updateIdentityType(IdentityType updatedIdentityType, LDAPIdentityType identityTypeEntry) {
//        identityTypeEntry.setEnabled(updatedIdentityType.isEnabled());
//        identityTypeEntry.setExpirationDate(updatedIdentityType.getExpirationDate());
//
//        Attributes ldapAttributes = identityTypeEntry.getLDAPAttributes();
//
//        NamingEnumeration<? extends javax.naming.directory.Attribute> all = ldapAttributes.getAll();
//        Attributes clonedAttributes = (Attributes) identityTypeEntry.getLDAPAttributes().clone();
//
//        while (all.hasMoreElements()) {
//            javax.naming.directory.Attribute attribute = (javax.naming.directory.Attribute) all.nextElement();
//
//            if (clonedAttributes.get(attribute.getID()) != null) {
//                if (!attribute.getID().equalsIgnoreCase(LDAPConstants.ENTRY_UUID)
//                        && !attribute.getID().equalsIgnoreCase(LDAPConstants.CREATE_TIMESTAMP)) {
//                    getLDAPManager().modifyAttribute(identityTypeEntry.getDN(), attribute);
//                }
//            } else {
//                getLDAPManager().addAttribute(identityTypeEntry.getDN(), attribute);
//            }
//        }
//
//        identityTypeEntry.getCustomAttributes().clear();
//
//        Collection<Attribute<? extends Serializable>> updatedAttributes = updatedIdentityType.getAttributes();
//
//        for (Attribute<? extends Serializable> attribute : updatedAttributes) {
//            identityTypeEntry.getCustomAttributes()
//                    .addAttribute(attribute.getName(), Base64.encodeObject(attribute.getValue()));
//        }
//
//        getLDAPManager().rebind(getCustomAttributesDN(identityTypeEntry.getDN()), identityTypeEntry.getCustomAttributes());
//    }
//
//    private void populateIdentityType(LDAPIdentityType ldapIdentityType, IdentityType identityType) {
//        identityType.setId(ldapIdentityType.getId());
//        identityType.setPartition(ldapIdentityType.get());
//
//        if (isCustomAttributesSupported()) {
//            identityType.setEnabled(ldapIdentityType.isEnabled());
//            identityType.setCreatedDate(ldapIdentityType.getCreatedDate());
//            identityType.setExpirationDate(ldapIdentityType.getExpirationDate());
//
//            Set<Entry<String, Serializable>> entrySet = ldapIdentityType.getCustomAttributes().getAttributes().entrySet();
//
//            for (Entry<String, Serializable> entry : entrySet) {
//                if (!entry.getKey().equals(LDAPConstants.CUSTOM_ATTRIBUTE_ENABLED)
//                        && !entry.getKey().equals(LDAPConstants.CUSTOM_ATTRIBUTE_EXPIRY_DATE)) {
//                    identityType.setAttribute(new Attribute<Serializable>(entry.getKey(), (Serializable) Base64
//                            .decodeToObject(entry.getValue().toString())));
//                }
//            }
//        }
//    }
//
//    protected <T extends LDAPIdentityType> T lookupEntryById(SecurityContext context, Class<T> type, String id)
//            throws IdentityManagementException {
//        T identityType = null;
//
//        NamingEnumeration<SearchResult> search = getLDAPManager().lookupById(getConfig().getBaseDN(), id);
//
//        try {
//            if (search.hasMore()) {
//                SearchResult sr = search.next();
//                String nameInNamespace = sr.getNameInNamespace();
//                String baseDN = nameInNamespace.substring(nameInNamespace.indexOf(",") + 1);
//
//                identityType = type.getConstructor(String.class).newInstance(baseDN);
//
//                populateLDAPEntry(identityType, sr);
//            }
//
//            if (search.hasMore()) {
//                throw MESSAGES.identityTypeAmbiguosFoundWithId(id);
//            }
//        } catch (NamingException e) {
//            throw MESSAGES.ldapStoreSearchFailed(e);
//        } catch (Exception e) {
//            throw new IdentityManagementException("Error creating instance for type [" + type.getName() + "].", e);
//        } finally {
//            if (search != null) {
//                try {
//                    search.close();
//                } catch (NamingException e) {
//                }
//            }
//        }
//
//        if (identityType == null) {
//            throw MESSAGES.attributedTypeNotFoundWithId(type, id, context.get());
//        }
//
//        return identityType;
//    }
//
//    private <T extends LDAPIdentityType> T populateIdentityTypeEntry(T identityType) {
//        String filter = "(&(objectClass=*)(" + identityType.getBidingName() + ")) ";
//
//        NamingEnumeration<SearchResult> search = null;
//
//        try {
//            search = getLDAPManager().search(identityType.getDnSuffix(), filter);
//
//            if (search.hasMore()) {
//                populateLDAPEntry(identityType, search.next());
//            } else {
//                identityType = null;
//            }
//        } catch (NamingException e) {
//            throw MESSAGES.ldapStoreSearchFailed(e);
//        } finally {
//            if (search != null) {
//                try {
//                    search.close();
//                } catch (NamingException e) {
//                }
//            }
//        }
//
//        return identityType;
//    }
//
//    private <T extends LDAPIdentityType> void populateLDAPEntry(T identityType, SearchResult sr) throws NamingException {
//        identityType.setLDAPAttributes(sr.getAttributes());
//
//        if (isCustomAttributesSupported()) {
//            identityType.setCustomAttributes(getCustomAttributes(identityType));
//        }
//
//        // for now, the store is not supporting partitions. ldap does not provide a good attribute to hold such
//        // information.
//        // maybe in this case we should mix stores.
//        identityType.setPartition(new Realm(Realm.DEFAULT_REALM));
//
//        if (isCustomAttributesSupported()) {
//            identityType.setCustomAttributes(getCustomAttributes(identityType));
//        }
//    }
//
//    /**
//     * <p>
//     * Returns the custom attributes for the given parent DN.
//     * </p>
//     *
//     * @param parentDN
//     * @return
//     */
//    private LDAPCustomAttributes getCustomAttributes(LDAPAttributedType attributedType) {
//        String customDN = getCustomAttributesDN(attributedType.getDN());
//
//        LDAPCustomAttributes customAttributes = null;
//
//        try {
//            customAttributes = getLDAPManager().lookup(customDN);
//        } catch (Exception ignore) {
//        }
//
//        if (customAttributes == null) {
//            getLDAPManager().bind(customDN, attributedType.getCustomAttributes());
//        }
//
//        return customAttributes;
//    }
//
//    /**
//     * <p>
//     * Returns the parent group for the given child group.
//     * </p>
//     *
//     * @param childGroup
//     * @return
//     */
//    protected Group getParentGroup(LDAPGroup childGroup, boolean loadAllHiearchy) {
//        StringBuffer filter = new StringBuffer();
//
//        filter.append("(" + MEMBER + EQUAL + childGroup.getDN() + ")");
//
//        NamingEnumeration<SearchResult> answer = null;
//
//        // Search for objects with these matching attributes
//        try {
//            SearchControls controls = new SearchControls();
//
//            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
//
//            answer = getLDAPManager().search(getConfig().getBaseDN(), filter.toString(), new String[] { CN }, controls);
//
//            while (answer.hasMoreElements()) {
//                SearchResult sr = (SearchResult) answer.nextElement();
//                Attributes attributes = sr.getAttributes();
//                String cn = (String) attributes.get(CN).get();
//                String nameInNamespace = sr.getNameInNamespace();
//                String str = CN + EQUAL + cn + COMMA;
//                String baseDN = nameInNamespace.substring(nameInNamespace.indexOf(str) + str.length());
//
//                if (loadAllHiearchy) {
//                    return getGroup(cn, baseDN);
//                } else {
//                    LDAPGroup parentEntry = new LDAPGroup(baseDN);
//                    Group parentGroup = new Group(cn);
//
//                    populateLDAPEntry(parentEntry, sr);
//                    populateIdentityType(parentEntry, parentGroup);
//
//                    return parentGroup;
//                }
//            }
//        } catch (NamingException e) {
//            throw MESSAGES.ldapStoreSearchFailed(e);
//        } finally {
//            if (answer != null) {
//                try {
//                    answer.close();
//                } catch (NamingException e) {
//                }
//            }
//        }
//
//        return null;
//    }
//
//    protected List<Group> getParentGroups(SecurityContext context, LDAPGroup childGroup) {
//        List<Group> result = new ArrayList<Group>();
//
//        Group parentGroup = getParentGroup(childGroup, true);
//
//        if (parentGroup == null) {
//            return result;
//        }
//
//        result.add(parentGroup);
//        result.addAll(getParentGroups(context, (LDAPGroup) lookupEntryById(context, parentGroup)));
//
//        return result;
//    }
//
//    private void addGroup(SecurityContext context, Group newGroup) {
//        LDAPGroup groupEntry = new LDAPGroup(getGroupBaseDN(newGroup.getPath()));
//
//        groupEntry.setName(newGroup.getName());
//
//        addIdentityType(context, newGroup, groupEntry);
//
//        if (newGroup.getParentGroup() != null) {
//            LDAPGroup parentGroupentry = lookupGroup(newGroup.getParentGroup().getPath());
//
//            addMember(parentGroupentry, groupEntry);
//        }
//    }
//
//    private String getGroupBaseDN(String groupPath) {
//        if (!groupPath.startsWith("/")) {
//            groupPath = "/" + groupPath;
//        }
//
//        String groupMappingDN = getConfig().getGroupMappingDN(groupPath);
//
//        if (groupMappingDN == null) {
//            groupMappingDN = getConfig().getGroupDNSuffix();
//        }
//
//        return groupMappingDN;
//    }
//
//    private void addRole(SecurityContext context, Role newRole) {
//        LDAPRole ldapRole = new LDAPRole(getConfig().getRoleDNSuffix());
//
//        ldapRole.setName(newRole.getName());
//
//        addIdentityType(context, newRole, ldapRole);
//    }
//
//    private void addAgent(SecurityContext context, Agent newAgent) {
//        LDAPAgent ldapAgent = new LDAPAgent(getConfig().getAgentDNSuffix());
//
//        ldapAgent.setLoginName(newAgent.getLoginName());
//
//        addIdentityType(context, newAgent, ldapAgent);
//    }
//
//    private void addUser(SecurityContext context, User newUser) {
//        LDAPUser ldapUser = new LDAPUser(getConfig().getUserDNSuffix());
//
//        ldapUser.setLoginName(newUser.getLoginName());
//        ldapUser.setFirstName(newUser.getFirstName());
//        ldapUser.setLastName(newUser.getLastName());
//        ldapUser.setFullName(ldapUser.getUserCN());
//        ldapUser.setEmail(newUser.getEmail());
//
//        addIdentityType(context, newUser, ldapUser);
//    }
//
//    private void updateGroup(SecurityContext context, Group updatedGroup) {
//        LDAPGroup groupEntry = (LDAPGroup) lookupEntryById(context, updatedGroup);
//
//        updateIdentityType(updatedGroup, groupEntry);
//    }
//
//    private void updateRole(SecurityContext context, Role updatedRole) {
//        LDAPRole roleEntry = (LDAPRole) lookupEntryById(context, updatedRole);
//
//        updateIdentityType(updatedRole, roleEntry);
//    }
//
//    private void updateAgent(SecurityContext context, Agent updatedAgent) {
//        LDAPAgent agentEntry = (LDAPAgent) lookupEntryById(context, updatedAgent);
//
//        updateIdentityType(updatedAgent, agentEntry);
//    }
//
//    private void updateUser(SecurityContext context, User updatedUser) {
//        LDAPUser userEntry = (LDAPUser) lookupEntryById(context, updatedUser);
//
//        userEntry.setFirstName(updatedUser.getFirstName());
//        userEntry.setLastName(updatedUser.getLastName());
//        userEntry.setFullName(userEntry.getUserCN());
//        userEntry.setEmail(updatedUser.getEmail());
//
//        updateIdentityType(updatedUser, userEntry);
//    }
//
//    private String getBaseDN(Class<? extends IdentityType> identityTypeClass) {
//        String baseDN = null;
//
//        if (IDMUtil.isUserType(identityTypeClass)) {
//            baseDN = getConfig().getUserDNSuffix();
//        } else if (IDMUtil.isRoleType(identityTypeClass)) {
//            baseDN = getConfig().getRoleDNSuffix();
//        } else if (IDMUtil.isGroupType(identityTypeClass)) {
//            baseDN = getConfig().getGroupDNSuffix();
//        } else if (IDMUtil.isAgentType(identityTypeClass)) {
//            baseDN = getConfig().getAgentDNSuffix();
//        } else {
//            baseDN = getConfig().getBaseDN();
//        }
//
//        return baseDN;
//    }
//
//    private void addGroupRoleRelationship(SecurityContext context, GroupRole groupRole) {
//        LDAPAgent agentEntry = (LDAPAgent) lookupEntryById(context, groupRole.getAssignee());
//        LDAPGroup groupEntry = (LDAPGroup) lookupEntryById(context, groupRole.getGroup());
//        LDAPRole roleEntry = (LDAPRole) lookupEntryById(context, groupRole.getRole());
//
//        NamingEnumeration<SearchResult> search = null;
//
//        try {
//            search = lookupGroupRoleEntry(agentEntry, groupEntry);
//
//            // if the grouprole entry does not exists create it as a child of the agent entry.
//            if (!search.hasMore()) {
//                LDAPGroupRole groupRoleEntry = new LDAPGroupRole(agentEntry, groupEntry, roleEntry);
//
//                getLDAPManager().createSubContext(groupRoleEntry.getDN(), groupRoleEntry.getLDAPAttributes());
//
//                addMember(groupRoleEntry, roleEntry);
//            }
//        } catch (NamingException e) {
//            throw MESSAGES.ldapStoreCouldNotCreateGroupRoleEntry(e);
//        } finally {
//            if (search != null) {
//                try {
//                    search.close();
//                } catch (NamingException e) {
//                }
//            }
//        }
//    }
//
//    private void addGroupMembership(SecurityContext context, GroupMembership groupMembership) {
//        LDAPGroup groupEntry = (LDAPGroup) lookupEntryById(context, groupMembership.getGroup());
//        LDAPAgent memberEntry = (LDAPAgent) lookupEntryById(context, groupMembership.getMember());
//
//        addMember(groupEntry, memberEntry);
//    }
//
//    private void addGrantRelationship(SecurityContext context, Grant grant) {
//        LDAPRole roleEntry = (LDAPRole) lookupEntryById(context, grant.getRole());
//        LDAPEntry assigneeEntry = (LDAPEntry) lookupEntryById(context, grant.getAssignee());
//
//        addMember(roleEntry, assigneeEntry);
//    }
//
//    private void addMember(LDAPEntry parentEntry, LDAPEntry childEntry) {
//        parentEntry.addMember(childEntry);
//        getLDAPManager().modifyAttribute(parentEntry.getDN(), parentEntry.getLDAPAttributes().get(MEMBER));
//    }
//
//    @SuppressWarnings("unchecked")
//    protected <T extends IdentityType> T lookupEntryById(SecurityContext context, T identityType)
//            throws IdentityManagementException {
//        T identityTypeEntry = null;
//
//        if (Agent.class.isInstance(identityType)) {
//            Agent agent = (Agent) identityType;
//
//            if (User.class.isInstance(agent)) {
//                identityTypeEntry = (T) lookupEntryById(context, LDAPUser.class, agent.getId());
//            } else {
//                identityTypeEntry = (T) lookupEntryById(context, LDAPAgent.class, agent.getId());
//            }
//        } else if (Role.class.isInstance(identityType)) {
//            identityTypeEntry = (T) lookupEntryById(context, LDAPRole.class, identityType.getId());
//        } else if (Group.class.isInstance(identityType)) {
//            identityTypeEntry = (T) lookupEntryById(context, LDAPGroup.class, identityType.getId());
//        } else {
//            throw new IdentityManagementException("Unsupported type [" + identityType.getClass().getName() + "].");
//        }
//
//        return identityTypeEntry;
//    }
//
//    private String getIdAttribute(Class<? extends IdentityType> identityTypeClass) {
//        String idAttribute = null;
//
//        if (IDMUtil.isAgentType(identityTypeClass)) {
//            idAttribute = UID;
//        } else if (IDMUtil.isRoleType(identityTypeClass)) {
//            idAttribute = CN;
//        } else if (IDMUtil.isGroupType(identityTypeClass)) {
//            idAttribute = CN;
//        }
//
//        return idAttribute;
//    }
//
//    protected LDAPRole lookupRole(String name) {
//        return populateIdentityTypeEntry(new LDAPRole(name, getConfig().getRoleDNSuffix()));
//    }
//
//    protected LDAPAgent lookupAgent(String loginName) {
//        return populateIdentityTypeEntry(new LDAPAgent(loginName, getConfig().getAgentDNSuffix()));
//    }
//
//    protected LDAPAgent lookupAgent(Agent agent) {
//        LDAPAgent storedAgent = null;
//
//        if (User.class.isInstance(agent)) {
//            storedAgent = lookupUser(agent.getLoginName());
//        } else {
//            storedAgent = lookupAgent(agent.getLoginName());
//        }
//
//        return storedAgent;
//    }
//
//    protected LDAPGroup lookupGroup(String groupPath) {
//        return lookupGroup(groupPath, getGroupBaseDN(groupPath));
//    }
//
//    protected LDAPGroup lookupGroup(String groupPath, String baseDN) {
//        String name = null;
//
//        if (!groupPath.startsWith("/")) {
//            groupPath = "/" + groupPath;
//        }
//
//        String[] groupPaths = groupPath.split("/");
//
//        name = groupPaths[groupPaths.length - 1];
//
//        LDAPGroup ldapGroup = new LDAPGroup(name, baseDN);
//
//        ldapGroup.setPath(groupPath);
//
//        return populateIdentityTypeEntry(ldapGroup);
//    }
//
//    private LDAPUser lookupUser(String loginName) {
//        return populateIdentityTypeEntry(new LDAPUser(loginName, getConfig().getUserDNSuffix()));
//    }
//
//    private void removeGroupRoleRelationship(SecurityContext context, GroupRole groupRole) {
//        LDAPGroup groupEntry = (LDAPGroup) lookupEntryById(context, groupRole.getGroup());
//        LDAPAgent agentEntry = (LDAPAgent) lookupEntryById(context, groupRole.getAssignee());
//        LDAPRole roleEntry = (LDAPRole) lookupEntryById(context, groupRole.getRole());
//
//        NamingEnumeration<SearchResult> search = null;
//
//        try {
//            search = lookupGroupRoleEntry(agentEntry, groupEntry);
//
//            if (search.hasMore()) {
//                LDAPGroupRole groupRoleEntry = new LDAPGroupRole(agentEntry, groupEntry, roleEntry);
//
//                removeMember(groupRoleEntry, roleEntry);
//
//                String members = groupRoleEntry.getLDAPAttributes().get(MEMBER).get().toString();
//
//                if (members.trim().isEmpty()) {
//                    getLDAPManager().destroySubcontext(search.next().getNameInNamespace());
//                }
//            }
//
//        } catch (NamingException e) {
//            throw MESSAGES.ldapStoreCouldNotRemoveGroupRoleEntry(e);
//        }
//    }
//
//    private void removeGroupMembership(SecurityContext context, GroupMembership groupMembership) {
//        LDAPGroup groupEntry = (LDAPGroup) lookupEntryById(context, groupMembership.getGroup());
//        LDAPAgent agentEntry = (LDAPAgent) lookupEntryById(context, groupMembership.getMember());
//
//        removeMember(groupEntry, agentEntry);
//    }
//
//    private void removeGrantRelationship(SecurityContext context, Grant grant) {
//        LDAPRole roleEntry = (LDAPRole) lookupEntryById(context, grant.getRole());
//        LDAPEntry agentEntry = (LDAPEntry) lookupEntryById(context, grant.getAssignee());
//
//        removeMember(roleEntry, agentEntry);
//    }
//
//    private NamingEnumeration<SearchResult> lookupGroupRoleEntry(LDAPAgent agentEntry, LDAPGroup groupEntry)
//            throws NamingException {
//        return getLDAPManager().search(agentEntry.getDN(), groupEntry.getBidingName());
//    }
//
//    private void removeMember(LDAPEntry parentEntry, LDAPEntry childEntry) {
//        parentEntry.removeMember(childEntry);
//        getLDAPManager().modifyAttribute(parentEntry.getDN(), parentEntry.getLDAPAttributes().get(MEMBER));
//    }
//
//    private Group getGroupById(SecurityContext context, String id) {
//        if (id == null) {
//            return null;
//        }
//
//        LDAPGroup ldapGroup = lookupEntryById(context, LDAPGroup.class, id);
//
//        if (ldapGroup != null) {
//            Group parentGroup = getParentGroup(ldapGroup, false);
//            Group group = null;
//
//            if (parentGroup != null) {
//                group = new Group(ldapGroup.getName(), getGroupById(context, parentGroup.getId()));
//            } else {
//                group = new Group(ldapGroup.getName());
//            }
//
//            populateIdentityType(ldapGroup, group);
//
//            return group;
//        }
//
//        return null;
//    }
//
//    private Group getGroup(String groupPath, String baseDN) {
//        if (groupPath != null) {
//            LDAPGroup ldapGroup = lookupGroup(groupPath, baseDN);
//
//            if (ldapGroup != null) {
//                Group group = new Group(ldapGroup.getName(), getParentGroup(ldapGroup, true));
//
//                populateIdentityType(ldapGroup, group);
//
//                return group;
//            }
//        }
//
//        return null;
//    }
//
//    private boolean isCustomAttributesSupported() {
//        return this.configuration.supportsFeature(FeatureGroup.attribute, null);
//    }


    @Override
    public <I extends IdentityType> I getIdentity(Class<I> identityType, String id) {
        return null;  //TODO: Implement getIdentity
    }

    @Override
    public void setAttribute(IdentityContext context, AttributedType type, Attribute<? extends Serializable> attribute) {
        //TODO: Implement setAttribute
    }

    @Override
    public <V extends Serializable> Attribute<V> getAttribute(IdentityContext context, AttributedType type, String attributeName) {
        return null;  //TODO: Implement getAttribute
    }

    @Override
    public void removeAttribute(IdentityContext context, AttributedType type, String attributeName) {
        //TODO: Implement removeAttribute
    }
}
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

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class LDAPQuery {

//    private IdentityContext context;
//    private List<LDAPQueryParameter> managedParameters = new ArrayList<LDAPQueryParameter>();
//    private Boolean hasCustomAttributes = null;
//    private IdentityQuery<?> identityQuery;
//    private LDAPIdentityStore identityStore;
//    private boolean hasRelationshipParameters;
//
//    public LDAPQuery(IdentityContext context, IdentityQuery<?> identityQuery, LDAPIdentityStore identityStore) {
//        this.context = context;
//        this.identityQuery = identityQuery;
//        this.identityStore = identityStore;
//
//        for (Entry<QueryParameter, Object[]> entry : identityQuery.getParameters().entrySet()) {
//            QueryParameter queryParameter = entry.getKey();
//            Object[] values = entry.getValue();
//
//            LDAPQueryParameter parameter = new LDAPQueryParameter(queryParameter, values);
//
//            if (parameter.isMappedToManagedAttribute()) {
//                this.managedParameters.add(parameter);
//            } else if (parameter.isMembershipParameter()) {
//                this.hasRelationshipParameters = true;
//            } else {
//                this.hasCustomAttributes = true;
//            }
//        }
//    }
//
//    public StringBuffer createManagedAttributesFilter() {
//        if (getManagedParameters().isEmpty()) {
//            return null;
//        }
//
//        StringBuffer filter = new StringBuffer("(&(objectClass=*)");
//
//        for (LDAPQueryParameter ldapQueryParameter : getManagedParameters()) {
//            filter.append(ldapQueryParameter.createFilter());
//        }
//
//        filter.append(")");
//
//        return filter;
//    }
//
//    public String createRelationshipFilter() {
//        StringBuffer relationshipFilter = new StringBuffer();
//
//        if (IDMUtil.isAgentType(identityQuery.getIdentityType())) {
//            relationshipFilter.append(createHasRoleFilter());
//            relationshipFilter.append(createMemberOfFilter());
//            relationshipFilter.append(createGroupRoleFilter());
//        } else if (IDMUtil.isRoleType(identityQuery.getIdentityType())) {
//            relationshipFilter.append(createRoleOfFilter());
//        } else if (IDMUtil.isGroupType(identityQuery.getIdentityType())) {
//            relationshipFilter.append(createHasMemberFilter());
//            relationshipFilter.append(createChildGroupsFilter());
//        }
//
//        return relationshipFilter.toString();
//    }
//
//    public boolean hasCustomAttributes() {
//        return this.hasCustomAttributes != null && this.hasCustomAttributes;
//    }
//
//    public List<LDAPQueryParameter> getManagedParameters() {
//        return this.managedParameters;
//    }
//
//    private String createHasMemberFilter() {
//        StringBuffer parentEntriesFilter = new StringBuffer();
//
//        if (identityQuery.getParameters().containsKey(org.picketlink.idm.model.sample.Group.HAS_MEMBER)) {
//            Object[] values = identityQuery.getParameters().get(org.picketlink.idm.model.sample.Group.HAS_MEMBER);
//            IdentityType[] agents = new IdentityType[values.length];
//
//            for (int j = 0; j < values.length; j++) {
//                Object value = values[j];
//                agents[j] = (IdentityType) value;
//            }
//
//            for (IdentityType identityType : agents) {
//                if (identityType != null) {
//
//                    if (org.picketlink.idm.model.sample.Group.class.isInstance(identityType)) {
//                        parentEntriesFilter.append(createMembersFilter(identityType));
//                    } else if (org.picketlink.idm.model.sample.Agent.class.isInstance(identityType)) {
//                        RelationshipQuery<GroupMembership> query = context.getIdentityManager()
//                                .createRelationshipQuery(GroupMembership.class);
//
//                        query.setParameter(GroupMembership.MEMBER, identityType);
//
//                        List<GroupMembership> result = query.getResultList();
//
//                        for (GroupMembership groupMembership : result) {
////                            LDAPEntry ldapEntry = (LDAPEntry) this.identityStore.lookupEntryById(context, groupMembership.getGroup());
////
////                            parentEntriesFilter.append("(").append(ldapEntry.getBidingName()).append(")");
//                        }
//                    } else {
//                        throw new IdentityManagementException(
//                                "Unsupported type for Group.HAS_MEMBER QueryParameter. You should specify a Agent or Group only.");
//                    }
//                }
//            }
//
//            if (parentEntriesFilter.length() > 0) {
//                parentEntriesFilter.insert(0, "(|");
//                parentEntriesFilter.append(")");
//            }
//        }
//
//        return parentEntriesFilter.toString();
//    }
//
//    private String createRoleOfFilter() {
//        StringBuffer filter = new StringBuffer();
//
//        if (identityQuery.getParameters().containsKey(IdentityType.ROLE_OF)) {
//            Map<String, Integer> userCount = new HashMap<String, Integer>();
//            Object[] values = identityQuery.getParameters().get(IdentityType.ROLE_OF);
//
//            for (Object user : values) {
//                if (org.picketlink.idm.model.sample.Agent.class.isInstance(user) || org.picketlink.idm.model.sample.Group.class.isInstance(user)) {
//                    RelationshipQuery<Grant> query = context.getIdentityManager()
//                            .createRelationshipQuery(Grant.class);
//
//                    query.setParameter(Grant.ASSIGNEE, user);
//
//                    List<Grant> result = query.getResultList();
//
//                    for (Grant grant : result) {
////                        LDAPEntry ldapEntry = (LDAPEntry) this.identityStore.lookupEntryById(context, grant.getRole());
////                        String entryName = ldapEntry.getBidingName();
////
////                        filter.append("(").append(ldapEntry.getBidingName()).append(")");
////
////                        if (!userCount.containsKey(entryName)) {
////                            userCount.put(entryName, 1);
////                        } else {
////                            Integer count = userCount.get(entryName);
////                            userCount.put(entryName, count + 1);
////                        }
//                    }
//                } else {
//                    throw new IdentityManagementException(
//                            "Unsupported type for IdentityType.ROLE_OF QueryParameter. You should specify a Agent or a Group.");
//                }
//            }
//
//            Set<Entry<String, Integer>> entrySet = userCount.entrySet();
//
//            for (Entry<String, Integer> entry : entrySet) {
//                if (entry.getValue() != values.length) {
//                    String filterTmp = filter.toString();
//
//                    filterTmp = filterTmp.replaceAll("\\(" + entry.getKey() + "\\)", "");
//
//                    filter = new StringBuffer(filterTmp);
//                }
//            }
//        }
//
//        if (filter.length() > 0) {
//            filter.insert(0, "(|");
//            filter.append(")");
//        }
//
//        return filter.toString();
//    }
//
//    private String createGroupRoleFilter() {
//        if (identityQuery.getParameters().containsKey(IdentityType.HAS_GROUP_ROLE)) {
//            StringBuffer groupRoleFilter = new StringBuffer();
//
//            NamingEnumeration<SearchResult> search = null;
//
//            try {
//                Object[] groupRoles = identityQuery.getParameters().get(org.picketlink.idm.model.sample.User.HAS_GROUP_ROLE);
//
//                for (Object group : groupRoles) {
//                    GroupRole groupRole = (GroupRole) group;
//
//                    RelationshipQuery<GroupRole> query = context.getIdentityManager()
//                            .createRelationshipQuery(GroupRole.class);
//
//                    query.setParameter(GroupRole.ASSIGNEE, groupRole.getAssignee());
//                    query.setParameter(GroupRole.ROLE, groupRole.getRole());
//                    query.setParameter(GroupRole.GROUP, groupRole.getGroup());
//
//                    List<GroupRole> result = query.getResultList();
//
//                    for (GroupRole relationship : result) {
////                        LDAPEntry ldapEntry = (LDAPEntry) this.identityStore.lookupEntryById(context, relationship.getAssignee());
////
////                        if (ldapEntry == null) {
////                            throw new IdentityManagementException("Relationship references a inexistent IdentityType ["
////                                    + relationship.getAssignee() + "]");
////                        }
////
////                        groupRoleFilter.append("(").append(ldapEntry.getBidingName()).append(")");
//                    }
//                }
//            } catch (Exception e) {
//                throw new IdentityManagementException(e);
//            } finally {
//                if (search != null) {
//                    try {
//                        search.close();
//                    } catch (NamingException e) {
//                    }
//                }
//            }
//
//            return groupRoleFilter.toString();
//        }
//
//        return "";
//    }
//
//    private String createHasRoleFilter() {
//        StringBuffer filter = new StringBuffer();
//
//        if (identityQuery.getParameters().containsKey(org.picketlink.idm.model.sample.User.HAS_ROLE)) {
//            Object[] roles = identityQuery.getParameters().get(org.picketlink.idm.model.sample.User.HAS_ROLE);
//            Map<String, Integer> memberCount = new HashMap<String, Integer>();
//
//            for (Object role : roles) {
//                if (org.picketlink.idm.model.sample.Role.class.isInstance(role)) {
//                    RelationshipQuery<Grant> query = context.getIdentityManager()
//                            .createRelationshipQuery(Grant.class);
//
//                    query.setParameter(Grant.ROLE, role);
//
//                    List<Grant> result = query.getResultList();
//
//                    for (Grant grant : result) {
//// FIXME
////                        LDAPEntry ldapAgent = (LDAPEntry) this.identityStore.lookupEntryById(context, grant.getAssignee());
////                        String bindDN = ldapAgent.getBidingName();
////
////                        filter.append("(").append(bindDN).append(")");
////
////                        if (!memberCount.containsKey(bindDN)) {
////                            memberCount.put(bindDN, 1);
////                        } else {
////                            Integer count = memberCount.get(bindDN);
////                            memberCount.put(bindDN, count + 1);
////                        }
//                    }
//                } else {
//                    throw new IdentityManagementException(
//                            "Unsupported type for User.HAS_ROLE QueryParameter. You should specify a Role type only.");
//                }
//            }
//
//            Set<Entry<String, Integer>> entrySet = memberCount.entrySet();
//
//            for (Entry<String, Integer> entry : entrySet) {
//                if (entry.getValue() != roles.length) {
//                    String filterTmp = filter.toString();
//
//                    filterTmp = filterTmp.replaceAll("\\(" + entry.getKey() + "\\)", "");
//
//                    filter = new StringBuffer(filterTmp);
//                }
//            }
//        }
//
//        if (filter.length() > 0) {
//            filter.insert(0, "(|");
//            filter.append(")");
//        }
//
//        return filter.toString();
//    }
//
//    private String createMemberOfFilter() {
//        StringBuffer filter = new StringBuffer();
//
//        if (identityQuery.getParameters().containsKey(org.picketlink.idm.model.sample.User.MEMBER_OF)) {
//            Map<String, Integer> userCount = new HashMap<String, Integer>();
//
//            Object[] groups = identityQuery.getParameters().get(org.picketlink.idm.model.sample.User.MEMBER_OF);
//
//            for (Object group : groups) {
//                if (org.picketlink.idm.model.sample.Group.class.isInstance(group)) {
//                    RelationshipQuery<GroupMembership> query = context.getIdentityManager()
//                            .createRelationshipQuery(GroupMembership.class);
//
//                    query.setParameter(GroupMembership.GROUP, group);
//
//                    List<GroupMembership> result = query.getResultList();
//
//                    for (GroupMembership groupMembership : result) {
//// FIXME
////                        LDAPEntry ldapAgent = (LDAPEntry) this.identityStore.lookupEntryById(context, groupMembership.getMember());
////                        String userId = ldapAgent.getBidingName();
////
////                        filter.append("(").append(userId).append(")");
////
////                        if (!userCount.containsKey(userId)) {
////                            userCount.put(userId, 1);
////                        } else {
////                            Integer count = userCount.get(userId);
////                            userCount.put(userId, count + 1);
////                        }
//                    }
//                } else {
//                    throw new IdentityManagementException(
//                            "Unsupported type for User.MEMBER_OF QueryParameter. You should specify a Group type.");
//                }
//            }
//
//            Set<Entry<String, Integer>> entrySet = userCount.entrySet();
//
//            for (Entry<String, Integer> entry : entrySet) {
//                if (entry.getValue() != groups.length) {
//                    String filterTmp = filter.toString();
//
//                    filterTmp = filterTmp.replaceAll("\\(" + entry.getKey() + "\\)", "");
//
//                    filter = new StringBuffer(filterTmp);
//                }
//            }
//        }
//
//        if (filter.length() > 0) {
//            filter.insert(0, "(|");
//            filter.append(")");
//        }
//
//        return filter.toString();
//    }
//
//    /**
//     * <p>
//     * Returns a filter where only the specified {@link Agent} are member of.
//     * </p>
//     *
//     * @param members
//     * @param baseDN
//     * @return
//     */
//    private String createMembersFilter(IdentityType identityType) {
//        String membersFilter = "";
//        boolean isGroupMember = false;
//
//        if (identityType != null) {
//// FIXME
////            LDAPEntry ldapEntry = null;
////
////            try {
////                ldapEntry = (LDAPEntry) this.identityStore.lookupEntryById(context, identityType);
////            } catch (IdentityManagementException ime) {
////                return membersFilter;
////            }
////
////            if (ldapEntry != null) {
////                membersFilter = membersFilter + "(member=" + ldapEntry.getDN() + ")";
////            }
////
////            if (org.picketlink.idm.model.Group.class.isInstance(identityType)) {
////                isGroupMember = true;
////            }
//        }
//
//        StringBuffer parentEntriesFilter = new StringBuffer();
//
//        if (membersFilter.length() > 0) {
//            NamingEnumeration<SearchResult> search = null;
//
//            try {
//                search = getLDAPManager().search(getConfig().getBaseDN(), membersFilter.toString());
//
//                while (search.hasMoreElements()) {
//                    SearchResult searchResult = search.next();
//                    String entryCN = searchResult.getAttributes().get(CN).get().toString();
//
//                    parentEntriesFilter.append("(").append(CN).append(LDAPConstants.EQUAL).append(entryCN).append(")");
//
//                    if (isGroupMember) {
//// FIXME
////                        String id = searchResult.getAttributes().get(LDAPConstants.ENTRY_UUID).get().toString();
////                        LDAPGroup childGroup = this.identityStore.lookupEntryById(context, LDAPGroup.class, id);
////                        List<org.picketlink.idm.model.Group> parentGroups = this.identityStore.getParentGroups(context, childGroup);
////
////                        for (org.picketlink.idm.model.Group parentGroup : parentGroups) {
////                            parentEntriesFilter.append("(").append(CN).append(LDAPConstants.EQUAL)
////                                    .append(parentGroup.getName()).append(")");
////                        }
//                    }
//                }
//            } catch (Exception e) {
//                throw new IdentityManagementException(e);
//            } finally {
//                if (search != null) {
//                    try {
//                        search.close();
//                    } catch (NamingException e) {
//                    }
//                }
//            }
//        }
//
//        if (parentEntriesFilter.length() > 0) {
//            parentEntriesFilter.insert(0, "(|");
//            parentEntriesFilter.append(")");
//        }
//
//        return parentEntriesFilter.toString();
//    }
//
//    private String createChildGroupsFilter() {
//        if (identityQuery.getParameters().containsKey(org.picketlink.idm.model.sample.Group.PARENT)) {
//// FIXME
////            String parentName = identityQuery.getParameters().get(org.picketlink.idm.model.Group.PARENT)[0].toString();
////            LDAPGroup parentGroup = this.identityStore.lookupGroup(parentName);
////
////            NamingEnumeration<?> members = null;
////
////            StringBuffer childGroupsFilter = new StringBuffer();
////
////            try {
////                members = parentGroup.getLDAPAttributes().get(MEMBER).getAll();
////
////                while (members.hasMoreElements()) {
////                    String groupDN = (String) members.nextElement();
////
////                    if (groupDN.toString().trim().isEmpty()) {
////                        continue;
////                    }
////
////                    String groupName = groupDN.split(",")[0];
////
////                    childGroupsFilter.append("(").append(groupName).append(")");
////                }
////            } catch (NamingException e) {
////                throw new IdentityManagementException(e);
////            } finally {
////                if (members != null) {
////                    try {
////                        members.close();
////                    } catch (NamingException e) {
////                    }
////                }
////            }
////
////            return childGroupsFilter.toString();
//        }
//
//        return "";
//    }
//
//    private LDAPIdentityStoreConfiguration getConfig() {
//        return this.identityStore.getConfig();
//    }
//
//    private LDAPOperationManager getLDAPManager() {
//        return null;
//// FIXME
////        return this.identityStore.getLDAPManager();
//    }
//
//    public boolean hasRelationshipParameters() {
//        return this.hasRelationshipParameters;
//    }
}

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
import static org.picketlink.idm.ldap.internal.LDAPConstants.MEMBER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchResult;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.internal.util.IDMUtil;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class LDAPQuery {

    private List<LDAPQueryParameter> managedParameters = new ArrayList<LDAPQueryParameter>();
    private Boolean hasCustomAttributes = null;
    private IdentityQuery<?> identityQuery;
    private LDAPIdentityStore identityStore;
    private boolean hasRelationshipParameters;

    public LDAPQuery(IdentityQuery<?> identityQuery, LDAPIdentityStore identityStore) {
        this.identityQuery = identityQuery;
        this.identityStore = identityStore;

        for (Entry<QueryParameter, Object[]> entry : identityQuery.getParameters().entrySet()) {
            QueryParameter queryParameter = entry.getKey();
            Object[] values = entry.getValue();

            LDAPQueryParameter parameter = new LDAPQueryParameter(queryParameter, values);

            if (parameter.isMappedToManagedAttribute()) {
                this.managedParameters.add(parameter);
            } else if (parameter.isMembershipParameter()) {
                this.hasRelationshipParameters = true;
            } else {
                this.hasCustomAttributes = true;
            }
        }
    }

    public StringBuffer createManagedAttributesFilter() {
        if (getManagedParameters().isEmpty()) {
            return null;
        }

        StringBuffer filter = new StringBuffer("(&(objectClass=*)");

        for (LDAPQueryParameter ldapQueryParameter : getManagedParameters()) {
            filter.append(ldapQueryParameter.createFilter());
        }

        filter.append(")");

        return filter;
    }

    public String createRelationshipFilter() {
        StringBuffer relationshipFilter = new StringBuffer();

        if (IDMUtil.isAgentType(identityQuery.getIdentityType())) {
            relationshipFilter.append(createHasRoleFilter());
            relationshipFilter.append(createMemberOfFilter());
            relationshipFilter.append(createGroupRoleFilter());
        } else if (IDMUtil.isRoleType(identityQuery.getIdentityType())) {
            relationshipFilter.append(createRoleOfFilter());
        } else if (IDMUtil.isGroupType(identityQuery.getIdentityType())) {
            relationshipFilter.append(createHasMemberFilter());
            relationshipFilter.append(createChildGroupsFilter());
        }

        return relationshipFilter.toString();
    }

    public boolean hasCustomAttributes() {
        return this.hasCustomAttributes != null && this.hasCustomAttributes;
    }

    public List<LDAPQueryParameter> getManagedParameters() {
        return this.managedParameters;
    }

    private String createHasMemberFilter() {
        if (identityQuery.getParameters().containsKey(Group.HAS_MEMBER)) {
            Object[] values = identityQuery.getParameters().get(Group.HAS_MEMBER);
            Agent[] agents = new Agent[values.length];

            for (int j = 0; j < values.length; j++) {
                Object value = values[j];
                agents[j] = (Agent) value;
            }

            return createMembersFilter(agents, getConfig().getGroupDNSuffix());
        }

        return "";
    }

    private String createRoleOfFilter() {
        if (identityQuery.getParameters().containsKey(User.ROLE_OF)) {
            Object[] values = identityQuery.getParameters().get(Role.ROLE_OF);
            Agent[] agents = new Agent[values.length];

            for (int j = 0; j < values.length; j++) {
                Object value = values[j];
                agents[j] = (Agent) value;
            }

            return createMembersFilter(agents, getConfig().getRoleDNSuffix());
        }
        return "";
    }

    private String createGroupRoleFilter() {
        if (identityQuery.getParameters().containsKey(IdentityType.HAS_GROUP_ROLE)) {
            StringBuffer groupRoleFilter = new StringBuffer();

            NamingEnumeration<SearchResult> search = null;

            try {
                Object[] groupRoles = identityQuery.getParameters().get(User.HAS_GROUP_ROLE);

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
                                    + getConfig().getRoleDNSuffix())) {
                                groupRoleFilter.append("(").append(userId).append(")");
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

            return groupRoleFilter.toString();
        }

        return "";
    }

    private String createHasRoleFilter() {
        if (identityQuery.getParameters().containsKey(User.HAS_ROLE)) {
            Object[] roleNames = identityQuery.getParameters().get(User.HAS_ROLE);
            LDAPEntry[] roles = new LDAPEntry[roleNames.length];

            for (int i = 0; i < roleNames.length; i++) {
                String name = (String) roleNames[i];

                if (name != null) {
                    roles[i] = this.identityStore.lookupRole(name);
                }
            }

            return createParentMembersFilter(roles);
        }

        return "";
    }

    private String createMemberOfFilter() {
        if (identityQuery.getParameters().containsKey(User.MEMBER_OF)) {
            Object[] groupNames = identityQuery.getParameters().get(User.MEMBER_OF);
            LDAPEntry[] groups = new LDAPEntry[groupNames.length];

            for (int i = 0; i < groupNames.length; i++) {
                String name = (String) groupNames[i];

                if (name != null) {
                    groups[i] = this.identityStore.lookupGroup(name);
                }
            }

            return createParentMembersFilter(groups);
        }

        return "";
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
        String membersFilter = "";

        for (Agent agent : members) {
            if (agent != null) {
                if (Agent.class.isInstance(agent)) {
                    LDAPAgent ldapAgent = this.identityStore.lookupAgent(agent.getLoginName());

                    if (ldapAgent != null) {
                        membersFilter = membersFilter + "(member=" + ldapAgent.getDN() + ")";
                    }
                }
            }
        }

        StringBuffer parentEntriesFilter = new StringBuffer();

        if (membersFilter.length() > 0) {
            NamingEnumeration<SearchResult> search = null;

            try {
                search = getLDAPManager().search(baseDN, membersFilter.toString());

                while (search.hasMoreElements()) {
                    SearchResult searchResult = search.next();
                    String entryCN = searchResult.getAttributes().get(CN).get().toString();

                    parentEntriesFilter.append("(").append(CN).append(LDAPConstants.EQUAL).append(entryCN).append(")");
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

        if (parentEntriesFilter.length() > 0) {
            parentEntriesFilter.insert(0, "(|");
            parentEntriesFilter.append(")");
        }

        return parentEntriesFilter.toString();
    }

    private String createParentMembersFilter(LDAPEntry[] parents) {
        StringBuffer additionalFilter = new StringBuffer();

        Map<String, Integer> userCount = new HashMap<String, Integer>();

        for (LDAPEntry ldapEntry : parents) {
            javax.naming.directory.Attribute memberAttribute = null;

            if (ldapEntry != null) {
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

    private String createChildGroupsFilter() {
        if (identityQuery.getParameters().containsKey(Group.PARENT)) {
            String parentName = identityQuery.getParameters().get(Group.PARENT)[0].toString();
            LDAPGroup parentGroup = this.identityStore.lookupGroup(parentName);

            NamingEnumeration<?> members = null;

            StringBuffer childGroupsFilter = new StringBuffer();

            try {
                members = parentGroup.getLDAPAttributes().get(MEMBER).getAll();

                while (members.hasMoreElements()) {
                    String groupDN = (String) members.nextElement();

                    if (groupDN.toString().trim().isEmpty()) {
                        continue;
                    }

                    String groupName = groupDN.split(",")[0];

                    childGroupsFilter.append("(").append(groupName).append(")");
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

            return childGroupsFilter.toString();
        }

        return "";
    }

    private LDAPIdentityStoreConfiguration getConfig() {
        return this.identityStore.getConfig();
    }

    private LDAPOperationManager getLDAPManager() {
        return getConfig().getLdapManager();
    }

    public boolean hasRelationshipParameters() {
        return this.hasRelationshipParameters;
    }
}

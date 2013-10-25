package org.picketlink.test.idm.usecases;

import org.junit.Test;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.GroupMembership;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.LDAPUserGroupJPARoleConfigurationTester;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Pedro Igor
 */
@Configuration (include = LDAPUserGroupJPARoleConfigurationTester.class)
public class LDAPUserGroupMembershipJPARoleTestCase extends AbstractPartitionManagerTestCase {

    public LDAPUserGroupMembershipJPARoleTestCase(final IdentityConfigurationTester visitor) {
        super(visitor);
    }

    @Test
    public void testGrantGroupRole() {
        Group group = createGroup("someGroup");
        Role role = createRole("someRole");

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.add(new Grant(group, role));

        List<Grant> result = relationshipManager
                .createRelationshipQuery(Grant.class)
                .setParameter(Grant.ASSIGNEE, group)
                .getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());

        Grant relationship = result.get(0);

        assertEquals(group.getId(), relationship.getAssignee().getId());
        assertEquals(role.getId(), relationship.getRole().getId());
    }

    @Test
    public void testAddUserToGroup() {
        User user = createUser("john");
        Group group = createGroup("someGroup");

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.add(new GroupMembership(user, group));

        List<GroupMembership> result = relationshipManager
                .createRelationshipQuery(GroupMembership.class)
                .setParameter(GroupMembership.GROUP, group)
                .getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());

        GroupMembership relationship = result.get(0);

        assertEquals(group.getId(), relationship.getGroup().getId());
        assertEquals(user.getId(), relationship.getMember().getId());
    }

    @Test
    public void testGrantRoleToUserGroup() {
        Role role = createRole("someRole");
        Group group = createGroup("someGroup");

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.add(new Grant(group, role));

        List<Grant> grants = relationshipManager
                .createRelationshipQuery(Grant.class)
                .setParameter(Grant.ASSIGNEE, group)
                .getResultList();

        assertFalse(grants.isEmpty());
        assertEquals(1, grants.size());

        Grant grant = grants.get(0);

        assertEquals(group.getId(), grant.getAssignee().getId());
        assertEquals(role.getId(), grant.getRole().getId());

        User user = createUser("john");

        relationshipManager.add(new GroupMembership(user, group));

        List<GroupMembership> memberships = relationshipManager
                .createRelationshipQuery(GroupMembership.class)
                .setParameter(GroupMembership.GROUP, group)
                .getResultList();

        assertFalse(memberships.isEmpty());
        assertEquals(1, memberships.size());

        GroupMembership membership = memberships.get(0);

        assertEquals(group.getId(), membership.getGroup().getId());
        assertEquals(user.getId(), membership.getMember().getId());

        memberships = relationshipManager
                .createRelationshipQuery(GroupMembership.class)
                .setParameter(GroupMembership.MEMBER, user)
                .getResultList();

        assertFalse(memberships.isEmpty());
        assertEquals(1, memberships.size());

        membership = memberships.get(0);

        assertEquals(group.getId(), membership.getGroup().getId());
        assertEquals(user.getId(), membership.getMember().getId());

        grants = relationshipManager
                .createRelationshipQuery(Grant.class)
                .setParameter(Grant.ASSIGNEE, membership.getGroup())
                .getResultList();

        assertFalse(grants.isEmpty());
        assertEquals(1, grants.size());

        grant = grants.get(0);

        assertEquals(group.getId(), grant.getAssignee().getId());
        assertEquals(role.getId(), grant.getRole().getId());
    }

}

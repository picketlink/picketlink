package org.picketlink.test.idm.performance;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.GroupMembership;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.LDAPJPAPerformanceConfigurationTester;

import java.util.List;

import static org.junit.Assert.assertFalse;

/**
 * @author Pedro Igor
 */
@Configuration (include = LDAPJPAPerformanceConfigurationTester.class)
public class LDAPJPAPerformanceTestCase extends AbstractPartitionManagerTestCase {

    public LDAPJPAPerformanceTestCase(final IdentityConfigurationTester visitor) {
        super(visitor);
    }

    @Test
    public void testUserQueries() {
        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<User> query = identityManager.createIdentityQuery(User.class);

        query.setParameter(User.LOGIN_NAME, "newuser1");

        assertFalse(query.getResultList().isEmpty());
    }

    @Test
    public void testGroupQueries() {
        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Group> query = identityManager.createIdentityQuery(Group.class);

        query.setParameter(Group.NAME, "users1");

        assertFalse(query.getResultList().isEmpty());
    }

    @Test
    public void testGroupMembershipByGroupQueries() {
        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Group> query = identityManager.createIdentityQuery(Group.class);

        query.setParameter(Group.NAME, "users1");

        List<Group> result = query.getResultList();

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        for (Group group: result) {
            RelationshipQuery<GroupMembership> relationshipQuery = relationshipManager.createRelationshipQuery(GroupMembership.class);

            relationshipQuery.setParameter(GroupMembership.GROUP, group);

            assertFalse(relationshipQuery.getResultList().isEmpty());
        }
    }

    @Test
    public void testGroupMembershipByMemberQueries() {
        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<User> query = identityManager.createIdentityQuery(User.class);

        query.setParameter(User.LOGIN_NAME, "newuser1");

        List<User> result = query.getResultList();

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        for (User user: result) {
            RelationshipQuery<GroupMembership> relationshipQuery = relationshipManager.createRelationshipQuery(GroupMembership.class);

            relationshipQuery.setParameter(GroupMembership.MEMBER, user);

            assertFalse(relationshipQuery.getResultList().isEmpty());
        }
    }

}

package org.picketlink.test.idm.config;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.User;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import static org.junit.Assert.*;
import static org.picketlink.idm.model.sample.SampleModel.*;

/**
 * @author Pedro Igor
 */
@Singleton
@Startup
public class IDMInitializer {

    @Inject
    private PartitionManager partitionManager;

    @PostConstruct
    public void init() {
        IdentityManager identityManager = this.partitionManager.createIdentityManager();
        RelationshipManager relationshipManager = this.partitionManager.createRelationshipManager();

        User john = new User("john");

        identityManager.add(john);

        Role tester = new Role("Tester");

        identityManager.add(tester);

        Group qaGroup = new Group("QA");

        identityManager.add(qaGroup);

        grantRole(relationshipManager, john, tester);
        addToGroup(relationshipManager, john, qaGroup);
        grantGroupRole(relationshipManager, john, tester, qaGroup);

        assertTrue(hasRole(relationshipManager, john, tester));
        assertTrue(isMember(relationshipManager, john, qaGroup));
        assertTrue(hasGroupRole(relationshipManager, john, tester, qaGroup));
    }

}

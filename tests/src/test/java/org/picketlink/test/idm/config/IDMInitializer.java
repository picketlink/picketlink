package org.picketlink.test.idm.config;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import static org.junit.Assert.assertTrue;
import static org.picketlink.idm.model.basic.BasicModel.addToGroup;
import static org.picketlink.idm.model.basic.BasicModel.grantGroupRole;
import static org.picketlink.idm.model.basic.BasicModel.grantRole;
import static org.picketlink.idm.model.basic.BasicModel.hasGroupRole;
import static org.picketlink.idm.model.basic.BasicModel.hasRole;
import static org.picketlink.idm.model.basic.BasicModel.isMember;

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

        Password password = new Password(john.getLoginName());

        identityManager.updateCredential(john, password);

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

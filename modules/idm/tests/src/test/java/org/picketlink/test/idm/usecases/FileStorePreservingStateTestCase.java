package org.picketlink.test.idm.usecases;

import org.junit.Test;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.User;

import java.io.Serializable;

import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA. User: pedroigor Date: 8/6/13 Time: 7:32 PM To change this template use File | Settings |
 * File Templates.
 */
public class FileStorePreservingStateTestCase {

    public static final String REALM_A = "Realm A";
    public static final String REALM_B = "Realm B";
    public static final String REALM_C = "Realm C";

    @Test
    public void testPreserveState() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("file-store-preserve-state")
                .stores()
                    .file()
                        .workingDirectory("/tmp/teste")
                        .supportAllFeatures();

        PartitionManager partitionManager = new DefaultPartitionManager(builder.buildAll());

        Realm realmA = new Realm(REALM_A);

        partitionManager.add(realmA);

        Realm realmB = new Realm(REALM_B);

        partitionManager.add(realmB);

        Realm realmC = new Realm(REALM_C);

        partitionManager.add(realmC);

        User userA = new User("User Realm A");

        partitionManager.createIdentityManager(realmA).add(userA);

        User userB = new User("User Realm B");

        partitionManager.createIdentityManager(realmB).add(userB);

        User userC = new User("User Realm C");

        partitionManager.createIdentityManager(realmC).add(userC);

        builder = new IdentityConfigurationBuilder();

        builder
            .named("file-store-preserve-state")
                .stores()
                    .file()
                        .preserveState(true)
                        .workingDirectory("/tmp/teste")
                        .supportAllFeatures();

        partitionManager = new DefaultPartitionManager(builder.buildAll());

        Realm storedRealmA = partitionManager.getPartition(Realm.class, REALM_A);
        Realm storedRealmB = partitionManager.getPartition(Realm.class, REALM_B);
        Realm storedRealmC = partitionManager.getPartition(Realm.class, REALM_C);

        assertEquals(realmA.getId(), storedRealmA.getId());
        assertEquals(realmB.getId(), storedRealmB.getId());
        assertEquals(realmC.getId(), storedRealmC.getId());

        storedRealmA.setAttribute(new Attribute<Serializable>("someAttribute", "1"));
        storedRealmB.setAttribute(new Attribute<Serializable>("someAttribute", "2"));
        storedRealmC.setAttribute(new Attribute<Serializable>("someAttribute", "3"));

        partitionManager.update(storedRealmA);
        partitionManager.update(storedRealmB);
        partitionManager.update(storedRealmC);

        builder = new IdentityConfigurationBuilder();

        builder
            .named("file-store-preserve-state")
                .stores()
                    .file()
                        .preserveState(true)
                        .workingDirectory("/tmp/teste")
                        .supportAllFeatures();

        partitionManager = new DefaultPartitionManager(builder.buildAll());

        storedRealmA = partitionManager.getPartition(Realm.class, REALM_A);
        storedRealmB = partitionManager.getPartition(Realm.class, REALM_B);
        storedRealmC = partitionManager.getPartition(Realm.class, REALM_C);

        assertEquals(storedRealmA.getAttribute("someAttribute").getValue(), "1");
        assertEquals(storedRealmB.getAttribute("someAttribute").getValue(), "2");
        assertEquals(storedRealmC.getAttribute("someAttribute").getValue(), "3");

        User storedUserA = partitionManager.createIdentityManager(storedRealmA).createIdentityQuery(User.class)
                .setParameter(User.LOGIN_NAME, "User Realm A").getResultList().get(0);
        User storedUserB = partitionManager.createIdentityManager(storedRealmB).createIdentityQuery(User.class)
                .setParameter(User.LOGIN_NAME, "User Realm B").getResultList().get(0);
        User storedUserC = partitionManager.createIdentityManager(storedRealmC).createIdentityQuery(User.class)
                .setParameter(User.LOGIN_NAME, "User Realm C").getResultList().get(0);

        assertEquals(userA.getId(), storedUserA.getId());
        assertEquals(userB.getId(), storedUserB.getId());
        assertEquals(userC.getId(), storedUserC.getId());

        storedUserA.setAttribute(new Attribute<Serializable>("userAttribute", "1"));
        storedUserB.setAttribute(new Attribute<Serializable>("userAttribute", "2"));
        storedUserC.setAttribute(new Attribute<Serializable>("userAttribute", "3"));

        partitionManager.createIdentityManager(storedRealmA).update(storedUserA);
        partitionManager.createIdentityManager(storedRealmB).update(storedUserB);
        partitionManager.createIdentityManager(storedRealmC).update(storedUserC);

        storedUserA = partitionManager.createIdentityManager(storedRealmA).createIdentityQuery(User.class)
                .setParameter(User.LOGIN_NAME, "User Realm A").getResultList().get(0);
        storedUserB = partitionManager.createIdentityManager(storedRealmB).createIdentityQuery(User.class)
                .setParameter(User.LOGIN_NAME, "User Realm B").getResultList().get(0);
        storedUserC = partitionManager.createIdentityManager(storedRealmC).createIdentityQuery(User.class)
                .setParameter(User.LOGIN_NAME, "User Realm C").getResultList().get(0);

        assertEquals(storedUserA.getAttribute("userAttribute").getValue(), "1");
        assertEquals(storedUserB.getAttribute("userAttribute").getValue(), "2");
        assertEquals(storedUserC.getAttribute("userAttribute").getValue(), "3");
    }

}

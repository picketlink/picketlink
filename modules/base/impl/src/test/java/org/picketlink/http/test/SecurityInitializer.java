/*
 ** JBoss, Home of Professional Open Source
 ** Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 ** contributors by the @authors tag. See the copyright.txt in the
 ** distribution for a full listing of individual contributors.
 **
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 ** http://www.apache.org/licenses/LICENSE-2.0
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 **/
package org.picketlink.http.test;

import org.picketlink.event.PartitionManagerCreateEvent;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.credential.Digest;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.GroupMembership;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.http.test.model.Acme;

import javax.enterprise.event.Observes;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * <p>This class is only necessary in order to initialize the underlying identity stores with some default data during the application startup.
 * In real world applications, you'll prefer to provide a specific page for user registration and role and group management.</p>
 *
 * <p>We are defining this bean as an EJB only because we may need to deal with transactions to actually persist data to the JPA identity store,
 * if it is being used by your project. You're not required to use EJB to persist identity data using PicketLink. However you should make sure
 * you're handling transactions properly if you are using JPA.</p>
 */
public class SecurityInitializer {

    private PartitionManager partitionManager;

    public void initIdentityStore(@Observes PartitionManagerCreateEvent event) {
        this.partitionManager = event.getPartitionManager();

        configureDefaultPartition();
        configureAcmePartition();
    }

    private void configureAcmePartition() {
        Acme acme = new Acme("Acme");

        addPartition(acme);

        User defaultUser = new User("manu");

        addUser(defaultUser, acme);
        addUser(new User("jbid test"), acme);

        Role managerRole = new Role("Manager");

        // creates a generic role representing users
        addRole(managerRole, acme);

        Group administratorGroup = new Group("Administrators");

        // creates an administrator group
        addGroup(administratorGroup, acme);

        // grant a role to an user
        grantRole(defaultUser, managerRole);

        // add an user as a member of a group
        addMember(defaultUser, administratorGroup);
    }

    private void configureDefaultPartition() {
        Realm realm = new Realm(Realm.DEFAULT_REALM);

        addPartition(realm);

        User defaultUser = new User("picketlink");

        addUser(defaultUser, realm);
        addUser(new User("jbid test"), realm);

        Role managerRole = new Role("Manager");

        // creates a generic role representing users
        addRole(managerRole, realm);

        Group administratorGroup = new Group("Administrators");

        // creates an administrator group
        addGroup(administratorGroup, realm);

        // grant a role to an user
        grantRole(defaultUser, managerRole);

        // add an user as a member of a group
        addMember(defaultUser, administratorGroup);
    }

    public void addPartition(Partition partition) {
        if (this.partitionManager.getPartition(partition.getClass(), partition.getName()) == null) {
            this.partitionManager.add(partition);
        }
    }

    /**
     * <p>This is a very simple example on how to create users and query them using the PicketLink IDM API. In this case, we only
     * create an user if he is not persisted already.</p>
     *
     * @param user
     * @param partition
     */
    public void addUser(User user, Partition partition) {
        IdentityManager identityManager = getIdentityManager(partition);
        IdentityQuery<User> query = identityManager.createIdentityQuery(User.class);

        query.setParameter(User.LOGIN_NAME, user.getLoginName());

        List<User> result = query.getResultList();

        if (!result.isEmpty()) {
            identityManager.remove(result.get(0));
        }

        identityManager.add(user);
        identityManager.updateCredential(user, new Password(user.getLoginName()));

        Digest digest = new Digest();

        digest.setRealm("PicketLink Test DIGEST Realm");
        digest.setUsername(user.getLoginName());
        digest.setPassword(user.getLoginName());

        identityManager.updateCredential(user, digest);

        X509Certificate certificate = getTestingCertificate(getClass().getClassLoader());

        identityManager.updateCredential(user, certificate);
    }

    /**
     * <p>This is a very simple example on how to create roles and query them using the PicketLink IDM API. In this case, we only
     * create a role if it is not persisted already.</p>
     *
     * @param role
     * @param partition
     */
    public void addRole(Role role, Partition partition) {
        IdentityManager identityManager = getIdentityManager(partition);
        IdentityQuery<Role> query = identityManager.createIdentityQuery(Role.class);

        query.setParameter(Role.NAME, role.getName());

        List<Role> result = query.getResultList();

        if (!result.isEmpty()) {
            identityManager.remove(result.get(0));
        }

        identityManager.add(role);
    }

    /**
     * <p>This is a very simple example on how to create groups and query them using the PicketLink IDM API. In this case, we only
     * create a group if it is not persisted already.</p>
     *
     * @param group
     * @param partition
     */
    public void addGroup(Group group, Partition partition) {
        IdentityManager identityManager = getIdentityManager(partition);
        IdentityQuery<Group> query = identityManager.createIdentityQuery(Group.class);

        query.setParameter(Group.NAME, group.getName());

        List<Group> result = query.getResultList();

        if (!result.isEmpty()) {
            identityManager.remove(result.get(0));
        }

        identityManager.add(group);
    }

    /**
     * <p>This is a very simple example on how to grant a role to an user.</p>
     *  @param user
     * @param role
     */
    public void grantRole(User user, Role role) {
        RelationshipManager relationshipManager = getRelationshipManager();

        relationshipManager.add(new Grant(user, role));
    }

    /**
     * <p>This is a very simple example on how to add an user as a member of a group.</p>
     *  @param user
     * @param group
     */
    public void addMember(User user, Group group) {
        RelationshipManager relationshipManager = getRelationshipManager();

        relationshipManager.add(new GroupMembership(user, group));
    }

    private IdentityManager getIdentityManager(Partition partition) {
        return this.partitionManager.createIdentityManager(partition);
    }

    private RelationshipManager getRelationshipManager() {
        return this.partitionManager.createRelationshipManager();
    }

    private X509Certificate getTestingCertificate(ClassLoader classLoader) {
        InputStream bis = classLoader.getResourceAsStream("cert/servercert.txt");
        X509Certificate cert = null;

        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) cf.generateCertificate(bis);
        } catch (Exception e) {
            throw new IllegalStateException("Could not load testing certificate.", e);
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                }
            }
        }
        return cert;
    }

}
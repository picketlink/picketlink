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

package org.picketlink.test.integration.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.picketlink.Identity;
import org.picketlink.annotations.PicketLink;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.internal.IdentityManagerFactory;
import org.picketlink.idm.model.sample.Realm;
import org.picketlink.idm.model.sample.User;
import org.picketlink.test.integration.AbstractArquillianTestCase;
import org.picketlink.test.integration.ArchiveUtils;

/**
 * <p>
 * Perform some tests against an IDM configuration with multiple realms configured.
 * </p>
 * <p>
 * We test a scenario where the same user exists in different realms with different credentials. Without using multiple realms,
 * adding the same user to a realm (with the same loginName) is not possible.
 * </p>
 * 
 * @author Pedro Igor
 * 
 */
public class MultiRealmAuthenticationTestCase extends AbstractArquillianTestCase {

    private static final String USER_NAME = "john";
    private static final String STAGING_REALM_NAME = "Staging";
    private static final String TESTING_REALM_NAME = "Testing";

    @Inject
    private RealmSelector realmSelector;

    @Inject
    private Instance<IdentityManager> identityManagerInstance;
    
    @Inject
    private IdentityManagerFactory identityManagerFactory;

    @Inject
    private DefaultLoginCredentials credentials;

    @Inject
    private Identity identity;

    @Deployment
    public static WebArchive createDeployment() {
        return ArchiveUtils.create(MultiRealmAuthenticationTestCase.class, Resources.class, RealmSelector.class);
    }

    @Before
    public void onFinish() {
        this.identity.logout();
    }

    @Test
    @InSequence(1)
    public void testIdentityManagerForDefaultRealm() throws Exception {
        User user = new User(USER_NAME);

        IdentityManager identityManager = this.identityManagerInstance.get();

        identityManager.add(user);

        assertEquals(Realm.DEFAULT_REALM, user.getPartition().getId());

        assertLogin(user, identityManager);
    }

    @Test
    @InSequence(2)
    public void testIdentityManagerForStagingRealm() throws Exception {
        this.realmSelector.setRealmName(STAGING_REALM_NAME);

        User user = new User(USER_NAME);

        IdentityManager identityManager = this.identityManagerInstance.get();

        identityManager.add(user);

        assertEquals(STAGING_REALM_NAME, user.getPartition().getId());

        assertLogin(user, identityManager);
    }

    @Test
    @InSequence(3)
    public void testIdentityManagerForTestingRealm() throws Exception {
        this.realmSelector.setRealmName(TESTING_REALM_NAME);

        User user = new User(USER_NAME);

        IdentityManager identityManager = this.identityManagerInstance.get();

        identityManager.add(user);

        assertEquals(TESTING_REALM_NAME, user.getPartition().getId());

        assertLogin(user, identityManager);
    }

    @Test
    @InSequence(4)
    public void testLoginAttemptFromDifferentRealm() throws Exception {
        this.realmSelector.setRealmName(TESTING_REALM_NAME);

        IdentityManager identityManager = this.identityManagerInstance.get();

        User user = identityManager.getUser(USER_NAME);

        assertEquals(TESTING_REALM_NAME, user.getPartition().getId());

        this.credentials.setUserId(user.getLoginName());
        this.credentials.setPassword(buildUserPassword(this.identityManagerFactory.getRealm(Realm.DEFAULT_REALM)));

        this.identity.login();

        // should fail. The provided password is configured for john when using the default realm.
        assertFalse(this.identity.isLoggedIn());

        this.credentials.setPassword(buildUserPassword(this.identityManagerFactory.getRealm(TESTING_REALM_NAME)));

        this.identity.login();

        // correct credentials.
        assertTrue(this.identity.isLoggedIn());
    }

    private void assertLogin(User user, IdentityManager identityManager) {
        Realm userRealm = (Realm) user.getPartition();
        Password password = new Password(buildUserPassword(userRealm));

        identityManager.updateCredential(user, password);

        this.credentials.setUserId(user.getLoginName());
        this.credentials.setPassword(String.valueOf(password.getValue()));

        this.identity.login();

        assertTrue(this.identity.isLoggedIn());
    }

    /**
     * <p>User's password is a concatenation of loginName + Partition.id.</p>
     * 
     * @param user
     * @return
     */
    private String buildUserPassword(Realm realm) {
        return USER_NAME + realm.getId();
    }

    @RequestScoped
    public static class RealmSelector {

        @Inject
        private IdentityManagerFactory identityManagerFactory;

        private String realmName;

        @Produces
        @PicketLink
        public Realm select() {
            if (this.realmName == null) {
                this.realmName = Realm.DEFAULT_REALM;
            }

            return this.identityManagerFactory.getRealm(this.realmName);
        }

        void setRealmName(String realmName) {
            this.realmName = realmName;
        }
    }

    @ApplicationScoped
    public static class Resources {

        @Produces
        public IdentityConfiguration buildIDMConfiguration() {
            IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

            builder.stores().file().addRealm(Realm.DEFAULT_REALM, TESTING_REALM_NAME, STAGING_REALM_NAME).supportAllFeatures();

            return builder.build();
        }

    }
}

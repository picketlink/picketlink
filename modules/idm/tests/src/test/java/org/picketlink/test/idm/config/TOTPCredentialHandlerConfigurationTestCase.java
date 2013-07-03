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

package org.picketlink.test.idm.config;

import java.util.Calendar;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.TOTPCredential;
import org.picketlink.idm.credential.TOTPCredentials;
import org.picketlink.idm.credential.totp.TimeBasedOTP;
import org.picketlink.idm.jpa.internal.JPAContextInitializer;
import org.picketlink.idm.jpa.schema.CredentialObject;
import org.picketlink.idm.jpa.schema.CredentialObjectAttribute;
import org.picketlink.idm.jpa.schema.IdentityObject;
import org.picketlink.idm.jpa.schema.IdentityObjectAttribute;
import org.picketlink.idm.jpa.schema.PartitionObject;
import org.picketlink.idm.jpa.schema.RelationshipIdentityObject;
import org.picketlink.idm.jpa.schema.RelationshipObject;
import org.picketlink.idm.jpa.schema.RelationshipObjectAttribute;
import org.picketlink.idm.model.sample.User;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.picketlink.idm.credential.internal.TOTPCredentialHandler.DELAY_WINDOW;
import static org.picketlink.idm.credential.internal.TOTPCredentialHandler.INTERVAL_SECONDS;
import static org.picketlink.idm.credential.internal.TOTPCredentialHandler.NUMBER_DIGITS;
import static org.picketlink.idm.credential.totp.TimeBasedOTP.DEFAULT_ALGORITHM;
import static org.picketlink.idm.credential.totp.TimeBasedOTP.DEFAULT_DELAY_WINDOW;
import static org.picketlink.idm.credential.totp.TimeBasedOTP.DEFAULT_INTERVAL_SECONDS;
import static org.picketlink.idm.credential.totp.TimeBasedOTP.DEFAULT_NUMBER_DIGITS;

/**
 * <p>Some tests for the configuration of the encoding when using the {@link org.picketlink.idm.credential.internal.PasswordCredentialHandler}.</p>
 *
 * @author Pedro Silva
 */
public class TOTPCredentialHandlerConfigurationTestCase {

    private static final String USER_TOTP_SECRET = "my_secret";
    private static final String USER_PASSWORD = "passwd";
    private static final String USER_NAME = "user";

    private EntityManagerFactory emf;
    private EntityManager entityManager;

    @Before
    public void onInit() {
        this.emf = Persistence.createEntityManagerFactory("jpa-identity-store-tests-pu");
        this.entityManager = emf.createEntityManager();
        this.entityManager.getTransaction().begin();
    }

    @After
    public void onDestroy() {
        this.entityManager.getTransaction().commit();
        this.entityManager.close();
        this.emf.close();
    }

    @Test
    public void testNoDelayWindow() throws Exception {
        IdentityManager identityManager = createIdentityManager(DELAY_WINDOW, 0);

        TOTPCredentials credentials = new TOTPCredentials();

        credentials.setUsername(USER_NAME);
        credentials.setPassword(new Password(USER_PASSWORD));

        TimeBasedOTP totp = new TimeBasedOTP();

        String token = totp.generate(USER_TOTP_SECRET);

        credentials.setToken(token);

        identityManager.validateCredentials(credentials);

        assertEquals(Status.VALID, credentials.getStatus());

        Calendar calendar = Calendar.getInstance();

        // let's delay 30 seconds.
        calendar.add(Calendar.SECOND, -30);

        totp.setCalendar(calendar);

        token = totp.generate(USER_TOTP_SECRET);

        credentials.setToken(token);

        identityManager.validateCredentials(credentials);

        assertEquals(Status.INVALID, credentials.getStatus());
    }

    @Test
    public void testNumberDigits() throws Exception {
        int numberDigits = 8;

        IdentityManager identityManager = createIdentityManager(NUMBER_DIGITS, numberDigits);

        TOTPCredentials credentials = new TOTPCredentials();

        credentials.setUsername(USER_NAME);
        credentials.setPassword(new Password(USER_PASSWORD));

        TimeBasedOTP totp = new TimeBasedOTP(DEFAULT_ALGORITHM, numberDigits, DEFAULT_INTERVAL_SECONDS, DEFAULT_DELAY_WINDOW);

        String token = totp.generate(USER_TOTP_SECRET);

        assertEquals(numberDigits, token.length());

        credentials.setToken(token);

        identityManager.validateCredentials(credentials);

        assertEquals(Status.VALID, credentials.getStatus());
    }

    @Test
    public void testInterval() throws Exception {
        int timeIntervalInSeconds = 10;

        IdentityManager identityManager = createIdentityManager(INTERVAL_SECONDS, timeIntervalInSeconds);

        TOTPCredentials credentials = new TOTPCredentials();

        credentials.setUsername(USER_NAME);
        credentials.setPassword(new Password(USER_PASSWORD));

        TimeBasedOTP totp = new TimeBasedOTP(DEFAULT_ALGORITHM, DEFAULT_NUMBER_DIGITS, timeIntervalInSeconds, DEFAULT_DELAY_WINDOW);

        String token = totp.generate(USER_TOTP_SECRET);

        credentials.setToken(token);

        identityManager.validateCredentials(credentials);

        assertEquals(Status.VALID, credentials.getStatus());

        Calendar calendar = Calendar.getInstance();

        // let's delay
        calendar.add(Calendar.SECOND, -timeIntervalInSeconds);

        totp.setCalendar(calendar);

        token = totp.generate(USER_TOTP_SECRET);

        credentials.setToken(token);

        identityManager.validateCredentials(credentials);

        assertEquals(Status.VALID, credentials.getStatus());
    }


    private IdentityManager createIdentityManager(String propertyKey, Object propertyValue) {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
            .stores()
                .jpa()
                    .setCredentialHandlerProperty(propertyKey, propertyValue)
                    .addContextInitializer(new JPAContextInitializer(emf) {
                        @Override
                        public EntityManager getEntityManager() {
                            return entityManager;
                        }
                    })
                    .supportAllFeatures()
                    .identityClass(IdentityObject.class)
                    .attributeClass(IdentityObjectAttribute.class)
                    .relationshipClass(RelationshipObject.class)
                    .relationshipIdentityClass(RelationshipIdentityObject.class)
                    .relationshipAttributeClass(RelationshipObjectAttribute.class)
                    .credentialClass(CredentialObject.class)
                    .credentialAttributeClass(CredentialObjectAttribute.class)
                    .partitionClass(PartitionObject.class);

        PartitionManager partitionManager = null;
        fail("Create PartitionManager");

        IdentityManager identityManager = partitionManager.createIdentityManager();

        createUser(identityManager);

        return identityManager;
    }

    private void createUser(IdentityManager identityManager) {
        User user = new User(USER_NAME);

        identityManager.add(user);

        user = identityManager.getUser(user.getLoginName());

        assertNotNull(user);

        TOTPCredential credential = new TOTPCredential(USER_PASSWORD, USER_TOTP_SECRET);

        identityManager.updateCredential(user, credential);
    }
}

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

package org.picketlink.test.authentication;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.picketlink.Identity;
import org.picketlink.authentication.LockedAccountException;
import org.picketlink.authentication.UserAlreadyLoggedInException;
import org.picketlink.authentication.event.AlreadyLoggedInEvent;
import org.picketlink.authentication.event.LockedAccountEvent;
import org.picketlink.authentication.event.LoggedInEvent;
import org.picketlink.authentication.event.LoginFailedEvent;
import org.picketlink.authentication.event.PostAuthenticateEvent;
import org.picketlink.authentication.event.PostLoggedOutEvent;
import org.picketlink.authentication.event.PreAuthenticateEvent;
import org.picketlink.authentication.event.PreLoggedOutEvent;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.basic.User;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.picketlink.idm.model.basic.BasicModel.getUser;

/**
 * <p>
 * Perform some tests against the events raised during authentication. We also check the order which they are fired, what
 * allow us to assert better about how and when events are handled.
 * </p>
 * 
 * @author Pedro Igor
 * 
 */
public class AuthenticationEventHandlingTestCase extends AbstractAuthenticationTestCase {

    @Inject
    private EventExecutionObserver observer;

    @Deployment
    public static WebArchive deploy() {
        return deploy(AuthenticationEventHandlingTestCase.class);
    }

    @Test
    public void testSuccessfulAuthenticationEvents() throws Exception {
        Listener preAuthenticationListener = this.observer.addListener(PreAuthenticateEvent.class);
        Listener loggedInAuthenticationListener = this.observer.addListener(LoggedInEvent.class);
        Listener postAuthenticationListener = this.observer.addListener(PostAuthenticateEvent.class);

        DefaultLoginCredentials credentials = getCredentials();

        credentials.setUserId(USER_NAME);
        credentials.setPassword(USER_PASSWORD);

        Identity identity = getIdentity();

        identity.login();

        assertEquals(0, preAuthenticationListener.getExecutionIndex());
        assertEquals(1, postAuthenticationListener.getExecutionIndex());
        assertEquals(2, loggedInAuthenticationListener.getExecutionIndex());
    }

    @Test
    public void testUnsuccessfulAuthenticationEvents() throws Exception {
        Listener preAuthenticationListener = this.observer.addListener(PreAuthenticateEvent.class);
        Listener loginFailedAuthenticationListener = this.observer.addListener(LoginFailedEvent.class);
        Listener postAuthenticationListener = this.observer.addListener(PostAuthenticateEvent.class);

        DefaultLoginCredentials credentials = getCredentials();

        credentials.setUserId(USER_NAME);
        credentials.setPassword("badpassword");

        Identity identity = getIdentity();

        identity.login();

        assertEquals(0, preAuthenticationListener.getExecutionIndex());
        assertEquals(1, loginFailedAuthenticationListener.getExecutionIndex());
        assertFalse(postAuthenticationListener.wasNotified());
    }

    @Test
    public void testAlreadyLoggedInEvent() throws Exception {
        DefaultLoginCredentials credentials = getCredentials();

        credentials.setUserId(USER_NAME);
        credentials.setPassword(USER_PASSWORD);

        Identity identity = getIdentity();

        identity.login();

        Listener preAuthenticationListener = this.observer.addListener(PreAuthenticateEvent.class);
        Listener alreadyLoggedInAuthenticationListener = this.observer.addListener(AlreadyLoggedInEvent.class);
        Listener postAuthenticationListener = this.observer.addListener(PostAuthenticateEvent.class);

        try {
            identity.login();
            fail();
        } catch (UserAlreadyLoggedInException e) {
        }

        assertEquals(0, alreadyLoggedInAuthenticationListener.getExecutionIndex());
        assertFalse(preAuthenticationListener.wasNotified());
        assertFalse(postAuthenticationListener.wasNotified());
    }

    @Test
    public void testLockedAccountEvent() throws Exception {
        Listener preAuthenticationListener = this.observer.addListener(PreAuthenticateEvent.class);
        Listener lockedAccountListener = this.observer.addListener(LockedAccountEvent.class);
        Listener postAuthenticationListener = this.observer.addListener(PostAuthenticateEvent.class);

        IdentityManager identityManager = getIdentityManager();

        User user = getUser(identityManager, USER_NAME);

        user.setEnabled(false);

        identityManager.update(user);

        try {
            DefaultLoginCredentials credentials = getCredentials();

            credentials.setUserId(USER_NAME);
            credentials.setPassword(USER_PASSWORD);

            Identity identity = getIdentity();

            identity.login();

            fail();
        } catch (LockedAccountException e) {
        }

        assertEquals(0, preAuthenticationListener.getExecutionIndex());
        assertEquals(1, lockedAccountListener.getExecutionIndex());
        assertFalse(postAuthenticationListener.wasNotified());
    }

    @Test
    public void testLogoutEvents() throws Exception {
        Listener preLoggedOutListener = this.observer.addListener(PreLoggedOutEvent.class);
        Listener postLoggedOutListener = this.observer.addListener(PostLoggedOutEvent.class);

        DefaultLoginCredentials credentials = getCredentials();

        credentials.setUserId(USER_NAME);
        credentials.setPassword(USER_PASSWORD);

        Identity identity = getIdentity();

        identity.login();
        identity.logout();

        assertEquals(0, preLoggedOutListener.getExecutionIndex());
        assertEquals(1, postLoggedOutListener.getExecutionIndex());
    }

    public static class Listener {
        private Class<?> eventType;
        private int executionIndex = -1;

        public Listener(Class<?> expectedEventType) {
            this.eventType = expectedEventType;
        }

        boolean wasNotified() {
            return getExecutionIndex() >= 0;
        }

        Class<?> getEventType() {
            return this.eventType;
        }

        void setExecutionIndex(int executionIndex) {
            this.executionIndex = executionIndex;
        }

        int getExecutionIndex() {
            return this.executionIndex;
        }
    }

    @RequestScoped
    public static class EventExecutionObserver {

        private List<Listener> listeners = new ArrayList<AuthenticationEventHandlingTestCase.Listener>();

        private int executionCount;

        void notifyListeners(@Observes(notifyObserver = Reception.IF_EXISTS) Object event) {
            for (Listener listener : this.listeners) {
                if (listener.getEventType().equals(event.getClass())) {
                    listener.setExecutionIndex(this.executionCount++);
                }
            }
        }

        Listener addListener(Class<?> eventType) {
            Listener listener = new Listener(eventType);

            this.listeners.add(listener);

            return listener;
        }
    }
}

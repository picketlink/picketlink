/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.identity.federation.web.core;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.constants.GeneralConstants;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents an Identity Server
 *
 * @author Anil.Saldhana@redhat.com
 * @since Sep 17, 2009
 */
public class IdentityServer implements HttpSessionListener {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    // Configurable count for the active session count
    private static int count = AccessController.doPrivileged(new PrivilegedAction<Integer>() {
        public Integer run() {
            String val = System.getProperty("identity.server.log.count", "100");
            return Integer.parseInt(val);
        }
    });

    private static int activeSessionCount = 0;

    private IdentityParticipantStack stack = new STACK();

    public static class STACK implements IdentityParticipantStack {

        private final ConcurrentHashMap<String, Stack<String>> sessionParticipantsMap = new ConcurrentHashMap<String, Stack<String>>();

        private final ConcurrentHashMap<String, Set<String>> inTransitMap = new ConcurrentHashMap<String, Set<String>>();

        private final ConcurrentHashMap<String, Boolean> postBindingMap = new ConcurrentHashMap<String, Boolean>();

        /**
         * @see org.picketlink.identity.federation.web.core.IdentityParticipantStack#peek(java.lang.String)
         */
        public String peek(String sessionID) {
            Stack<String> stack = sessionParticipantsMap.get(sessionID);
            if (stack != null)
                return stack.peek();
            return "";
        }

        /**
         * @see org.picketlink.identity.federation.web.core.IdentityParticipantStack#pop(java.lang.String)
         */
        public String pop(String sessionID) {
            String result = null;
            Stack<String> stack = sessionParticipantsMap.get(sessionID);
            if (stack != null && stack.isEmpty() == false) {
                result = stack.pop();
            }
            return result;
        }

        /**
         * @see org.picketlink.identity.federation.web.core.IdentityParticipantStack#register(java.lang.String,
         *      java.lang.String, boolean)
         */
        public void register(String sessionID, String participant, boolean postBinding) {
            Stack<String> stack = sessionParticipantsMap.get(sessionID);
            if (stack == null) {
                stack = new Stack<String>();
                sessionParticipantsMap.put(sessionID, stack);
            }
            if (stack.contains(participant) == false) {
                stack.push(participant);
                postBindingMap.put(participant, Boolean.valueOf(postBinding));
            }
        }

        /**
         * @see org.picketlink.identity.federation.web.core.IdentityParticipantStack#getParticipants(java.lang.String)
         */
        public int getParticipants(String sessionID) {
            Stack<String> stack = sessionParticipantsMap.get(sessionID);
            if (stack != null)
                return stack.size();

            return 0;
        }

        /**
         * @see org.picketlink.identity.federation.web.core.IdentityParticipantStack#registerTransitParticipant(java.lang.String,
         *      java.lang.String)
         */
        public boolean registerTransitParticipant(String sessionID, String participant) {
            Set<String> transitSet = inTransitMap.get(sessionID);
            if (transitSet == null) {
                transitSet = new HashSet<String>();
                inTransitMap.put(sessionID, transitSet);
            }
            return transitSet.add(participant);
        }

        /**
         * @see org.picketlink.identity.federation.web.core.IdentityParticipantStack#deRegisterTransitParticipant(java.lang.String,
         *      java.lang.String)
         */
        public boolean deRegisterTransitParticipant(String sessionID, String participant) {
            Set<String> transitSet = inTransitMap.get(sessionID);
            if (transitSet != null) {
                postBindingMap.remove(participant);
                return transitSet.remove(participant);
            }
            return false;
        }

        /**
         * @see org.picketlink.identity.federation.web.core.IdentityParticipantStack#getNumOfParticipantsInTransit(java.lang.String)
         */
        public int getNumOfParticipantsInTransit(String sessionID) {
            Set<String> transitSet = inTransitMap.get(sessionID);
            if (transitSet != null)
                return transitSet.size();
            return 0;
        }

        /**
         * @see org.picketlink.identity.federation.web.core.IdentityParticipantStack#getBinding(java.lang.String)
         */
        public Boolean getBinding(String participant) {
            return postBindingMap.get(participant);
        }

        /**
         * @see org.picketlink.identity.federation.web.core.IdentityParticipantStack#totalSessions()
         */
        public int totalSessions() {
            return sessionParticipantsMap.keySet().size();
        }

        /**
         * @see org.picketlink.identity.federation.web.core.IdentityParticipantStack#createSession(java.lang.String)
         */
        public void createSession(String id) {
            sessionParticipantsMap.put(id, new Stack<String>());
            inTransitMap.put(id, new HashSet<String>());
        }

        /**
         * @see org.picketlink.identity.federation.web.core.IdentityParticipantStack#removeSession(java.lang.String)
         */
        public void removeSession(String id) {
            sessionParticipantsMap.remove(id);
            inTransitMap.remove(id);
        }
    }

    /**
     * Return the active session count
     *
     * @return
     */
    public int getActiveSessionCount() {
        return activeSessionCount;
    }

    /**
     * Return a reference to the internal stack
     *
     * @return
     */
    public IdentityParticipantStack stack() {
        return stack;
    }

    /**
     * Set a custom instance of the {@link IdentityParticipantStack}
     *
     * @param theStack
     */
    public void setStack(IdentityParticipantStack theStack) {
        this.stack = theStack;
    }

    /**
     * @see HttpSessionListener#sessionCreated(HttpSessionEvent)
     */
    public void sessionCreated(HttpSessionEvent sessionEvent) {
        activeSessionCount++;

        if (activeSessionCount % count == 0)
            logger.samlIdentityServerActiveSessionCount(activeSessionCount);

        HttpSession session = sessionEvent.getSession();

        logger.samlIdentityServerSessionCreated(session.getId(), activeSessionCount);

        // Ensure that the IdentityServer instance is set on the servlet context
        ServletContext servletContext = session.getServletContext();

        IdentityServer idserver = (IdentityServer) servletContext.getAttribute(GeneralConstants.IDENTITY_SERVER);

        if (idserver == null) {
            idserver = this;
            servletContext.setAttribute(GeneralConstants.IDENTITY_SERVER, this);
        }

        if (idserver != this)
            throw logger.notEqualError(idserver.toString(), this.toString());

        String id = sessionEvent.getSession().getId();
        stack.createSession(id);
    }

    /**
     * @see HttpSessionListener#sessionDestroyed(HttpSessionEvent)
     */
    public void sessionDestroyed(HttpSessionEvent sessionEvent) {
        --activeSessionCount;

        String id = sessionEvent.getSession().getId();

        logger.samlIdentityServerSessionDestroyed(id, activeSessionCount);

        stack.removeSession(id);
    }
}
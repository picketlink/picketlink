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
package org.picketlink.identity.federation.web.core;

import org.picketlink.common.constants.GeneralConstants;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>The {@link org.picketlink.identity.federation.web.core.SessionManager} is responsible to track users sessions.</p>
 *
 * @author Pedro Igor
 */
public class SessionManager {

    private static final String SESSION_MANAGER = "SESSION_MANAGER";

    /**
     * <p>Returns the instance associated with the given {@link javax.servlet.ServletContext}.</p>
     *
     * <p>Before calling this method, make sure you have initialized the application's context with a session manager.</p>
     *
     * @param context
     *
     * @throws java.lang.IllegalArgumentException If no SessionManager exists in the given ServletContext.
     */
    public static SessionManager get(ServletContext context) throws IllegalArgumentException {
        SessionManager sessionManager = (SessionManager) context.getAttribute(SESSION_MANAGER);

        if (sessionManager == null) {
            throw new IllegalArgumentException("No SessionManager found in the given ServletContext.");
        }

        return sessionManager;
    }

    private final Map<String, HttpSession> registry = Collections.synchronizedMap(new HashMap<String, HttpSession>());

    /**
     * <p>Creates a new instance and associate it with the given {@link javax.servlet.ServletContext}.</p>
     *
     * @param context
     */
    public SessionManager(ServletContext context, InitializationCallback initializationCallback) {
        if (context.getAttribute(SESSION_MANAGER) != null) {
            throw new IllegalStateException("SessionManager already configured.");
        }

        context.setAttribute(SESSION_MANAGER, this);
        initializationCallback.registerSessionListener(SessionManagerListener.class);
    }

    /**
     * <p>Registers the session associated with the given principal.</p>
     *
     * @param principal
     * @param session
     */
    public void add(Principal principal, HttpSession session) {
        session.setAttribute(GeneralConstants.PRINCIPAL_ID, principal);

        synchronized (this.registry) {
            this.registry.put(principal.getName(), session);
        }
    }

    /**
     * <p>Invalidates the session associated with the given principal.</p>
     *
     * @param principal
     */
    public void invalidate(Principal principal) {
        synchronized (this.registry) {
            String principalName = principal.getName();
            HttpSession session = this.registry.get(principalName);

            if (session != null) {
                Principal sessionPrincipal = getPrincipal(session);

                if (sessionPrincipal == null || !sessionPrincipal.getName().equals(principalName)) {
                    throw new RuntimeException("Principal [" + principalName + "] not associated with session [" + session.getId() + "] or session is associated with a different principal [" + sessionPrincipal.getName() + "].");
                }

                removeSession(session);

                session.invalidate();
            }
        }
    }

    /**
     * <p>Removes the given session from the registry.</p>
     *
     * @param session
     */
    void removeSession(HttpSession session) {
        Principal principal = getPrincipal(session);

        if (principal != null) {
            synchronized (this.registry) {
                this.registry.remove(principal.getName());
            }
        }
    }

    private Principal getPrincipal(HttpSession session) {
        return (Principal) session.getAttribute(GeneralConstants.PRINCIPAL_ID);
    }

    /**
     * <p>This listeners is responsible to remove sessions from the registry when destroyed by the container. This is an important
     * thing in order to keep sessions in sync and avoid the registry to grown indefinetely.</p>
     */
    public static class SessionManagerListener implements HttpSessionListener{

        @Override
        public void sessionCreated(HttpSessionEvent se) {

        }

        @Override
        public void sessionDestroyed(HttpSessionEvent se) {
            SessionManager sessionManager = SessionManager.get(se.getSession().getServletContext());

            if (sessionManager != null) {
                sessionManager.removeSession(se.getSession());
            }
        }

    }

    /**
     * <p>PicketLink provides support for different bindings or containers. Each of them has a different way to programmaticaly
     * configure the application, such as registering listeners.</p>
     */
    public interface InitializationCallback {

        void registerSessionListener(Class<? extends HttpSessionListener> listener);

    }
}
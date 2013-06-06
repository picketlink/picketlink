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
package org.picketlink.authentication.web.support;

import org.picketlink.authentication.web.FormAuthenticationScheme;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * This class maintains a cache of {@link SavedRequest} instances created from {@link HttpServletRequest} instances. This class
 * is to be used during authentication to help to retrieve previous informations from the request made for the first time before
 * the authentication process begins. It also stores the cached request in the user session for later use, if necessary.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class RequestCache {

    private ConcurrentHashMap<String, SavedRequest> requestCache = new ConcurrentHashMap<String, SavedRequest>();

    /**
     * <p>
     * Saves a {@link HttpServletRequest} as a {@link SavedRequest} instance. All the state from the original request will be
     * copied.
     * </p>
     *
     * @param request
     */
    public void saveRequest(HttpServletRequest request) {
        this.requestCache.put(getCurrentSession(request).getId(), new SavedRequest(request));
    }

    /**
     * <p>
     * Returns the user session. If no session was created a exception is raised. A valid session must exist before invoking
     * this method.
     * </p>
     */
    private HttpSession getCurrentSession(HttpServletRequest request) {
        HttpSession session = request.getSession(true);

        if (session == null) {
            throw new IllegalStateException("Unable to cache the request. User session was not created.");
        }
        return session;
    }

    /**
     * <p>
     * Removes a cached request and stores it in the session.
     * </p>
     */
    public SavedRequest removeAndStoreSavedRequestInSession(HttpServletRequest request) {
        HttpSession session = getCurrentSession(request);
        SavedRequest savedRequest = this.requestCache.remove(session.getId());

        session.setAttribute(FormAuthenticationScheme.SAVED_REQUEST, savedRequest);

        return savedRequest;
    }
}
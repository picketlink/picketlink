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
package org.picketlink.identity.federation.web.util;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Utility Class for http/redirect
 *
 * @author Anil.Saldhana@redhat.com
 * @since Dec 15, 2008
 */
public class HTTPRedirectUtil {

    /**
     * Send the response to the redirected destination while adding the character encoding of "UTF-8" as well as adding
     * headers
     * for cache-control and Pragma
     *
     * @param destination Destination URI where the response needs to redirect
     * @param response HttpServletResponse
     *
     * @throws IOException
     */
    public static void sendRedirectForRequestor(String destination, HttpServletResponse response) throws IOException {
        common(destination, response);
        response.setHeader("Cache-Control", "no-cache, no-store");
        sendRedirect(response, destination);
    }

    /**
     * @see #sendRedirectForRequestor(String, HttpServletResponse)
     */
    public static void sendRedirectForResponder(String destination, HttpServletResponse response) throws IOException {
        common(destination, response);
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate,private");
        sendRedirect(response, destination);
    }

    private static void common(String destination, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Location", destination);
        response.setHeader("Pragma", "no-cache");
    }

    private static void sendRedirect(HttpServletResponse response, String destination) throws IOException {
        response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        response.sendRedirect(destination);
    }
}
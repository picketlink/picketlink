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
package org.picketlink.http.internal.util;

import javax.servlet.http.HttpServletRequest;

import static org.picketlink.config.http.InboundHeaderConfiguration.X_REQUESTED_WITH_AJAX;
import static org.picketlink.config.http.InboundHeaderConfiguration.X_REQUESTED_WITH_HEADER_NAME;

/**
 * @author Pedro Igor
 */
public class RequestUtil {

    /**
     * <p>Checks if the given {@link javax.servlet.http.HttpServletRequest} was sent using AJAX. Most AJAX libraries
     * send the <code>X-Requested-With</code> header with value <code>XMLHttpRequest</code> to indicate that AJAX is being used.</p>
     *
     * @param request the request.
     * @return True if this is an AJAX request. Otherwise, false.
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader(X_REQUESTED_WITH_HEADER_NAME);
        return requestedWith != null && X_REQUESTED_WITH_AJAX.equalsIgnoreCase(requestedWith);
    }

}

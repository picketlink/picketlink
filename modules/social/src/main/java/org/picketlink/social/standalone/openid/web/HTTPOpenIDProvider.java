/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.social.standalone.openid.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openid4java.message.AuthSuccess;
import org.openid4java.message.DirectError;
import org.openid4java.message.Message;
import org.openid4java.message.ParameterList;
import org.openid4java.server.InMemoryServerAssociationStore;
import org.openid4java.server.ServerManager;

/**
 * Common code at an OpenID Provider
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jul 7, 2009
 */
public class HTTPOpenIDProvider {
    public String process(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        ServerManager manager = new ServerManager();
        manager.setSharedAssociations(new InMemoryServerAssociationStore());
        manager.setPrivateAssociations(new InMemoryServerAssociationStore());
        manager.setOPEndpointUrl(request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                + "/simple-openid/provider.jsp");

        ParameterList requestp;

        if ("complete".equals(request.getParameter("_action"))) // Completing the authz and authn process by redirecting here
        {
            requestp = (ParameterList) session.getAttribute("parameterlist"); // On a redirect from the OP authn & authz
                                                                              // sequence
        } else {
            requestp = new ParameterList(request.getParameterMap());
        }

        String mode = requestp.hasParameter("openid.mode") ? requestp.getParameterValue("openid.mode") : null;

        Message responsem;
        String responseText;

        if ("associate".equals(mode)) {
            // --- process an association request ---
            responsem = manager.associationResponse(requestp);
            responseText = responsem.keyValueFormEncoding();
        } else if ("checkid_setup".equals(mode) || "checkid_immediate".equals(mode)) {
            // interact with the user and obtain data needed to continue
            // List userData = userInteraction(requestp);
            String userSelectedId = null;
            String userSelectedClaimedId = null;
            Boolean authenticatedAndApproved = Boolean.FALSE;

            if ((session.getAttribute("authenticatedAndApproved") == null)
                    || (((Boolean) session.getAttribute("authenticatedAndApproved")) == Boolean.FALSE)) {
                session.setAttribute("parameterlist", requestp);
                response.sendRedirect("provider_authorization.jsp");
            } else {
                userSelectedId = (String) session.getAttribute("openid.claimed_id");
                userSelectedClaimedId = (String) session.getAttribute("openid.identity");
                authenticatedAndApproved = (Boolean) session.getAttribute("authenticatedAndApproved");
                // Remove the parameterlist so this provider can accept requests from elsewhere
                session.removeAttribute("parameterlist");
                session.setAttribute("authenticatedAndApproved", Boolean.FALSE); // Makes you authorize each and every time
            }

            // --- process an authentication request ---
            responsem = manager.authResponse(requestp, userSelectedId, userSelectedClaimedId,
                    authenticatedAndApproved.booleanValue());

            // caller will need to decide which of the following to use:
            // - GET HTTP-redirect to the return_to URL
            // - HTML FORM Redirection
            // responseText = response.wwwFormEncoding();
            if (responsem instanceof AuthSuccess) {
                response.sendRedirect(((AuthSuccess) responsem).getDestinationUrl(true));
                return "";
            } else {
                responseText = "<pre>" + responsem.keyValueFormEncoding() + "</pre>";
            }
        } else if ("check_authentication".equals(mode)) {
            // --- processing a verification request ---
            responsem = manager.verify(requestp);
            responseText = responsem.keyValueFormEncoding();
        } else {
            // --- error response ---
            responsem = DirectError.createDirectError("Unknown request");
            responseText = responsem.keyValueFormEncoding();
        }

        return responseText != null ? responseText.trim() : null;
    }

}
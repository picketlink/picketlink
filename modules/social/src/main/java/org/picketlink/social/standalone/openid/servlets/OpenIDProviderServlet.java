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
package org.picketlink.social.standalone.openid.servlets;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openid4java.message.Parameter;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.sts.PicketLinkCoreSTS;
import org.picketlink.social.standalone.openid.providers.helpers.OpenIDParameterList;
import org.picketlink.social.standalone.openid.providers.helpers.OpenIDProtocolContext;
import org.picketlink.social.standalone.openid.providers.helpers.OpenIDProviderManager.OpenIDMessage;

/**
 * Servlet that provides the Provider functionality for OpenID
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jul 15, 2009
 */
public class OpenIDProviderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private transient ServletContext servletContext = null;
    private String securePageName = "securepage.jsp";

    private transient PicketLinkCoreSTS sts = PicketLinkCoreSTS.instance();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.servletContext = config.getServletContext();
        String secpageStr = this.servletContext.getInitParameter("securePage");
        if (secpageStr != null && secpageStr.length() > 0)
            securePageName = secpageStr;

        String configFile = null;
        String configFileStr = this.servletContext.getInitParameter("configFile");
        if (configFileStr != null && configFileStr.length() > 0) {
            try {
                configFile = servletContext.getResource(configFileStr).toExternalForm();
            } catch (MalformedURLException e) {
                throw new ServletException(e);
            }
        }

        log("configFile=" + configFile);

        sts.installDefaultConfiguration(new String[] { configFile });
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();

        OpenIDProtocolContext protoCtx = new OpenIDProtocolContext();

        protoCtx.setEndpoint(request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                + request.getContextPath() + "/provider/");

        OpenIDParameterList requestp;

        if ("complete".equals(request.getParameter("_action"))) // Completing the authz and authn process by redirecting here
        {
            requestp = (OpenIDParameterList) session.getAttribute("parameterlist"); // On a redirect from the OP authn & authz
                                                                                    // sequence
        } else {
            requestp = new OpenIDParameterList(request.getParameterMap());
            Parameter openidIdentity = requestp.getParameter("openid.identity");
            if (openidIdentity != null) {
                session.setAttribute("openid.identity", openidIdentity.getValue());
            } else {
                log("The Parameter openid.identity is null ");
            }
        }

        protoCtx.setRequestParameterList(requestp);

        String mode = requestp.hasParameter("openid.mode") ? requestp.getParameterValue("openid.mode") : null;

        OpenIDMessage responsem = null;
        String responseText = null;

        log("mode=" + mode + "::ParameterMap:" + requestp);

        if ("associate".equals(mode)) {
            protoCtx.setMode(OpenIDProtocolContext.MODE.ASSOCIATE);
            try {
                sts.issueToken(protoCtx);
            } catch (ProcessingException e) {
                throw new ServletException(e);
            }

            responsem = protoCtx.getResponseMessage();

            // --- process an association request ---
            responseText = responsem.getResponseText();
        } else if ("checkid_setup".equals(mode) || "checkid_immediate".equals(mode)) {
            // interact with the user and obtain data needed to continue
            // List userData = userInteraction(requestp);
            String userSelectedId = null;
            String userSelectedClaimedId = null;
            Boolean authenticatedAndApproved = Boolean.FALSE;

            if ((session.getAttribute("authenticatedAndApproved") == null)
                    || (((Boolean) session.getAttribute("authenticatedAndApproved")) == Boolean.FALSE)) {
                session.setAttribute("parameterlist", requestp);
                response.setContentType("text/html");
                response.sendRedirect(request.getContextPath() + "/" + this.securePageName);
                return;
            } else {
                userSelectedId = (String) session.getAttribute("openid.claimed_id");
                userSelectedClaimedId = (String) session.getAttribute("openid.identity");
                authenticatedAndApproved = (Boolean) session.getAttribute("authenticatedAndApproved");
                // Remove the parameterlist so this provider can accept requests from elsewhere
                session.removeAttribute("parameterlist");
                session.setAttribute("authenticatedAndApproved", Boolean.FALSE); // Makes you authorize each and every time

                // Fallback
                if (authenticatedAndApproved == Boolean.TRUE && userSelectedId == null) {
                    userSelectedId = userSelectedClaimedId;
                }
                if ("checkid_setup".equals(mode))
                    protoCtx.setMode(OpenIDProtocolContext.MODE.CHECK_ID_SETUP);
                else
                    protoCtx.setMode(OpenIDProtocolContext.MODE.CHECK_ID_IMMEDIATE);

                protoCtx.setAuthenticationHolder(new OpenIDProtocolContext.AUTH_HOLDER(userSelectedId, userSelectedClaimedId,
                        authenticatedAndApproved));

                try {
                    // --- process an authentication request ---
                    sts.issueToken(protoCtx);
                } catch (ProcessingException e) {
                    throw new ServletException(e);
                }

                responsem = protoCtx.getResponseMessage();

                // caller will need to decide which of the following to use:
                // - GET HTTP-redirect to the return_to URL
                // - HTML FORM Redirection
                // responseText = response.wwwFormEncoding();
                if (responsem.isSuccessful()) {
                    response.sendRedirect(responsem.getDestinationURL(true));
                    return;
                } else {
                    responseText = "<pre>" + responsem.getResponseText() + "</pre>";
                }

            }
        } else if ("check_authentication".equals(mode)) {
            try {
                // --- processing a verification request ---
                sts.validateToken(protoCtx);
            } catch (ProcessingException e) {
                throw new ServletException(e);
            }
            responsem = protoCtx.getResponseMessage();

            responseText = responsem.getResponseText();
        } else {
            protoCtx.setIssueError(Boolean.TRUE);
            protoCtx.setErrorText("Unknown request");
            try {
                // --- error response ---
                sts.issueToken(protoCtx);
            } catch (ProcessingException e) {
                throw new ServletException(e);
            }
            responsem = protoCtx.getResponseMessage();

            responseText = responsem.getResponseText();
        }

        log("response=" + responseText);
        response.getWriter().write(responseText);
    }
}
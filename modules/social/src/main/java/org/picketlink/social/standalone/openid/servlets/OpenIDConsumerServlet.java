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

import org.picketlink.social.standalone.openid.api.OpenIDManager;
import org.picketlink.social.standalone.openid.api.OpenIDRequest;
import org.picketlink.social.standalone.openid.api.exceptions.OpenIDGeneralException;
import org.picketlink.social.standalone.openid.web.HTTPOpenIDContext;
import org.picketlink.social.standalone.openid.web.HTTPProtocolAdaptor;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * OpenID Consumer Servlet that gets a post request from the main JSP page of the consumer web application.
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jul 10, 2009
 */
public class OpenIDConsumerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private transient ServletContext servletContext;
    private String returnURL;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.servletContext = config.getServletContext();
        returnURL = this.servletContext.getInitParameter("returnURL");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (returnURL == null)
            returnURL = "http://" + req.getServerName() + ":" + req.getServerPort() + req.getContextPath()
                    + "/consumer_return.jsp";

        String userEntry = req.getParameter("openid");
        OpenIDRequest openIDReq = new OpenIDRequest(userEntry);

        HttpSession session = req.getSession();
        OpenIDManager manager = (OpenIDManager) session.getAttribute("openid_manager");
        if (manager == null) {
            manager = new OpenIDManager(openIDReq);
            session.setAttribute("openid_manager", manager);
        }
        manager.setUserString(userEntry);

        try {
            OpenIDManager.OpenIDProviderList listOfProviders = manager.discoverProviders();
            HTTPOpenIDContext httpOpenIDCtx = new HTTPOpenIDContext(req, resp, this.servletContext);
            httpOpenIDCtx.setReturnURL(returnURL);

            HTTPProtocolAdaptor adapter = new HTTPProtocolAdaptor(httpOpenIDCtx);
            OpenIDManager.OpenIDProviderInformation providerInfo = manager.associate(adapter, listOfProviders);
            manager.authenticate(adapter, providerInfo);
        } catch (OpenIDGeneralException e) {
            log("[OpenIDConsumerServlet]Exception in dealing with the provider:", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
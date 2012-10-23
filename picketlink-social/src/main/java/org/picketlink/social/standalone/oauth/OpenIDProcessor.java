/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.social.standalone.oauth;

import java.io.IOException;
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;

/**
 * Processor for the OpenID interaction
 *
 * @author Anil Saldhana
 * @since Sep 22, 2011
 */
public class OpenIDProcessor {

    public static final String AUTH_TYPE = "authType";

    private ConsumerManager openIdConsumerManager;
    private FetchRequest fetchRequest;

    private String openIdServiceUrl = null;

    private String returnURL = null;

    private String requiredAttributes, optionalAttributes = null;

    private boolean initialized = false;

    protected List<String> roles = new ArrayList<String>();

    public static ThreadLocal<Principal> cachedPrincipal = new ThreadLocal<Principal>();

    public static ThreadLocal<List<String>> cachedRoles = new ThreadLocal<List<String>>();
    public static String EMPTY_PASSWORD = "EMPTY";

    private enum STATES {
        AUTH, AUTHZ, FINISH
    };

    private enum Providers {
        GOOGLE("https://www.google.com/accounts/o8/id"), YAHOO("https://me.yahoo.com/"), MYSPACE("myspace.com"), MYOPENID(
                "https://myopenid.com/");

        private String name;

        Providers(String name) {
            this.name = name;
        }

        String get() {
            return name;
        }
    }

    public OpenIDProcessor(String theReturnURL, String requiredAttributes, String optionalAttributes) {
        this.returnURL = theReturnURL;
        this.requiredAttributes = requiredAttributes;
        this.optionalAttributes = optionalAttributes;
    }

    /**
     * Return whether the processor has initialized
     *
     * @return
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Initialize the processor
     *
     * @param requiredRoles
     * @throws MessageException
     * @throws ConsumerException
     */
    public void initialize(List<String> requiredRoles) throws MessageException, ConsumerException {
        if (openIdConsumerManager == null)
            openIdConsumerManager = new ConsumerManager();

        fetchRequest = FetchRequest.createFetchRequest();
        // Work on the required attributes
        if (StringUtil.isNotNull(requiredAttributes)) {
            List<String> tokens = StringUtil.tokenize(requiredAttributes);
            for (String token : tokens) {
                fetchRequest.addAttribute(token, OpenIDAliasMapper.get(token), true);
            }
        }
        // Work on the optional attributes
        if (StringUtil.isNotNull(optionalAttributes)) {
            List<String> tokens = StringUtil.tokenize(optionalAttributes);
            for (String token : tokens) {
                String type = OpenIDAliasMapper.get(token);
                if (type == null) {
                    System.out.println("Null Type returned for " + token);
                }
                fetchRequest.addAttribute(token, type, false);
            }
        }

        roles.addAll(requiredRoles);
        initialized = true;
    }

    @SuppressWarnings("unchecked")
    public boolean prepareAndSendAuthRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Figure out the service url
        String authType = request.getParameter(AUTH_TYPE);
        if (authType == null || authType.length() == 0) {
            authType = (String) request.getSession().getAttribute(AUTH_TYPE);
        }
        determineServiceUrl(authType);

        String openId = openIdServiceUrl;
        HttpSession session = request.getSession(true);
        if (openId != null) {
            session.setAttribute("openid", openId);
            List<DiscoveryInformation> discoveries;
            try {
                discoveries = openIdConsumerManager.discover(openId);
            } catch (DiscoveryException e) {
                throw new RuntimeException(e);
            }

            DiscoveryInformation discovered = openIdConsumerManager.associate(discoveries);
            session.setAttribute("discovery", discovered);
            try {
                AuthRequest authReq = openIdConsumerManager.authenticate(discovered, returnURL);

                // Add in required attributes
                authReq.addExtension(fetchRequest);

                String url = authReq.getDestinationUrl(true);
                response.sendRedirect(url);

                request.getSession().setAttribute("STATE", STATES.AUTH.name());
                return false;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public Principal processIncomingAuthResult(HttpServletRequest request, HttpServletResponse response) throws IOException {
        OpenIdPrincipal openIDPrincipal = null;
        HttpSession session = request.getSession(false);
        if (session == null)
            throw new RuntimeException("wrong lifecycle: session was null");

        // extract the parameters from the authentication response
        // (which comes in as a HTTP request from the OpenID provider)
        ParameterList responseParamList = new ParameterList(request.getParameterMap());
        // retrieve the previously stored discovery information
        DiscoveryInformation discovered = (DiscoveryInformation) session.getAttribute("discovery");
        if (discovered == null)
            throw new RuntimeException("discovered information was null");
        // extract the receiving URL from the HTTP request
        StringBuffer receivingURL = request.getRequestURL();
        String queryString = request.getQueryString();
        if (queryString != null && queryString.length() > 0)
            receivingURL.append("?").append(request.getQueryString());

        // verify the response; ConsumerManager needs to be the same
        // (static) instance used to place the authentication request
        VerificationResult verification;
        try {
            verification = openIdConsumerManager.verify(receivingURL.toString(), responseParamList, discovered);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // examine the verification result and extract the verified identifier
        Identifier identifier = verification.getVerifiedId();

        if (identifier != null) {
            AuthSuccess authSuccess = (AuthSuccess) verification.getAuthResponse();

            Map<String, List<String>> attributes = null;
            if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
                FetchResponse fetchResp;
                try {
                    fetchResp = (FetchResponse) authSuccess.getExtension(AxMessage.OPENID_NS_AX);
                } catch (MessageException e) {
                    throw new RuntimeException(e);
                }

                attributes = fetchResp.getAttributes();
            }

            openIDPrincipal = createPrincipal(identifier.getIdentifier(), discovered.getOPEndpoint(),
                    attributes);
            request.getSession().setAttribute("PRINCIPAL", openIDPrincipal);

            
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
        return openIDPrincipal;
    }

    private OpenIdPrincipal createPrincipal(String identifier, URL openIdProvider, Map<String, List<String>> attributes) {
        return new OpenIdPrincipal(identifier, openIdProvider, attributes);
    } 

    private void determineServiceUrl(String service) {
        openIdServiceUrl = Providers.GOOGLE.get();
        if (StringUtil.isNotNull(service)) {
            if ("google".equals(service))
                openIdServiceUrl = Providers.GOOGLE.get();
            else if ("yahoo".equals(service))
                openIdServiceUrl = Providers.YAHOO.get();
            else if ("myspace".equals(service))
                openIdServiceUrl = Providers.MYSPACE.get();
            else if ("myopenid".equals(service))
                openIdServiceUrl = Providers.MYOPENID.get();
        }
    }
}
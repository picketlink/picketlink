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
package org.picketlink.social.standalone.login;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
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
import org.picketlink.social.standalone.fb.FacebookConstants;
import org.picketlink.social.standalone.fb.FacebookPrincipal;
import org.picketlink.social.standalone.fb.FacebookProcessor;
import org.picketlink.social.standalone.oauth.OAuthConstants;
import org.picketlink.social.standalone.oauth.OpenIDProcessor;
import org.picketlink.social.standalone.oauth.OpenIdPrincipal;
import org.picketlink.social.standalone.oauth.StringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Perform external authentication using Facebook Connect and Google OpenID
 *
 * @author anil saldhana
 * @since Sep 19, 2012
 */
public class ExternalAuthentication {

    protected static Logger log = Logger.getLogger(ExternalAuthentication.class);
    protected boolean trace = log.isTraceEnabled();

    private enum AUTH_PROVIDERS {
        FACEBOOK, OPENID;
    }

    private ConsumerManager openIdConsumerManager;
    private FetchRequest fetchRequest;
    private String openIdServiceUrl = null;

    public static final String AUTH_TYPE = "authType";

    protected FacebookProcessor facebookProcessor;
    protected OpenIDProcessor openidProcessor;

    protected String returnURL;
    protected String clientID;
    protected String clientSecret;
    protected String facebookScope = "email";

    private String requiredAttributes = "name,email,ax_firstName,ax_lastName,ax_fullName,ax_email";

    private String optionalAttributes = null;

    // Whether the authenticator has to to save and restore request
    protected boolean saveRestoreRequest = true;

    private enum STATES {
        AUTH, AUTHZ, FINISH
    }

    ;

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

    /**
     * A comma separated string that represents the roles the web app needs to pass authorization
     *
     * @param roleStr
     */
    public void setRoleString(String roleStr) {
        if (roleStr == null)
            throw new RuntimeException("Role String is null in configuration");
        StringTokenizer st = new StringTokenizer(getSystemPropertyAsString(roleStr), ",");
        while (st.hasMoreElements()) {
            roles.add(st.nextToken());
        }
    }

    public void setSaveRestoreRequest(boolean saveRestoreRequest) {
        this.saveRestoreRequest = saveRestoreRequest;
    }

    protected List<String> roles = new ArrayList<String>();

    /**
     * Set the url where the 3rd party authentication service will redirect after authentication
     *
     * @param returnURL
     */
    public void setReturnURL(String returnURL) {
        this.returnURL = getSystemPropertyAsString(returnURL);
    }

    /**
     * Set the client id for facebook
     *
     * @param clientID
     */
    public void setClientID(String clientID) {
        this.clientID = getSystemPropertyAsString(clientID);
    }

    /**
     * Set the client secret for facebook
     *
     * @param clientSecret
     */
    public void setClientSecret(String clientSecret) {
        this.clientSecret = getSystemPropertyAsString(clientSecret);
    }

    /**
     * Set the scope for facebook (Default: email)
     *
     * @param facebookScope
     */
    public void setFacebookScope(String facebookScope) {
        this.facebookScope = getSystemPropertyAsString(facebookScope);
    }

    /**
     * Authenticate the request
     *
     * @param request
     * @param response
     *
     * @return
     *
     * @throws IOException
     * @throws {@link RuntimeException} when the response is not of type catalina response object
     */
    public boolean authenticate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (facebookProcessor == null)
            facebookProcessor = new FacebookProcessor(clientID, clientSecret, facebookScope, returnURL, roles);

        if (openidProcessor == null)
            openidProcessor = new OpenIDProcessor(returnURL, requiredAttributes, optionalAttributes);

        HttpSession session = request.getSession();
        // Determine the type of service based on request param
        String authType = request.getParameter(AUTH_TYPE);
        if (authType != null && authType.length() > 0) {
            // Place it on the session
            session.setAttribute(AUTH_TYPE, authType);
        }
        if (authType == null || authType.length() == 0) {
            authType = (String) session.getAttribute(AUTH_TYPE);
        }
        if (authType == null) {
            authType = AUTH_PROVIDERS.FACEBOOK.name();
        }
        if (authType != null && authType.equals(AUTH_PROVIDERS.FACEBOOK.name())) {
            return processFacebook(request, response);
        } else {
            return processOpenID(request, response);
        }
    }

    protected boolean processFacebook(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String state = (String) session.getAttribute("STATE");

        if (STATES.FINISH.name().equals(state)) {
            Principal principal = request.getUserPrincipal();
            if (principal == null) {
                principal = getFacebookPrincipal(request, response);
            }
            if (principal == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }
            return dealWithFacebookPrincipal(request, response, principal);
        }

        if (state == null || state.isEmpty()) {
            return initialFacebookInteraction(request, response);
        }
        // We have sent an auth request
        if (state.equals(STATES.AUTH.name())) {
            return facebookProcessor.handleAuthStage(request, response);
        }

        // Principal facebookPrincipal = null;
        if (state.equals(STATES.AUTHZ.name())) {
            Principal principal = getFacebookPrincipal(request, response);

            if (principal == null) {
                log.error("Principal was null. Maybe login modules need to be configured properly. Or user chose no data");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }

            return dealWithFacebookPrincipal(request, response, principal);
        }
        return false;
    }

    protected boolean processOpenID(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Principal userPrincipal = request.getUserPrincipal();
        if (userPrincipal != null) {
            if (trace)
                log.trace("Logged in as:" + userPrincipal);
            return true;
        }

        if (!openidProcessor.isInitialized()) {
            try {
                openidProcessor.initialize(roles);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        HttpSession httpSession = request.getSession();
        String state = (String) httpSession.getAttribute("STATE");
        if (trace)
            log.trace("state=" + state);

        if (STATES.FINISH.name().equals(state)) {
            // This is a replay. We need to resend a request back to the OpenID provider
            httpSession.setAttribute("STATE", STATES.AUTH.name());

            return prepareAndSendAuthRequest(request, response);
        }

        if (state == null || state.isEmpty()) {
            return prepareAndSendAuthRequest(request, response);
        }
        // We have sent an auth request
        if (state.equals(STATES.AUTH.name())) {
            Principal principal = processIncomingAuthResult(request, response);

            if (principal == null) {
                log.error("Principal was null. Maybe login modules need to be configured properly. Or user chose no data");
                return false;
            }

            return dealWithOpenIDPrincipal(request, response, principal);
        }
        return false;
    }

    public boolean initialFacebookInteraction(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        Map<String, String> params = new HashMap<String, String>();
        params.put(OAuthConstants.REDIRECT_URI_PARAMETER, returnURL);
        params.put(OAuthConstants.CLIENT_ID_PARAMETER, clientID);

        if (facebookScope != null) {
            params.put(OAuthConstants.SCOPE_PARAMETER, facebookScope);
        }

        String location = new StringBuilder(FacebookConstants.SERVICE_URL).append("?").append(createFacebookQueryString(params))
                .toString();
        try {
            session.setAttribute("STATE", STATES.AUTH.name());
            if (trace)
                log.trace("Redirect:" + location);
            response.sendRedirect(location);
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean dealWithFacebookPrincipal(HttpServletRequest request, HttpServletResponse response, Principal principal) throws IOException {
        SocialRequestWrapper requestWrapper = (SocialRequestWrapper) request;
        requestWrapper.setUserPrincipal(principal);

        request.getSession().setAttribute("STATE", STATES.FINISH.name());

        return true;
    }

    private boolean dealWithOpenIDPrincipal(HttpServletRequest request, HttpServletResponse response, Principal principal) throws IOException {
        HttpSession httpSession = request.getSession();

        SocialRequestWrapper requestWrapper = (SocialRequestWrapper) request;
        requestWrapper.setUserPrincipal(principal);


        if (trace)
            log.trace("Logged in as:" + principal);
        httpSession.setAttribute("STATE", STATES.FINISH.name());
        return true;
    }

    public Principal getFacebookPrincipal(HttpServletRequest request, HttpServletResponse response) {
        Principal facebookPrincipal = handleFacebookAuthenticationResponse(request, response);
        if (facebookPrincipal == null)
            return null;

        request.getSession().setAttribute("PRINCIPAL", facebookPrincipal);
        return facebookPrincipal;
    }

    protected Principal handleFacebookAuthenticationResponse(HttpServletRequest request, HttpServletResponse response) {
        String error = request.getParameter(OAuthConstants.ERROR_PARAMETER);
        if (error != null) {
            throw new RuntimeException("error:" + error);
        } else {
            String returnUrl = returnURL;
            String authorizationCode = request.getParameter(OAuthConstants.CODE_PARAMETER);
            if (authorizationCode == null) {
                log.error("Authorization code parameter not found");
                return null;
            }

            URLConnection connection = sendFacebookAccessTokenRequest(returnUrl, authorizationCode, response);

            Map<String, String> params = formUrlDecode(readUrlContent(connection));
            String accessToken = params.get(OAuthConstants.ACCESS_TOKEN_PARAMETER);
            String expires = params.get(FacebookConstants.EXPIRES);

            if (trace)
                log.trace("Access Token=" + accessToken + " :: Expires=" + expires);

            if (accessToken == null) {
                throw new RuntimeException("No access token found");
            }

            return readInIdentity(request, response, accessToken, returnUrl);
        }
    }

    protected URLConnection sendFacebookAccessTokenRequest(String returnUrl, String authorizationCode, HttpServletResponse response) {
        String returnUri = returnURL;

        Map<String, String> params = new HashMap<String, String>();
        params.put(OAuthConstants.REDIRECT_URI_PARAMETER, returnUri);
        params.put(OAuthConstants.CLIENT_ID_PARAMETER, clientID);
        params.put(OAuthConstants.CLIENT_SECRET_PARAMETER, clientSecret);
        params.put(OAuthConstants.CODE_PARAMETER, authorizationCode);

        String location = new StringBuilder(FacebookConstants.ACCESS_TOKEN_ENDPOINT_URL).append("?")
                .append(createFacebookQueryString(params)).toString();

        try {
            if (trace)
                log.trace("AccessToken Request=" + location);
            URL url = new URL(location);
            URLConnection connection = url.openConnection();
            return connection;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean prepareAndSendAuthRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
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

    private String createFacebookQueryString(Map<String, String> params) {
        StringBuilder queryString = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String paramName = entry.getKey();
            String paramValue = entry.getValue();
            if (first) {
                first = false;
            } else {
                queryString.append("&");
            }
            queryString.append(paramName).append("=");
            String encodedParamValue;
            try {
                if (paramValue == null)
                    throw new RuntimeException("paramValue is null");
                encodedParamValue = URLEncoder.encode(paramValue, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            queryString.append(encodedParamValue);
        }
        return queryString.toString();
    }

    private Principal readInIdentity(HttpServletRequest request, HttpServletResponse response, String accessToken,
                                     String returnUrl) {
        FacebookPrincipal facebookPrincipal = null;
        try {
            String urlString = new StringBuilder(FacebookConstants.PROFILE_ENDPOINT_URL).append("?access_token=")
                    .append(URLEncoder.encode(accessToken, "UTF-8")).toString();
            if (trace)
                log.trace("Profile read:" + urlString);

            URL profileUrl = new URL(urlString);
            String profileContent = readUrlContent(profileUrl.openConnection());
            JSONObject jsonObject = new JSONObject(profileContent);

            facebookPrincipal = new FacebookPrincipal();
            facebookPrincipal.setAccessToken(accessToken);
            facebookPrincipal.setId(jsonObject.getString("id"));
            facebookPrincipal.setName(jsonObject.getString("name"));
            facebookPrincipal.setFirstName(jsonObject.getString("first_name"));
            facebookPrincipal.setLastName(jsonObject.getString("last_name"));
            facebookPrincipal.setGender(jsonObject.getString("gender"));
            facebookPrincipal.setTimezone(jsonObject.getString("timezone"));
            facebookPrincipal.setLocale(jsonObject.getString("locale"));
            if (jsonObject.getString("email") != null) {
                facebookPrincipal.setEmail(jsonObject.getString("email"));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return facebookPrincipal;
    }

    private String readUrlContent(URLConnection connection) {
        StringBuilder result = new StringBuilder();
        try {
            Reader reader = new InputStreamReader(connection.getInputStream());
            char[] buffer = new char[50];
            int nrOfChars;
            while ((nrOfChars = reader.read(buffer)) != -1) {
                result.append(buffer, 0, nrOfChars);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result.toString();
    }

    private Map<String, String> formUrlDecode(String encodedData) {
        Map<String, String> params = new HashMap<String, String>();
        String[] elements = encodedData.split("&");
        for (String element : elements) {
            String[] pair = element.split("=");
            if (pair.length == 2) {
                String paramName = pair[0];
                String paramValue;
                try {
                    paramValue = URLDecoder.decode(pair[1], "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                params.put(paramName, paramValue);
            } else {
                throw new RuntimeException("Unexpected name-value pair in response: " + element);
            }
        }
        return params;
    }

    /**
     * <p>
     * Get the system property value if the string is of the format ${sysproperty}
     * </p>
     * <p>
     * You can insert default value when the system property is not set, by separating it at the beginning with ::
     * </p>
     * <p>
     * <b>Examples:</b>
     * </p>
     *
     * <p>
     * ${idp} should resolve to a value if the system property "idp" is set.
     * </p>
     * <p>
     * ${idp::http://localhost:8080} will resolve to http://localhost:8080 if the system property "idp" is not set.
     * </p>
     *
     * @param str
     *
     * @return
     */
    private String getSystemPropertyAsString(String str) {
        if (str.contains("${")) {
            Pattern pattern = Pattern.compile("\\$\\{([^}]+)}");
            Matcher matcher = pattern.matcher(str);

            StringBuffer buffer = new StringBuffer();
            String sysPropertyValue = null;

            while (matcher.find()) {
                String subString = matcher.group(1);
                String defaultValue = "";

                // Look for default value
                if (subString.contains("::")) {
                    int index = subString.indexOf("::");
                    defaultValue = subString.substring(index + 2);
                    subString = subString.substring(0, index);
                }
                sysPropertyValue = SecurityActions.getSystemProperty(subString, defaultValue);
                matcher.appendReplacement(buffer, sysPropertyValue);
            }

            matcher.appendTail(buffer);
            str = buffer.toString();
        }
        return str;
    }

    @SuppressWarnings("unchecked")
    public Principal processIncomingAuthResult(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Principal principal = null;
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

            principal = createOpenIDPrincipal(identifier.getIdentifier(), discovered.getOPEndpoint(),
                    attributes);
            request.getSession().setAttribute("PRINCIPAL", principal);

            if (trace)
                log.trace("Logged in as:" + principal);
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
        return principal;
    }

    private OpenIdPrincipal createOpenIDPrincipal(String identifier, URL openIdProvider, Map<String, List<String>> attributes) {
        return new OpenIdPrincipal(identifier, openIdProvider, attributes);
    }

}
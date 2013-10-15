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
package org.picketlink.social.standalone.fb;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.picketlink.social.standalone.oauth.OAuthConstants;

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

/**
 * Processor to perform Facebook interaction
 *
 * @author Anil Saldhana
 * @since Sep 22, 2011
 */
public class FacebookProcessor {

    public static final String FB_AUTH_STATE_SESSION_ATTRIBUTE = "FB_AUTH_STATE_SESSION_ATTRIBUTE";
    protected static Logger log = Logger.getLogger(FacebookProcessor.class);
    protected boolean trace = log.isTraceEnabled();

    protected FacebookUtil util = new FacebookUtil(FacebookConstants.SERVICE_URL);

    public static ThreadLocal<Principal> cachedPrincipal = new ThreadLocal<Principal>();

    public static ThreadLocal<List<String>> cachedRoles = new ThreadLocal<List<String>>();
    public static String EMPTY_PASSWORD = "EMPTY";

    protected List<String> roles = new ArrayList<String>();

    public enum STATES {
        AUTH, AUTHZ, FINISH
    }

    ;

    protected String clientID;
    protected String clientSecret;
    protected String scope;
    protected String returnURL;

    public FacebookProcessor(String clientID, String clientSecret, String scope, String returnURL, List<String> requiredRoles) {
        super();
        this.clientID = clientID;
        this.clientSecret = clientSecret;
        this.scope = scope;
        this.returnURL = returnURL;
        this.roles.addAll(requiredRoles);
    }

    public void setRoleString(String roleStr) {
        if (roleStr == null)
            throw new RuntimeException("Role String is null in configuration");
        StringTokenizer st = new StringTokenizer(roleStr, ",");
        while (st.hasMoreElements()) {
            roles.add(st.nextToken());
        }
    }

    public boolean initialInteraction(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        Map<String, String> params = new HashMap<String, String>();
        params.put(OAuthConstants.REDIRECT_URI_PARAMETER, returnURL);
        params.put(OAuthConstants.CLIENT_ID_PARAMETER, clientID);

        if (scope != null) {
            params.put(OAuthConstants.SCOPE_PARAMETER, scope);
        }

        String location = new StringBuilder(FacebookConstants.SERVICE_URL).append("?").append(util.createQueryString(params))
                .toString();
        try {
            session.setAttribute(FB_AUTH_STATE_SESSION_ATTRIBUTE, STATES.AUTH.name());
            if (trace)
                log.trace("Redirect:" + location);
            response.sendRedirect(location);
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean handleAuthStage(HttpServletRequest request, HttpServletResponse response) {
        request.getSession().setAttribute(FB_AUTH_STATE_SESSION_ATTRIBUTE, STATES.AUTHZ.name());
        sendAuthorizeRequest(this.returnURL, response);
        return false;
    }

    protected void sendAuthorizeRequest(String returnUrl, HttpServletResponse response) {
        String returnUri = returnUrl;

        Map<String, String> params = new HashMap<String, String>();
        params.put(OAuthConstants.REDIRECT_URI_PARAMETER, returnUri);
        params.put(OAuthConstants.CLIENT_ID_PARAMETER, clientID);
        if (scope != null) {
            params.put(OAuthConstants.SCOPE_PARAMETER, scope);
        }
        String location = new StringBuilder(FacebookConstants.AUTHENTICATION_ENDPOINT_URL).append("?")
                .append(util.createQueryString(params)).toString();
        try {
            response.sendRedirect(location);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Principal getPrincipal(HttpServletRequest request, HttpServletResponse response) {
        Principal facebookPrincipal = handleAuthenticationResponse(request, response);
        if (facebookPrincipal == null)
            return null;

        request.getSession().setAttribute("PRINCIPAL", facebookPrincipal);

        return facebookPrincipal;
    }

    public Principal handleAuthenticationResponse(HttpServletRequest request, HttpServletResponse response) {
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

            URLConnection connection = sendAccessTokenRequest(returnUrl, authorizationCode, response);

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

    protected URLConnection sendAccessTokenRequest(String returnUrl, String authorizationCode, HttpServletResponse response) {
        String returnUri = returnURL;

        Map<String, String> params = new HashMap<String, String>();
        params.put(OAuthConstants.REDIRECT_URI_PARAMETER, returnUri);
        params.put(OAuthConstants.CLIENT_ID_PARAMETER, clientID);
        params.put(OAuthConstants.CLIENT_SECRET_PARAMETER, clientSecret);
        params.put(OAuthConstants.CODE_PARAMETER, authorizationCode);

        String location = new StringBuilder(FacebookConstants.ACCESS_TOKEN_ENDPOINT_URL).append("?")
                .append(util.createQueryString(params)).toString();

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
            if (jsonObject.has("name")) {
                facebookPrincipal.setName(jsonObject.getString("name"));
            }
            if (jsonObject.has("username")) {
                facebookPrincipal.setUsername(jsonObject.getString("username"));
            }
            if (jsonObject.has("first_name")) {
                facebookPrincipal.setFirstName(jsonObject.getString("first_name"));
            }
            if (jsonObject.has("last_name")) {
                facebookPrincipal.setLastName(jsonObject.getString("last_name"));
            }
            if (jsonObject.has("gender")) {
                facebookPrincipal.setGender(jsonObject.getString("gender"));
            }
            if (jsonObject.has("timezone")) {
                facebookPrincipal.setTimezone(jsonObject.getString("timezone"));
            }
            if (jsonObject.has("locale")) {
                facebookPrincipal.setLocale(jsonObject.getString("locale"));
            }
            if (jsonObject.has("email")) {
                String emailString = jsonObject.getString("email");
                facebookPrincipal.setName(emailString);
                facebookPrincipal.setEmail(emailString);
            }
            facebookPrincipal.setJsonObject(jsonObject);
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
}
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
package org.picketlink.social.standalone.fb;

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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
 
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Processor to perform Facebook interaction
 *
 * @author Anil Saldhana
 * @since Sep 22, 2011
 */
public class FacebookProcessor {
    private static final String FB_AUTH_STATE_SESSION_ATTRIBUTE = "FB_AUTH_STATE_SESSION_ATTRIBUTE";
    protected static Logger log = Logger.getLogger(FacebookProcessor.class);
    protected boolean trace = log.isTraceEnabled();

    protected FacebookUtil util = new FacebookUtil(FacebookConstants.SERVICE_URL);

    public static ThreadLocal<Principal> cachedPrincipal = new ThreadLocal<Principal>();

    public static ThreadLocal<List<String>> cachedRoles = new ThreadLocal<List<String>>();
    public static String EMPTY_PASSWORD = "EMPTY";

    protected List<String> roles = new ArrayList<String>();

    public enum STATES {
        AUTH, AUTHZ, FINISH
    };

    protected String clientID;
    protected String clientSecret;
    protected String scope;
    private String returnURL;

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

    protected Principal handleAuthenticationResponse(HttpServletRequest request, HttpServletResponse response) {
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
            facebookPrincipal.setName(jsonObject.getString("name"));
            facebookPrincipal.setFirstName(jsonObject.getString("first_name"));
            facebookPrincipal.setLastName(jsonObject.getString("last_name"));
            facebookPrincipal.setGender(jsonObject.getString("gender"));
            facebookPrincipal.setTimezone(jsonObject.getString("timezone"));
            facebookPrincipal.setLocale(jsonObject.getString("locale"));
            if (jsonObject.getString("email") != null) {
                facebookPrincipal.setName(jsonObject.getString("email"));
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
}
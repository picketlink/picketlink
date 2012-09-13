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
package org.picketlink.oauth.server.endpoint;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.amber.oauth2.as.issuer.MD5Generator;
import org.apache.amber.oauth2.as.issuer.OAuthIssuer;
import org.apache.amber.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.amber.oauth2.as.request.OAuthTokenRequest;
import org.apache.amber.oauth2.as.response.OAuthASResponse;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.error.OAuthError;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.apache.amber.oauth2.common.message.types.GrantType;

/**
 * Token End Point
 *
 * @author anil saldhana
 * @since Aug 27, 2012
 */
// public class TokenEndpoint extends HttpServlet {

@Path("/token")
public class TokenEndpoint implements Serializable {

    private static final long serialVersionUID = 1L;

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response authorize(@Context HttpServletRequest request) throws OAuthSystemException {

        OAuthTokenRequest oauthRequest = null;

        OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());

        try {
            oauthRequest = new OAuthTokenRequest(request);
            // Get the values from DB
            String clientID = "xyz";
            String authorizationCode = "123";
            String password = "something";
            String username = "yz";

            // check if clientid is valid
            if (!clientID.equals(oauthRequest.getParam(OAuth.OAUTH_CLIENT_ID))) {
                OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("client_id not found")
                        .buildJSONMessage();
                return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
            }

            // do checking for different grant types
            if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(GrantType.AUTHORIZATION_CODE.toString())) {
                if (!authorizationCode.equals(oauthRequest.getParam(OAuth.OAUTH_CODE))) {
                    OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                            .setError(OAuthError.TokenResponse.INVALID_GRANT).setErrorDescription("invalid authorization code")
                            .buildJSONMessage();
                    return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
                }
            } else if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(GrantType.PASSWORD.toString())) {
                if (!password.equals(oauthRequest.getPassword()) || !username.equals(oauthRequest.getUsername())) {
                    OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                            .setError(OAuthError.TokenResponse.INVALID_GRANT)
                            .setErrorDescription("invalid username or password").buildJSONMessage();
                    return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
                }
            } else if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(GrantType.REFRESH_TOKEN.toString())) {
                OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_GRANT).setErrorDescription("invalid username or password")
                        .buildJSONMessage();
                return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
            }

            OAuthResponse response = OAuthASResponse.tokenResponse(HttpServletResponse.SC_OK)
                    .setAccessToken(oauthIssuerImpl.accessToken()).setExpiresIn("3600").buildJSONMessage();

            return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
        } catch (OAuthProblemException e) {
            OAuthResponse res = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST).error(e).buildJSONMessage();
            return Response.status(res.getResponseStatus()).entity(res.getBody()).build();
        }
    }

    @GET
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response authorizeGet(@Context HttpServletRequest request) throws OAuthSystemException {
        OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());

        OAuthResponse response = OAuthASResponse.tokenResponse(HttpServletResponse.SC_OK)
                .setAccessToken(oauthIssuerImpl.accessToken()).setExpiresIn("3600").buildJSONMessage();

        return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
    }

    /*
     * @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
     * IOException { try { OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
     *
     * OAuthResponse oauthResponse = OAuthASResponse.tokenResponse(HttpServletResponse.SC_OK)
     * .setAccessToken(oauthIssuerImpl.accessToken()).setExpiresIn("3600").buildJSONMessage();
     *
     * response.setStatus(oauthResponse.getResponseStatus()); PrintWriter pw = response.getWriter();
     * pw.print(oauthResponse.getBody()); pw.flush(); pw.close(); } catch (OAuthSystemException e) { OAuthResponse r; try { r =
     * OAuthResponse.errorResponse(401).error(OAuthProblemException.error(e.getMessage())).buildJSONMessage();
     *
     * response.setStatus(r.getResponseStatus());
     *
     * PrintWriter pw = response.getWriter(); pw.print(r.getBody()); pw.flush(); pw.close();
     *
     * response.sendError(401); } catch (OAuthSystemException e1) { e1.printStackTrace(); } } }
     *
     * @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
     * IOException { OAuthTokenRequest oauthRequest = null;
     *
     * OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
     *
     * try { oauthRequest = new OAuthTokenRequest(request);
     *
     * // Get the values from DB String clientID = "xyz"; String authorizationCode = "123"; String password = "something";
     * String username = "yz";
     *
     * // check if clientid is valid if (!clientID.equals(oauthRequest.getParam(OAuth.OAUTH_CLIENT_ID))) { OAuthResponse
     * oauthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
     * .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("client_id not found") .buildJSONMessage();
     * sendOauthMessage(response, oauthResponse); return; }
     *
     * // do checking for different grant types if
     * (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(GrantType.AUTHORIZATION_CODE.toString())) { if
     * (!authorizationCode.equals(oauthRequest.getParam(OAuth.OAUTH_CODE))) { OAuthResponse oauthResponse =
     * OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
     * .setError(OAuthError.TokenResponse.INVALID_GRANT).setErrorDescription("invalid authorization code") .buildJSONMessage();
     * sendOauthMessage(response, oauthResponse); return; } } else if
     * (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(GrantType.PASSWORD.toString())) { if
     * (!password.equals(oauthRequest.getPassword()) || !username.equals(oauthRequest.getUsername())) { OAuthResponse
     * oauthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
     * .setError(OAuthError.TokenResponse.INVALID_GRANT)
     * .setErrorDescription("invalid username or password").buildJSONMessage(); sendOauthMessage(response, oauthResponse);
     * return; } } else if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(GrantType.REFRESH_TOKEN.toString())) {
     * OAuthResponse oauthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
     * .setError(OAuthError.TokenResponse.INVALID_GRANT).setErrorDescription("Wrong Grant Type:Refresh Token")
     * .buildJSONMessage(); sendOauthMessage(response, oauthResponse); return; }
     *
     * OAuthResponse oauthResponse = OAuthASResponse.tokenResponse(HttpServletResponse.SC_OK)
     * .setAccessToken(oauthIssuerImpl.accessToken()).setExpiresIn("3600").buildJSONMessage(); sendOauthMessage(response,
     * oauthResponse);
     *
     * return; } catch (OAuthProblemException e) { try { OAuthResponse oauthResponse =
     * OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST).error(e) .buildJSONMessage();
     * sendOauthMessage(response, oauthResponse); } catch (OAuthSystemException e1) { // TODO Auto-generated catch block
     * e1.printStackTrace(); } return; } catch (OAuthSystemException e) { try { OAuthResponse oauthResponse =
     * OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
     * .error(OAuthProblemException.error(e.getMessage())).buildJSONMessage(); sendOauthMessage(response, oauthResponse); }
     * catch (OAuthSystemException e1) { // TODO Auto-generated catch block e1.printStackTrace(); } return; } }
     *
     * private void sendOauthMessage(HttpServletResponse response, OAuthResponse oauthResponse) throws IOException {
     * response.setStatus(oauthResponse.getResponseStatus()); PrintWriter pw = response.getWriter();
     * pw.print(oauthResponse.getBody()); pw.flush(); pw.close(); }
     */
}
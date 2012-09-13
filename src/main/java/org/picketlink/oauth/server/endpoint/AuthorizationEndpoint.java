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
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.amber.oauth2.as.issuer.MD5Generator;
import org.apache.amber.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.amber.oauth2.as.request.OAuthAuthzRequest;
import org.apache.amber.oauth2.as.response.OAuthASResponse;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.apache.amber.oauth2.common.message.types.ResponseType;
import org.apache.amber.oauth2.common.utils.OAuthUtils;

/**
 * OAuth2 Authorization Endpoint
 *
 * @author anil saldhana
 * @since Aug 27, 2012
 */
// public class AuthorizationEndpoint extends HttpServlet {
@Path("/authz")
public class AuthorizationEndpoint implements Serializable {
    private static final long serialVersionUID = 1L;

    @GET
    public Response authorize(@Context HttpServletRequest request) throws URISyntaxException, OAuthSystemException {

        OAuthAuthzRequest oauthRequest = null;

        OAuthIssuerImpl oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());

        try {
            oauthRequest = new OAuthAuthzRequest(request);

            // build response according to response_type
            String responseType = oauthRequest.getParam(OAuth.OAUTH_RESPONSE_TYPE);

            OAuthASResponse.OAuthAuthorizationResponseBuilder builder = OAuthASResponse.authorizationResponse(request,
                    HttpServletResponse.SC_FOUND);

            if (responseType.equals(ResponseType.CODE.toString())) {
                builder.setCode(oauthIssuerImpl.authorizationCode());
            }
            if (responseType.equals(ResponseType.TOKEN.toString())) {
                builder.setAccessToken(oauthIssuerImpl.accessToken());
                builder.setExpiresIn(3600L);
            }

            String redirectURI = oauthRequest.getParam(OAuth.OAUTH_REDIRECT_URI);

            final OAuthResponse response = builder.location(redirectURI).buildQueryMessage();
            URI url = new URI(response.getLocationUri());

            return Response.status(response.getResponseStatus()).location(url).build();

        } catch (OAuthProblemException e) {

            final Response.ResponseBuilder responseBuilder = Response.status(HttpServletResponse.SC_FOUND);

            String redirectUri = e.getRedirectUri();

            if (OAuthUtils.isEmpty(redirectUri)) {
                throw new WebApplicationException(responseBuilder
                        .entity("OAuth callback url needs to be provided by client!!!").build());
            }
            final OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND).error(e)
                    .location(redirectUri).buildQueryMessage();
            final URI location = new URI(response.getLocationUri());
            return responseBuilder.location(location).build();
        }
    }
    /*
     * @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
     * IOException { OAuthAuthzRequest oauthRequest = null;
     *
     * OAuthIssuerImpl oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
     *
     * try { oauthRequest = new OAuthAuthzRequest(request);
     *
     * // build response according to response_type String responseType = oauthRequest.getParam(OAuth.OAUTH_RESPONSE_TYPE);
     *
     * OAuthASResponse.OAuthAuthorizationResponseBuilder builder = OAuthASResponse.authorizationResponse(request,
     * HttpServletResponse.SC_FOUND);
     *
     * if (responseType.equals(ResponseType.CODE.toString())) { builder.setCode(oauthIssuerImpl.authorizationCode()); } if
     * (responseType.equals(ResponseType.TOKEN.toString())) { builder.setAccessToken(oauthIssuerImpl.accessToken());
     * builder.setExpiresIn(3600L); }
     *
     * String redirectURI = oauthRequest.getParam(OAuth.OAUTH_REDIRECT_URI); if (redirectURI == null) { throw
     * OAuthProblemException.error(OAuth.OAUTH_REDIRECT_URI + " needed"); }
     *
     * final OAuthResponse oauthResponse = builder.location(redirectURI).buildQueryMessage();
     *
     * response.sendRedirect(oauthResponse.getLocationUri());
     *
     * } catch (OAuthProblemException e) {
     *
     * String redirectUri = e.getRedirectUri();
     *
     * if (OAuthUtils.isEmpty(redirectUri)) { throw new ServletException("OAuth callback url needs to be provided by client!!!:"
     * + e); } try { final OAuthResponse oauthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND).error(e)
     * .location(redirectUri).buildQueryMessage(); sendOauthMessage(response, oauthResponse); } catch (OAuthSystemException e1)
     * { throw new RuntimeException(e1); } } catch (OAuthSystemException e) { throw new
     * ServletException("OAuth callback url needs to be provided by client!!!"); } }
     *
     * private void sendOauthMessage(HttpServletResponse response, OAuthResponse oauthResponse) throws IOException {
     * response.setStatus(oauthResponse.getResponseStatus()); PrintWriter pw = response.getWriter(); String oauthResponseBody =
     * oauthResponse.getBody(); pw.print(oauthResponseBody); pw.flush(); pw.close(); }
     */

}
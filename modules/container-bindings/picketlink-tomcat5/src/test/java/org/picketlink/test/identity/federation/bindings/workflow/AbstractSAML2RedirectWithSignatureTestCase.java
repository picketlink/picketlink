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
package org.picketlink.test.identity.federation.bindings.workflow;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import junit.framework.Assert;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.Session;
import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.identity.federation.bindings.tomcat.idp.IDPWebBrowserSSOValve;
import org.picketlink.identity.federation.bindings.tomcat.sp.SPRedirectSignatureFormAuthenticator;
import org.picketlink.identity.federation.bindings.tomcat.sp.ServiceProviderAuthenticator;
import org.picketlink.identity.federation.web.core.IdentityServer;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;
import org.picketlink.test.identity.federation.bindings.authenticators.AuthenticatorTestUtils;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaContext;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaRequest;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaResponse;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaSession;

/**
 * Abstract class to create SAML2 Redirect Binding testcases using signatures.
 *
 * @author Pedro Igor
 * @since Dec 2, 2011
 */
public abstract class AbstractSAML2RedirectWithSignatureTestCase {
    protected static final String BASE_PROFILE = "saml2/redirect";

    private static final String IDP_PROFILE = BASE_PROFILE + "/idp-sig/";

    private MockCatalinaSession idpHttpSession = new MockCatalinaSession();

    protected IDPWebBrowserSSOValve createIdentityProvider() {
        return AuthenticatorTestUtils.createIdentityProvider(IDP_PROFILE);
    }

    protected void addIdentityServerParticipants(IDPWebBrowserSSOValve idp, String url) {
        IdentityServer identityServer = getIdentityServer(idp);

        identityServer.stack().register(getIDPHttpSession().getId(), url, false);
    }

    protected MockCatalinaSession getIDPHttpSession() {
        return this.idpHttpSession;
    }

    protected IdentityServer getIdentityServer(IDPWebBrowserSSOValve idp) {
        return (IdentityServer) ((MockCatalinaContext) idp.getContainer()).getAttribute("IDENTITY_SERVER");
    }

    protected ServiceProviderAuthenticator createServiceProvider(String spProfile) {
        Thread.currentThread().setContextClassLoader(AuthenticatorTestUtils.createContextClassLoader(spProfile));

        SPRedirectSignatureFormAuthenticator sp = new SPRedirectSignatureFormAuthenticator();

        sp.setContainer(new MockCatalinaContext());

        try {
            sp.testStart();
            sp.getConfiguration().setIdpUsesPostBinding(false);
        } catch (LifecycleException e) {
            Assert.fail("Error while creating Employee SP.");
        }

        return sp;
    }

    protected void setQueryStringFromResponse(MockCatalinaResponse idpLogoutEmployeeResponse,
            MockCatalinaRequest idpLogoutResponseRequest) throws IOException {
        String samlParameter = null;
        String samlParameterValue = null;

        if (idpLogoutEmployeeResponse.redirectString.contains(GeneralConstants.SAML_REQUEST_KEY + "=")) {
            samlParameter = GeneralConstants.SAML_REQUEST_KEY;
            samlParameterValue = getSAMLRequest(idpLogoutEmployeeResponse);
        } else {
            samlParameter = GeneralConstants.SAML_RESPONSE_KEY;
            samlParameterValue = getSAMLResponse(idpLogoutEmployeeResponse);
        }

        idpLogoutResponseRequest.setParameter(samlParameter, RedirectBindingUtil.urlDecode(samlParameterValue));

        boolean hasRelayState = idpLogoutEmployeeResponse.redirectString.indexOf("&RelayState") != -1;

        if (hasRelayState) {
            idpLogoutResponseRequest.setParameter(GeneralConstants.RELAY_STATE,
                    RedirectBindingUtil.urlDecode(getSAMLRelayState(idpLogoutEmployeeResponse)));
        }

        idpLogoutResponseRequest.setParameter(GeneralConstants.SAML_SIG_ALG_REQUEST_KEY,
                RedirectBindingUtil.urlDecode(getSAMLSigAlg(idpLogoutEmployeeResponse)));
        idpLogoutResponseRequest.setParameter(GeneralConstants.SAML_SIGNATURE_REQUEST_KEY,
                RedirectBindingUtil.urlDecode(getSAMLSignature(idpLogoutEmployeeResponse)));

        StringBuffer queryString = new StringBuffer();

        queryString.append(samlParameter + "=" + samlParameterValue);

        if (hasRelayState) {
            queryString.append("&").append(GeneralConstants.RELAY_STATE).append("=")
                    .append(getSAMLRelayState(idpLogoutEmployeeResponse));
        }

        queryString.append("&").append(GeneralConstants.SAML_SIG_ALG_REQUEST_KEY).append("=")
                .append(getSAMLSigAlg(idpLogoutEmployeeResponse));
        queryString.append("&").append(GeneralConstants.SAML_SIGNATURE_REQUEST_KEY).append("=")
                .append(getSAMLSignature(idpLogoutEmployeeResponse));

        idpLogoutResponseRequest.setQueryString(queryString.toString());
    }

    protected String getSAMLResponse(MockCatalinaResponse response) {
        int endIndex = response.redirectString.indexOf("&SigAlg=");

        if (response.redirectString.contains("&RelayState=")) {
            endIndex = response.redirectString.indexOf("&RelayState=");
        }

        return response.redirectString.substring(response.redirectString.indexOf(GeneralConstants.SAML_RESPONSE_KEY + "=")
                + (GeneralConstants.SAML_RESPONSE_KEY + "=").length(), endIndex);
    }

    protected String getSAMLSignature(MockCatalinaResponse response) {
        return response.redirectString.substring(response.redirectString.indexOf("&Signature=") + "&Signature=".length());
    }

    protected String getSAMLRelayState(MockCatalinaResponse response) {
        return response.redirectString.substring(response.redirectString.indexOf("&RelayState=") + "&RelayState=".length(),
                response.redirectString.lastIndexOf("&SigAlg="));
    }

    protected String getSAMLSigAlg(MockCatalinaResponse response) {
        return response.redirectString.substring(response.redirectString.indexOf("&SigAlg=") + "&SigAlg=".length(),
                response.redirectString.lastIndexOf("&Signature="));
    }

    protected String getSAMLRequest(MockCatalinaResponse response) {
        int endIndex = response.redirectString.indexOf("&SigAlg=");

        if (response.redirectString.contains("&RelayState=")) {
            endIndex = response.redirectString.indexOf("&RelayState=");
        }

        return response.redirectString.substring(response.redirectString.indexOf(GeneralConstants.SAML_REQUEST_KEY + "=")
                + (GeneralConstants.SAML_REQUEST_KEY + "=").length(), endIndex);
    }

    protected MockCatalinaRequest createRequest(HttpSession httpSession, boolean withUserPrincipal) {
        MockCatalinaRequest request = AuthenticatorTestUtils.createRequest("192.168.1.3", withUserPrincipal);

        request.setSession((Session) httpSession);

        return request;
    }

    

    protected MockCatalinaRequest createIDPRequest(boolean withUserPrincipal) {
        return createRequest(this.getIDPHttpSession(), withUserPrincipal);
    }

}

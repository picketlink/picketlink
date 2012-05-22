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

package org.picketlink.test.identity.federation.bindings.authenticators;

import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.realm.GenericPrincipal;
import org.picketlink.identity.federation.bindings.tomcat.idp.IDPWebBrowserSSOValve;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.core.IdentityServer;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaContext;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaContextClassLoader;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaRealm;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaRequest;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaResponse;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaSession;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class AuthenticatorTestUtils {

    public static IDPWebBrowserSSOValve createIdentityProvider(String baseClassLoaderPath) {
        Thread.currentThread().setContextClassLoader(createContextClassLoader(baseClassLoaderPath));

        IDPWebBrowserSSOValve idpWebBrowserSSOValve = new IDPWebBrowserSSOValve();

        MockCatalinaContext catalinaContext = new MockCatalinaContext();

        idpWebBrowserSSOValve.setContainer(catalinaContext);

        catalinaContext.setAttribute("IDENTITY_SERVER", new IdentityServer());

        try {
            idpWebBrowserSSOValve.start();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }

        return idpWebBrowserSSOValve;
    }
    
    public static MockCatalinaContextClassLoader createContextClassLoader(String resource) {
        URL[] urls = new URL[]

        { Thread.currentThread().getContextClassLoader().getResource(resource) };

        MockCatalinaContextClassLoader mcl = new MockCatalinaContextClassLoader(urls);

        mcl.setDelegate(Thread.currentThread().getContextClassLoader());
        mcl.setProfile(resource);

        return mcl;
    }
    
    public static MockCatalinaRequest createRequest(String userAddress, boolean withUserPrincipal) {
        MockCatalinaRequest request = new MockCatalinaRequest();

        request = new MockCatalinaRequest();
        request.setMethod("GET");
        request.setRemoteAddr(userAddress);
        request.setSession(new MockCatalinaSession());
        request.setContext(new MockCatalinaContext());

        if (withUserPrincipal) {
            request.setUserPrincipal(createPrincipal());
        }

        return request;
    }
    
    public static GenericPrincipal createPrincipal() {
        MockCatalinaRealm realm = new MockCatalinaRealm("user", "user", new Principal() {
            public String getName() {
                return "user";
            }
        });
        List<String> roles = new ArrayList<String>();
        roles.add("manager");
        roles.add("employee");

        List<String> rolesList = new ArrayList<String>();
        rolesList.add("manager");

        return new GenericPrincipal(realm, "user", "user", roles);
    }
    
    public static void populateParametersWithQueryString(String queryString, MockCatalinaRequest request) {
        String samlParameter = null;
        String samlParameterValue = null;

        if (queryString.contains(GeneralConstants.SAML_REQUEST_KEY + "=")) {
            samlParameter = GeneralConstants.SAML_REQUEST_KEY;
            samlParameterValue = getSAMLRequest(queryString);
        } else {
            samlParameter = GeneralConstants.SAML_RESPONSE_KEY;
            samlParameterValue = getSAMLResponse(queryString);
        }
        
        try {
            request.setParameter(samlParameter, RedirectBindingUtil.urlDecode(samlParameterValue));

            boolean hasRelayState = queryString.indexOf("&RelayState") != -1;

            if (hasRelayState) {
                request.setParameter(GeneralConstants.RELAY_STATE,
                        RedirectBindingUtil.urlDecode(getSAMLRelayState(queryString)));
            }

            request.setParameter(GeneralConstants.SAML_SIG_ALG_REQUEST_KEY,
                    RedirectBindingUtil.urlDecode(getSAMLSigAlg(queryString)));
            request.setParameter(GeneralConstants.SAML_SIGNATURE_REQUEST_KEY,
                    RedirectBindingUtil.urlDecode(getSAMLSignature(queryString)));

            request.setQueryString(queryString.toString());        
            
        } catch (Exception e) {
            Assert.fail("Erro while populating request with SAML parameters.");
        }
    }
    
    private static final  String getSAMLResponse(String queryString) {
        int endIndex = queryString.indexOf("&SigAlg=");
        
        if (queryString.contains("&RelayState=")) {
            endIndex = queryString.indexOf("&RelayState=");
        }
        
        // no signature info
        if (endIndex == -1) {
            endIndex = queryString.length();
        }

        return queryString.substring(queryString.indexOf(GeneralConstants.SAML_RESPONSE_KEY + "=")
                + (GeneralConstants.SAML_RESPONSE_KEY + "=").length(), endIndex);
    }

    private static final  String getSAMLSignature(String queryString) {
        return queryString.substring(queryString.indexOf("&Signature=") + "&Signature=".length());
    }

    private static final  String getSAMLRelayState(String queryString) {
        return queryString.substring(queryString.indexOf("&RelayState=") + "&RelayState=".length(),
                queryString.lastIndexOf("&SigAlg="));
    }

    private static final  String getSAMLSigAlg(String queryString) {
        int indexOfSigAlg = queryString.indexOf("&SigAlg=");
        
        // no signature info
        if (indexOfSigAlg == -1) {
            return "";
        }
        
        return queryString.substring(indexOfSigAlg + "&SigAlg=".length(),
                queryString.lastIndexOf("&Signature="));
    }

    private static final  String getSAMLRequest(String queryString) {
        int endIndex = queryString.indexOf("&SigAlg=");

        if (queryString.contains("&RelayState=")) {
            endIndex = queryString.indexOf("&RelayState=");
        }

        return queryString.substring(queryString.indexOf(GeneralConstants.SAML_REQUEST_KEY + "=")
                + (GeneralConstants.SAML_REQUEST_KEY + "=").length(), endIndex);
    }
    

}
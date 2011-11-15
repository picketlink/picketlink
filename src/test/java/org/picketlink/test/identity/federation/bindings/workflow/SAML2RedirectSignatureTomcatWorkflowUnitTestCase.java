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


import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import junit.framework.Assert;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.realm.GenericPrincipal;
import org.junit.Test;
import org.picketlink.identity.federation.bindings.tomcat.idp.IDPWebBrowserSSOValve;
import org.picketlink.identity.federation.bindings.tomcat.sp.SPRedirectSignatureFormAuthenticator;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaContext;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaContextClassLoader;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaLoginConfig;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaRealm;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaRequest;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaResponse;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaSession;

/**
 * <p>
 *  This {@code TestCase} tests the interaction between the SP and the IDP in a scenario where token signature is used. 
 * </p>
 * <p>
 *  This class also tests the use of the {@code SPRedirectSignatureFormAuthenticator.idpAddress} and the {@code IDPWebBrowserSSOValve.validatingAliasToTokenIssuer} properties.
 *  <br/>
 *  The objective is test the following scenarios:
 *  <br/><br/>
 *      1) User's machine is the same of the SP and the IDP. (testSAML2RedirectWithSameConsumerAndProvider)
 *      <br/>
 *      2) User's machine is different of the SP and the IDP. (testSAML2RedirectWithSifferentConsumerAndProvider)
 *          192.168.1.1 -> IDP Address (IDP_PROFILE/WEB-INF/picketlink-idfed.xml)
 *          192.168.1.2 -> SP Address (SP_PROFILE/WEB-INF/picketlink-idfed.xml)
 *          192.168.1.3 -> End User Address
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 * @since Nov 14, 2011
 */
public class SAML2RedirectSignatureTomcatWorkflowUnitTestCase
{
   private static final String profile = "saml2/redirect";

   private static final String IDP_PROFILE = profile + "/idp-sig/";

   private static final String SP_PROFILE = profile + "/sp/employee-sig";

   private final ClassLoader tcl = Thread.currentThread().getContextClassLoader();
   
   private String SAML_REQUEST_KEY = "SAMLRequest=";

   private String SAML_RESPONSE_KEY = "SAMLResponse=";
   
   /**
    * Tests the token's signatures validations when the requester and the SP/IDP as on the same host.
    * The keyprovider is configured with the same ValidatingAlias for all of them.
    * 
    * @throws Exception
    */
   @Test
   public void testSAML2RedirectWithSameConsumerAndProvider() throws Exception
   {
      testWorkflow("192.168.1.1", "192.168.1.1", false);
   }
   
   /**
    * Tests the token's signatures validations when the requester is in a differente host than the SP and IDP.
    * The keyprovider is configured with a ValidatingAlias for specific for the SP (192.168.1.2) that is different from the IDP (localhost) and the user (192.168.1.1).
    */
   @Test
   public void testSAML2RedirectWithSifferentConsumerAndProvider() throws Exception
   {
      testWorkflow("192.168.1.3", "192.168.1.1", true);
   }

   private void testWorkflow(String userAddress, String idpAddress, boolean validatingAliasToTokenIssuer) throws LifecycleException, IOException, ServletException
   {
      MockCatalinaRequest request = createRequest(userAddress);
      
      // Sends a initial request to the SP. Requesting a resource ...
      MockCatalinaResponse idpAuthRequest = sendSPRequest(request, false, idpAddress);
      
      assertNotNull("Redirect String can not be null.", idpAuthRequest.redirectString);
      
      // Sends a auth request to the IDP
      request = createRequest(userAddress);
      
      request.setParameter("SAMLRequest", RedirectBindingUtil.urlDecode(getSAMLRequest(idpAuthRequest)));
      request.setParameter("SigAlg", RedirectBindingUtil.urlDecode(getSAMLSigAlg(idpAuthRequest)));
      request.setParameter("Signature", RedirectBindingUtil.urlDecode(getSAMLSignature(idpAuthRequest)));
      request.setQueryString(SAML_REQUEST_KEY + getSAMLRequest(idpAuthRequest) + "&SigAlg=" + getSAMLSigAlg(idpAuthRequest) + "&Signature=" + getSAMLSignature(idpAuthRequest));
      
      request.setUserPrincipal(new GenericPrincipal(createRealm(), "user", "user", getRoles()) );
      
      MockCatalinaResponse idpAuthResponse = sendIDPRequest(request, validatingAliasToTokenIssuer); 
      
      assertNotNull("Redirect String can not be null.", idpAuthResponse.redirectString);
      
      // Sends the IDP response to the SP. Now the user is succesfully authenticated and access for the requested resource is granted...    
      request = createRequest(userAddress);
      request.getContext().setRealm(createRealm());
      
      request.setParameter("SAMLResponse", RedirectBindingUtil.urlDecode(getSAMLResponse(idpAuthResponse)));
      request.setParameter("SigAlg", RedirectBindingUtil.urlDecode(getSAMLSigAlg(idpAuthResponse)));
      request.setParameter("Signature", RedirectBindingUtil.urlDecode(getSAMLSignature(idpAuthResponse)));
      request.setQueryString(SAML_RESPONSE_KEY + getSAMLResponse(idpAuthResponse) + "&SigAlg=" + getSAMLSigAlg(idpAuthResponse) + "&Signature=" + getSAMLSignature(idpAuthResponse));
      
      sendSPRequest(request, true, idpAddress);
   }

   private MockCatalinaRequest createRequest(String userAddress)
   {
      MockCatalinaRequest request = new MockCatalinaRequest();
      
      request = new MockCatalinaRequest();
      request.setMethod("GET");
      request.setRemoteAddr(userAddress);
      request.setSession(new MockCatalinaSession());
      request.setContext(new MockCatalinaContext());
      
      return request;
   }

   private String getSAMLResponse(MockCatalinaResponse response)
   {
      return response.redirectString.substring(response.redirectString.indexOf(SAML_RESPONSE_KEY) +
            SAML_RESPONSE_KEY.length(), response.redirectString.indexOf("&SigAlg="));
   }

   private String getSAMLSignature(MockCatalinaResponse response)
   {
      return response.redirectString.substring(response.redirectString.indexOf("&Signature=") +
            "&Signature=".length());
   }

   private String getSAMLSigAlg(MockCatalinaResponse response)
   {
      return response.redirectString.substring(response.redirectString.indexOf("&SigAlg=") +
            "&SigAlg=".length(), response.redirectString.lastIndexOf("&Signature="));
   }

   private String getSAMLRequest(MockCatalinaResponse response)
   {
      return response.redirectString.substring(response.redirectString.indexOf(SAML_REQUEST_KEY) +
            SAML_REQUEST_KEY.length(), response.redirectString.indexOf("&SigAlg="));
   }

   private List<String> getRoles()
   {
      List<String> roles = new ArrayList<String>();
      roles.add("manager");
      roles.add("employee");
      return roles;
   }

   private MockCatalinaRealm createRealm()
   {
      return new MockCatalinaRealm("user", "user", new Principal()
      {   
         public String getName()
         { 
            return "user";
         }
      });
   }

   private MockCatalinaResponse sendIDPRequest(MockCatalinaRequest request, boolean validatingAliasToTokenIssuer)
         throws LifecycleException, IOException, ServletException
   {
      MockCatalinaContextClassLoader mclIDP = setupTCL(IDP_PROFILE);
      Thread.currentThread().setContextClassLoader(mclIDP);

      IDPWebBrowserSSOValve idp = new IDPWebBrowserSSOValve();
      
      idp.setSignOutgoingMessages(true);
      idp.setIgnoreIncomingSignatures(false);
      idp.setValidatingAliasToTokenIssuer(validatingAliasToTokenIssuer);
      
      idp.setContainer(request.getContext());
      idp.start();
      
      MockCatalinaResponse response = new MockCatalinaResponse();
      
      idp.invoke(request, response);
      
      return response;
   }

   private MockCatalinaResponse sendSPRequest(MockCatalinaRequest request, boolean validateAuthentication, String idpAddress)
         throws LifecycleException, IOException
   {
      MockCatalinaContextClassLoader mclSPEmp = setupTCL(SP_PROFILE);
      Thread.currentThread().setContextClassLoader(mclSPEmp); 
      
      SPRedirectSignatureFormAuthenticator sp = new SPRedirectSignatureFormAuthenticator();
      
      sp.setIdpAddress(idpAddress);
      
      request.setParameter(GeneralConstants.RELAY_STATE, null);
      
      MockCatalinaLoginConfig loginConfig = new MockCatalinaLoginConfig();
      
      sp.setContainer(request.getContext());
      sp.testStart();
      
      MockCatalinaResponse response = new MockCatalinaResponse();
      
      if (validateAuthentication) {
         Assert.assertTrue("Employee app succesfully authenticated.", sp.authenticate(request, response, loginConfig));
      } else {
         sp.authenticate(request, response, loginConfig);
      }
      
      return response;
   }
   
   private MockCatalinaContextClassLoader setupTCL(String resource)
   {
      URL[] urls = new URL[] {tcl.getResource(resource)};
      
      MockCatalinaContextClassLoader mcl = new MockCatalinaContextClassLoader(urls);
      mcl.setDelegate(tcl);
      mcl.setProfile(resource);
      return mcl;
   }
   
}

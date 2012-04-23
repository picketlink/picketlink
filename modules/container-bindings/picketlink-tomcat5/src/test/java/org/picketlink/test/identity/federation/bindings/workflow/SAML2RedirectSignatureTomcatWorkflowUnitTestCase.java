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

import javax.servlet.ServletException;

import junit.framework.Assert;

import org.apache.catalina.LifecycleException;
import org.junit.Test;
import org.picketlink.identity.federation.bindings.tomcat.idp.IDPWebBrowserSSOValve;
import org.picketlink.identity.federation.bindings.tomcat.sp.SPRedirectSignatureFormAuthenticator;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaLoginConfig;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaRequest;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaResponse;

/**
 * <p>
 *  This {@code TestCase} tests the interaction between the SP and the IDP in a scenario where token signature is used. 
 * </p>
 * <p>
 *  This class also tests the use of the {@code SPRedirectSignatureFormAuthenticator.idpAddress} and the {@code IDPWebBrowserSSOValve.validatingAliasToTokenIssuer} properties 
 *  during the token's signature validation process.
 * </p>
 * <p>
 *  The objective is test the following scenarios:
 *      <ul>
 *      <li>User's machine is the same of the SP and the IDP. (testSAML2RedirectWithSameConsumerAndProvider)</li>
 *      <li> User's machine is different of the SP and the IDP. (testSAML2RedirectWithSifferentConsumerAndProvider)
 *          <br/>
 *          192.168.1.1 -> IDP Address (IDP_PROFILE/WEB-INF/picketlink-idfed.xml)
 *          <br/>
 *          192.168.1.2 -> SP Address (SP_PROFILE/WEB-INF/picketlink-idfed.xml)
 *          <br/>
 *          192.168.1.3 -> End User Address
 *      </li>
 *      <ul>
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 * @since Nov 14, 2011
 */
public class SAML2RedirectSignatureTomcatWorkflowUnitTestCase extends AbstractSAML2RedirectWithSignatureTestCase
{

   private static final String SP_EMPLOYEE_PROFILE = BASE_PROFILE + "/sp/employee-sig";

   private SPRedirectSignatureFormAuthenticator employeeServiceProvider;

   /**
    * Tests the token's signatures validations when the requester and the SP/IDP as on the same host.
    * The keyprovider is configured with the same ValidatingAlias for all of them.
    * 
    * @throws Exception
    */
   @Test
   public void testSAML2RedirectWithSameConsumerAndProvider() throws Exception
   {
      testWorkflow("192.168.1.1", "192.168.1.1");
   }

   /**
    * Tests the token's signatures validations when the requester is in a differente host than the SP and IDP.
    * <br/>
    * The keyprovider is configured with a ValidatingAlias for a specific SP (192.168.1.2) that is different from the IDP (192.168.1.1) and the user (192.168.1.3).
    * <br/>
    * Test fails if:
    *   <ul>
    *       <li>If you change the IDP address the test will fail because the SP's keystore and SPRedirectSignatureFormAuthenticator.idpAddress is configured to use a validating alias with value 192.168.1.1.</li> 
    *       <li>If you change the SP address (SP_PROFILE/WEB-INF/picketlink-idfed.xml) the test will fail because the IDP's keystore is only configured to use a validating alias with value 192.168.1.2.</li>
    *       <li>If you ommit the SPRedirectSignatureFormAuthenticator.idpAddress because the user's address will be used to validate the token. His address is not in the keystore.</li>
    *       <li>If you ommit the IDPWebBrowserSSOValve.validatingAliasToTokenIssuer because the user's address will be used to validate the token. His address is not in the keystore.</li>
    *   </ul>
    */
   @Test
   public void testSAML2RedirectWithDifferentConsumerAndProvider() throws Exception
   {
      testWorkflow("192.168.1.3", "192.168.1.1");
   }

   private void testWorkflow(String userAddress, String idpAddress)
         throws LifecycleException, IOException, ServletException
   {
      MockCatalinaRequest request = createRequest(userAddress, false);

      // Sends a initial request to the SP. Requesting a resource ...
      MockCatalinaResponse idpAuthRequest = sendSPRequest(request, false, idpAddress);

      assertNotNull("Redirect String can not be null.", idpAuthRequest.redirectString);

      // Sends a auth request to the IDP
      request = createRequest(userAddress, true);

      setQueryStringFromResponse(idpAuthRequest, request);

      MockCatalinaResponse idpAuthResponse = sendIDPRequest(request);

      assertNotNull("Redirect String can not be null.", idpAuthResponse.redirectString);

      // Sends the IDP response to the SP. Now the user is succesfully authenticated and access for the requested resource is granted...    
      request = createRequest(userAddress, false);

      setQueryStringFromResponse(idpAuthResponse, request);

      sendSPRequest(request, true, idpAddress);
   }

   private MockCatalinaResponse sendIDPRequest(MockCatalinaRequest request)
         throws LifecycleException, IOException, ServletException
   {
      IDPWebBrowserSSOValve idp = createIdentityProvider();

      MockCatalinaResponse response = new MockCatalinaResponse();

      idp.invoke(request, response);

      return response;
   }

   private MockCatalinaResponse sendSPRequest(MockCatalinaRequest request, boolean validateAuthentication,
         String idpAddress) throws LifecycleException, IOException
   {

      MockCatalinaResponse response = new MockCatalinaResponse();

      if (validateAuthentication)
      {
         Assert.assertTrue("Employee app succesfully authenticated.",
               getEmployeeServiceProvider().authenticate(request, response, new MockCatalinaLoginConfig()));
      }
      else
      {
         getEmployeeServiceProvider().authenticate(request, response, new MockCatalinaLoginConfig());
      }

      return response;
   }

   public SPRedirectSignatureFormAuthenticator getEmployeeServiceProvider()
   {
      if (this.employeeServiceProvider == null)
      {
         this.employeeServiceProvider = createServiceProvider(SP_EMPLOYEE_PROFILE);
      }

      return this.employeeServiceProvider;
   }

}

/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;

import junit.framework.Assert;

import org.apache.catalina.LifecycleException;
import org.junit.Test;
import org.picketlink.identity.federation.bindings.tomcat.idp.IDPWebBrowserSSOValve;
import org.picketlink.identity.federation.bindings.tomcat.sp.SPRedirectSignatureFormAuthenticator;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaLoginConfig;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaRequest;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaResponse;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaSession;

/**
 * <p>Unit test the SAML2 Logout Mechanism for Tomcat bindings with token signature.</>
 * <p>This test uses a scenario where there are two SPs (Employee e Sales) pointing to the same IDP. When the user sends a GLO logout request to the Employee SP
 * Picketlink will start the logout process and invalidate the user in both SPs.</p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 * @since Dec 1, 2011
 */
@SuppressWarnings("unused")
public class SAML2LogoutSignatureTomcatWorkflowUnitTestCase extends AbstractSAML2RedirectWithSignatureTestCase
{
   private static final String SP_SALES_URL = "http://192.168.1.4:8080/sales/";

   private static final String SP_SALES_PROFILE = BASE_PROFILE + "/sp/sales-sig";

   private static final String SP_EMPLOYEE_URL = "http://192.168.1.2:8080/employee/";

   private static final String SP_EMPLOYEE_PROFILE = BASE_PROFILE + "/sp/employee-sig";

   private IDPWebBrowserSSOValve idpWebBrowserSSOValve;

   private MockCatalinaSession employeeHttpSession = new MockCatalinaSession();

   private MockCatalinaSession salesHttpSession = new MockCatalinaSession();

   private SPRedirectSignatureFormAuthenticator salesServiceProvider;

   private SPRedirectSignatureFormAuthenticator employeeServiceProvider;

   /**
    * Tests the GLO logout mechanism. 
    * 
    * @throws LifecycleException
    * @throws IOException
    * @throws ServletException
    */
   @Test
   public void testSAML2LogOutFromSP() throws LifecycleException, IOException, ServletException
   {

      // requests a GLO logout to the Employee SP
      MockCatalinaRequest originalEmployeeLogoutRequest = createRequest(employeeHttpSession, true);

      originalEmployeeLogoutRequest.setParameter(GeneralConstants.GLOBAL_LOGOUT, "true");

      MockCatalinaResponse originalEmployeeLogoutResponse = sendSPRequest(originalEmployeeLogoutRequest,
            getEmployeeServiceProvider());

      // sends the LogoutRequest to the IDP
      MockCatalinaRequest idpLogoutRequest = createIDPRequest(true);

      setQueryStringFromResponse(originalEmployeeLogoutResponse, idpLogoutRequest);

      MockCatalinaResponse idpLogoutResponse = sendIDPRequest(idpLogoutRequest);

      // The IDP responds with a LogoutRequest. Send it to the Sales SP with the RelayState pointing to the Employee SP
      MockCatalinaRequest salesLogoutRequest = createRequest(salesHttpSession, true);

      setQueryStringFromResponse(idpLogoutResponse, salesLogoutRequest);

      MockCatalinaResponse salesLogoutResponse = sendSPRequest(salesLogoutRequest, getSalesServiceProvider());
      
      // At this moment the user is not logged in Sales SP anymore.
      assertTrue(this.salesHttpSession.isInvalidated());
      
      // sends the StatusResponse to the IDP to continue the logout process.
      MockCatalinaRequest processSalesStatusResponse = createIDPRequest(true);

      setQueryStringFromResponse(salesLogoutResponse, processSalesStatusResponse);

      MockCatalinaResponse salesStatusResponse = sendIDPRequest(processSalesStatusResponse);

      // The IDP responds with a LogoutRequest. Send it to the Employee SP.
      MockCatalinaRequest employeeLogoutRequest = createRequest(employeeHttpSession, true);

      setQueryStringFromResponse(salesStatusResponse, employeeLogoutRequest);

      MockCatalinaResponse employeeLogoutResponse = sendSPRequest(employeeLogoutRequest, getEmployeeServiceProvider());
      
      // At this moment the user is not logged in Employee SP anymore.
      assertTrue(this.employeeHttpSession.isInvalidated());
      
      Assert.assertNotNull(employeeLogoutRequest.getForwardPath());
      Assert.assertEquals(employeeLogoutRequest.getForwardPath(), GeneralConstants.LOGOUT_PAGE_NAME);
      assertEquals(0, getIdentityServer(getIDPWebBrowserSSOValve()).stack().getParticipants(getIDPHttpSession().getId()));
      assertEquals(0, getIdentityServer(getIDPWebBrowserSSOValve()).stack().getNumOfParticipantsInTransit(getIDPHttpSession().getId()));

      //Finally the session should be invalidated
      assertTrue(getIDPHttpSession().isInvalidated());
   }

   private MockCatalinaResponse sendSPRequest(MockCatalinaRequest request, SPRedirectSignatureFormAuthenticator sp) throws LifecycleException,
         IOException, ServletException
   {
      MockCatalinaResponse response = new MockCatalinaResponse();

      sp.authenticate(request, response, new MockCatalinaLoginConfig());

      return response;
   }

   private MockCatalinaResponse sendIDPRequest(MockCatalinaRequest request) throws LifecycleException, IOException,
         ServletException
   {
      IDPWebBrowserSSOValve idp = getIDPWebBrowserSSOValve();

      MockCatalinaResponse response = new MockCatalinaResponse();

      response.setWriter(new PrintWriter(new ByteArrayOutputStream()));

      idp.invoke(request, response);
      
      ((MockCatalinaSession) request.getSession()).clear();
      
      return response;
   }

   private IDPWebBrowserSSOValve getIDPWebBrowserSSOValve() throws LifecycleException
   {
      if (this.idpWebBrowserSSOValve == null)
      {
         this.idpWebBrowserSSOValve = createIdentityProvider();
         addIdentityServerParticipants(this.idpWebBrowserSSOValve, SP_EMPLOYEE_URL);
         addIdentityServerParticipants(this.idpWebBrowserSSOValve, SP_SALES_URL);
      }

      return this.idpWebBrowserSSOValve;
   }
   
   public SPRedirectSignatureFormAuthenticator getEmployeeServiceProvider() {
      if (this.employeeServiceProvider == null)
      {
         this.employeeServiceProvider = createServiceProvider(SP_EMPLOYEE_PROFILE);
      }

      return this.employeeServiceProvider;
   }
   
   public SPRedirectSignatureFormAuthenticator getSalesServiceProvider() {
      if (this.salesServiceProvider == null)
      {
         this.salesServiceProvider = createServiceProvider(SP_SALES_PROFILE);
      }

      return this.salesServiceProvider;
   }
}
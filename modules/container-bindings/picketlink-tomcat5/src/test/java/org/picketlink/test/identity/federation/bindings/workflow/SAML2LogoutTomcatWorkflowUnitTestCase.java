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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.realm.GenericPrincipal;
import org.junit.Test;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.bindings.tomcat.idp.IDPWebBrowserSSOValve;
import org.picketlink.identity.federation.bindings.tomcat.sp.SPRedirectFormAuthenticator;
import org.picketlink.identity.federation.saml.v2.protocol.LogoutRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.StatusResponseType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.core.IdentityServer;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaContext;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaContextClassLoader;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaLoginConfig;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaRealm;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaRequest;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaResponse;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaSession;

/**
 * Unit test the SAML2 Logout Mechanism for Tomcat bindings
 * @author Anil.Saldhana@redhat.com
 * @since Oct 21, 2009
 */
@SuppressWarnings("unused")
public class SAML2LogoutTomcatWorkflowUnitTestCase
{
   private final String profile = "saml2/logout";

   private ClassLoader tcl;

   private final String IDP = "http://localhost:8080/idp/";

   private final String employee = "http://localhost:8080/employee/";

   private final String sales = "http://localhost:8080/sales/";

   private final String RELAY_STATE_KEY = "RelayState=";

   private final String SAML_REQUEST_KEY = "SAMLRequest=";

   private final String SAML_RESPONSE_KEY = "SAMLResponse=";

   /**
    * Test that the SP Redirect Authenticator generates the logout request
    * to the IDP when there is a parameter "GLO" set to true
    * @see {@code GeneralConstants#GLOBAL_LOGOUT}
    * @throws Exception
    */
   @Test
   public void testSPLogOutRequestGeneration() throws Exception
   {
      MockCatalinaSession session = new MockCatalinaSession();
      session.setAttribute(GeneralConstants.PRINCIPAL_ID, new Principal()
      {
         public String getName()
         {
            return "anil";
         }
      });
      List<String> rolesList = new ArrayList<String>();
      rolesList.add("manager");
      session.setAttribute(GeneralConstants.ROLES_ID, rolesList);

      MockCatalinaContext context = new MockCatalinaContext();
      session.setServletContext(context);

      //Let us feed the LogOutRequest to the SPFilter
      MockCatalinaContextClassLoader mclSPEmp = setupTCL(profile + "/sp/employee");
      Thread.currentThread().setContextClassLoader(mclSPEmp);

      SPRedirectFormAuthenticator sp = new SPRedirectFormAuthenticator();
      sp.setContainer(context);
      sp.testStart();

      MockCatalinaRequest catalinaRequest = new MockCatalinaRequest();
      MockCatalinaResponse response = new MockCatalinaResponse();
      MockCatalinaLoginConfig loginConfig = new MockCatalinaLoginConfig();

      ByteArrayOutputStream filterbaos = new ByteArrayOutputStream();
      response.setWriter(new PrintWriter(filterbaos));
      catalinaRequest.setParameter(GeneralConstants.GLOBAL_LOGOUT, "true");
      sp.authenticate(catalinaRequest, response, loginConfig);

      String redirectStr = response.redirectString;
      String logoutRequest = redirectStr.substring(redirectStr.indexOf(SAML_REQUEST_KEY) + SAML_REQUEST_KEY.length());

      InputStream stream = RedirectBindingUtil.urlBase64DeflateDecode(logoutRequest);

      SAML2Request saml2Request = new SAML2Request();
      LogoutRequestType lor = (LogoutRequestType) saml2Request.getRequestType(stream);
      assertEquals("Match Employee URL", employee, lor.getIssuer().getValue());
   }

   @Test
   public void testSAML2LogOutFromIDP() throws Exception
   {
      MockCatalinaSession session = new MockCatalinaSession();

      MockCatalinaContextClassLoader mclIDP = setupTCL(profile + "/idp");
      Thread.currentThread().setContextClassLoader(mclIDP);

      MockCatalinaContext catalinaContext = new MockCatalinaContext();
      session.setServletContext(catalinaContext);

      IdentityServer server = this.getIdentityServer(session);
      catalinaContext.setAttribute("IDENTITY_SERVER", server);

      IDPWebBrowserSSOValve idp = new IDPWebBrowserSSOValve();

      idp.setContainer(catalinaContext);
      idp.setSignOutgoingMessages(false);
      idp.setIgnoreIncomingSignatures(true);
      idp.start();

      //Assume that we already have the principal and roles set in the session
      MockCatalinaRealm realm = new MockCatalinaRealm("anil", "test", new Principal()
      {
         public String getName()
         {
            return "anil";
         }
      });
      List<String> roles = new ArrayList<String>();
      roles.add("manager");
      roles.add("employee");

      List<String> rolesList = new ArrayList<String>();
      rolesList.add("manager");

      MockCatalinaRequest request = new MockCatalinaRequest();
      session.clear();
      request.setSession(session);

      request.addHeader("Referer", sales);

      GenericPrincipal genericPrincipal = new GenericPrincipal(realm, "anil", "test", roles);
      request.setUserPrincipal(genericPrincipal);

      String samlMessage = RedirectBindingUtil.deflateBase64Encode(createLogOutRequest(sales).getBytes());
      request.setParameter("SAMLRequest", samlMessage);

      MockCatalinaResponse response = new MockCatalinaResponse();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      response.setWriter(new PrintWriter(baos));

      // The IDP is preloaded with 2 participants : "http://localhost:8080/sales/"
      // and "http://localhost:8080/employee"

      //Lets start the workflow with get 
      request.setMethod("GET");
      idp.invoke(request, response);

      String redirectStr = response.redirectString;

      String destination = redirectStr.substring(0, redirectStr.indexOf(SAML_REQUEST_KEY) - 1);
      String relayState = redirectStr.substring(redirectStr.indexOf(RELAY_STATE_KEY) + RELAY_STATE_KEY.length());
      String logoutRequest = redirectStr.substring(redirectStr.indexOf(SAML_REQUEST_KEY) + SAML_REQUEST_KEY.length(),
            redirectStr.indexOf(RELAY_STATE_KEY) - 1);

      InputStream stream = RedirectBindingUtil.urlBase64DeflateDecode(logoutRequest);

      SAML2Request saml2Request = new SAML2Request();
      LogoutRequestType lor = (LogoutRequestType) saml2Request.getRequestType(stream);
      assertEquals("Match Employee URL", employee, destination);
      assertEquals("Destination exists", employee, lor.getDestination().toString());

      //Let us feed the LogOutRequest to the SPFilter
      MockCatalinaContextClassLoader mclSPEmp = setupTCL(profile + "/sp/employee");
      Thread.currentThread().setContextClassLoader(mclSPEmp);

      MockCatalinaContext context = new MockCatalinaContext();
      context.setRealm(realm);
      session.setServletContext(context);

      SPRedirectFormAuthenticator sp = new SPRedirectFormAuthenticator();
      sp.setContainer(context);
      sp.testStart();

      request = new MockCatalinaRequest();
      request.setSession(session);
      request.setParameter("SAMLRequest", RedirectBindingUtil.urlDecode(logoutRequest));
      request.setParameter("RelayState", relayState);

      MockCatalinaResponse filterResponse = new MockCatalinaResponse();
      ByteArrayOutputStream filterbaos = new ByteArrayOutputStream();
      filterResponse.setWriter(new PrintWriter(filterbaos));

      sp.authenticate(request, response, new LoginConfig());

      redirectStr = response.redirectString;

      destination = redirectStr.substring(0, redirectStr.indexOf(SAML_RESPONSE_KEY) - 1);
      relayState = redirectStr.substring(redirectStr.indexOf(RELAY_STATE_KEY) + RELAY_STATE_KEY.length());
      assertNotNull("RelayState exists", relayState);
      String logoutResponse = redirectStr.substring(
            redirectStr.indexOf(SAML_RESPONSE_KEY) + SAML_RESPONSE_KEY.length(),
            redirectStr.indexOf(RELAY_STATE_KEY) - 1);

      stream = RedirectBindingUtil.urlBase64DeflateDecode(logoutResponse);
      StatusResponseType statusResponse = (StatusResponseType) saml2Request.getSAML2ObjectFromStream(stream);
      assertEquals("Match IDP URL", IDP, destination);

      //Now the SP (employee app) has logged out and sending a status response to IDP
      Thread.currentThread().setContextClassLoader(mclIDP);

      session.clear();
      request.clear();

      request.setMethod("GET");
      request.setSession(session);
      request.setUserPrincipal(genericPrincipal);
      request.setParameter("SAMLResponse", RedirectBindingUtil.urlDecode(logoutResponse));
      request.setParameter("RelayState", relayState);

      idp.invoke(request, response);

      destination = redirectStr.substring(0, redirectStr.indexOf(SAML_RESPONSE_KEY) - 1);
      relayState = redirectStr.substring(redirectStr.indexOf(RELAY_STATE_KEY) + RELAY_STATE_KEY.length());
      logoutResponse = redirectStr.substring(redirectStr.indexOf(SAML_RESPONSE_KEY) + SAML_RESPONSE_KEY.length(),
            redirectStr.indexOf(RELAY_STATE_KEY) - 1);

      stream = RedirectBindingUtil.urlBase64DeflateDecode(logoutResponse);

      SAML2Response saml2Response = new SAML2Response();
      statusResponse = (StatusResponseType) saml2Request.getSAML2ObjectFromStream(stream);
      assertEquals("Match IDP URL", IDP, destination);

      //Now we should have got a full success report from IDP
      MockCatalinaContextClassLoader mclSPSales = setupTCL(profile + "/sp/employee");
      Thread.currentThread().setContextClassLoader(mclSPSales);
      sp = new SPRedirectFormAuthenticator();
      sp.setContainer(context);
      sp.testStart();

      session.clear();
      request.clear();
      request.setSession(session);
      request.setUserPrincipal(genericPrincipal);
      request.setParameter("SAMLResponse", RedirectBindingUtil.urlDecode(logoutResponse));
      request.setParameter("RelayState", relayState);
      request.setContext(context);

      sp.authenticate(request, response, new LoginConfig());

      assertEquals(0, server.stack().getParticipants(session.getId()));
      assertEquals(0, server.stack().getNumOfParticipantsInTransit(session.getId()));

      //Finally the session should be invalidated
      assertTrue(session.isInvalidated());
   }

   private MockCatalinaContextClassLoader setupTCL(String resource)
   {
      tcl = Thread.currentThread().getContextClassLoader();
      URL[] urls = new URL[]
      {tcl.getResource(resource)};

      MockCatalinaContextClassLoader mcl = new MockCatalinaContextClassLoader(urls);
      mcl.setDelegate(tcl);
      mcl.setProfile(resource);
      return mcl;
   }

   private String createLogOutRequest(String url) throws Exception
   {
      SAML2Request samlRequest = new SAML2Request();
      LogoutRequestType lot = samlRequest.createLogoutRequest(url);
      StringWriter sw = new StringWriter();
      samlRequest.marshall(lot, sw);
      return sw.toString();
   }

   //Get the Identity server with 2 participants
   private IdentityServer getIdentityServer(HttpSession session)
   {
      IdentityServer server = new IdentityServer();
      server.sessionCreated(new HttpSessionEvent(session));

      server.stack().register(session.getId(), sales, false);
      server.stack().register(session.getId(), employee, false);
      return server;
   }
}
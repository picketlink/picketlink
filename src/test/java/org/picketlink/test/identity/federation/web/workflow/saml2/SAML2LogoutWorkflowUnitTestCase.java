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
package org.picketlink.test.identity.federation.web.workflow.saml2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

import org.junit.Test;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.Base64;
import org.picketlink.identity.federation.saml.v2.SAML2Object;
import org.picketlink.identity.federation.saml.v2.protocol.LogoutRequestType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.core.IdentityServer;
import org.picketlink.identity.federation.web.filters.SPFilter;
import org.picketlink.identity.federation.web.servlets.IDPServlet;
import org.picketlink.identity.federation.web.util.PostBindingUtil;
import org.picketlink.test.identity.federation.web.mock.MockContextClassLoader;
import org.picketlink.test.identity.federation.web.mock.MockFilterChain;
import org.picketlink.test.identity.federation.web.mock.MockFilterConfig;
import org.picketlink.test.identity.federation.web.mock.MockHttpServletRequest;
import org.picketlink.test.identity.federation.web.mock.MockHttpServletResponse;
import org.picketlink.test.identity.federation.web.mock.MockHttpSession;
import org.picketlink.test.identity.federation.web.mock.MockServletConfig;
import org.picketlink.test.identity.federation.web.mock.MockServletContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Unit test the SAML2 Logout workflow
 * @author Anil.Saldhana@redhat.com
 * @since Oct 7, 2009
 */
public class SAML2LogoutWorkflowUnitTestCase
{
   private final String profile = "saml2/logout";

   private ClassLoader tcl;

   private final String employee = "http://localhost:8080/employee/";

   private final String sales = "http://localhost:8080/sales/";

   /**
    * Test that the SP web filter generates the logout request
    * to the IDP when there is a parameter "GLO" set to true
    * @see {@code GeneralConstants#GLOBAL_LOGOUT}
    * @throws Exception
    */
   @Test
   public void testSPFilterLogOutRequestGeneration() throws Exception
   {
      tcl = Thread.currentThread().getContextClassLoader();

      MockHttpSession session = new MockHttpSession();
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

      ServletContext servletContext = new MockServletContext();
      session.setServletContext(servletContext);

      //Let us feed the LogOutRequest to the SPFilter
      MockContextClassLoader mclSPEmp = setupTCL(profile + "/sp/employee");
      Thread.currentThread().setContextClassLoader(mclSPEmp);
      SPFilter spEmpl = new SPFilter();
      MockFilterConfig filterConfig = new MockFilterConfig(servletContext);
      filterConfig.addInitParameter(GeneralConstants.IGNORE_SIGNATURES, "true");

      spEmpl.init(filterConfig);

      MockHttpSession filterSession = new MockHttpSession();
      MockHttpServletRequest filterRequest = new MockHttpServletRequest(filterSession, "POST");
      filterRequest.addParameter(GeneralConstants.GLOBAL_LOGOUT, "true");

      MockHttpServletResponse filterResponse = new MockHttpServletResponse();
      ByteArrayOutputStream filterbaos = new ByteArrayOutputStream();
      filterResponse.setWriter(new PrintWriter(filterbaos));

      spEmpl.doFilter(filterRequest, filterResponse, new MockFilterChain());

      String spResponse = new String(filterbaos.toByteArray());
      Document spHTMLResponse = DocumentUtil.getDocument(spResponse);
      NodeList nodes = spHTMLResponse.getElementsByTagName("INPUT");
      Element inputElement = (Element) nodes.item(0);
      String logoutRequest = inputElement.getAttributeNode("VALUE").getValue();

      byte[] b64Decoded = PostBindingUtil.base64Decode(logoutRequest);
      SAML2Request saml2Request = new SAML2Request();
      LogoutRequestType lor = (LogoutRequestType) saml2Request.getRequestType(new ByteArrayInputStream(b64Decoded));
      assertEquals("Match Employee URL", employee, lor.getIssuer().getValue());
   }

   /**
    * In this test case, we preload the IDP with 2 active participants
    * namely the Sales app and Employee App. After this, the employee app
    * issues a logout request. The IDP is supposed to receive this logout request,
    * a) note that there are 2 session participants
    * b) issue a logout request to the sales app
    * c) the sales app invalidates its session
    * d) the sales app issues a logout response (status response type) to the IDP
    * e) the IDP sees that we have 1 participant left and because it is the same as the
    *    original logout requestor, invalidates its session and sends the logout success
    *    to the employee app. 
    * f) employee app invalidates its session
    * @throws Exception
    */
   @Test
   public void testSAML2LogOutFromIDPServlet() throws Exception
   {
      tcl = Thread.currentThread().getContextClassLoader();
      MockHttpSession session = new MockHttpSession();

      MockContextClassLoader mclIDP = setupTCL(profile + "/idp");
      Thread.currentThread().setContextClassLoader(mclIDP);

      URL url = Thread.currentThread().getContextClassLoader().getResource("roles.properties");
      assertNotNull("roles.properties visible?", url);

      ServletContext servletContext = new MockServletContext();
      session.setServletContext(servletContext);

      IdentityServer server = this.getIdentityServer(session);
      servletContext.setAttribute("IDENTITY_SERVER", server);
      MockServletConfig servletConfig = new MockServletConfig(servletContext);

      IDPServlet idp = new IDPServlet();
      //No signing outgoing messages
      servletConfig.addInitParameter(GeneralConstants.SIGN_OUTGOING_MESSAGES, "false");

      //Initialize the servlet
      idp.init(servletConfig);

      //Assume that we already have the principal and roles set in the session
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

      MockHttpServletRequest request = new MockHttpServletRequest(session, "POST");
      request.addHeader("Referer", sales);

      String samlMessage = Base64.encodeBytes(createLogOutRequest(sales).getBytes());
      session.setAttribute("SAMLRequest", samlMessage);

      MockHttpServletResponse response = new MockHttpServletResponse();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      response.setWriter(new PrintWriter(baos));

      // The IDP is preloaded with 2 participants : "http://localhost:8080/sales/"
      // and "http://localhost:8080/employee"

      //Lets start the workflow with post 
      idp.testPost(request, response);

      String idpResponse = new String(baos.toByteArray());
      assertNotNull(idpResponse);

      Document htmlResponse = DocumentUtil.getDocument(idpResponse);
      assertNotNull(htmlResponse);
      NodeList nodes = htmlResponse.getElementsByTagName("INPUT");
      Element inputElement = (Element) nodes.item(0);
      String logoutOrigResponse = inputElement.getAttributeNode("VALUE").getValue();

      String relayState = null;
      if (nodes.getLength() > 1)
         relayState = ((Element) nodes.item(1)).getAttributeNode("VALUE").getValue();

      String logoutResponse = new String(Base64.decode(logoutOrigResponse));

      SAML2Request samlRequest = new SAML2Request();
      ByteArrayInputStream bis = new ByteArrayInputStream(logoutResponse.getBytes());
      SAML2Object samlObject = samlRequest.getSAML2ObjectFromStream(bis);
      assertTrue(samlObject instanceof LogoutRequestType);

      //Let us feed the LogOutRequest to the SPFilter
      MockContextClassLoader mclSPEmp = setupTCL(profile + "/sp/employee");
      Thread.currentThread().setContextClassLoader(mclSPEmp);
      SPFilter spEmpl = new SPFilter();
      MockFilterConfig filterConfig = new MockFilterConfig(servletContext);
      filterConfig.addInitParameter(GeneralConstants.IGNORE_SIGNATURES, "true");

      spEmpl.init(filterConfig);

      MockHttpSession filterSession = new MockHttpSession();
      MockHttpServletRequest filterRequest = new MockHttpServletRequest(filterSession, "POST");
      filterRequest.addParameter("SAMLResponse", logoutOrigResponse);
      filterRequest.addParameter("RelayState", relayState);

      MockHttpServletResponse filterResponse = new MockHttpServletResponse();
      ByteArrayOutputStream filterbaos = new ByteArrayOutputStream();
      filterResponse.setWriter(new PrintWriter(filterbaos));

      spEmpl.doFilter(filterRequest, filterResponse, new MockFilterChain());
      String spResponse = new String(filterbaos.toByteArray());
      Document spHTMLResponse = DocumentUtil.getDocument(spResponse);
      nodes = spHTMLResponse.getElementsByTagName("INPUT");
      inputElement = (Element) nodes.item(0);
      logoutOrigResponse = inputElement.getAttributeNode("VALUE").getValue();
      relayState = null;
      if (nodes.getLength() > 1)
         relayState = ((Element) nodes.item(1)).getAttributeNode("VALUE").getValue();

      //Now the SP (employee app) has logged out and sending a status response to IDP
      Thread.currentThread().setContextClassLoader(mclIDP);
      session.setAttribute("SAMLResponse", logoutOrigResponse);
      session.setAttribute("RelayState", relayState);

      idp.testPost(request, response);

      idpResponse = new String(filterbaos.toByteArray());
      assertNotNull(idpResponse);

      htmlResponse = DocumentUtil.getDocument(idpResponse);
      assertNotNull(htmlResponse);
      nodes = htmlResponse.getElementsByTagName("INPUT");
      inputElement = (Element) nodes.item(0);
      logoutOrigResponse = inputElement.getAttributeNode("VALUE").getValue();

      relayState = null;
      if (nodes.getLength() > 1)
         relayState = ((Element) nodes.item(1)).getAttributeNode("VALUE").getValue();

      //Now we should have got a full success report from IDP
      MockContextClassLoader mclSPSales = setupTCL(profile + "/sp/employee");
      Thread.currentThread().setContextClassLoader(mclSPSales);
      SPFilter spSales = new SPFilter();

      spSales.init(filterConfig);

      filterRequest.addParameter("SAMLResponse", logoutOrigResponse);
      filterRequest.addParameter("RelayState", relayState);

      spSales.doFilter(filterRequest, filterResponse, new MockFilterChain());

      spResponse = new String(filterbaos.toByteArray());

      assertEquals(0, server.stack().getParticipants(session.getId()));
      assertEquals(0, server.stack().getNumOfParticipantsInTransit(session.getId()));

      spHTMLResponse = DocumentUtil.getDocument(spResponse);
      nodes = spHTMLResponse.getElementsByTagName("INPUT");
      inputElement = (Element) nodes.item(0);
      logoutOrigResponse = inputElement.getAttributeNode("VALUE").getValue();
      relayState = null;
      if (nodes.getLength() > 1)
         relayState = ((Element) nodes.item(1)).getAttributeNode("VALUE").getValue();

      //Finally the session should be invalidated
      assertTrue(filterSession.isInvalidated());
   }

   private MockContextClassLoader setupTCL(String resource)
   {
      URL[] urls = new URL[]
      {tcl.getResource(resource)};

      MockContextClassLoader mcl = new MockContextClassLoader(urls);
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
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

import junit.framework.TestCase;

import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.Base64; 
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.core.IdentityServer;
import org.picketlink.identity.federation.web.filters.SPFilter;
import org.picketlink.identity.federation.web.servlets.IDPLoginServlet;
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
 * Unit test the workflow for SAML2 Post Binding
 * @author Anil.Saldhana@redhat.com
 * @since Oct 8, 2009
 */
public class SAML2PostWorkflowUnitTestCase extends TestCase
{
   private String profile = "saml2/post";
   private ClassLoader tcl = Thread.currentThread().getContextClassLoader();
   
   private String employee = "http://localhost:8080/employee/";
   private String identity = "http://localhost:8080/idp/";
   
   public void testAuthForIDPServletAndSPFilter() throws Exception
   {
      String id = IDGenerator.create("ID_");
      SAML2Request saml2Request = new SAML2Request();
      AuthnRequestType art = saml2Request.createAuthnRequestType(id, 
            employee, identity, employee);
      
      ServletContext servletContext = new MockServletContext();
      
      //First we go to the employee application
      MockContextClassLoader mclSPEmp = setupTCL(profile + "/sp/employee");
      Thread.currentThread().setContextClassLoader(mclSPEmp);
      SPFilter spEmpl = new SPFilter();
      MockFilterConfig filterConfig = new MockFilterConfig(servletContext);
      filterConfig.addInitParameter(GeneralConstants.IGNORE_SIGNATURES, "true");
      
      spEmpl.init(filterConfig);
      
      MockHttpSession filterSession = new MockHttpSession();
      MockHttpServletRequest filterRequest = new MockHttpServletRequest(filterSession, "POST");
     
      MockHttpServletResponse filterResponse = new MockHttpServletResponse();
      ByteArrayOutputStream filterbaos = new ByteArrayOutputStream();
      filterResponse.setWriter(new PrintWriter(filterbaos));
      
      spEmpl.doFilter(filterRequest, filterResponse, new MockFilterChain());
      String spResponse = new String(filterbaos.toByteArray());
      Document spHTMLResponse = DocumentUtil.getDocument(spResponse);
      NodeList nodes = spHTMLResponse.getElementsByTagName("INPUT");
      Element inputElement = (Element)nodes.item(0);
      String idpResponse = inputElement.getAttributeNode("VALUE").getValue();
      @SuppressWarnings("unused")
      String relayState = null;
      if(nodes.getLength() > 1)
         relayState = ((Element)nodes.item(1)).getAttributeNode("VALUE").getValue();
      
      //Lets call the IDPServlet

      MockHttpSession session = new MockHttpSession();
      servletContext = new MockServletContext();
      session.setServletContext(servletContext);
      IdentityServer server = this.getIdentityServer(session);
      servletContext.setAttribute("IDENTITY_SERVER", server);
      MockServletConfig servletConfig = new MockServletConfig(servletContext);
      
      MockContextClassLoader mclIDP = setupTCL(profile + "/idp");
      Thread.currentThread().setContextClassLoader(mclIDP);
      
      MockHttpServletRequest request = new MockHttpServletRequest(session, "POST"); 
      request.addHeader("Referer", "http://localhost:8080/employee/");
      
      request.addParameter(GeneralConstants.USERNAME_FIELD, "anil");
      request.addParameter(GeneralConstants.PASS_FIELD, "anil");
      

      MockHttpServletResponse response = new MockHttpServletResponse();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      response.setWriter(new PrintWriter(baos));
      
      IDPLoginServlet login = new IDPLoginServlet();
      login.init(servletConfig);
      
      String samlAuth = DocumentUtil.getDocumentAsString(saml2Request.convert(art));
      
      String samlMessage = Base64.encodeBytes(samlAuth.getBytes());
      session.setAttribute("SAMLRequest", samlMessage);
      
      login.testPost(request, response);
      
      IDPServlet idp = new IDPServlet();
      //No signing outgoing messages
      servletConfig.addInitParameter(GeneralConstants.SIGN_OUTGOING_MESSAGES, "false");
      
      //Initialize the servlet
      idp.init(servletConfig); 
      
      //Lets start the workflow with post 
      idp.testPost(request, response);   
    
      String idpResponseString = new String(baos.toByteArray());
      Document idpHTMLResponse = DocumentUtil.getDocument(idpResponseString);
      nodes = idpHTMLResponse.getElementsByTagName("INPUT");
      inputElement = (Element)nodes.item(0);
      idpResponse = inputElement.getAttributeNode("VALUE").getValue();
      relayState = null;
      if(nodes.getLength() > 1)
         relayState = ((Element)nodes.item(1)).getAttributeNode("VALUE").getValue();
      
      byte[] samlIDPResponse = PostBindingUtil.base64Decode(idpResponse);
      
      SAML2Response saml2Response = new SAML2Response();
      ResponseType rt = saml2Response.getResponseType(new ByteArrayInputStream(samlIDPResponse));
      
      assertEquals("Match Identity URL:" , this.identity, rt.getIssuer().getValue()); 
   }
   
   private MockContextClassLoader setupTCL(String resource)
   {
      URL[] urls = new URL[] {tcl.getResource(resource)};
      
      MockContextClassLoader mcl = new MockContextClassLoader(urls);
      mcl.setDelegate(tcl);
      mcl.setProfile(resource);
      return mcl;
   }
   

   //Get the Identity server 
   private IdentityServer getIdentityServer(HttpSession session)
   {
      IdentityServer server = new IdentityServer();
      server.sessionCreated(new HttpSessionEvent(session)); 
      return server;
   } 
}
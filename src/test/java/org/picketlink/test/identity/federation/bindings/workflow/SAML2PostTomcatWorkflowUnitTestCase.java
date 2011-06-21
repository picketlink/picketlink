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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

import junit.framework.TestCase;

import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.realm.GenericPrincipal;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.bindings.tomcat.idp.IDPWebBrowserSSOValve;
import org.picketlink.identity.federation.bindings.tomcat.sp.SPPostFormAuthenticator;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.Base64;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.core.IdentityServer;
import org.picketlink.identity.federation.web.util.PostBindingUtil;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaContext;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaContextClassLoader;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaRealm;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaRequest;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaResponse;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaSession;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Anil.Saldhana@redhat.com
 * @since Oct 29, 2009
 */
public class SAML2PostTomcatWorkflowUnitTestCase extends TestCase
{
   private String profile = "saml2/post";
   private ClassLoader tcl = Thread.currentThread().getContextClassLoader();
   
   private String employee = "http://localhost:8080/employee/";
   private String identity = "http://localhost:8080/idp/";
   
   public void testSAML2Post() throws Exception
   {
      String id = IDGenerator.create("ID_");
      SAML2Request saml2Request = new SAML2Request();
      AuthnRequestType art = saml2Request.createAuthnRequestType(id, 
            employee, identity, employee);
      
      MockCatalinaContext servletContext = new MockCatalinaContext();
      
      //First we go to the employee application
      MockCatalinaContextClassLoader mclSPEmp = setupTCL(profile + "/sp/employee");
      Thread.currentThread().setContextClassLoader(mclSPEmp);
      SPPostFormAuthenticator spEmpl = new SPPostFormAuthenticator();
      
      MockCatalinaContext context = new MockCatalinaContext();
      spEmpl.setContainer(context);
      spEmpl.testStart();
       
      MockCatalinaRequest catalinaRequest = new MockCatalinaRequest();
     
      MockCatalinaResponse catalinaResponse = new MockCatalinaResponse();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      catalinaResponse.setWriter(new PrintWriter(baos));
      
      LoginConfig loginConfig = new LoginConfig();
      spEmpl.authenticate(catalinaRequest, catalinaResponse, loginConfig);
      
      String spResponse = new String(baos.toByteArray());
      Document spHTMLResponse = DocumentUtil.getDocument(spResponse);
      NodeList nodes = spHTMLResponse.getElementsByTagName("INPUT");
      Element inputElement = (Element)nodes.item(0);
      String idpResponse = inputElement.getAttributeNode("VALUE").getValue();
      @SuppressWarnings("unused")
      String relayState = null;
      if(nodes.getLength() > 1)
         relayState = ((Element)nodes.item(1)).getAttributeNode("VALUE").getValue();
      
      //Lets call the IDPServlet

      MockCatalinaSession session = new MockCatalinaSession();
      servletContext = new MockCatalinaContext();
      session.setServletContext(servletContext);
      IdentityServer server = this.getIdentityServer(session);
      servletContext.setAttribute("IDENTITY_SERVER", server);
      
      
      MockCatalinaContextClassLoader mclIDP = setupTCL(profile + "/idp");
      Thread.currentThread().setContextClassLoader(mclIDP);
      
      MockCatalinaRequest request = new MockCatalinaRequest(); 
      request.addHeader("Referer", "http://localhost:8080/employee/");
      
      request.setParameter(GeneralConstants.USERNAME_FIELD, "anil");
      request.setParameter(GeneralConstants.PASS_FIELD, "anil");
      

      MockCatalinaResponse response = new MockCatalinaResponse();
      baos = new ByteArrayOutputStream();
      response.setWriter(new PrintWriter(baos));
      
      context = new MockCatalinaContext();
      IDPWebBrowserSSOValve idp = new IDPWebBrowserSSOValve();
      idp.setContainer(context);
      idp.setSignOutgoingMessages(false);
      idp.start();
      
      String samlAuth = DocumentUtil.getDocumentAsString(saml2Request.convert(art));
      
      String samlMessage = Base64.encodeBytes(samlAuth.getBytes()); 
      
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
      
      request = new MockCatalinaRequest();
      request.setRemoteAddr(employee);
      request.setSession(session);
      request.setParameter("SAMLRequest", samlMessage);
      request.setUserPrincipal(new GenericPrincipal(realm, "anil", "test", roles) );
      request.setMethod("POST");
      
      //Lets start the workflow with post 
      idp.invoke(request, response);   
    
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
   
   private MockCatalinaContextClassLoader setupTCL(String resource)
   {
      URL[] urls = new URL[] {tcl.getResource(resource)};
      
      MockCatalinaContextClassLoader mcl = new MockCatalinaContextClassLoader(urls);
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
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

import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.catalina.realm.GenericPrincipal;
import org.picketlink.identity.federation.bindings.tomcat.idp.IDPWebBrowserSSOValve;
import org.picketlink.identity.federation.bindings.tomcat.sp.SPRedirectFormAuthenticator;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;  
import org.picketlink.test.identity.federation.bindings.mock.*;

/**
 * Unit test for the Workflow for the SAML2 Redirect Binding
 * @author Anil.Saldhana@redhat.com
 * @since Oct 20, 2009
 */
public class SAML2RedirectTomcatWorkflowUnitTestCase extends TestCase
{
   private String profile = "saml2/redirect";
   private ClassLoader tcl = Thread.currentThread().getContextClassLoader();
   private String employee = "http://localhost:8080/employee/"; 
   
   private String SAML_REQUEST_KEY = "SAMLRequest=";

   private String SAML_RESPONSE_KEY = "SAMLResponse=";
   
   public void testSAML2Redirect() throws Exception
   {
      MockCatalinaContextClassLoader mclSPEmp = setupTCL(profile + "/sp/employee");
      Thread.currentThread().setContextClassLoader(mclSPEmp); 
      
      SPRedirectFormAuthenticator sp = new SPRedirectFormAuthenticator();
      
      MockCatalinaContext context = new MockCatalinaContext();
      MockCatalinaRequest request = new MockCatalinaRequest();
      
      request.setParameter(GeneralConstants.RELAY_STATE, null);
      
      MockCatalinaResponse response = new MockCatalinaResponse();
      MockCatalinaLoginConfig loginConfig = new MockCatalinaLoginConfig();
      
      sp.setContainer(context);
      sp.testStart();
      
      sp.authenticate(request, response, loginConfig);
      
      String redirectStr = response.redirectString;
      assertNotNull("Redirect String is null?", redirectStr);
      String saml = redirectStr.substring(redirectStr.indexOf(SAML_REQUEST_KEY) +
            SAML_REQUEST_KEY.length());
   
      MockCatalinaSession session = new MockCatalinaSession();
      
      //Now send it to IDP 
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
      
      MockCatalinaContextClassLoader mclIDP = setupTCL(profile + "/idp/");
      Thread.currentThread().setContextClassLoader(mclIDP);
      
      request = new MockCatalinaRequest();
      request.setRemoteAddr(employee);
      request.setSession(session);
      request.setParameter("SAMLRequest", RedirectBindingUtil.urlDecode(saml));
      request.setUserPrincipal(new GenericPrincipal(realm, "anil", "test", roles) );
      request.setMethod("GET");
      
      response = new MockCatalinaResponse();
      
      IDPWebBrowserSSOValve idp = new IDPWebBrowserSSOValve();
      
      idp.setSignOutgoingMessages(false);
      idp.setIgnoreIncomingSignatures(true);
      
      idp.setContainer(context);
      idp.start();
      idp.invoke(request, response); 
      
      redirectStr = response.redirectString;
      String samlResponse = RedirectBindingUtil.urlDecode(redirectStr.substring(redirectStr.indexOf(SAML_RESPONSE_KEY) +
            SAML_RESPONSE_KEY.length()));
      
      mclSPEmp = setupTCL(profile + "/sp/employee");
      Thread.currentThread().setContextClassLoader(mclSPEmp); 
      
      sp = new SPRedirectFormAuthenticator();
      
      context = new MockCatalinaContext();
      
      context.setRealm(realm);
      request = new MockCatalinaRequest();
      request.setContext(context);

      request.setMethod("GET");
      request.setParameter("SAMLResponse", samlResponse);
      request.setParameter("RelayState", null);
      request.setSession(session);
      
      response = new MockCatalinaResponse();
      loginConfig = new MockCatalinaLoginConfig();
      
      sp.setContainer(context);
      sp.testStart();
      
      assertTrue("Employee app auth success", sp.authenticate(request, response, loginConfig) ); 
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
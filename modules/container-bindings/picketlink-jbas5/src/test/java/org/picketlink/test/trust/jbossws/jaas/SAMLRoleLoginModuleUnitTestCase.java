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
package org.picketlink.test.trust.jbossws.jaas;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.jboss.security.SimplePrincipal;
import org.junit.Before;
import org.junit.Test;
import org.picketlink.identity.federation.bindings.jboss.subject.PicketLinkPrincipal;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.core.saml.v2.util.StatementUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLAssertionWriter;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.core.wstrust.SamlCredential;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.trust.jbossws.jaas.SAMLRoleLoginModule;

/**
 * Unit test the {@code SAMLRoleLoginModule}
 * @author Anil.Saldhana@redhat.com
 * @since Jun 6, 2011
 */
public class SAMLRoleLoginModuleUnitTestCase
{
   public static class MySAMLModule implements LoginModule
   {
      public MySAMLModule(){}
      
      private Subject theSubject = null;
      @SuppressWarnings("rawtypes")
      private Map sharedState = null;
      
      public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
            Map<String, ?> options)
      {
         theSubject = subject;
         this.sharedState = sharedState;
      }

      @SuppressWarnings("unchecked")
      public boolean login() throws LoginException
      {
         sharedState.put("javax.security.auth.login.name", new PicketLinkPrincipal(""));
         return true;
      }

      public boolean commit() throws LoginException
      {
         NameIDType issuer = new NameIDType();
         AssertionType assertion = AssertionUtil.createAssertion(IDGenerator.create("ID_"), issuer);
         
         List<String> roles = new ArrayList<String>();
         roles.add("test1"); roles.add("test2");
         
         AttributeStatementType att = StatementUtil.createAttributeStatement(roles);
         assertion.addStatement(att);
         
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         try
         {
            SAMLAssertionWriter writer = new SAMLAssertionWriter(StaxUtil.getXMLStreamWriter(baos));
            writer.write(assertion);
            SamlCredential cred = new SamlCredential(new String(baos.toByteArray()));
            theSubject.getPublicCredentials().add(cred);
         }
         catch (ProcessingException e)
         { 
            throw new RuntimeException(e);
         }
         return true;
      }

      public boolean abort() throws LoginException
      {
         return true;
      }

      public boolean logout() throws LoginException
      {
         return true;
      }
   }
   
   @Before
   public void setup()
   {
      Configuration.setConfiguration(new Configuration(){

         @SuppressWarnings({"rawtypes", "unchecked"})
         @Override
         public AppConfigurationEntry[] getAppConfigurationEntry(String name)
         {
            final Map options = new HashMap();
            
            AppConfigurationEntry a1 = new AppConfigurationEntry(MySAMLModule.class.getName(), LoginModuleControlFlag.REQUIRED, options);
            AppConfigurationEntry a2 = new AppConfigurationEntry(SAMLRoleLoginModule.class.getName(), LoginModuleControlFlag.REQUIRED, options);
            return new AppConfigurationEntry[]{a1,a2};
         }});
   }
   
   @Test
   public void testAuth() throws Exception
   {
      Subject subject = new Subject();
      
      LoginContext lc = new LoginContext("something", subject);
      lc.login();
      
      Set<Group> groups = subject.getPrincipals(Group.class);
      assertNotNull(groups);
      boolean foundMatch = false;
      for(Group gp: groups)
      {
          if(gp.getName().equals("Roles"))
          {
              assertTrue(gp.isMember(new SimplePrincipal("test1")));
              assertTrue(gp.isMember(new SimplePrincipal("test2")));
              foundMatch = true;
          }
      }
      assertTrue(foundMatch);
   }
}
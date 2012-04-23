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
package org.picketlink.test.identity.federation.bindings.jboss.auth;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;

import org.jboss.security.SimplePrincipal;
import org.jboss.security.auth.callback.ObjectCallback;
import org.junit.Before;
import org.junit.Test;
import org.picketlink.identity.federation.bindings.jboss.auth.SAML2STSLoginModule;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.core.saml.v2.util.StatementUtil;
import org.picketlink.identity.federation.core.wstrust.SamlCredential;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;

/**
 * Unit Test the {@code SAML2STSLoginModule}
 * @author Anil.Saldhana@redhat.com
 * @since Jun 7, 2011
 */
public class SAML2STSLoginModuleUnitTestCase
{
   @Before
   public void setup()
   {
      System.setProperty("java.security.debug", "true");

      Configuration.setConfiguration(new Configuration()
      {
         @SuppressWarnings(
         {"rawtypes", "unchecked"})
         @Override
         public AppConfigurationEntry[] getAppConfigurationEntry(String name)
         {
            final Map options = new HashMap();
            options.put("configFile", "sts-client.properties");
            options.put("localValidation", "true");
            options.put("localValidationSecurityDomain", "someSD");
            options.put("localTestingOnly", "true");
            options.put("roleKey", "Role,SomeAttrib");

            AppConfigurationEntry a2 = new AppConfigurationEntry(SAML2STSLoginModule.class.getName(),
                  LoginModuleControlFlag.REQUIRED, options);
            return new AppConfigurationEntry[]
            {a2};
         }
      });
   }

   public class MyCBH implements CallbackHandler
   {

      public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
      {
         AssertionType assertion = AssertionUtil.createAssertion(IDGenerator.create("ID_"), new NameIDType());

         assertion.setSubject(AssertionUtil.createAssertionSubject("anil"));

         List<String> roles = new ArrayList<String>();
         roles.add("test1");
         roles.add("test2");
         assertion.addStatement(StatementUtil.createAttributeStatement(roles));
         assertion.addStatement(StatementUtil.createAttributeStatement("SomeAttrib", "testX"));

         try
         {
            SamlCredential cred = new SamlCredential(AssertionUtil.asString(assertion));
            ObjectCallback obj = (ObjectCallback) callbacks[0];
            obj.setCredential(cred);
         }
         catch (ProcessingException e)
         {
            throw new RuntimeException(e);
         }
      }

   }

   @Test
   public void testAuth() throws Exception
   {
      Subject subject = new Subject();

      LoginContext lc = new LoginContext("something", subject, new MyCBH());
      lc.login();

      Set<Group> groups = subject.getPrincipals(Group.class);
      Group roleGroup = null;
      for(Group grp: groups){
          if(grp.getName().equalsIgnoreCase("Roles")){
              roleGroup = grp;
              break;
          }
      }
      assertNotNull(roleGroup);
      assertTrue(roleGroup.isMember(new SimplePrincipal("test1")));
      assertTrue(roleGroup.isMember(new SimplePrincipal("test2")));
      assertTrue(roleGroup.isMember(new SimplePrincipal("testX")));
   }
}
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
package org.picketlink.trust.jbossws.jaas;

import java.io.ByteArrayInputStream;
import java.security.Principal;
import java.security.acl.Group;
import java.util.List;
import java.util.Set;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.login.LoginException;

import org.jboss.security.SecurityConstants;
import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.auth.spi.AbstractServerLoginModule;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.core.util.StringUtil;
import org.picketlink.identity.federation.core.wstrust.SamlCredential;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;

/**
 * <p>
 * A login module that extracts the roles from the SAML assertion 
 * that has been set in the Subject. This module is always a follow up
 * to other modules such as {@code JBWSTokenIssuingLoginModule}
 * </p>
 * 
 * <p>
 * This login module checks the {@code Subject} for a {@code SamlCredential}
 * in the public credentials section. From the credential, we extract the 
 * assertion. The assertion should contain the roles.
 * </p>
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Jun 6, 2011
 */
public class SAMLRoleLoginModule extends AbstractServerLoginModule
{  
   @Override
   public boolean commit() throws LoginException
   { 
      super.loginOk = true;
      return super.commit();
   }

   /**
    * We first check the shared state for the principal.
    * If not, we look inside the subject for a non-{@code Group} Principal
    */
   @Override
   protected Principal getIdentity()
   { 
      Principal principal =  (Principal) sharedState.get("javax.security.auth.login.name");
      if(principal != null)
         return principal;
      
      //Lets try the cbh
      NameCallback nameCallback = new NameCallback("UserName:");
      try
      {
         callbackHandler.handle(new Callback[] {nameCallback} );
         String userName = nameCallback.getName();
         if(StringUtil.isNotNull(userName))
            return new SimplePrincipal(userName);
      }
      catch (Exception e)
      { 
         throw new RuntimeException(e);
      }

      Set<Principal> principals = subject.getPrincipals();
      for(Principal p: principals)
      {
         if(!(p instanceof Group))
         {
            return p;
         }
      }
      throw new RuntimeException(ErrorCodes.PROCESSING_EXCEPTION + "Unable to get the Identity from the subject.");
   }
   
   @Override
   protected Group[] getRoleSets() throws LoginException
   {
      //Get the SAML Assertion
      SamlCredential samlCredential = null;
      Set<Object> creds = subject.getPublicCredentials();
      for(Object cred: creds)
      {
         if( cred instanceof SamlCredential)
         {
            samlCredential = (SamlCredential) cred;
            break;
         } 
      }
      if( samlCredential == null)
         throw new RuntimeException(ErrorCodes.NULL_VALUE + "SAML Credential not found in the subject");
      
      try
      { 
         String assertionStr = samlCredential.getAssertionAsString();
         if(StringUtil.isNullOrEmpty(assertionStr))
            throw new RuntimeException(ErrorCodes.NULL_VALUE + "Assertion String is null or empty");
         
         SAMLParser parser = new SAMLParser();
         AssertionType assertion = (AssertionType) parser.parse(new ByteArrayInputStream(assertionStr.getBytes()));
         List<String> roles = AssertionUtil.getRoles(assertion, null);
         Group roleGroup = new SimpleGroup(SecurityConstants.ROLES_IDENTIFIER);
         for(String role: roles)
         {
            roleGroup.addMember(new SimplePrincipal(role));
         }
         return new Group[] { roleGroup};
      }
      catch (Exception e)
      { 
         throw new RuntimeException(e);
      }
   }
}
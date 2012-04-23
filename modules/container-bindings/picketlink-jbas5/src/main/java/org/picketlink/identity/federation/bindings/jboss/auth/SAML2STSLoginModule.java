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
package org.picketlink.identity.federation.bindings.jboss.auth;

import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.Certificate;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.jboss.security.plugins.JaasSecurityDomain;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.core.wstrust.STSClient;
import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.w3c.dom.Element;

/**
 * <p>
 * This {@code LoginModule} authenticates clients by validating their SAML assertions with an external security
 * token service (such as PicketLinkSTS). If the supplied assertion contains roles, these roles are extracted
 * and included in the {@code Group} returned by the {@code getRoleSets} method.
 * </p>
 * <p>
 * This module defines the following module options:
 * <li>
 *  <ul>configFile - this property identifies the properties file that will be used to establish communication with
 *  the external security token service.
 *  </ul>
 *  <ul>cache.invalidation:  set it to true if you require invalidation of JBoss Auth Cache at SAML Principal expiration.
 *  </ul>
 *  <ul>jboss.security.security_domain: name of the security domain where this login module is configured. This is only required
 *  if the cache.invalidation option is configured.
 *  </ul>
 *  <ul>roleKey: a comma separated list of strings that define the attributes in SAML assertion for user roles</ul>
 *  <ul>localValidation: if you want to validate the assertion locally for signature and expiry</ul>
 * </li>
 * </p>
 * <p>
 * Any properties specified besides the above properties are assumed to be used to configure how the {@code STSClient}
 * will connect to the STS. For example, the JBossWS {@code StubExt.PROPERTY_SOCKET_FACTORY} can be specified in order
 * to inform the socket factory that must be used to connect to the STS. All properties will be set in the request
 * context of the {@code Dispatch} instance used by the {@code STSClient} to send requests to the STS.  
 * </p>
 * <p>
 * An example of a {@code configFile} can be seen bellow:
 * <pre>
 * serviceName=PicketLinkSTS
 * portName=PicketLinkSTSPort
 * endpointAddress=http://localhost:8080/picketlink-sts/PicketLinkSTS
 * username=JBoss
 * password=JBoss
 * </pre>
 * The first three properties specify the STS endpoint URL, service name, and port name. The last two properties
 * specify the username and password that are to be used by the application server to authenticate to the STS and
 * have the SAML assertions validated.
 * </p>
 * <p>
 * <b>NOTE:</b> Sub-classes can use {@link #getSTSClient()} method to customize the {@link STSClient} class to make calls to STS/
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 * @author Anil.Saldhana@redhat.com
 */
public class SAML2STSLoginModule extends SAML2STSCommonLoginModule
{
   protected static Logger log = Logger.getLogger(SAML2STSCommonLoginModule.class);

   protected boolean trace = log.isTraceEnabled();

   protected boolean localValidation(Element assertionElement) throws Exception
   {
      //For unit tests
      if (localTestingOnly)
         return true;

      try
      {
         Context ctx = new InitialContext();

         JaasSecurityDomain sd = (JaasSecurityDomain) ctx.lookup(localValidationSecurityDomain);
         KeyStore ts = sd.getTrustStore();

         if (ts == null)
         {
            throw new LoginException(ErrorCodes.NULL_VALUE + "SAML2STSLoginModule: null truststore for " + sd.getName());
         }

         String alias = sd.getKeyStoreAlias();

         if (alias == null)
         {
            throw new LoginException(ErrorCodes.NULL_VALUE + "SAML2STSLoginModule: null KeyStoreAlias for "
                  + sd.getName() + "; set 'KeyStoreAlias' in '" + sd.getName() + "' security domain configuration");
         }

         Certificate cert = ts.getCertificate(alias);

         if (cert == null)
         {
            throw new LoginException(ErrorCodes.NULL_VALUE + "SAML2STSLoginModule: no certificate found for alias '"
                  + alias + "' in the '" + sd.getName() + "' security domain");
         }

         PublicKey publicKey = cert.getPublicKey();

         boolean sigValid = AssertionUtil.isSignatureValid(assertionElement, publicKey);
         if (!sigValid)
         {
            throw new LoginException(ErrorCodes.INVALID_DIGITAL_SIGNATURE + "SAML2STSLoginModule: "
                  + WSTrustConstants.STATUS_CODE_INVALID + " : invalid SAML V2.0 assertion signature");
         }

         AssertionType assertion = SAMLUtil.fromElement(assertionElement);

         if (AssertionUtil.hasExpired(assertion))
         {
            throw new LoginException(ErrorCodes.EXPIRED_ASSERTION + "SAML2STSLoginModule: "
                  + WSTrustConstants.STATUS_CODE_INVALID + "::assertion expired or used before its lifetime period");
         }
      }
      catch (NamingException e)
      {
         throw new LoginException(e.toString());
      }
      return true;
   }
}
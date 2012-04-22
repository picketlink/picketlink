/*
 * JBoss, Home of Professional Open Source Copyright 2009, Red Hat Middleware
 * LLC, and individual contributors by the @authors tag. See the copyright.txt
 * in the distribution for a full listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.picketlink.identity.federation.core.wstrust.auth;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;

import org.picketlink.identity.federation.core.wstrust.STSClient;
import org.picketlink.identity.federation.core.wstrust.WSTrustException;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.w3c.dom.Element;

/**
 * JAAS LoginModule for JBoss STS (Security Token Service) that issues security tokens.
 * 
 * <h3>Configuration example</h3>
 * <pre>{@code
 * <application-policy name="saml-issue-token">
 *   <authentication>
 *     <login-module code="org.picketlink.identity.federation.core.wstrust.auth.STSIssuingLoginModule" flag="required">
 *       <module-option name="configFile">/sts-client.properties</module-option>
 *       <module-option name="endpointURI"></module-option>
 *       <module-option name="tokenType"></module-option>
 *     </login-module>
 *   </authentication>
 * </application-policy>
 * }
 * </pre>
 * 
 * This login module expects to be created with a callback handler that can handle  {@link NameCallback} 
 * and a {@link PasswordCallback}, which should be match the username and password for whom a security 
 * token will be issued.
 * <p/>
 * 
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 * 
 */
public class STSIssuingLoginModule extends AbstractSTSLoginModule
{
   public static final String ENDPOINT_OPTION = "endpointURI";

   public static final String TOKEN_TYPE_OPTION = "tokenType";

   private String endpointURI;

   private String tokenType;

   @Override
   public void initialize(final Subject subject, final CallbackHandler callbackHandler,
         final Map<String, ?> sharedState, final Map<String, ?> options)
   {
      super.initialize(subject, callbackHandler, sharedState, options);

      endpointURI = (String) options.get(ENDPOINT_OPTION);
      if (endpointURI == null)
         endpointURI = (String) options.get(ENDPOINT_ADDRESS); //base class
      tokenType = (String) options.get(TOKEN_TYPE_OPTION);
      if (tokenType == null)
         tokenType = SAMLUtil.SAML2_TOKEN_TYPE;
   }

   /**
    * This method will issue a token for the configured user. 
    * 
    * @return Element The issued element.
    * @throws LoginException If an error occurs while trying to perform the authentication.
    */
   public Element invokeSTS(final STSClient stsClient) throws WSTrustException
   {
      return stsClient.issueToken(endpointURI, tokenType);
   }
}
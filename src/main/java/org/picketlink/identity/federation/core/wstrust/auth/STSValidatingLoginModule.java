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

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.wstrust.STSClient;
import org.picketlink.identity.federation.core.wstrust.WSTrustException;
import org.w3c.dom.Element;

/**
 * JAAS LoginModule for JBoss STS (Security Token Service) that validates security tokens.
 * </p> 
 * This LoginModule only performs validation of existing SAML Assertions and does not issue 
 * any such Assertions.
 * 
 * <h3>Configuration example</h3>
 * <pre>{@code
 * <application-policy name="saml-validate-token">
 *   <authentication>
 *     <login-module code="org.picketlink.identity.federation.core.wstrust.auth.STSValidatingLoginModule" flag="required">
 *       <module-option name="configFile">/sts-client.properties</module-option>
 *     </login-module>
 *   </authentication>
 * </application-policy>
 * }</pre>
 * 
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 */
public class STSValidatingLoginModule extends AbstractSTSLoginModule
{
   private final Logger log = Logger.getLogger(STSValidatingLoginModule.class);

   /**
    * This method will validate the token with the configured STS. 
    * 
    * @return Element The token that was validated.
    * @throws LoginException If it was not possible to validate the token for any reason.
    */
   public Element invokeSTS(final STSClient stsClient) throws WSTrustException, LoginException
   {
      try
      {
         // See if a previous stacked login module stored the token.
         Element token = (Element) getSharedToken();

         if (token == null)
            token = getSamlTokenFromCaller();

         final boolean result = stsClient.validateToken(token);
         log.debug("Validation result: " + result);
         if (result == false)
         {
            // Throw an exception as returing false only says that this login module should be ignored.
            throw new LoginException(ErrorCodes.PROCESSING_EXCEPTION + "Could not validate the SAML Security Token :"
                  + token);
         }

         return token;
      }
      catch (final IOException e)
      {
         throw new LoginException(ErrorCodes.PROCESSING_EXCEPTION + "IOException : " + e.getMessage());
      }
      catch (final UnsupportedCallbackException e)
      {
         throw new LoginException(ErrorCodes.PROCESSING_EXCEPTION + "UnsupportedCallbackException : " + e.getMessage());
      }
   }

   private Element getSamlTokenFromCaller() throws UnsupportedCallbackException, LoginException, IOException
   {
      final TokenCallback callback = new TokenCallback();

      getCallbackHandler().handle(new Callback[]
      {callback});

      final Element token = (Element) callback.getToken();
      if (token == null)
         throw new LoginException(ErrorCodes.NULL_VALUE + "Could not locate a Security Token from the callback.");

      return token;
   }
}

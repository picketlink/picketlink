/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.identity.federation.core.wstrust.auth;

import org.picketlink.common.exceptions.fed.WSTrustException;
import org.picketlink.identity.federation.core.wstrust.STSClient;
import org.w3c.dom.Element;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import java.io.IOException;

/**
 * JAAS LoginModule for JBoss STS (Security Token Service) that validates security tokens. </p> This LoginModule only
 * performs
 * validation of existing SAML Assertions and does not issue any such Assertions.
 *
 * <h3>Configuration example</h3>
 *
 * <pre>
 * {@code
 * <application-policy name="saml-validate-token">
 *   <authentication>
 *     <login-module code="org.picketlink.identity.federation.core.wstrust.auth.STSValidatingLoginModule"
 * flag="required">
 *       <module-option name="configFile">/sts-client.properties</module-option>
 *     </login-module>
 *   </authentication>
 * </application-policy>
 * }
 * </pre>
 *
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 */
public class STSValidatingLoginModule extends AbstractSTSLoginModule {

    /**
     * This method will validate the token with the configured STS.
     *
     * @return Element The token that was validated.
     *
     * @throws LoginException If it was not possible to validate the token for any reason.
     */
    public Element invokeSTS(final STSClient stsClient) throws WSTrustException, LoginException {
        try {
            // See if a previous stacked login module stored the token.
            Element token = (Element) getSharedToken();

            if (token == null)
                token = getSamlTokenFromCaller();

            final boolean result = stsClient.validateToken(token);

            logger.debug("SAML Token Validation result: " + result);

            if (result == false) {
                // Throw an exception as returing false only says that this login module should be ignored.
                throw logger.authCouldNotValidateSAMLToken(token);
            }

            return token;
        } catch (final IOException e) {
            throw logger.authLoginError(e);
        } catch (final UnsupportedCallbackException e) {
            throw logger.authLoginError(e);
        }
    }

    private Element getSamlTokenFromCaller() throws UnsupportedCallbackException, LoginException, IOException {
        final TokenCallback callback = new TokenCallback();

        getCallbackHandler().handle(new Callback[]{callback});

        final Element token = (Element) callback.getToken();
        if (token == null)
            throw logger.authCouldNotLocateSecurityToken();

        return token;
    }
}

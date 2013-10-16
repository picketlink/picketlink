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
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.w3c.dom.Element;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import java.util.Map;

/**
 * JAAS LoginModule for JBoss STS (Security Token Service) that issues security tokens.
 *
 * <h3>Configuration example</h3>
 *
 * <pre>
 * {@code
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
 * This login module expects to be created with a callback handler that can handle {@link javax.security.auth.callback.NameCallback} and a
 * {@link javax.security.auth.callback.PasswordCallback}, which should be match the username and password for whom a security token will be issued.
 * <p/>
 *
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 */
public class STSIssuingLoginModule extends AbstractSTSLoginModule {

    public static final String ENDPOINT_OPTION = "endpointURI";

    public static final String TOKEN_TYPE_OPTION = "tokenType";

    private String endpointURI;

    private String tokenType;

    @Override
    public void initialize(final Subject subject, final CallbackHandler callbackHandler, final Map<String, ?> sharedState,
                           final Map<String, ?> options) {
        super.initialize(subject, callbackHandler, sharedState, options);

        endpointURI = (String) options.get(ENDPOINT_OPTION);
        if (endpointURI == null)
            endpointURI = (String) options.get(ENDPOINT_ADDRESS); // base class
        tokenType = (String) options.get(TOKEN_TYPE_OPTION);
        if (tokenType == null)
            tokenType = SAMLUtil.SAML2_TOKEN_TYPE;
    }

    /**
     * This method will issue a token for the configured user.
     *
     * @return Element The issued element.
     *
     * @throws javax.security.auth.login.LoginException If an error occurs while trying to perform the authentication.
     */
    public Element invokeSTS(final STSClient stsClient) throws WSTrustException {
        return stsClient.issueToken(endpointURI, tokenType);
    }
}
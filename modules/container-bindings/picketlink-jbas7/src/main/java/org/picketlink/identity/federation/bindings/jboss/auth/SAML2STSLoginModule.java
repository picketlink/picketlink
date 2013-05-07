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
import org.jboss.security.JBossJSSESecurityDomain;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.factories.JBossAuthCacheInvalidationFactory;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.w3c.dom.Element;

/**
 * <p>
 * This {@code LoginModule} implements the local validation of SAML assertions on AS7. The specified
 * {@code localValidationSecurityDomain} property must correspond to a AS7 JSSE domain that configures a truststore and
 * a server-alias that identifies the certificate used to validate the assertions.
 * </p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class SAML2STSLoginModule extends SAML2STSCommonLoginModule {

    protected boolean localValidation(Element assertionElement) throws Exception {
        // For unit tests
        if (localTestingOnly)
            return true;

        try {
            Context ctx = new InitialContext();
            String jsseLookupString = super.localValidationSecurityDomain + "/jsse";

            JBossJSSESecurityDomain sd = (JBossJSSESecurityDomain) ctx.lookup(jsseLookupString);
            String securityDomain = sd.getSecurityDomain();

            KeyStore ts = sd.getTrustStore();
            if (ts == null) {
                throw logger.authNullKeyStoreFromSecurityDomainError(securityDomain);
            }

            String alias = sd.getServerAlias();
            if (alias == null) {
                throw logger.authNullKeyStoreAliasFromSecurityDomainError(securityDomain);
            }

            Certificate cert = ts.getCertificate(alias);
            if (cert == null) {
                throw logger.authNoCertificateFoundForAliasError(alias, securityDomain);
            }

            PublicKey publicKey = cert.getPublicKey();
            boolean sigValid = AssertionUtil.isSignatureValid(assertionElement, publicKey);
            if (!sigValid) {
                throw logger.authSAMLInvalidSignatureError();
            }

            AssertionType assertion = SAMLUtil.fromElement(assertionElement);
            if (AssertionUtil.hasExpired(assertion)) {
                throw logger.authSAMLAssertionExpiredError();
            }
        } catch (NamingException e) {
            throw new LoginException(e.toString());
        }
        return true;
    }

    @Override
    protected JBossAuthCacheInvalidationFactory.TimeCacheExpiry getCacheExpiry() throws Exception {
        return AS7AuthCacheInvalidationFactory.getCacheExpiry();
    }
}
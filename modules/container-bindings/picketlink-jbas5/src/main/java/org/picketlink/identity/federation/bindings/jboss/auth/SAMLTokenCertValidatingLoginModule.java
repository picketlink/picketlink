/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
import java.security.Principal;
import java.security.acl.Group;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.PKIXParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.jboss.security.SecurityConstants;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.auth.callback.ObjectCallback;
import org.jboss.security.plugins.JaasSecurityDomain;
import org.picketlink.identity.federation.bindings.jboss.subject.PicketLinkGroup;
import org.picketlink.identity.federation.bindings.jboss.subject.PicketLinkPrincipal;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.constants.AttributeConstants;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.factories.JBossAuthCacheInvalidationFactory;
import org.picketlink.identity.federation.core.factories.JBossAuthCacheInvalidationFactory.TimeCacheExpiry;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.core.util.NamespaceContext;
import org.picketlink.identity.federation.core.util.StringUtil;
import org.picketlink.identity.federation.core.wstrust.SamlCredential;
import org.picketlink.identity.federation.core.wstrust.auth.AbstractSTSLoginModule;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.BaseIDAbstractType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * This LoginModule authenticates clients by validating their SAML assertions locally. If the supplied assertion contains roles, these roles are extracted and included in the Group returned by the getRoleSets method.
 * The LoginModule is designed to validate SAML token using X509 certificate stored in XML signature within SAML assertion token.
 * 
 * It validates:
 * <ol>
 * <li>CertPath against specified truststore. It has to have common valid public certificate in the trusted entries.</li>
 * <li>X509 certificate stored in SAML token didn't expire</li>
 * <li>if signature itself is valid</li>
 * <li>SAML token expiration</li>
 * </ol>
 * 
 * This module defines the following module options:
 * 
 *  roleKey: key of the attribute name that we need to use for Roles from the SAML assertion. This can be a comma-separated string values such as (Role,Membership)
 *  localValidationSecurityDomain:  the security domain for the trust store information (via the JaasSecurityDomain)
 *  cache.invalidation - set it to true if you require invalidation of JBoss Auth Cache at SAML Principal expiration.
 *  jboss.security.security_domain -security domain at which Principal will expire if cache.invalidation is used.
 *  tokenEncodingType: encoding type of SAML token delivered via http request's header.
 *  Possible values are:
 *      base64 - content encoded as base64. In case of encoding will vary between base64 and gzip use base64 and LoginModule will detect gzipped data.
 *      gzip - gzipped content encoded as base64
 *      none - content not encoded in any way
 *  samlTokenHttpHeader - name of http request header to fetch SAML token from. For example: "Authorize"
 *  samlTokenHttpHeaderRegEx - Java regular expression to be used to get SAML token from "samlTokenHttpHeader". Example: use: ."(.)".* to parse SAML token from header content like this: SAML_assertion="HHDHS=", at the same time set samlTokenHttpHeaderRegExGroup to 1.
 *  samlTokenHttpHeaderRegExGroup - Group value to be used when parsing out value of http request header specified by "samlTokenHttpHeader" using "samlTokenHttpHeaderRegEx".
 *
 * @author Peter Skopek: pskopek at redhat dot com
 *
 */
@SuppressWarnings("unchecked")
public class SAMLTokenCertValidatingLoginModule extends
        SAMLTokenFromHttpRequestAbstractLoginModule {
    
    protected Principal principal;

    protected SamlCredential credential;

    protected AssertionType assertion;

    protected boolean enableCacheInvalidation = false;

    protected String securityDomain = null;

    protected String localValidationSecurityDomain;

    protected String roleKey = AttributeConstants.ROLE_IDENTIFIER_ASSERTION;


    /**
     * Options that are computed by this login module. Few options are removed and the rest are set in the dispatch sts call
     */
    protected Map<String, Object> options = new HashMap<String, Object>();

    /**
     * Original Options that are sent by the JDK JAAS Framework
     */
    protected Map<String, Object> rawOptions = new HashMap<String, Object>();

    /**
     * This is an option that should identify the configuration file for WSTrustClient.
     */
    public static final String STS_CONFIG_FILE = "configFile";

    /**
     * Key to specify the end point address
     */
    public static final String ENDPOINT_ADDRESS = "endpointAddress";

    /**
     * Key to specify the port name
     */
    public static final String PORT_NAME = "portName";

    /**
     * Key to specify the service name
     */
    public static final String SERVICE_NAME = "serviceName";

    /**
     * Key to specify the username
     */
    public static final String USERNAME_KEY = "username";

    /**
     * Key to specify the password
     */
    public static final String PASSWORD_KEY = "password";

    // A variable used by the unit test to pass local validation
    protected boolean localTestingOnly = false;

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.security.auth.spi.AbstractServerLoginModule#initialize(javax.security.auth.Subject,
     * javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        super.initialize(subject, callbackHandler, sharedState, options);
        this.options.putAll(options);
        this.rawOptions.putAll(options);

        if (logger.isTraceEnabled()) {
            logger.trace(options.toString());
        }

        String cacheInvalidation = (String) this.options.remove("cache.invalidation");
        if (cacheInvalidation != null && !cacheInvalidation.isEmpty()) {
            this.enableCacheInvalidation = Boolean.parseBoolean(cacheInvalidation);

            this.securityDomain = (String) this.options.remove(SecurityConstants.SECURITY_DOMAIN_OPTION);
            if (this.securityDomain == null || this.securityDomain.isEmpty())
                throw logger.optionNotSet(SecurityConstants.SECURITY_DOMAIN_OPTION);
        }

        String roleKeyStr = (String) options.get("roleKey");
        if (StringUtil.isNotNull(roleKeyStr)) {
            roleKey = roleKeyStr.trim();
        }

        localValidationSecurityDomain = (String) options.get("localValidationSecurityDomain");

        if (localValidationSecurityDomain == null) {
           logger.error(ErrorCodes.LOCAL_VALIDATION_SEC_DOMAIN_MUST_BE_SPECIFIED);
           throw logger.optionNotSet("localValidationSecurityDomain");
        }

        if (localValidationSecurityDomain.startsWith("java:") == false)
            localValidationSecurityDomain = SecurityConstants.JAAS_CONTEXT_ROOT + "/" + localValidationSecurityDomain;
        
        // initialize xmlsec
        org.apache.xml.security.Init.init();

    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.security.auth.spi.AbstractServerLoginModule#login()
     */
    @Override
    public boolean login() throws LoginException {
        // if shared data exists, set our principal and assertion variables.
        if (super.login()) {
            Object sharedPrincipal = super.sharedState.get("javax.security.auth.login.name");
            if (sharedPrincipal instanceof Principal)
                this.principal = (Principal) sharedPrincipal;
            else {
                try {
                    this.principal = createIdentity(sharedPrincipal.toString());
                } catch (Exception e) {
                    throw logger.authFailedToCreatePrincipal(e);
                }
            }

            Object credential = super.sharedState.get("javax.security.auth.login.password");
            if (credential instanceof SamlCredential)
                this.credential = (SamlCredential) credential;
            else
                throw logger.authSharedCredentialIsNotSAMLCredential(credential.getClass().getName());
            return true;
        }

        // obtain the assertion from the callback handler.
        ObjectCallback callback = new ObjectCallback(null);
        Element assertionElement = null;
        try {
            if (getSamlTokenHttpHeader() != null) {
                this.credential = getCredentialFromHttpRequest();
            }
            else {
                super.callbackHandler.handle(new Callback[] { callback });
                if (callback.getCredential() instanceof SamlCredential == false)
                    throw logger.authSharedCredentialIsNotSAMLCredential(callback.getCredential().getClass().getName());
                this.credential = (SamlCredential) callback.getCredential();
            }
            assertionElement = this.credential.getAssertionAsElement();
        } catch (Exception e) {
            throw logger.authErrorHandlingCallback(e);
        }

        try {
            this.assertion = SAMLUtil.fromElement(assertionElement);
        } catch (Exception e) {
            throw logger.authFailedToParseSAMLAssertion(e);
        }
        

        try {
            // cert path validation
            validateSAMLCredential();
            
            // if the assertion is valid, create a principal containing the assertion subject.
            SubjectType subject = assertion.getSubject();
            if (subject != null) {
                BaseIDAbstractType baseID = subject.getSubType().getBaseID();
                if (baseID instanceof NameIDType) {
                    NameIDType nameID = (NameIDType) baseID;
                    this.principal = new PicketLinkPrincipal(nameID.getValue());

                    // If the user has configured cache invalidation of subject based on saml token expiry
                    if (enableCacheInvalidation) {
                        TimeCacheExpiry cacheExpiry = this.getCacheExpiry();
                        XMLGregorianCalendar expiry = AssertionUtil.getExpiration(assertion);
                        if (expiry != null) {
                            Date expiryDate = expiry.toGregorianCalendar().getTime();

                            logger.trace("Creating Cache Entry for JBoss at [" + new Date() + "] , with expiration set to SAML expiry = " + expiryDate);

                            cacheExpiry.register(securityDomain, expiryDate, principal);
                        } else {
                            logger.samlAssertionWithoutExpiration(assertion.getID());
                        }
                    }
                }
            }
        } catch (Throwable e) {
            logger.error(e);
            LoginException le = new LoginException(e.getMessage());
            throw le;
        }

        // if password-stacking has been configured, set the principal and the assertion in the shared map.
        if (getUseFirstPass()) {
            super.sharedState.put("javax.security.auth.login.name", this.principal);
            super.sharedState.put("javax.security.auth.login.password", this.credential);
        }
        return (super.loginOk = true);
    }



    /* (non-Javadoc)
     * @see org.jboss.security.auth.spi.AbstractServerLoginModule#commit()
     */
    @Override
    public boolean commit() throws LoginException {
        if (super.commit()) {
            final boolean added = subject.getPublicCredentials().add(this.credential);
            if (added && logger.isTraceEnabled())
                logger.trace("Added Credential " + this.credential);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Called if the overall authentication failed (phase 2).
     */
    @Override
    public boolean abort() throws LoginException {
        clearState();
        super.abort();
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        clearState();
        super.logout();
        return true;
    }

    private void clearState() {
        AbstractSTSLoginModule.removeAllSamlCredentials(subject);
        credential = null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.security.auth.spi.AbstractServerLoginModule#getIdentity()
     */
    @Override
    protected Principal getIdentity() {
        return this.principal;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.security.auth.spi.AbstractServerLoginModule#getRoleSets()
     */
    @Override
    protected Group[] getRoleSets() throws LoginException {
        if (this.assertion == null) {
            try {
                this.assertion = SAMLUtil.fromElement(this.credential.getAssertionAsElement());
            } catch (Exception e) {
                throw logger.authFailedToParseSAMLAssertion(e);
            }
        }
        if (logger.isTraceEnabled()) {
            try {
                logger.trace("Assertion from where roles will be sought = " + AssertionUtil.asString(assertion));
            } catch (ProcessingException ignore) {
            }
        }

        List<String> roleKeys = new ArrayList<String>();
        if (StringUtil.isNotNull(roleKey)) {
            roleKeys.addAll(StringUtil.tokenize(roleKey));
        }

        String groupName = SecurityConstants.ROLES_IDENTIFIER;
        Group rolesGroup = new PicketLinkGroup(groupName);
        List<String> roles = AssertionUtil.getRoles(assertion, roleKeys);
        for (String role : roles) {
            rolesGroup.addMember(new SimplePrincipal(role));
        }

        return new Group[] { rolesGroup };
    }

    protected JBossAuthCacheInvalidationFactory.TimeCacheExpiry getCacheExpiry() throws Exception {
        return JBossAuthCacheInvalidationFactory.getCacheExpiry();
    }


    /**
     * This method validates SAML Credential in following steps:
     * <ol>
     *   <li>Validate the signing key embedded in SAML token is still valid, not expired</li>
     *   <li>Validate the signing key embedded in SAML token is trusted against a local truststore,  such as certpath validation</li>
     *   <li>Validate SAML token is still valid, not expired</li>
     *   <li>Validate the SAML signature using the embedded signing key in SAML token itself as you indicated below</li>
     * </ol>
     *
     * If something goes wrong throws LoginException.
     *
     * @throws LoginException
     */
    private void validateSAMLCredential() throws LoginException, ConfigurationException, CertificateExpiredException, CertificateNotYetValidException {

        X509Certificate cert = getX509Certificate();

        // public certificate validation
        validateCertPath(cert);

        // check time validity of the certificate
        cert.checkValidity();
        
        boolean sigValid = false;
        try {
            sigValid = AssertionUtil.isSignatureValid(credential.getAssertionAsElement(), cert.getPublicKey());
        } catch (ProcessingException e) {
            logger.processingError(e);
        }
        if (!sigValid) {
            throw logger.authSAMLInvalidSignatureError();
        }
        
        if (AssertionUtil.hasExpired(assertion)) {
            throw logger.authSAMLAssertionExpiredError();
        }

    }

    /**
     * Extract x509 certificate from SAML Assertion's signature.
     * @return
     * @throws LoginException
     */
    private X509Certificate getX509Certificate() throws LoginException {

        try {
            Element assertion = credential.getAssertionAsElement();
            String xmlSignatureNSPrefix = findNameSpacePrefix(assertion, JBossSAMLURIConstants.XMLDSIG_NSURI.get());
            
            String expression = "//" + xmlSignatureNSPrefix + ":Signature[1]";

            XPathFactory xpf = XPathFactory.newInstance();
            XPath xpath = xpf.newXPath();
            xpath.setNamespaceContext(
                    NamespaceContext.create()
                        .addNsUriPair(xmlSignatureNSPrefix, JBossSAMLURIConstants.XMLDSIG_NSURI.get())
                    );

            Element sigElement =
                (Element) xpath.evaluate(expression, credential.getAssertionAsElement(), XPathConstants.NODE);
            XMLSignature signature =
                new XMLSignature(sigElement, "");

            if (logger.isTraceEnabled()) {
                logger.trace("sigElement="+sigElement.getTextContent());
            }
            
            KeyInfo keyInfo = signature.getKeyInfo();
            if (!keyInfo.containsX509Data()) {
                log.error("Cannot find X509Data element");
                throw new LoginException("Cannot find X509Data element");
            }

            X509Certificate certificate = signature.getKeyInfo().getX509Certificate();

            if (certificate == null) {
                logger.error("Not able to extract x509 certificate");
                throw new LoginException("Not able to extract x509 certificate");
            }

            if (logger.isTraceEnabled()) {
                logger.trace("Got certificate="+certificate.toString());
            }
            return certificate;
        }
        catch (Exception e) {
            logger.error(e);
            throw new LoginException(e.getLocalizedMessage());
        }
    }

    private String findNameSpacePrefix(Element element, String xmlns) {
        NodeList nl = element.getElementsByTagNameNS(xmlns, "Signature");
        if (nl.getLength() > 0) {
            return nl.item(0).getPrefix();
        }
        else {
            return null;
        }
    }
    
    /**
     * Validate certificate path against keystore specified as SecurityDomain in module-option.
     * @param cert
     */
    private void validateCertPath(X509Certificate certificate) throws LoginException {
        // get cert path
        CertPath certPath = null;
        try {
            CertificateFactory certFact = CertificateFactory.getInstance("X.509");
            certPath = certFact.generateCertPath(Arrays.asList(certificate));
        } catch (CertificateEncodingException e) {
            logger.error(e.getMessage());
            throw new LoginException(e.getLocalizedMessage());
        } catch (CertificateException e) {
            logger.error(e.getMessage());
            throw new LoginException(e.getLocalizedMessage());
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Certificates from SAML token:");
            for (Certificate c : certPath.getCertificates()) {
               logger.trace("Type of certificate=" + c.getType());
               logger.trace(c.toString()); 
            }
        }
        
        
        // certpath validation
        try {
            // get keystore
            Context ctx = new InitialContext();
            JaasSecurityDomain sd = (JaasSecurityDomain) ctx.lookup(localValidationSecurityDomain);
            KeyStore trustStore = sd.getTrustStore();

            if (trustStore == null) {
                throw logger.authNullKeyStoreFromSecurityDomainError(sd.getName());
            }

            if (logger.isTraceEnabled()) {
                logger.trace("Certificates from truststore:");
                
                Enumeration<String> tsAliases = trustStore.aliases();
                while (tsAliases.hasMoreElements()) {
                    String alias = tsAliases.nextElement();
                    logger.trace("Alias="+alias);
                    Certificate[] chain = trustStore.getCertificateChain(alias);
                    if (chain != null) {
                        logger.trace(alias + " is a chain:");
                        for (Certificate c: chain) {
                            logger.trace(c.toString());
                        }
                    }
                    
                    Certificate crt = trustStore.getCertificate(alias);  
                    if (crt != null) {
                        logger.trace(alias + " is a certificate of type " + crt.getType());
                        logger.trace(crt.toString());
                    }
                    
                }
            }            
            
            // Create the parameters for the validator
            PKIXParameters params = new PKIXParameters(trustStore);

            // Disable CRL checking since we are not supplying any CRLs
            params.setRevocationEnabled(false);

            // Create the validator and validate the path
            // To create a path, see Creating a Certification Path
            CertPathValidator certPathValidator
                = CertPathValidator.getInstance(CertPathValidator.getDefaultType());
            
            if (logger.isTraceEnabled()) {
                logger.trace("certPathValidator is ready");
            }
            
            CertPathValidatorResult result = certPathValidator.validate(certPath, params);
            if (logger.isTraceEnabled()) {
                logger.trace("CertPathValidatorResult="+result);
            }

        } catch (Exception e) {
            logger.error(e);
            throw new LoginException(e.getLocalizedMessage());
        }
    }

}

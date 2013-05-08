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

import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.Source;
import javax.xml.ws.Dispatch;

import org.jboss.security.SecurityConstants;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.auth.callback.ObjectCallback;
import org.picketlink.common.ErrorCodes;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.exceptions.fed.WSTrustException;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.common.util.StringUtil;
import org.picketlink.identity.federation.bindings.jboss.subject.PicketLinkGroup;
import org.picketlink.identity.federation.bindings.jboss.subject.PicketLinkPrincipal;
import org.picketlink.identity.federation.core.constants.AttributeConstants;
import org.picketlink.identity.federation.core.constants.PicketLinkFederationConstants;
import org.picketlink.identity.federation.core.factories.JBossAuthCacheInvalidationFactory.TimeCacheExpiry;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.core.wstrust.STSClient;
import org.picketlink.identity.federation.core.wstrust.STSClientConfig.Builder;
import org.picketlink.identity.federation.core.wstrust.SamlCredential;
import org.picketlink.identity.federation.core.wstrust.auth.AbstractSTSLoginModule;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.BaseIDAbstractType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.w3c.dom.Element;

/**
 * <p>
 * This {@code LoginModule} authenticates clients by validating their SAML assertions with an external security token service
 * (such as PicketLinkSTS). If the supplied assertion contains roles, these roles are extracted and included in the
 * {@code Group} returned by the {@code getRoleSets} method.
 * </p>
 * <p>
 * This module defines the following module options:
 * <ul>
 * <li>
 * configFile - this property identifies the properties file that will be used to establish communication with the external
 * security token service.
 * </li>
 * <li>
 * cache.invalidation: set it to true if you require invalidation of JBoss Auth Cache at SAML Principal expiration.
 * </li>
 * <li>
 * jboss.security.security_domain: name of the security domain where this login module is configured. This is only required if
 * the cache.invalidation option is configured.
 * </li>
 * <li>
 * roleKey: a comma separated list of strings that define the attributes in SAML assertion for user roles
 * </li>
 * <li>
 * localValidation: if you want to validate the assertion locally for signature and expiry
 * </li>
 * <li>
 * localValidationSecurityDomain:  the security domain for the trust store information (via the JaasSecurityDomain)
 * </li>
 * <li>
 * tokenEncodingType: encoding type of SAML token delivered via http request's header.
 * Possible values are:
 *    base64 - content encoded as base64. In case of encoding will vary between base64 and gzip use base64 and LoginModule will detect gzipped data.
 *    gzip - gzipped content encoded as base64
 *    none - content not encoded in any way
 * </li>
 * <li>
 * samlTokenHttpHeader - name of http request header to fetch SAML token from. For example: "Authorize"
 * </li>
 * <li>
 * samlTokenHttpHeaderRegEx - Java regular expression to be used to get SAML token from "samlTokenHttpHeader". Example: use: ."(.)".* to parse SAML token from header content like this: SAML_assertion="HHDHS=", at the same time set samlTokenHttpHeaderRegExGroup to 1.
 * </li>
 * <li>
 * samlTokenHttpHeaderRegExGroup - Group value to be used when parsing out value of http request header specified by "samlTokenHttpHeader" using "samlTokenHttpHeaderRegEx".
 * </li>
 * </ul>
 * </p>
 * <p>
 * Any properties specified besides the above properties are assumed to be used to configure how the {@code STSClient} will
 * connect to the STS. For example, the JBossWS {@code StubExt.PROPERTY_SOCKET_FACTORY} can be specified in order to inform the
 * socket factory that must be used to connect to the STS. All properties will be set in the request context of the
 * {@code Dispatch} instance used by the {@code STSClient} to send requests to the STS.
 * </p>
 * <p>
 * An example of a {@code configFile} can be seen bellow:
 *
 * <pre>
 * serviceName=PicketLinkSTS
 * portName=PicketLinkSTSPort
 * endpointAddress=http://localhost:8080/picketlink-sts/PicketLinkSTS
 * username=JBoss
 * password=JBoss
 * </pre>
 *
 * The first three properties specify the STS endpoint URL, service name, and port name. The last two properties specify the
 * username and password that are to be used by the application server to authenticate to the STS and have the SAML assertions
 * validated.
 * </p>
 * <p>
 * <b>NOTE:</b> Sub-classes can use {@link #getSTSClient()} method to customize the {@link STSClient} class to make calls to
 * STS/
 * </p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 * @author Anil.Saldhana@redhat.com
 */
@SuppressWarnings("unchecked")
public abstract class SAML2STSCommonLoginModule extends SAMLTokenFromHttpRequestAbstractLoginModule {
    
    protected String stsConfigurationFile;

    protected Principal principal;

    protected SamlCredential credential;

    protected AssertionType assertion;

    protected boolean enableCacheInvalidation = false;

    protected String securityDomain = null;

    protected boolean localValidation = false;

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
        // save the config file and cache validation options, removing them from the map - all remaining properties will
        // be set in the request context of the Dispatch instance used to send requests to the STS.
        this.stsConfigurationFile = (String) this.options.remove(STS_CONFIG_FILE);
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

        String localValidationStr = (String) options.get("localValidation");
        if (StringUtil.isNotNull(localValidationStr)) {
            localValidation = Boolean.parseBoolean(localValidationStr);
            localValidationSecurityDomain = (String) options.get("localValidationSecurityDomain");

            if (localValidationSecurityDomain == null) {
               logger.error(ErrorCodes.LOCAL_VALIDATION_SEC_DOMAIN_MUST_BE_SPECIFIED);
               throw logger.optionNotSet("localValidationSecurityDomain");
            }
            
            if (localValidationSecurityDomain.startsWith("java:") == false)
                localValidationSecurityDomain = SecurityConstants.JAAS_CONTEXT_ROOT + "/" + localValidationSecurityDomain;

            String localTestingOnlyStr = (String) options.get("localTestingOnly");
            if (StringUtil.isNotNull(localTestingOnlyStr)) {
                localTestingOnly = Boolean.valueOf(localTestingOnlyStr);
            }
        }
        
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
                
                if (callback.getCredential() instanceof String) {
                    callback.setCredential(new SamlCredential(DocumentUtil.getDocument(callback.getCredential().toString()).getDocumentElement()));
                }
                
                if (callback.getCredential() instanceof SamlCredential == false)
                    throw logger.authSharedCredentialIsNotSAMLCredential(callback.getCredential().getClass().getName());
                this.credential = (SamlCredential) callback.getCredential();
            }
            assertionElement = this.credential.getAssertionAsElement();
        } catch (Exception e) {
            throw logger.authErrorHandlingCallback(e);
        }
    
        
        // if there is no shared data, validate the assertion using the STS.
        if (localValidation) {
            logger.trace("Local Validation is being Performed");
            try {
                boolean isValid = localValidation(assertionElement);
                if (isValid) {
                    logger.trace("Local Validation passed.");
                }
            } catch (Exception e) {
                LoginException le = new LoginException();
                le.initCause(e);
                throw le;
            }
        } else {
            logger.trace("Local Validation is disabled. Verifying with STS");

            // sts config file has to be present to call STS (using sts client)
            if (this.stsConfigurationFile == null)
                throw logger.authSTSConfigFileNotFound();

            // send the assertion to the STS for validation.
            STSClient client = this.getSTSClient();
            try {
                boolean isValid = client.validateToken(assertionElement);
                // if the STS says the assertion is invalid, throw an exception to signal that authentication has failed.
                if (isValid == false)
                    throw logger.authInvalidSAMLAssertionBySTS();
            } catch (WSTrustException we) {
                throw logger.authAssertionValidationError(we);
            }
        }

        // if the assertion is valid, create a principal containing the assertion subject.
        try {
            this.assertion = SAMLUtil.fromElement(assertionElement);
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
        } catch (Exception e) {
            throw logger.authFailedToParseSAMLAssertion(e);
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

    /**
     * Get the {@link STSClient} object with which we can make calls to the STS
     *
     * @return
     */
    protected STSClient getSTSClient() {
        /*
         * Builder builder = new Builder(this.stsConfigurationFile); STSClient client = new STSClient(builder.build());
         */

        Builder builder = null;
        STSClient client = null;
        if (rawOptions.containsKey(STS_CONFIG_FILE)) {
            builder = new Builder(this.stsConfigurationFile);
            client = new STSClient(builder.build());
        } else {
            builder = new Builder();
            builder.endpointAddress((String) rawOptions.get(ENDPOINT_ADDRESS));
            builder.portName((String) rawOptions.get(PORT_NAME)).serviceName((String) rawOptions.get(SERVICE_NAME));
            builder.username((String) rawOptions.get(USERNAME_KEY)).password((String) rawOptions.get(PASSWORD_KEY));

            String passwordString = (String) rawOptions.get(PASSWORD_KEY);
            if (passwordString != null && passwordString.startsWith(PicketLinkFederationConstants.PASS_MASK_PREFIX)) {
                // password is masked
                String salt = (String) rawOptions.get(PicketLinkFederationConstants.SALT);
                if (StringUtil.isNullOrEmpty(salt))
                    throw logger.optionNotSet("Salt");

                String iCount = (String) rawOptions.get(PicketLinkFederationConstants.ITERATION_COUNT);
                if (StringUtil.isNullOrEmpty(iCount))
                    throw logger.optionNotSet("Iteration Count");

                int iterationCount = Integer.parseInt(iCount);
                try {
                    builder.password(StringUtil.decode(passwordString, salt, iterationCount));
                } catch (Exception e) {
                    throw logger.unableToDecodePasswordError(passwordString);
                }
            }
            client = new STSClient(builder.build());
        }

        // if the login module options map still contains any properties, assume they are for configuring the connection
        // to the STS and set them in the Dispatch request context.
        if (!this.options.isEmpty()) {
            Dispatch<Source> dispatch = client.getDispatch();
            for (Map.Entry<String, ?> entry : this.options.entrySet())
                dispatch.getRequestContext().put(entry.getKey(), entry.getValue());
        }
        return client;
    }
    
    /**
     * Locally validate the SAML Assertion element
     *
     * @param assertionElement
     * @return
     * @throws Exception
     */
    protected abstract boolean localValidation(Element assertionElement) throws Exception;

    protected abstract TimeCacheExpiry getCacheExpiry() throws Exception;

}
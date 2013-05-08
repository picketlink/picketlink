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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.jacc.PolicyContext;
import javax.servlet.http.HttpServletRequest;

import org.jboss.security.auth.spi.AbstractServerLoginModule;
import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.util.Base64;
import org.picketlink.identity.federation.core.wstrust.SamlCredential;

/**
 * This is not login module with full functionality.
 * It just adds ability to get SAML token from http header specified by module option. 
 * 
 * @author Peter Skopek: pskopek at redhat dot com
 *
 */
public abstract class SAMLTokenFromHttpRequestAbstractLoginModule extends
        AbstractServerLoginModule {

    protected static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    protected String tokenEncoding = SAML2STSCommonLoginModule.NONE_TOKEN_ENCODING;

    /**
     * Specify which http header contains saml token.
     * If null, default behavior will be used, credentials got from callback.
     */
    private String samlTokenHttpHeader = null;

    /**
     * Regular expression to parse samlTokenHttpHeader to obtain saml token only.
     * Token itself has to be Base64 encoded.
     * Use .* to match whole content.
     */
    private String samlTokenHttpHeaderRegEx = null;

    private Pattern pattern = null; 

    
    /**
     * Group which will be used to retrieve matched part of the token header content.
     * Defaults to 0.
     * pattern.matcher.group(samlTokenHttpHeaderRegExGroup)
     */
    private int samlTokenHttpHeaderRegExGroup = 0;
    
    /**
     * Key to specify token compression. 
     * Supported types: 
     *   {@link GZIP_TOKEN_ENCODING} - gzip
     *   {@link BASE64_TOKEN_ENCODING} - base64
     *   {@link NONE_TOKEN_ENCODING} - none
     */
    public static final String TOKEN_ENCODING_TYPE_KEY = "tokenEncodingType";

    /**
     * Token encoding type: gzip
     */
    public static final String GZIP_TOKEN_ENCODING = "gzip"; 

    /**
     * Token encoding type: none 
     */
    public static final String NONE_TOKEN_ENCODING = "none"; 

    /**
     * Token encoding type: base64 
     */
    public static final String BASE64_TOKEN_ENCODING = "base64"; 

    public static final String WEB_REQUEST_KEY = "javax.servlet.http.HttpServletRequest";
    public static final String REG_EX_PATTERN_KEY = "samlTokenHttpHeaderRegEx";
    public static final String REG_EX_GROUP_KEY = "samlTokenHttpHeaderRegExGroup";
    public static final String SAML_TOKEN_HTTP_HEADER_KEY = "samlTokenHttpHeader";
    
    
    protected SamlCredential getCredentialFromHttpRequest() throws Exception {
        
        HttpServletRequest request = (HttpServletRequest) PolicyContext.getContext(WEB_REQUEST_KEY);
        String encodedSamlToken = null;
        if (samlTokenHttpHeaderRegEx != null && !samlTokenHttpHeaderRegEx.equals("")) {
            String content = request.getHeader(samlTokenHttpHeader);
            if (logger.isTraceEnabled()) {
                log.trace("http header with SAML token [" + samlTokenHttpHeader + "]=" + content);
            }
            log.trace("samlTokenHttpHeaderRegEx="+samlTokenHttpHeaderRegEx);
            Matcher m = pattern.matcher(content);
            m.matches();
            log.trace("samlTokenHttpHeaderRegExGroup="+samlTokenHttpHeaderRegExGroup);
            encodedSamlToken = m.group(samlTokenHttpHeaderRegExGroup);
        }
        else {
            encodedSamlToken = request.getHeader(samlTokenHttpHeader);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("encodedSamlToken="+encodedSamlToken);
        }

        String samlToken = null;
        if (tokenEncoding.equals(NONE_TOKEN_ENCODING)
                || tokenEncoding == null) {
            samlToken = encodedSamlToken;
        }
        else { 
            // gzip and base64 encodings are handled in this Base64.decode call
            byte[] decompressed = Base64.decode(encodedSamlToken);
            samlToken = new String(decompressed);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("decoded samlToken="+samlToken);
        }
        
        return new SamlCredential(samlToken);
    }

    /**
     * @return the tokenEncoding
     */
    public String getTokenEncoding() {
        return tokenEncoding;
    }

    /**
     * @return the samlTokenHttpHeader
     */
    public String getSamlTokenHttpHeader() {
        return samlTokenHttpHeader;
    }

    /**
     * @return the samlTokenHttpHeaderRegEx
     */
    public String getSamlTokenHttpHeaderRegEx() {
        return samlTokenHttpHeaderRegEx;
    }

    /**
     * @return the samlTokenHttpHeaderRegExGroup
     */
    public int getSamlTokenHttpHeaderRegExGroup() {
        return samlTokenHttpHeaderRegExGroup;
    }


    /* (non-Javadoc)
     * @see org.jboss.security.auth.spi.AbstractServerLoginModule#initialize(javax.security.auth.Subject, javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map<String, ?> sharedState, Map<String, ?> options) {
        
        super.initialize(subject, callbackHandler, sharedState, options);
        
        samlTokenHttpHeader = (String)this.options.get(SAML_TOKEN_HTTP_HEADER_KEY);

        String encoding = (String)this.options.get(TOKEN_ENCODING_TYPE_KEY);
        if (encoding != null) {
            this.tokenEncoding = encoding;
        }

        samlTokenHttpHeaderRegEx = (String)this.options.get(REG_EX_PATTERN_KEY);
        if (samlTokenHttpHeaderRegEx != null) {
            this.pattern = Pattern.compile(samlTokenHttpHeaderRegEx, Pattern.DOTALL);
        }    
        
        String group = (String)this.options.get(REG_EX_GROUP_KEY);
        if (group != null) {
            samlTokenHttpHeaderRegExGroup = Integer.parseInt(group);
        }
        
    }

}

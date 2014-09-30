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
package org.picketlink.identity.federation.web.handlers.saml2;

import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.StringUtil;
import org.picketlink.config.federation.IDPType;
import org.picketlink.identity.federation.core.impl.EmptyAttributeManager;
import org.picketlink.identity.federation.core.interfaces.AttributeManager;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType.ASTChoiceType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.assertion.StatementAbstractType;
import org.picketlink.identity.federation.saml.v2.protocol.LogoutRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.web.core.HTTPContext;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.picketlink.common.constants.GeneralConstants.SESSION_ATTRIBUTE_MAP;

/**
 * <p>
 * Handler dealing with attributes for SAML2
 * </p>
 * <p>
 * <b>Configuration for handler:</b>
 * </p>
 * <p>
 * <ul>
 * <li>ATTRIBUTE_MANAGER: a fqn of the attribute manager class. This is an IDP setting.</li>
 * <li>ATTRIBUTE_KEYS: a comma separated list of string values representing attributes to be sent. IDP setting.</li>
 * <li>ATTRIBUTE_CHOOSE_FRIENDLY_NAME : set to true if you require attributes to be keyed by friendly name rather than
 * default
 * name. SP Setting.</li>
 * </ul>
 * </p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 12, 2009
 */
public class SAML2AttributeHandler extends BaseSAML2Handler {

    protected AttributeManager attribManager = new EmptyAttributeManager();

    protected List<String> attributeKeys = new ArrayList<String>();

    protected boolean chooseFriendlyName = false;

    @Override
    public void initChainConfig(SAML2HandlerChainConfig handlerChainConfig) throws ConfigurationException {
        super.initChainConfig(handlerChainConfig);

        Object config = this.handlerChainConfig.getParameter(GeneralConstants.CONFIGURATION);

        // if the GeneralConstants.ATTIBUTE_MANAGER parameter is defined for this handler, ignore the PicketLinkIDP AttributeManager attribute.
        if (config instanceof IDPType && getAttributeManager() == null) {
            IDPType idpType = (IDPType) config;
            String attribStr = idpType.getAttributeManager();
            insantiateAttributeManager(attribStr);
        }
    }

    private Object getAttributeManager() {
        if (this.handlerConfig == null) {
            return null;
        }

        return this.handlerConfig.getParameter(GeneralConstants.ATTIBUTE_MANAGER);
    }

    @Override
    public void initHandlerConfig(SAML2HandlerConfig handlerConfig) throws ConfigurationException {
        super.initHandlerConfig(handlerConfig);

        String attribStr = (String) getAttributeManager();
        this.insantiateAttributeManager(attribStr);
        // Get a list of attributes we are interested in
        String attribList = (String) this.handlerConfig.getParameter(GeneralConstants.ATTRIBUTE_KEYS);
        if (StringUtil.isNotNull(attribList)) {
            this.attributeKeys.addAll(StringUtil.tokenize(attribList));
        }

        String chooseFriendlyNameStr = (String) handlerConfig.getParameter(GeneralConstants.ATTRIBUTE_CHOOSE_FRIENDLY_NAME);
        if (StringUtil.isNotNull(chooseFriendlyNameStr)) {
            chooseFriendlyName = Boolean.parseBoolean(chooseFriendlyNameStr);
        }
    }

    @SuppressWarnings("unchecked")
    public void handleRequestType(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException {
        // Do not handle log out request interaction
        if (request.getSAML2Object() instanceof LogoutRequestType)
            return;

        // only handle IDP side
        if (getType() == HANDLER_TYPE.SP)
            return;

        HTTPContext httpContext = (HTTPContext) request.getContext();
        HttpSession session = httpContext.getRequest().getSession(false);

        Principal userPrincipal = (Principal) session.getAttribute(GeneralConstants.PRINCIPAL_ID);

        if (userPrincipal == null)
            userPrincipal = httpContext.getRequest().getUserPrincipal();

        Map<String, Object> attribs = (Map<String, Object>) session.getAttribute(GeneralConstants.ATTRIBUTES);
        if (attribs == null) {
            attribs = this.attribManager.getAttributes(userPrincipal, attributeKeys);
            request.addOption(GeneralConstants.ATTRIBUTES, attribs);
            session.setAttribute(GeneralConstants.ATTRIBUTES, attribs);
        }
    }

    @Override
    public void handleStatusResponseType(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException {
        // only handle SP side
        if (getType() == HANDLER_TYPE.IDP)
            return;
        handleIDPResponse(request);
    }

    private void insantiateAttributeManager(String attribStr) throws ConfigurationException {
        if (attribStr != null && !"".equals(attribStr)) {
            try {
                attribManager = (AttributeManager) SecurityActions.loadClass(getClass(), attribStr).newInstance();
                logger.trace("AttributeManager set to " + attribStr);
            } catch (Exception e) {
                logger.attributeProviderInstationError(e);
                throw logger.configurationError(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void handleIDPResponse(SAML2HandlerRequest request) {
        if (!(request.getSAML2Object() instanceof ResponseType)) {
            return;
        }

        HTTPContext httpContext = (HTTPContext) request.getContext();
        HttpSession session = httpContext.getRequest().getSession(false);

        AssertionType assertion = (AssertionType) request.getOptions().get(GeneralConstants.ASSERTION);
        if (assertion == null)
            throw logger.samlHandlerAssertionNotFound();

        Set<StatementAbstractType> statements = assertion.getStatements();
        for (StatementAbstractType statement : statements) {
            if (statement instanceof AttributeStatementType) {
                AttributeStatementType attrStat = (AttributeStatementType) statement;
                List<ASTChoiceType> attrs = attrStat.getAttributes();
                for (ASTChoiceType attrChoice : attrs) {
                    Map<String, List<Object>> attrMap = (Map<String, List<Object>>) session.getAttribute(SESSION_ATTRIBUTE_MAP);

                    if (attrMap == null) {
                        attrMap = new HashMap<String, List<Object>>();
                        session.setAttribute(SESSION_ATTRIBUTE_MAP, attrMap);
                    }

                    AttributeType attr = attrChoice.getAttribute();
                    String attributeName;

                    if (chooseFriendlyName) {
                        attributeName = attr.getFriendlyName();
                    } else {
                        attributeName = attr.getName();
                    }

                    List<Object> values = attrMap.get(attributeName);

                    if (values == null) {
                        values = new ArrayList<Object>();
                        attrMap.put(attributeName, values);
                    }

                    values.addAll(attr.getAttributeValue());
                }
            }
        }
    }

}
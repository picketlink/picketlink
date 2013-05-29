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

import org.jboss.security.audit.AuditLevel;
import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.config.federation.IDPType;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditEvent;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditEventType;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditHelper;
import org.picketlink.identity.federation.core.impl.EmptyRoleGenerator;
import org.picketlink.identity.federation.core.interfaces.RoleGenerator;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.saml.v2.protocol.LogoutRequestType;
import org.picketlink.identity.federation.web.core.HTTPContext;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Handles the generation of roles on the IDP Side
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 7, 2009
 */
public class RolesGenerationHandler extends BaseSAML2Handler {

    private transient RoleGenerator roleGenerator = new EmptyRoleGenerator();

    @Override
    public void initChainConfig(SAML2HandlerChainConfig handlerChainConfig) throws ConfigurationException {
        super.initChainConfig(handlerChainConfig);
        Object config = this.handlerChainConfig.getParameter(GeneralConstants.CONFIGURATION);
        if (config instanceof IDPType) {
            IDPType idpType = (IDPType) config;
            String roleGeneratorString = idpType.getRoleGenerator();
            this.insantiateRoleValidator(roleGeneratorString);
        }
    }

    @Override
    public void initHandlerConfig(SAML2HandlerConfig handlerConfig) throws ConfigurationException {
        super.initHandlerConfig(handlerConfig);
        String roleGeneratorString = (String) this.handlerConfig.getParameter(GeneralConstants.ATTIBUTE_MANAGER);
        this.insantiateRoleValidator(roleGeneratorString);
    }

    /**
     * @see {@code SAML2Handler#handleRequestType(SAML2HandlerRequest, SAML2HandlerResponse)}
     */
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

        Map<String, Object> requestOptions = request.getOptions();
        PicketLinkAuditHelper auditHelper = (PicketLinkAuditHelper) requestOptions.get(GeneralConstants.AUDIT_HELPER);
        String contextPath = (String) requestOptions.get(GeneralConstants.CONTEXT_PATH);

        Principal userPrincipal = (Principal) session.getAttribute(GeneralConstants.PRINCIPAL_ID);
        List<String> roles = (List<String>) session.getAttribute(GeneralConstants.ROLES_ID);

        if (roles == null) {
            roles = roleGenerator.generateRoles(userPrincipal);
            if (auditHelper != null) {
                PicketLinkAuditEvent auditEvent = new PicketLinkAuditEvent(AuditLevel.INFO);
                auditEvent.setWhoIsAuditing(contextPath);
                auditEvent.setType(PicketLinkAuditEventType.GENERATED_ROLES);
                auditEvent.setOptionalString(userPrincipal.getName() + "(" + Arrays.toString(roles.toArray()) + ")");
                auditHelper.audit(auditEvent);
            }
            session.setAttribute(GeneralConstants.ROLES_ID, roles);
        }
        response.setRoles(roles);
    }

    private void insantiateRoleValidator(String attribStr) throws ConfigurationException {
        if (attribStr != null && !"".equals(attribStr)) {
            try {
                Class<?> clazz = SecurityActions.loadClass(getClass(), attribStr);
                roleGenerator = (RoleGenerator) clazz.newInstance();
                logger.trace("RoleGenerator set to " + this.roleGenerator);
            } catch (Exception e) {
                logger.samlHandlerRoleGeneratorSetupError(e);
                throw logger.configurationError(e);
            }
        }
    }
}
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
package org.picketlink.identity.federation.core.factories;

import org.jboss.security.xacml.core.model.context.RequestType;
import org.jboss.security.xacml.core.model.context.ResponseType;
import org.picketlink.identity.federation.saml.v2.profiles.xacml.assertion.XACMLAuthzDecisionStatementType;

/**
 * Provides handle to XACML Object Factory
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jul 30, 2009
 */
public class XACMLContextFactory {

    /**
     * Create an XACML Authorization Decision Statement Type
     *
     * @param request
     * @param response
     *
     * @return
     */
    public static XACMLAuthzDecisionStatementType createXACMLAuthzDecisionStatementType(RequestType request,
                                                                                        ResponseType response) {
        XACMLAuthzDecisionStatementType xacmlStatement = new XACMLAuthzDecisionStatementType();
        xacmlStatement.setRequest(request);
        xacmlStatement.setResponse(response);
        return xacmlStatement;
    }
}
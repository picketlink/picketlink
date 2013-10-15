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
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;

public abstract class AbstractSignatureHandler extends BaseSAML2Handler {

    /**
     * <p>
     * Indicates if signature support is enabled. If this handler is defined in the configuration file, signatures are
     * enabled
     * by default. But if the GeneralConstants.SUPPORTS_SIGNATURES request option exists consider its value.
     * </p>
     *
     * @param request
     *
     * @return
     */
    protected boolean isSupportsSignature(SAML2HandlerRequest request) {
        return request.getOptions().get(GeneralConstants.SUPPORTS_SIGNATURES) == null
                || ((Boolean) request.getOptions().get(GeneralConstants.SUPPORTS_SIGNATURES));
    }

}

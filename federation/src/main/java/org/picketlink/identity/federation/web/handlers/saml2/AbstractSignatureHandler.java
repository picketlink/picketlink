package org.picketlink.identity.federation.web.handlers.saml2;

import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.web.constants.GeneralConstants;

public abstract class AbstractSignatureHandler extends BaseSAML2Handler {

    /**
     * <p>
     * Indicates if signature support is enabled. If this handler is defined in the configuration file, signatures are enabled
     * by default. But if the GeneralConstants.SUPPORTS_SIGNATURES request option exists consider its value.
     * </p>
     * 
     * @param request
     * @return
     */
    protected boolean isSupportsSignature(SAML2HandlerRequest request) {
        return request.getOptions().get(GeneralConstants.SUPPORTS_SIGNATURES) == null
                || ((Boolean) request.getOptions().get(GeneralConstants.SUPPORTS_SIGNATURES));
    }
    
}

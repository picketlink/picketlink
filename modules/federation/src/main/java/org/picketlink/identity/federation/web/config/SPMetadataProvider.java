package org.picketlink.identity.federation.web.config;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.config.SPType;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;

/**
 * Author: tito
 */

public class SPMetadataProvider extends AbstractSAMLConfigurationProvider{

    @Override
    public IDPType getIDPConfiguration() throws ProcessingException {
        throw new RuntimeException(ErrorCodes.ILLEGAL_METHOD_CALLED);
    }

    @Override
    public SPType getSPConfiguration() throws ProcessingException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.identity.federation.web.config;

import java.io.InputStream;
import java.net.URI;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.config.SPType;
import org.picketlink.identity.federation.core.config.TrustType;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.util.CoreConfigUtil;
import org.picketlink.identity.federation.saml.v2.metadata.EntitiesDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.IDPSSODescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.IndexedEndpointType;
import org.picketlink.identity.federation.saml.v2.metadata.SPSSODescriptorType;
import org.picketlink.identity.federation.web.util.SAMLConfigurationProvider;

/**
 * <p>
 * An instance of {@link SAMLConfigurationProvider} that can be used to generate the IDP configuration using SAML2 Metadata.
 * </p>
 * <p>
 * This provider uses the following in sequence whichever is available:
 * <ol>
 * <li>a idp-metadata.xml file available in its immediate class path.</li>
 * <li></li>
 * </ol>
 * </p>
 *
 * @author Anil Saldhana
 * @since Feb 15, 2012
 */
public class IDPMetadataConfigurationProvider extends AbstractSAMLConfigurationProvider implements SAMLConfigurationProvider {
    public static final String IDP_MD_FILE = "idp-metadata.xml";

    /**
     * @see SAMLConfigurationProvider#getIDPConfiguration()
     */
    public IDPType getIDPConfiguration() throws ProcessingException {
        IDPType idpType = null;
        if (fileAvailable()) {
            try {
                EntitiesDescriptorType entities = parseMDFile();
                IDPSSODescriptorType idpSSO = CoreConfigUtil.getIDPDescriptor(entities);
                
                if (idpSSO != null) {
                    idpType = CoreConfigUtil.getIDPType(idpSSO);
                }
                
                configureTrustedDomainsFromMetadata(idpType, entities);
            } catch (ParsingException e) {
                throw logger.processingError(e);
            }
        } else {
            throw logger.nullValueError(IDP_MD_FILE);
        }

        if (configParsedIDPType != null) {
            idpType.importFrom(configParsedIDPType);
        }

        return idpType;
    }

    public SPType getSPConfiguration() throws ProcessingException {
        throw new RuntimeException(ErrorCodes.ILLEGAL_METHOD_CALLED);
    }

    private boolean fileAvailable() {
        InputStream is = SecurityActions.loadStream(getClass(), IDP_MD_FILE);
        return is != null;
    }

    private EntitiesDescriptorType parseMDFile() throws ParsingException {
        InputStream is = SecurityActions.loadStream(getClass(), IDP_MD_FILE);

        if (is == null)
            throw logger.nullValueError(IDP_MD_FILE);

        SAMLParser parser = new SAMLParser();
        return (EntitiesDescriptorType) parser.parse(is);
    }
    
    /**
     * <p>Configures the IDP trusted domains by looking at {@link SPSSODescriptorType} definitions along the metadata.</p>
     * 
     * @param idpType
     * @param entities
     */
    private void configureTrustedDomainsFromMetadata(IDPType idpType, EntitiesDescriptorType entities) {
        if (idpType.getTrust() == null) {
            idpType.setTrust(new TrustType());
        }
        
        for (Object entityDescriptorObj : entities.getEntityDescriptor()) {
            EntityDescriptorType entityDescriptorType = (EntityDescriptorType) entityDescriptorObj;
            SPSSODescriptorType spDescriptor = CoreConfigUtil.getSPDescriptor(entityDescriptorType);
            
            if (spDescriptor != null) {
                for (IndexedEndpointType assertionConsumerService : spDescriptor.getAssertionConsumerService()) {
                    URI location = assertionConsumerService.getLocation();
                    
                    idpType.getTrust().addDomain(location.getHost());
                }
            }
        }
    }
}
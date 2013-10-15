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
package org.picketlink.identity.federation.web.config;

import org.picketlink.common.ErrorCodes;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.config.federation.IDPType;
import org.picketlink.config.federation.SPType;
import org.picketlink.config.federation.TrustType;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.util.CoreConfigUtil;
import org.picketlink.identity.federation.saml.v2.metadata.EntitiesDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.IDPSSODescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.IndexedEndpointType;
import org.picketlink.identity.federation.saml.v2.metadata.SPSSODescriptorType;
import org.picketlink.identity.federation.web.util.SAMLConfigurationProvider;

import java.io.InputStream;
import java.net.URI;

/**
 * <p>
 * An instance of {@link SAMLConfigurationProvider} that can be used to generate the IDP configuration using SAML2
 * Metadata.
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
     * <p>Configures the IDP trusted domains by looking at {@link SPSSODescriptorType} definitions along the
     * metadata.</p>
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
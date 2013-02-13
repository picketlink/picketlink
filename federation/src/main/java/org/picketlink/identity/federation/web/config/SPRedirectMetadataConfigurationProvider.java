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

import java.io.InputStream;

import org.picketlink.common.ErrorCodes;
import org.picketlink.config.federation.IDPType;
import org.picketlink.config.federation.SPType;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.util.CoreConfigUtil;
import org.picketlink.identity.federation.saml.v2.metadata.EntitiesDescriptorType;
import org.picketlink.identity.federation.web.util.SAMLConfigurationProvider;

/**
 * <p>
 * An instance of {@link SAMLConfigurationProvider} that can be used to generate the SP configuration for the HTTP-Redirect
 * binding using SAML2 Metadata.
 * </p>
 * <p>
 * This provider uses the following in sequence whichever is available:
 * <ol>
 * <li>a sp-metadata.xml file available in its immediate class path.</li>
 * <li></li>
 * </ol>
 * </p>
 *
 * @author Anil Saldhana
 * @since Feb 15, 2012
 */
public class SPRedirectMetadataConfigurationProvider extends AbstractSAMLConfigurationProvider implements
        SAMLConfigurationProvider {
    public static final String SP_MD_FILE = "sp-metadata.xml";

    public static final String bindingURI = JBossSAMLURIConstants.SAML_HTTP_REDIRECT_BINDING.get();

    /**
     * @see SAMLConfigurationProvider#getIDPConfiguration()
     */
    public IDPType getIDPConfiguration() throws ProcessingException {
        throw new RuntimeException(ErrorCodes.ILLEGAL_METHOD_CALLED);
    }

    /**
     * @see SAMLConfigurationProvider#getSPConfiguration()
     */
    public SPType getSPConfiguration() throws ProcessingException {
        SPType spType = null;
        if (fileAvailable()) {
            try {
                EntitiesDescriptorType entities = parseMDFile();
                spType = CoreConfigUtil.getSPConfiguration(entities, bindingURI);
            } catch (ParsingException e) {
                throw logger.processingError(e);
            }
        }
        if (configParsedSPType != null) {
            spType.importFrom(configParsedSPType);
        }
        return spType;
    }

    private boolean fileAvailable() {
        InputStream is = SecurityActions.loadStream(getClass(), SP_MD_FILE);
        return is != null;
    }

    private EntitiesDescriptorType parseMDFile() throws ParsingException {
        InputStream is = SecurityActions.loadStream(getClass(), SP_MD_FILE);

        if (is == null)
            throw logger.nullValueError(SP_MD_FILE);

        SAMLParser parser = new SAMLParser();
        return (EntitiesDescriptorType) parser.parse(is);
    }
}
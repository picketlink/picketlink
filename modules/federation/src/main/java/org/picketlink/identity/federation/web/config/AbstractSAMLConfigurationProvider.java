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

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.config.PicketLinkConfigParser;
import org.picketlink.config.federation.IDPType;
import org.picketlink.config.federation.PicketLinkType;
import org.picketlink.config.federation.SPType;
import org.picketlink.config.federation.parsers.SAMLConfigParser;
import org.picketlink.identity.federation.web.util.SAMLConfigurationProvider;

import java.io.InputStream;

/**
 * An abstact class to hold the common functionality across providers
 *
 * @author Anil Saldhana
 * @since Feb 22, 2012
 */
public abstract class AbstractSAMLConfigurationProvider implements SAMLConfigurationProvider {

    protected static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    public static final String VALIDATING_ALIAS = "ValidatingAlias";

    protected IDPType configParsedIDPType = null;

    protected SPType configParsedSPType = null;

    protected PicketLinkType configParsedPicketLinkType = null;

    /**
     * <p>
     * Sets a {@link InputStream} created from a picketlink-idfed.xml file.
     * This method expects to parse the deprecated configuration, PicketLinkIDP or PicketLinkSP element/type, as the
     * first element.
     * </p>
     *
     * @param is
     *
     * @throws ParsingException
     */
    @Deprecated
    public void setConfigFile(InputStream is) throws ParsingException {
        if (is == null) {
            throw logger.nullArgumentError("InputStream");
        }

        SAMLConfigParser parser = new SAMLConfigParser();

        Object parsedObject = parser.parse(is);

        if (parsedObject instanceof IDPType)
            configParsedIDPType = (IDPType) parsedObject;
        else
            configParsedSPType = (SPType) parsedObject;

    }

    /**
     * <p>
     * Sets a {@link InputStream} created from a picketlink.xml file.
     * This method expects to parse the consolidated configuration, PicketLink element/type, as the first element.
     * </p>
     *
     * @param is
     *
     * @throws ParsingException
     */
    public void setConsolidatedConfigFile(InputStream is) throws ParsingException {
        if (is == null) {
            throw logger.nullArgumentError("InputStream");
        }

        PicketLinkConfigParser parser = new PicketLinkConfigParser();

        PicketLinkType parsedObject = (PicketLinkType) parser.parse(is);

        if (parsedObject.getIdpOrSP() instanceof IDPType)
            configParsedIDPType = (IDPType) parsedObject.getIdpOrSP();
        else
            configParsedSPType = (SPType) parsedObject.getIdpOrSP();

        this.configParsedPicketLinkType = parsedObject;

    }

    public abstract IDPType getIDPConfiguration() throws ProcessingException;

    public abstract SPType getSPConfiguration() throws ProcessingException;

    @Override
    public PicketLinkType getPicketLinkConfiguration() throws ProcessingException {
        return this.configParsedPicketLinkType;
    }
}
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
package org.picketlink.identity.federation.web.util;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.config.PicketLinkConfigParser;
import org.picketlink.config.federation.IDPType;
import org.picketlink.config.federation.PicketLinkType;
import org.picketlink.config.federation.SPType;
import org.picketlink.config.federation.handler.Handlers;
import org.picketlink.config.federation.parsers.SAMLConfigParser;

import java.io.InputStream;

/**
 * Deals with Configuration
 *
 * @author Anil.Saldhana@redhat.com
 * @since Aug 21, 2009
 */
public class ConfigurationUtil {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    public static PicketLinkType getConfiguration(InputStream is) throws ParsingException {
        if (is == null)
            throw logger.nullArgumentError("inputstream");
        PicketLinkConfigParser parser = new PicketLinkConfigParser();
        PicketLinkType picketLinkType = (PicketLinkType) parser.parse(is);
        return picketLinkType;
    }

    /**
     * Get the IDP Configuration from the passed configuration
     *
     * @param is
     *
     * @return
     *
     * @throws ParsingException
     */
    public static IDPType getIDPConfiguration(InputStream is) throws ParsingException {
        if (is == null)
            throw logger.nullArgumentError("inputstream");

        SAMLConfigParser parser = new SAMLConfigParser();
        return (IDPType) parser.parse(is);
    }

    /**
     * Get the SP Configuration from the passed inputstream
     *
     * @param is
     *
     * @return
     *
     * @throws ParsingException
     */
    public static SPType getSPConfiguration(InputStream is) throws ParsingException {
        if (is == null)
            throw logger.nullArgumentError("inputstream");
        return (SPType) (new SAMLConfigParser()).parse(is);
    }

    /**
     * Get the Handlers from the configuration
     *
     * @param is
     *
     * @return
     *
     * @throws ParsingException
     */
    public static Handlers getHandlers(InputStream is) throws ParsingException {
        if (is == null)
            throw logger.nullArgumentError("inputstream");
        return (Handlers) (new SAMLConfigParser()).parse(is);
    }
}
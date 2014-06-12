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
package org.picketlink.identity.federation.api.saml.api;

import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Client API for SAML
 * @author Anil Saldhana
 * @since June 12, 2014
 */
public class SAMLClient {
    /**
     * Given an assertion as a string, parse into {@link org.picketlink.identity.federation.saml.v2.assertion.AssertionType}
     * @param assertionAsString
     * @return
     * @throws IOException
     * @throws ParsingException
     */
    public AssertionType parseAssertion(String assertionAsString) throws IOException, ParsingException{
        SAMLParser samlParser = new SAMLParser();
        return (AssertionType) samlParser.parse(new ByteArrayInputStream(assertionAsString.getBytes("UTF-8")));
    }

    /**
     * Verify whether an {@link org.picketlink.identity.federation.saml.v2.assertion.AssertionType} has expired
     * @param assertionType
     * @return
     * @throws ConfigurationException
     */
    public boolean hasExpired(AssertionType assertionType) throws ConfigurationException {
        return AssertionUtil.hasExpired(assertionType);
    }
}
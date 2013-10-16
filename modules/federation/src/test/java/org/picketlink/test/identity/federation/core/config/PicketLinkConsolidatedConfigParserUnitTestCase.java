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

package org.picketlink.test.identity.federation.core.config;

import org.junit.Test;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.config.PicketLinkConfigParser;
import org.picketlink.config.federation.IDPType;
import org.picketlink.config.federation.PicketLinkType;
import org.picketlink.config.federation.SPType;
import org.picketlink.config.federation.STSType;
import org.picketlink.config.federation.handler.Handlers;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test to parse the Consolidated PicketLink Configuration in picketlink.xml
 *
 * @author anil saldhana
 */
public class PicketLinkConsolidatedConfigParserUnitTestCase {

    @Test
    public void testIDP() throws ParsingException {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/config/picketlink-idp.xml");
        PicketLinkConfigParser parser = new PicketLinkConfigParser();
        Object result = parser.parse(configStream);
        assertNotNull(result);
        PicketLinkType picketlink = (PicketLinkType) result;
        IDPType idp = (IDPType) picketlink.getIdpOrSP();
        assertNotNull(idp);
        assertTrue(picketlink.isEnableAudit());

        // asserts the StrictPostBinding attribute. Default is true, but for this test it was changed to true in the configuration file. 
        assertFalse(idp.isStrictPostBinding());

        assertEquals("TestIdentityParticipantStack", idp.getIdentityParticipantStack());
    }

    @Test
    public void testSP() throws ParsingException {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/config/picketlink-sp.xml");
        PicketLinkConfigParser parser = new PicketLinkConfigParser();
        Object result = parser.parse(configStream);
        assertNotNull(result);
        PicketLinkType picketlink = (PicketLinkType) result;
        SPType sp = (SPType) picketlink.getIdpOrSP();
        assertNotNull(sp);
        assertEquals("REDIRECT", sp.getBindingType());
        assertEquals("tomcat", sp.getServerEnvironment());
        assertEquals("someURL", sp.getRelayState());
        assertEquals("/someerror.jsp", sp.getErrorPage());
        assertEquals("/customLogoutPage.jsp", sp.getLogOutPage());
        assertTrue(sp.isSupportsSignature());
        assertTrue(picketlink.isEnableAudit());
    }

    @Test
    public void testSTS() throws ParsingException {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/config/picketlink-consolidated-sts.xml");
        PicketLinkConfigParser parser = new PicketLinkConfigParser();
        Object result = parser.parse(configStream);
        assertNotNull(result);
        PicketLinkType picketlink = (PicketLinkType) result;
        STSType sts = picketlink.getStsType();
        assertNotNull(sts);
        assertTrue(picketlink.isEnableAudit());
    }

    @Test
    public void testHandlers() throws ParsingException {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/config/picketlink-handlers.xml");
        PicketLinkConfigParser parser = new PicketLinkConfigParser();
        Object result = parser.parse(configStream);
        assertNotNull(result);
        PicketLinkType picketlink = (PicketLinkType) result;
        Handlers handlers = picketlink.getHandlers();
        assertNotNull(handlers);
        assertNotNull(handlers.getHandlerChainClass());
        assertFalse(handlers.getHandler().isEmpty());
    }
}
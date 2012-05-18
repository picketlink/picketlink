package org.picketlink.test.identity.federation.core.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.InputStream;
import java.util.logging.Handler;

import org.junit.Test;
import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.config.PicketLinkType;
import org.picketlink.identity.federation.core.config.SPType;
import org.picketlink.identity.federation.core.config.STSType;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.handler.config.Handlers;
import org.picketlink.identity.federation.core.parsers.config.PicketLinkConfigParser;

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
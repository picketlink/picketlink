/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.picketlink.test.config.idm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.junit.Test;
import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.config.PicketLinkType;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.parsers.config.PicketLinkConfigParser;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class IDMConfigParserTestCase {

    @Test
    public void testIDM() throws ParsingException {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/config/picketlink.xml");
        PicketLinkConfigParser parser = new PicketLinkConfigParser();
        Object result = parser.parse(configStream);
        assertNotNull(result);
        PicketLinkType picketlink = (PicketLinkType) result;

        IDPType idp = (IDPType) picketlink.getIdpOrSP();
        assertNotNull(idp);
        assertFalse(picketlink.isEnableAudit());
        assertNotNull(picketlink.getStsType());
        assertNotNull(picketlink.getHandlers());

        assertNotNull(picketlink.getIdmType());
    }
}

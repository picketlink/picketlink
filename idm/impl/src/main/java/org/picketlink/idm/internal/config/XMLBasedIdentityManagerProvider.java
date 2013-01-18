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

package org.picketlink.idm.internal.config;

import java.io.InputStream;

import org.picketlink.config.idm.parsers.IDMConfigParser;
import org.picketlink.identity.federation.core.config.PicketLinkType;
import org.picketlink.identity.federation.core.config.idm.IDMType;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.parsers.config.PicketLinkConfigParser;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.SecurityConfigurationException;

/**
 * Creating IDM runtime from parsed XML configuration
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class XMLBasedIdentityManagerProvider {

    public IdentityManager buildIdentityManager(InputStream inputStream) {
        try {
            // TODO: Think about subclassing AbstractSAMLConfigurationProvider (if it's going to be decoupled from federation module)
            PicketLinkConfigParser parser = new PicketLinkConfigParser();
            PicketLinkType plType = (PicketLinkType)parser.parse(inputStream);
            IDMType idmConfiguration = plType.getIdmType();
            return buildIdentityManager(idmConfiguration);
        } catch (ParsingException pe) {
            throw new SecurityConfigurationException("Could not parse picketlink configuration", pe);
        }
    }

    protected IdentityManager buildIdentityManager(IDMType idmType) {
        // TODO: implement
        return null;
    }
}

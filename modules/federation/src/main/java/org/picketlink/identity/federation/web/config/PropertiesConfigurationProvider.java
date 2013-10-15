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
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.StringUtil;
import org.picketlink.config.federation.IDPType;
import org.picketlink.config.federation.PicketLinkType;
import org.picketlink.config.federation.SPType;
import org.picketlink.config.federation.TrustType;
import org.picketlink.identity.federation.web.util.SAMLConfigurationProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A properties file based {@link SAMLConfigurationProvider}. For the IDP configuration, a idp_config.properties is
 * expected.
 * For the SP configuration, a sp_config.properties is expected.
 *
 * @author Anil.Saldhana@redhat.com
 * @since Aug 9, 2011
 */
public class PropertiesConfigurationProvider implements SAMLConfigurationProvider {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    public static final String IDP_FILE = "idp_config.properties";

    public static final String SP_FILE = "sp_config.properties";

    public IDPType getIDPConfiguration() throws ProcessingException {
        InputStream is = SecurityActions.loadStream(getClass(), IDP_FILE);
        if (is == null)
            throw logger.nullValueError(IDP_FILE);
        Properties props = new Properties();
        try {
            props.load(is);
        } catch (IOException e) {
            throw logger.processingError(e);
        }
        IDPType idp = new IDPType();
        idp.setIdentityURL(props.getProperty("idp.url"));
        String domains = props.getProperty("domains");
        if (StringUtil.isNotNull(domains)) {
            TrustType trustType = new TrustType();
            trustType.setDomains(domains);
            idp.setTrust(trustType);
        }

        return idp;
    }

    public SPType getSPConfiguration() throws ProcessingException {
        InputStream is = SecurityActions.loadStream(getClass(), SP_FILE);
        if (is == null)
            throw logger.nullValueError(SP_FILE);
        Properties props = new Properties();
        try {
            props.load(is);
        } catch (IOException e) {
            throw logger.processingError(e);
        }
        SPType sp = new SPType();
        sp.setIdentityURL(props.getProperty("idp.url"));
        sp.setServiceURL("service.url");
        String domains = props.getProperty("domains");
        if (StringUtil.isNotNull(domains)) {
            TrustType trustType = new TrustType();
            trustType.setDomains(domains);
            sp.setTrust(trustType);
        }

        return sp;
    }

    @Override
    public PicketLinkType getPicketLinkConfiguration() throws ProcessingException {
        // TODO Auto-generated method stub
        return null;
    }
}
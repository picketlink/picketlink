/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.picketlink.test.identity.federation.web.saml.config;

import org.junit.Test;
import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.config.federation.IDPType;
import org.picketlink.config.federation.PicketLinkType;
import org.picketlink.config.federation.SPType;
import org.picketlink.identity.federation.core.audit.PicketLinkAuditHelper;
import org.picketlink.identity.federation.web.filters.IDPFilter;
import org.picketlink.identity.federation.web.util.SAMLConfigurationProvider;
import org.picketlink.test.identity.federation.web.mock.MockFilterConfig;
import org.picketlink.test.identity.federation.web.mock.MockServletContext;

import static org.junit.Assert.assertEquals;

/**
 * @author Pedro Igor
 */
public class IDPFilterConfigurationTestCase {

    @Test
    public void testConfigurationProvider() throws Exception {
        IDPFilter filter = new IDPFilter() {
            @Override
            protected void startPicketLink() {
                assertEquals(CustomConfigProvider.class, this.configProvider.getClass());
            }
        };

        MockServletContext servletContext = new MockServletContext();

        servletContext.setInitParameter(GeneralConstants.CONFIG_PROVIDER, CustomConfigProvider.class.getName());

        filter.init(new MockFilterConfig(servletContext));
    }

    @Test
    public void testAuditHelper() throws Exception {
        IDPFilter filter = new IDPFilter() {
            @Override
            protected void startPicketLink() {
                assertEquals(CustomAuditHelper.class, this.auditHelper.getClass());
            }
        };

        MockServletContext servletContext = new MockServletContext();

        servletContext.setInitParameter(GeneralConstants.AUDIT_HELPER, CustomAuditHelper.class.getName());

        filter.init(new MockFilterConfig(servletContext));
    }

    public static class CustomConfigProvider implements SAMLConfigurationProvider {

        @Override
        public IDPType getIDPConfiguration() throws ProcessingException {
            return null;
        }

        @Override
        public SPType getSPConfiguration() throws ProcessingException {
            return null;
        }

        @Override
        public PicketLinkType getPicketLinkConfiguration() throws ProcessingException {
            return null;
        }
    }

    public static class CustomAuditHelper extends PicketLinkAuditHelper {

        public CustomAuditHelper() throws ConfigurationException {
            super(null);
        }

        @Override
        protected void configureAuditManager(String securityDomainName) throws ConfigurationException {
            //no-op
        }
    }
}

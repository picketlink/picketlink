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

package org.picketlink.test.integration.idm.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.internal.IdentityManagerFactory;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;
import org.picketlink.test.integration.AbstractArquillianTestCase;
import org.picketlink.test.integration.ArchiveUtils;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Pedro Igor
 *
 */
public class IdentityManagerFactoryTestCase extends AbstractArquillianTestCase {

    private static final String TESTING_REALM_NAME = "Testing";

    @Inject
    private IdentityManagerFactory identityManagerFactory;
    
    @Deployment
    public static WebArchive createDeployment() {
        return ArchiveUtils.create(IdentityManagerFactoryTestCase.class, Resources.class);
    }

    @Test
    public void testCeateIdentityManagerForDefaultRealm() throws Exception {
        IdentityManager identityManager = this.identityManagerFactory.createIdentityManager();
        
        assertNotNull(identityManager);
    }

    @Test
    public void testCeateIdentityManagerForTestingRealm() throws Exception {
        Partition partition = this.identityManagerFactory.getRealm(TESTING_REALM_NAME);
        
        assertNotNull(partition);
        
        IdentityManager identityManager = this.identityManagerFactory.createIdentityManager(partition);
        
        assertNotNull(identityManager);
    }

    @Test
    public void testInvalidRealm() throws Exception {
        Partition partition = this.identityManagerFactory.getRealm("NotConfiguredRealm");
        assertNull(partition);
    }

    @ApplicationScoped
    public static class Resources {
        
        @Produces
        public IdentityConfiguration buildIDMConfiguration() {
            IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
            
            builder
                .stores()
                    .file()
                        .addRealm(Realm.DEFAULT_REALM, TESTING_REALM_NAME)
                        .supportAllFeatures();
                    
            return builder.build();
        }
        
    } 
}

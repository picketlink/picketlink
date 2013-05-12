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

package org.picketlink.test.idm.config;

import org.junit.Assert;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.IdentityManagerFactory;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.builder.IdentityConfigurationBuilder;
import org.picketlink.idm.internal.DefaultIdentityManagerFactory;
import org.picketlink.idm.model.SimpleUser;

/**
 * @author Pedro Igor
 *
 */
public class ConfigurationAPITestCase {

    @Test
    public void testConfiguration() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
        
        builder
            .stores()
                .file()
                    .workingDirectory("/tmp/pl-idm")
                    .preserveState(false)
                    .asyncWrite(true)
                    .asyncWriteThreadPool(10)
                    .supportAllFeatures();
        
        IdentityConfiguration configuration = builder.build();
        
        Assert.assertNotNull(configuration);
        
        IdentityManagerFactory identityManagerFactory = new DefaultIdentityManagerFactory(configuration);
        
        IdentityManager identityManager = identityManagerFactory.createIdentityManager();
        
        identityManager.add(new SimpleUser("john"));
    }
    
}

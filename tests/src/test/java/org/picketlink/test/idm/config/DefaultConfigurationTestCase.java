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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.FileIdentityStoreConfiguration;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.AbstractArquillianTestCase;
import org.picketlink.test.util.ArchiveUtils;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Pedro Igor
 *
 */
public class DefaultConfigurationTestCase extends AbstractArquillianTestCase {

    @Inject
    private PartitionManager partitionManager;

    @Inject
    private IdentityManager identityManager;

    @Deployment
    public static WebArchive deploy() {
        return ArchiveUtils.create(DefaultConfigurationTestCase.class, AbstractArquillianTestCase.class);
    }
    
    @Test
    public void testDefaultConfiguration() throws Exception {
        this.identityManager.lookupIdentityById(IdentityType.class, "1");

        Collection<IdentityConfiguration> configurations = this.partitionManager.getConfigurations();

        assertEquals(1, configurations.size());

        IdentityConfiguration identityConfiguration = configurations.iterator().next();

        List<? extends IdentityStoreConfiguration> configuredStores = identityConfiguration.getStoreConfiguration();

        assertEquals(1, configuredStores.size());

        IdentityStoreConfiguration identityStoreConfiguration = configuredStores.get(0);

        assertEquals(FileIdentityStoreConfiguration.class, identityStoreConfiguration.getClass());

        User user = new User("john");

        this.identityManager.add(user);

        Password password = new Password("abcd1234");

        this.identityManager.updateCredential(user, password);

        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials();

        credentials.setUsername(user.getLoginName());
        credentials.setPassword(password);

        this.identityManager.validateCredentials(credentials);

        assertEquals(Credentials.Status.VALID, credentials.getStatus());

    }

}
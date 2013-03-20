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

package org.picketlink.idm.internal.config;

import org.picketlink.idm.config.FileIdentityStoreConfiguration;

/**
 * @author Pedro Silva
 *
 */
public class FileIdentityStoreConfigurationBuilder extends IdentityStoreConfigurationBuilder<FileIdentityStoreConfigurationBuilder, FileIdentityStoreConfiguration> {

    protected FileIdentityStoreConfigurationBuilder(ConfigurationBuilder<?> builder) {
        super(new FileIdentityStoreConfiguration(), builder);
    }

    public FileIdentityStoreConfigurationBuilder workingDir(String workingDir) {
        getConfiguration().setWorkingDir(workingDir);
        return this;
    }

    public FileIdentityStoreConfigurationBuilder alwaysCreateFiles(boolean alwaysCreateFiles) {
        getConfiguration().setAlwaysCreateFiles(alwaysCreateFiles);
        return this;
    }

    public FileIdentityStoreConfigurationBuilder asyncWrite(boolean asyncWrite) {
        getConfiguration().setAsyncWrite(asyncWrite);
        return this;
    }

    public FileIdentityStoreConfigurationBuilder asyncThreadPool(int asyncThreadPool) {
        getConfiguration().setAsyncThreadPool(asyncThreadPool);
        return this;
    }

}
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

package org.picketlink.idm.config;


/**
 * @author Pedro Igor
 *
 */
public class FileStoreConfigurationBuilder extends
        AbstractIdentityStoreConfigurationBuilder<FileIdentityStoreConfiguration, FileStoreConfigurationBuilder> {

    private String workingDirectory = "/tmp/pl-idm";
    private boolean preserveState = false;
    private boolean asyncWrite = false;
    private int asyncWriteThreadPool = 5;

    public FileStoreConfigurationBuilder(IdentityStoresConfigurationBuilder builder) {
        super(builder);
    }

    public FileStoreConfigurationBuilder workingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
        return this;
    }

    public FileStoreConfigurationBuilder preserveState(boolean preserveState) {
        this.preserveState = preserveState;
        return this;
    }

    public FileStoreConfigurationBuilder asyncWrite(boolean asyncWrite) {
        this.asyncWrite = asyncWrite;
        return this;
    }

    public FileStoreConfigurationBuilder asyncWriteThreadPool(int poolSize) {
        this.asyncWriteThreadPool = poolSize;
        return this;
    }

    @Override
    public FileIdentityStoreConfiguration create() {
        return new FileIdentityStoreConfiguration(this.workingDirectory, this.preserveState, this.asyncWrite,
                this.asyncWriteThreadPool, getSupportedFeatures(), getSupportedRelationships(), getRealms(), getTiers(),
                getContextInitializers(), getCredentialHandlerProperties(), getCredentialHandlers());
    }

    @Override
    public void validate() {
        super.validate();

        if (this.workingDirectory == null) {
            throw new SecurityConfigurationException("You must provide a not null working directory.");
        }

        if (this.asyncWriteThreadPool <= 0) {
            throw new SecurityConfigurationException("The thread pool size must be greater than zero.");
        }
    }

    @Override
    public FileStoreConfigurationBuilder readFrom(FileIdentityStoreConfiguration configuration) {
        this.workingDirectory = configuration.getWorkingDir();
        this.preserveState = configuration.isAlwaysCreateFiles();
        this.asyncWrite = configuration.isAsyncWrite();
        this.asyncWriteThreadPool = configuration.getAsyncThreadPool();

        return this;
    }
}
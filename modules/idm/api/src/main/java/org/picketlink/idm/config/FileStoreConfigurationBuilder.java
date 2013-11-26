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
 * <p>{@link IdentityStoreConfigurationBuilder} implementation which knows how to build a
 * {@link FileIdentityStoreConfiguration}.</p>
 *
 * @author Pedro Igor
 *
 */
public class FileStoreConfigurationBuilder extends
        IdentityStoreConfigurationBuilder<FileIdentityStoreConfiguration, FileStoreConfigurationBuilder> {

    private String workingDirectory;
    private boolean preserveState = false;
    private boolean asyncWrite = false;
    private int asyncWriteThreadPool = 5;

    public FileStoreConfigurationBuilder(IdentityStoresConfigurationBuilder builder) {
        super(builder);
    }

    /**
     * <p>Defines the working directory that should be used to store data.</p>
     *
     * @param workingDirectory
     * @return
     */
    public FileStoreConfigurationBuilder workingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
        return this;
    }

    /**
     * <p>Tells the store to preserve state between initializations. If true, previously stored data will be preserved.</p>
     *
     * <p>This behavior defaults to false. Previously stored data will be always deleted.</p>
     *
     * @param preserveState
     * @return
     */
    public FileStoreConfigurationBuilder preserveState(boolean preserveState) {
        this.preserveState = preserveState;
        return this;
    }

    /**
     * <p>Indicates that write operations should be done asynchronously.</p>
     *
     * <p>Defaults to false.</p>
     *
     * @param asyncWrite
     * @return
     */
    public FileStoreConfigurationBuilder asyncWrite(boolean asyncWrite) {
        this.asyncWrite = asyncWrite;
        return this;
    }

    /**
     * <p>If asyncWrite is enabled, defines the size of the thread pool.</p>
     *
     * @param poolSize
     * @return
     */
    public FileStoreConfigurationBuilder asyncWriteThreadPool(int poolSize) {
        this.asyncWriteThreadPool = poolSize;
        return this;
    }

    @Override
    protected FileIdentityStoreConfiguration create() {
        return new FileIdentityStoreConfiguration(
                this.workingDirectory,
                this.preserveState,
                this.asyncWrite,
                this.asyncWriteThreadPool,
                getSupportedTypes(),
                getUnsupportedTypes(),
                getContextInitializers(),
                getCredentialHandlerProperties(),
                getCredentialHandlers(),
                isSupportAttributes(),
                isSupportCredentials(),
                isSupportPermissions());
    }

    @Override
    protected void validate() {
        super.validate();

        if (this.asyncWriteThreadPool <= 0) {
            throw new SecurityConfigurationException("The thread pool size must be greater than zero.");
        }
    }

    @Override
    protected FileStoreConfigurationBuilder readFrom(FileIdentityStoreConfiguration configuration) {
        super.readFrom(configuration);
        this.workingDirectory = configuration.getWorkingDir();
        this.preserveState = !configuration.isAlwaysCreateFiles();
        this.asyncWrite = configuration.isAsyncWrite();
        this.asyncWriteThreadPool = configuration.getAsyncThreadPool();

        return this;
    }
}
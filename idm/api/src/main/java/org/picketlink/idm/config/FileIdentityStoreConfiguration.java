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

package org.picketlink.idm.config;

import org.picketlink.idm.SecurityConfigurationException;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class FileIdentityStoreConfiguration extends BaseAbstractStoreConfiguration<FileIdentityStoreConfiguration> {

    private int asyncThreadPool = 5;
    private boolean asyncWrite = false;
    private boolean alwaysCreateFiles = true;
    private String workingDir;

    @Override
    protected void initConfig() throws SecurityConfigurationException {
    }

    public String getWorkingDir() {
        return this.workingDir;
    }

    public FileIdentityStoreConfiguration setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
        return this;
    }

    public boolean isAlwaysCreateFiles() {
        return this.alwaysCreateFiles;
    }

    public FileIdentityStoreConfiguration setAlwaysCreateFiles(boolean alwaysCreateFiles) {
        this.alwaysCreateFiles = alwaysCreateFiles;
        return this;
    }

    public FileIdentityStoreConfiguration setAsyncWrite(boolean asyncWrite) {
        this.asyncWrite = asyncWrite;
        return this;
    }

    public boolean isAsyncWrite() {
        return this.asyncWrite;
    }

    public FileIdentityStoreConfiguration setAsyncThreadPool(int asyncThreadPool) {
        this.asyncThreadPool = asyncThreadPool;
        return this;
    }

    public int getAsyncThreadPool() {
        return this.asyncThreadPool;
    }

}
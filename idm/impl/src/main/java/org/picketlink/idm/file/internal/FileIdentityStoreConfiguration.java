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

package org.picketlink.idm.file.internal;

import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.BaseAbstractStoreConfiguration;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class FileIdentityStoreConfiguration extends BaseAbstractStoreConfiguration {

    private int asyncThreadPool = 5;
    private boolean asyncWrite = false;
    private boolean alwaysCreateFiles = true;
    private String workingDir;

    @Override
    public void initConfig() throws SecurityConfigurationException {
    }

    public String getWorkingDir() {
        return this.workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public boolean isAlwaysCreateFiles() {
        return this.alwaysCreateFiles;
    }

    public void setAlwaysCreateFiles(boolean alwaysCreateFiles) {
        this.alwaysCreateFiles = alwaysCreateFiles;
    }

    public void setAsyncWrite(boolean asyncWrite) {
        this.asyncWrite = asyncWrite;
    }

    public boolean isAsyncWrite() {
        return this.asyncWrite;
    }

    public void setAsyncThreadPool(int asyncThreadPool) {
        this.asyncThreadPool = asyncThreadPool;
    }

    public int getAsyncThreadPool() {
        return this.asyncThreadPool;
    }

}
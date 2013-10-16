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
package org.picketlink.identity.federation.core.sts.registry;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * A base class for file based STS operations
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jan 4, 2011
 */
public abstract class FileBasedSTSOperations {

    protected static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    protected File directory;

    public FileBasedSTSOperations() {
        // use the default location registry file location.
        StringBuilder builder = new StringBuilder();
        builder.append(System.getProperty("user.home"));
        builder.append(System.getProperty("file.separator") + "picketlink-store");
        builder.append(System.getProperty("file.separator") + "sts");

        // check if the $HOME/picketlink-store/sts directory exists.
        directory = new File(builder.toString());
        if (!directory.exists())
            directory.mkdirs();
    }

    /**
     * Create a file with the provided name
     *
     * @param fileName
     *
     * @return {@code File} handle
     */
    protected File create(String fileName) {
        if (fileName == null)
            throw logger.nullArgumentError("file name");

        // check if the specified file exists. If not, create it.
        File createdFile = new File(fileName);
        if (!createdFile.exists()) {
            try {
                createdFile.createNewFile();
            } catch (IOException ioe) {
                logger.debug("Error creating file: " + ioe.getMessage());
                ioe.printStackTrace();
            }
        }
        return createdFile;
    }
}
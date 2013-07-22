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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import org.picketlink.idm.IdentityManagementException;

/**
 * @author Pedro Silva
 *
 */
public final class FileUtils {

    private FileUtils() {

    }

    /**
     * <p>
     * Read the specified {@link File} instance and try to read a {@link Object} with the given parametrized type.
     * </p>
     *
     * @param file
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T readObject(File file) {
        ObjectInputStream ois = null;
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(file);

            ois = new ObjectInputStream(fis);

            return (T) ois.readObject();
        } catch (IdentityManagementException ime) {
            throw ime;
        } catch (Exception e) {
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
            }
        }

        return null;
    }

    /**
     * <p>
     * Check if the specified {@link File} exists. If not create it.
     * </p>
     *
     * @param file
     * @return
     */
    public static File createFileIfNotExists(File file) {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
            }
        }

        return file;
    }

    /**
     * <p>
     * Delete the specified {@link File} instance from the filesystem. If the instance is a directory it will be deleted
     * recursively, otherwise only the file will be deleted.
     * </p>
     *
     * @param file
     */
    public static void delete(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                String[] childs = file.list();

                for (String childName : childs) {
                    File child = new File(file.getPath() + File.separator + childName);
                    delete(child);
                }

                file.delete();
            } else {
                file.delete();
            }
        }
    }

}

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

package org.picketlink.idm.file.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * @author Pedro Silva
 * 
 */
public final class FileUtils {

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

        try {
            FileInputStream fis = new FileInputStream(file);
            ois = new ObjectInputStream(fis);

            return (T) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (ois != null) {
                    ois.close();
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
    public static File createFile(File file) {
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

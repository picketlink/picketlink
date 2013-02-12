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

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.picketlink.idm.IdentityManagementException;

/**
 * @author Pedro Silva
 *
 */
public abstract class AbstractFileEntry<T> implements Serializable {

    private static final long serialVersionUID = -3979114481984415635L;
    
    private String version;
    private String type;
    private Map<String, Serializable> properties = new HashMap<String, Serializable>();
    
    private transient T loadedObject; 

    protected AbstractFileEntry(String version, T object) {
        if (version == null) {
            throw new IdentityManagementException("Version not specified.");
        }

        this.version = version;

        if (object == null) {
            throw new IdentityManagementException("Could not create a null file entry.");
        }
        
        this.loadedObject = object;
        this.type = this.loadedObject.getClass().getName();
    }
    
    private void writeObject(ObjectOutputStream s) {
        try {
            s.writeObject(this.version);
            s.writeObject(this.type);

            doPopulateProperties(this.properties);
            
            s.writeObject(this.properties);
            
            doWriteObject(s);
        } catch (Exception e) {
            throw new IdentityManagementException("Error marshalling file entry.", e);
        }
    }

    protected void doWriteObject(ObjectOutputStream s) throws Exception {
        
    }

    protected abstract void doPopulateProperties(Map<String, Serializable> properties) throws Exception;

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream s) {
        try {
            this.version = (String) s.readObject();
            this.type = (String) s.readObject();
            this.properties = (Map<String, Serializable>) s.readObject();
            doReadObject(s);
            this.loadedObject = doPopulateEntry(this.properties);
        } catch (EOFException eof) {
        } catch (Exception e) {
            throw new IdentityManagementException("Error unmarshalling file entry.", e);
        }
    }

    protected void doReadObject(ObjectInputStream s) throws Exception {
        
    }

    protected abstract T doPopulateEntry(Map<String, Serializable> properties) throws Exception;
    
    protected T getEntry() {
        return this.loadedObject;
    }
    
    public String getType() {
        return this.type;
    }
    
    public String getVersion() {
        return this.version;
    }
}

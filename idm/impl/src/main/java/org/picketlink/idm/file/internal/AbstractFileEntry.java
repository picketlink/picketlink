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

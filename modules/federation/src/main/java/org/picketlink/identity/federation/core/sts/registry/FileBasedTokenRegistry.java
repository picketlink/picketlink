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

import org.picketlink.identity.federation.core.sts.PicketLinkCoreSTS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A File based implementation of the {@code SecurityTokenRegistry}
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jan 4, 2011
 */
public class FileBasedTokenRegistry extends FileBasedSTSOperations implements SecurityTokenRegistry {

    protected static final String FILE_NAME = "token.registry";

    // the file that stores the tokens.
    protected File registryFile;

    protected Map<String, TokenHolder> holders = new HashMap<String, TokenHolder>();

    public FileBasedTokenRegistry() {
        this(FILE_NAME);
    }

    public FileBasedTokenRegistry(String fileName) {
        super();
        if (directory == null)
            throw logger.nullValueError("directory");

        // check if the default registry file exists.
        this.registryFile = create(fileName);

        try {
            read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see org.picketlink.identity.federation.core.sts.registry.SecurityTokenRegistry#addToken(java.lang.String,
     *      java.lang.Object)
     */
    public void addToken(String tokenID, Object token) throws IOException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(PicketLinkCoreSTS.rte);

        if (!(token instanceof Serializable))
            throw logger.notSerializableError("Token");

        holders.put(tokenID, new TokenHolder(tokenID, token));
        flush();
    }

    /**
     * @see org.picketlink.identity.federation.core.sts.registry.SecurityTokenRegistry#removeToken(java.lang.String)
     */
    public void removeToken(String tokenID) throws IOException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(PicketLinkCoreSTS.rte);

        holders.remove(tokenID);
        flush();
    }

    /**
     * @see org.picketlink.identity.federation.core.sts.registry.SecurityTokenRegistry#getToken(java.lang.String)
     */
    public Object getToken(String tokenID) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(PicketLinkCoreSTS.rte);

        TokenHolder holder = holders.get(tokenID);
        if (holder != null)
            return holder.token;

        return null;
    }

    protected synchronized void flush() throws IOException {
        FileOutputStream fos = new FileOutputStream(registryFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(holders);
        oos.close();
    }

    @SuppressWarnings("unchecked")
    protected synchronized void read() throws IOException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(PicketLinkCoreSTS.rte);

        FileInputStream fis = new FileInputStream(registryFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        try {
            holders = (Map<String, TokenHolder>) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        } finally {
            ois.close();
        }
    }

    protected static class TokenHolder implements Serializable {

        private static final long serialVersionUID = 1L;

        String id;

        Object token;

        public TokenHolder(String id, Object token) {
            super();
            this.id = id;
            this.token = token;
        }

        public String getId() {
            return id;
        }

        public Object getToken() {
            return token;
        }
    }
}
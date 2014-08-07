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
package org.picketlink.json.jose.crypto;

import org.picketlink.json.JsonException;

/**
 * The Interface SignatureProvider.
 *
 * @author Pedro Igor
 */
public interface SignatureProvider {

    /**
     * Sign the data using specified algorithm and specified key.
     *
     * @param data the data
     * @param algorithm the algorithm
     * @param key the key
     * @return the byte[]
     * @throws JsonException the json exception
     */
    byte[] sign(byte[] data, Algorithm algorithm, byte[] key) throws JsonException;

    /**
     * Verify the data using specified algorithm, signature and specified key.
     *
     * @param data the data
     * @param algorithm the algorithm
     * @param signature the signature
     * @param key the key
     * @return true, if successful
     */
    boolean verify(byte[] data, Algorithm algorithm, byte[] signature, byte[] key);

}

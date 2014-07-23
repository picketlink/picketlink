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

/**
 * Authenticated cipher text. It holds cipher text and authentication tag.
 *
 * @author Giriraj Sharma
 */
public class AuthenticatedCipherText {

    /**
     * The cipher text.
     */
    private final byte[] cipherText;

    /**
     * The authentication tag.
     */
    private final byte[] authenticationTag;

    /**
     * Creates a new authenticated cipher text.
     *
     * @param cipherText The cipher text. Must not be {@code null}.
     * @param authenticationTag The authentication tag. Must not be {@code null}.
     */
    public AuthenticatedCipherText(final byte[] cipherText, final byte[] authenticationTag) {

        if (cipherText == null)
            throw new IllegalArgumentException("The cipher text must not be null");
        this.cipherText = cipherText;

        if (authenticationTag == null)
            throw new IllegalArgumentException("The authentication tag must not be null");
        this.authenticationTag = authenticationTag;
    }

    /**
     * Gets the cipher text.
     *
     * @return The cipher text.
     */
    public byte[] getCipherText() {
        return cipherText;
    }

    /**
     * Gets the authentication tag.
     *
     * @return The authentication tag.
     */
    public byte[] getAuthenticationTag() {
        return authenticationTag;
    }
}
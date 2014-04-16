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

import static org.picketlink.json.JsonMessages.MESSAGES;

/**
 * <p>{@link java.lang.Enum} representing all supported crypto algorithms.</p>
 *
 * @author Pedro Igor
 */
public enum Algorithm {

    NONE("none", null),

    // HMAC
    HS256("HMACSHA256", HMACSignatureProvider.instance()),
    HS384("HMACSHA384", HMACSignatureProvider.instance()),
    HS512("HMACSHA512", HMACSignatureProvider.instance()),

    // RSASSA-PKCS1-v1_5
    RS256("SHA256withRSA", RSASignatureProvider.instance()),
    RS384("SHA384withRSA", RSASignatureProvider.instance()),
    RS512("SHA512withRSA", RSASignatureProvider.instance());

    private final String algorithm;
    private final SignatureProvider signatureProvider;

    Algorithm(String algorithm, SignatureProvider signatureProvider) {
        this.algorithm = algorithm;
        this.signatureProvider = signatureProvider;
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    public boolean isNone() {
        return NONE.equals(this);
    }

    public SignatureProvider getSignatureProvider() {
        return this.signatureProvider;
    }

    public static Algorithm resolve(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException iae) {
            throw MESSAGES.cryptoNoSuchAlgorithm(name, iae);
        }
    }
}

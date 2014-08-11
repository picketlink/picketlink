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
package org.picketlink.json.jose;

import org.picketlink.json.jose.crypto.Algorithm;
import org.picketlink.json.jose.crypto.SignatureProvider;
import org.picketlink.json.jwt.JWT;

import javax.json.JsonObject;

import static org.picketlink.json.JsonConstants.COMMON.ALG;
import static org.picketlink.json.JsonConstants.COMMON.PERIOD;
import static org.picketlink.json.JsonMessages.MESSAGES;
import static org.picketlink.json.util.Base64Util.b64Encode;

/**
 * <p>This class represents a JSON Web Signature. </p>
 *
 * @author Pedro Igor
 */
public class JWS extends JWT {

    private final byte[] key;

    /**
     * <p>Creates a new instance using the claims set and values from the given {@link javax.json.JsonObject}.</p>
     *
     * @param headers
     * @param claims The claims set and their respective values.
     */
    protected JWS(JsonObject headers, JsonObject claims, byte[] key) {
        super(headers, claims);
        this.key = key;
    }

    /**
     * <p>Identifies the cryptographic algorithm used to secure the JWS.</p>
     *
     * @return
     */
    public String getAlgorithm() {
        if (!getHeaders().containsKey(ALG)) {
            throw MESSAGES.missingHeader(ALG);
        }
        return getHeaders().getString(ALG);
    }

    /**
     * @see org.picketlink.json.jwt.JWT#encode()
     */
    @Override
    public String encode() {
        String token = super.encode();
        Algorithm algorithm = Algorithm.resolve(getAlgorithm().toUpperCase());

        if (!algorithm.isNone()) {
            if (this.key == null) {
                throw MESSAGES.invalidNullArgument("Secret Key");
            }

            SignatureProvider signatureProvider = algorithm.getSignatureProvider();
            byte[] signature = signatureProvider.sign(token.getBytes(), algorithm, this.key);

            token = new StringBuilder(token).append(PERIOD).append(b64Encode(signature)).toString();
        }
        return token;
    }

}
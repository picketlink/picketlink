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

import org.picketlink.json.JsonConstants;
import org.picketlink.json.JsonException;
import org.picketlink.json.jose.crypto.Algorithm;
import org.picketlink.json.jwt.JWTBuilder;

import javax.json.JsonObject;
import javax.json.JsonValue;
import java.lang.reflect.Constructor;

import static javax.json.JsonValue.ValueType.ARRAY;
import static org.picketlink.json.JsonConstants.COMMON.ALG;
import static org.picketlink.json.JsonConstants.COMMON.HEADER_JSON_WEB_KEY;
import static org.picketlink.json.JsonConstants.COMMON.KEY_ID;
import static org.picketlink.json.JsonConstants.COMMON.PERIOD;
import static org.picketlink.json.JsonMessages.MESSAGES;
import static org.picketlink.json.jose.crypto.Algorithm.HS256;
import static org.picketlink.json.jose.crypto.Algorithm.HS384;
import static org.picketlink.json.jose.crypto.Algorithm.HS512;
import static org.picketlink.json.jose.crypto.Algorithm.RS256;
import static org.picketlink.json.jose.crypto.Algorithm.RS384;
import static org.picketlink.json.jose.crypto.Algorithm.RS512;
import static org.picketlink.json.util.JsonUtil.b64Decode;

/**
 * @author Pedro Igor
 */
public abstract class AbstractJWSBuilder<T extends JWS, B extends AbstractJWSBuilder<T, B>> extends JWTBuilder<T, B> {

    private byte[] key;

    protected AbstractJWSBuilder(Class<T> tokenType) {
        super(tokenType);
        header(ALG, Algorithm.NONE.getAlgorithm());
    }

    public B kid(String kid) {
        header(KEY_ID, kid);
        return (B) this;
    }

    public B hmac256(byte[] key) {
        header(ALG, HS256.name());
        this.key = key;
        return (B) this;
    }

    public B hmac384(byte[] key) {
        header(ALG, HS384.name());
        this.key = key;
        return (B) this;
    }

    public B hmac512(byte[] key) {
        header(ALG, HS512.name());
        this.key = key;
        return (B) this;
    }

    public B rsa256(byte[] key) {
        header(ALG, RS256.name());
        this.key = key;
        return (B) this;
    }

    public B rsa384(byte[] key) {
        header(ALG, RS384.name());
        this.key = key;
        return (B) this;
    }

    public B rsa512(byte[] key) {
        header(ALG, RS512.name());
        this.key = key;
        return (B) this;
    }

    public B keys(JWKSet keySet) {
        header(HEADER_JSON_WEB_KEY, keySet.getJsonObject().getJsonArray(HEADER_JSON_WEB_KEY));
        return (B) this;
    }

    public B keys(JWK... keys) {
        JWKSet jwkSet = new JWKSet(keys);
        return keys(jwkSet);
    }

    @Override
    protected T build(JsonObject headersObject, JsonObject claimsObject) {
        try {
            Constructor<T> constructor = getTokenType().getDeclaredConstructor(JsonObject.class, JsonObject.class, byte[].class);

            constructor.setAccessible(true);

            return (T) constructor.newInstance(headersObject, claimsObject, this.key);
        } catch (Exception nsme) {
            throw MESSAGES.couldNotCreateToken(getTokenType(), nsme);
        }
    }

    /**
     * <p>Builds a {@link org.picketlink.json.jose.JWS} instance from the given <code>json</code> string representing an encoded JWS.</p>
     *
     * <p>This method first tries to check if the given JWS provides any JWK representing the public key that should be used to
     * validate the signature.</p>
     *
     * <p>If no JWK is found, this method does not validate the signature but returns a JWS instance.</p>
     *
     * @param json The encoded JSON string representing a JWS.
     * @return A JWS representing the given encoded JSON string.
     * @throws org.picketlink.json.JsonException If parameters are null or any error occurs when parsing and validating the signature of the given encoded JSON.
     */
    @Override
    public T build(String json) throws JsonException {
        T uncheckedToken = super.build(json);
        String keyId = uncheckedToken.getHeader(KEY_ID);

        if (keyId != null) {
            JsonValue keysHeader = uncheckedToken.getHeaders().get(HEADER_JSON_WEB_KEY);

            if (keysHeader != null) {
                if (ARRAY.equals(keysHeader.getValueType())) {
                    JWKSet jwkSet = new JWKSet((javax.json.JsonArray) keysHeader);
                    JWK jwk = jwkSet.get(keyId);

                    if (!JsonConstants.RSA.equals(jwk.getKeyType())) {
                        throw MESSAGES.cryptoUnsupportedKey(jwk.getKeyType());
                    }

                    try {
                        this.key = jwk.toRSAPublicKey().getEncoded();
                    } catch (Exception e) {
                        throw MESSAGES.cryptoCouldNotParseKey(jwk.toString(), e);
                    }
                }
            }
        }

        if (this.key == null) {
            return uncheckedToken;
        }

        return build(json, this.key);
    }

    /**
     * <p>
     * Builds a {@link JWS} and validates its signatures based on the given encoded public <code>key</code>.
     * </p>
     *
     * @param json The encoded JSON string representing a JWS.
     * @param key The encoded representation of a public key.
     * @return A JWS representing the given encoded JSON string.
     * @throws org.picketlink.json.JsonException If parameters are null or any error occurs when parsing and validating the signature of the given encoded JSON.
     */
    public T build(String json, byte[] key) throws JsonException {
        if (key == null) {
            throw MESSAGES.invalidNullArgument("Encoded Public Key.");
        }

        T token = super.build(json);
        Algorithm algorithm = Algorithm.resolve(token.getAlgorithm().toUpperCase());

        if (!algorithm.isNone()) {
            if (key == null) {
                throw MESSAGES.invalidNullArgument("Signature Key");
            }

            String[] portions = json.split("\\" + PERIOD);

            if (portions.length < 2) {
                throw MESSAGES.cryptoSignatureNotPresent(json);
            }

            byte[] payload = json.substring(0, json.lastIndexOf(PERIOD)).getBytes();
            byte[] signature = b64Decode(portions[2]);
            boolean validSignature = algorithm.getSignatureProvider().verify(payload, algorithm, signature, key);

            if (!validSignature) {
                throw MESSAGES.cryptoInvalidSignature(json);
            }
        }

        return token;
    }

}

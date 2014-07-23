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
package org.picketlink.test.json.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;

import org.junit.Before;
import org.junit.Test;
import org.picketlink.json.jose.JWK;
import org.picketlink.json.jose.JWKBuilder;
import org.picketlink.json.jose.JWKSet;

/**
 * The Class JWKAPITestCase.
 *
 * @author Giriraj Sharma
 */
public class JWKAPITestCase {

    /** The key pair1. */
    private KeyPair keyPair1;
    
    /** The key pair2. */
    private KeyPair keyPair2;
    
    /** The key pair3. */
    private KeyPair keyPair3;
    
    /** The key pair4. */
    private KeyPair keyPair4;

    /**
     * On before.
     *
     * @throws Exception the exception
     */
    @Before
    public void onBefore() throws Exception {
        this.keyPair1 = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        this.keyPair2 = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        this.keyPair3 = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        this.keyPair4 = KeyPairGenerator.getInstance("RSA").generateKeyPair();
    }

    /**
     * Test rsajwk.
     */
    @Test
    public void testRSAJWK() {
        RSAPublicKey publicKey = (RSAPublicKey) this.keyPair1.getPublic();

        JWK jwk = createJWK(publicKey, "1");

        String jsonString = jwk.toString();

        assertEquals("{\"n\":\"" + jwk.getModulus() + "\",\"e\":\"AQAB\",\"kid\":\"1\",\"kty\":\"RSA\",\"alg\":\"RSA\",\"use\":\"enc\"}", jsonString);

        JWK parsedJwk = new JWKBuilder().build(jsonString);

        assertNotNull(parsedJwk);
        assertEquals(this.keyPair1.getPublic(), parsedJwk.toRSAPublicKey());
    }

    /**
     * Test jwk set.
     */
    @Test
    public void testJWKSet() {
        JWK jwkKeyPair1 = createJWK((RSAPublicKey) this.keyPair1.getPublic(), "1");
        JWK jwkKeyPair2 = createJWK((RSAPublicKey) this.keyPair2.getPublic(), "2");
        JWK jwkKeyPair3 = createJWK((RSAPublicKey) this.keyPair3.getPublic(), "3");
        JWK jwkKeyPair4 = createJWK((RSAPublicKey) this.keyPair4.getPublic(), "4");

        JWKSet jwkSet = new JWKSet(jwkKeyPair1, jwkKeyPair2, jwkKeyPair3, jwkKeyPair4);
        String jsonKeySet = jwkSet.toString();

        assertEquals("{\"keys\":[{\"n\":\"" + jwkKeyPair3.getModulus() + "\",\"e\":\"AQAB\",\"kid\":\"3\",\"kty\":\"RSA\",\"alg\":\"RSA\",\"use\":\"enc\"},{\"n\":\"" + jwkKeyPair2.getModulus() + "\",\"e\":\"AQAB\",\"kid\":\"2\",\"kty\":\"RSA\",\"alg\":\"RSA\",\"use\":\"enc\"},{\"n\":\"" + jwkKeyPair1.getModulus() + "\",\"e\":\"AQAB\",\"kid\":\"1\",\"kty\":\"RSA\",\"alg\":\"RSA\",\"use\":\"enc\"},{\"n\":\"" + jwkKeyPair4.getModulus() + "\",\"e\":\"AQAB\",\"kid\":\"4\",\"kty\":\"RSA\",\"alg\":\"RSA\",\"use\":\"enc\"}]}", jsonKeySet);

        JWKSet parsedKeySet = new JWKSet(jsonKeySet);

        assertNotNull(parsedKeySet.get("1"));
        assertNotNull(parsedKeySet.get("2"));
        assertNotNull(parsedKeySet.get("3"));
        assertNotNull(parsedKeySet.get("4"));
    }

    /**
     * Creates the jwk.
     *
     * @param publicKey the public key
     * @param keyId the key id
     * @return the jwk
     */
    public JWK createJWK(RSAPublicKey publicKey, String keyId) {
        return new JWKBuilder()
            .modulus(publicKey.getModulus())
            .publicExponent(publicKey.getPublicExponent())
            .keyIdentifier(keyId)
            .keyType("RSA")
            .keyAlgorithm(publicKey.getAlgorithm())
            .keyUse("enc")
            .build();
    }
}
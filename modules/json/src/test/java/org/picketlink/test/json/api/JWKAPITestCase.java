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

/**
 * @author Giriraj Sharma
 */
public class JWKAPITestCase {

    private KeyPair keyPair;

    @Before
    public void onBefore() throws Exception {
        this.keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
    }

    @Test
    public void testRSAJWK() {

        RSAPublicKey publicKey = (RSAPublicKey) this.keyPair.getPublic();
        JWK rsaJWK = new JWKBuilder()
            .publicExponent(publicKey.getPublicExponent())
            .keyIdentifier("1")
            .keyType("RSA")
            .keyAlgorithm(publicKey.getAlgorithm())
            .keyUse("enc")
            .build();

        String jsonString = rsaJWK.toString();

        assertEquals("{\"e\":\"AQAB\",\"kid\":\"1\",\"kty\":\"RSA\",\"alg\":\"RSA\",\"use\":\"enc\"}", jsonString);

        String jsonEncoded = rsaJWK.encode();
        assertNotNull(jsonEncoded);
    }

    @Test
    public void testECJWK() {

        JWK ecJWK = new JWKBuilder()
            .keyIdentifier("2")
            .keyType("EC")
            .keyAlgorithm("ES256")
            .keyOperations("sign", "verify")
            .curve("P-256")
            .build();

        String jsonString = ecJWK.toString();

        assertEquals("{\"kid\":\"2\",\"kty\":\"EC\",\"alg\":\"ES256\",\"key_ops\":[\"sign\",\"verify\"],\"crv\":\"P-256\"}", jsonString);

        String jsonEncoded = ecJWK.encode();
        assertNotNull(jsonEncoded);
    }

    @Test
    public void testOctetJWK() {

        JWK octetJWK = new JWKBuilder()
            .keyIdentifier("3")
            .keyType("oct")
            .keyAlgorithm("A128KW")
            .keyOperations("encrypt", "decrypt")
            .build();

        String jsonString = octetJWK.toString();

        assertEquals("{\"kid\":\"3\",\"kty\":\"oct\",\"alg\":\"A128KW\",\"key_ops\":[\"encrypt\",\"decrypt\"]}", jsonString);

        String jsonEncoded = octetJWK.encode();
        assertNotNull(jsonEncoded);
    }
}

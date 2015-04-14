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
import static org.picketlink.json.JsonConstants.RSA;
import static org.picketlink.test.json.api.JWEAPITestCase.assertJwEquals;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.junit.Before;
import org.junit.Test;
import org.picketlink.json.JsonException;
import org.picketlink.json.jose.JWK;
import org.picketlink.json.jose.JWKBuilder;
import org.picketlink.json.jose.JWKSet;
import org.picketlink.json.jose.JWS;
import org.picketlink.json.jose.JWSBuilder;

/**
 * The Class JWSRSAAPITestCase.
 *
 * @author Pedro Igor
 */
public class JWSRSAAPITestCase {

    private JWKSet keySet;

    private KeyPair keyPair1;

    private KeyPair keyPair2;

    /**
     * On before.
     *
     * @throws Exception the exception
     */
    @Before
    public void onBefore() throws Exception {
        this.keySet = new JWKSet();
        this.keyPair1 = KeyPairGenerator.getInstance("RSA").generateKeyPair();

        registerPublicKey("1", (RSAPublicKey) this.keyPair1.getPublic());

        this.keyPair2 = KeyPairGenerator.getInstance("RSA").generateKeyPair();

        registerPublicKey("2", (RSAPublicKey) this.keyPair2.getPublic());
    }

    /**
     * Register public key.
     *
     * @param kid the kid
     * @param publicKey the public key
     */
    private void registerPublicKey(String kid, RSAPublicKey publicKey) {
        JWK rsaJWK = new JWKBuilder()
            .modulus(publicKey.getModulus())
            .publicExponent(publicKey.getPublicExponent())
            .keyIdentifier(kid)
            .keyType(RSA)
            .keyUse("sig")
            .build();

        this.keySet.add(rsaJWK);
    }

    /**
     * Test RSA256 signature.
     *
     * @throws Exception the exception
     */
    @Test
    public void testRSA256Signature() throws Exception {
        PrivateKey privateKey = this.keyPair1.getPrivate();

        JWS token = new JWSBuilder()
            .rsa256(privateKey.getEncoded())
            .keys(this.keySet)
            .kid("1")
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .build();

        String jsonString = token.toString();

        JWK jwkKeyPair1 = this.keySet.get("1");
        JWK jwkKeyPair2 = this.keySet.get("2");

        assertJwEquals(
            "{\"typ\":\"JWT\",\"alg\":\"RS256\",\"keys\":[{\"n\":\""
                + jwkKeyPair2.getModulus()
                + "\",\"e\":\"AQAB\",\"kid\":\"2\",\"kty\":\"RSA\",\"use\":\"sig\"},{\"n\":\""
                + jwkKeyPair1.getModulus()
                + "\",\"e\":\"AQAB\",\"kid\":\"1\",\"kty\":\"RSA\",\"use\":\"sig\"}],\"kid\":\"1\"}.{\"jti\":\"1\",\"iss\":\"issuer\",\"sub\":\"subject\",\"aud\":\"audience\",\"exp\":123,\"iat\":456,\"nbf\":789}",
            jsonString);

        String jsonEncoded = token.encode();

        JWS parsedToken = new JWSBuilder().build(jsonEncoded);

        String parsedJsonString = parsedToken.toString();

        assertEquals(jsonString, parsedJsonString);

        assertNotNull(parsedToken);

    }

    /**
     * Test RSA384 signature.
     */
    @Test
    public void testRSA384Signature() {
        PrivateKey privateKey = this.keyPair1.getPrivate();

        JWS token = new JWSBuilder()
            .rsa384(privateKey.getEncoded())
            .keys(this.keySet)
            .kid("1")
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .build();

        String jsonString = token.toString();

        JWK jwkKeyPair1 = this.keySet.get("1");
        JWK jwkKeyPair2 = this.keySet.get("2");

        assertJwEquals(
            "{\"typ\":\"JWT\",\"alg\":\"RS384\",\"keys\":[{\"n\":\""
                + jwkKeyPair2.getModulus()
                + "\",\"e\":\"AQAB\",\"kid\":\"2\",\"kty\":\"RSA\",\"use\":\"sig\"},{\"n\":\""
                + jwkKeyPair1.getModulus()
                + "\",\"e\":\"AQAB\",\"kid\":\"1\",\"kty\":\"RSA\",\"use\":\"sig\"}],\"kid\":\"1\"}.{\"jti\":\"1\",\"iss\":\"issuer\",\"sub\":\"subject\",\"aud\":\"audience\",\"exp\":123,\"iat\":456,\"nbf\":789}",
            jsonString);

        String jsonEncoded = token.encode();

        JWS parsedToken = new JWSBuilder().build(jsonEncoded);

        String parsedJsonString = parsedToken.toString();

        assertEquals(jsonString, parsedJsonString);

        assertNotNull(parsedToken);
    }

    /**
     * Test RSA512 signature.
     */
    @Test
    public void testRSA512Signature() {
        PrivateKey privateKey = this.keyPair1.getPrivate();

        JWS token = new JWSBuilder()
            .rsa512(privateKey.getEncoded())
            .keys(this.keySet)
            .kid("1")
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .build();

        String jsonString = token.toString();

        JWK jwkKeyPair1 = this.keySet.get("1");
        JWK jwkKeyPair2 = this.keySet.get("2");

        assertJwEquals(
            "{\"typ\":\"JWT\",\"alg\":\"RS512\",\"keys\":[{\"n\":\""
                + jwkKeyPair2.getModulus()
                + "\",\"e\":\"AQAB\",\"kid\":\"2\",\"kty\":\"RSA\",\"use\":\"sig\"},{\"n\":\""
                + jwkKeyPair1.getModulus()
                + "\",\"e\":\"AQAB\",\"kid\":\"1\",\"kty\":\"RSA\",\"use\":\"sig\"}],\"kid\":\"1\"}.{\"jti\":\"1\",\"iss\":\"issuer\",\"sub\":\"subject\",\"aud\":\"audience\",\"exp\":123,\"iat\":456,\"nbf\":789}",
            jsonString);

        String jsonEncoded = token.encode();

        JWS parsedToken = new JWSBuilder().build(jsonEncoded);

        String parsedJsonString = parsedToken.toString();

        assertEquals(jsonString, parsedJsonString);

        assertNotNull(parsedToken);
    }

    /**
     * Test RSA512 signature using public key instance.
     */
    @Test
    public void testRSA512SignatureUsingPublicKeyInstance() {
        PrivateKey privateKey = this.keyPair1.getPrivate();

        JWS token = new JWSBuilder()
            .rsa512(privateKey.getEncoded())
            .keys(this.keySet)
            .kid("1")
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .build();

        String jsonString = token.toString();

        JWS parsedToken = new JWSBuilder().build(token.encode(), this.keyPair1.getPublic().getEncoded());

        assertNotNull(parsedToken);

        String parsedJsonString = parsedToken.toString();

        assertEquals(jsonString, parsedJsonString);
    }

    /**
     * Test RSA512 signature without JWK set.
     */
    @Test
    public void testRSA512SignatureWithoutJWKSet() {
        PrivateKey privateKey = this.keyPair1.getPrivate();

        JWS token = new JWSBuilder()
            .rsa512(privateKey.getEncoded())
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .build();

        String jsonString = token.toString();

        JWS parsedToken = new JWSBuilder().build(token.encode(), this.keyPair1.getPublic().getEncoded());

        assertNotNull(parsedToken);

        String parsedJsonString = parsedToken.toString();

        assertEquals(jsonString, parsedJsonString);
    }

    /**
     * Test no signature.
     */
    @Test
    public void testNoSignature() {
        JWS token = new JWSBuilder()
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .build();

        String jsonString = token.toString();

        assertJwEquals("{\"typ\":\"JWT\",\"alg\":\"none\"}.{\"jti\":\"1\",\"iss\":\"issuer\",\"sub\":\"subject\",\"aud\":\"audience\",\"exp\":123,\"iat\":456,\"nbf\":789}",
            jsonString);

        JWS parsedToken = new JWSBuilder().build(token.encode(), this.keyPair1.getPublic().getEncoded());

        assertNotNull(parsedToken);

        String parsedJsonString = parsedToken.toString();

        assertEquals(jsonString, parsedJsonString);
    }

    /**
     * Fail invalid signature.
     */
    @Test(expected = JsonException.class)
    public void failInvalidSignature() {
        PrivateKey privateKey = this.keyPair1.getPrivate();

        JWS token = new JWSBuilder()
            .rsa512(privateKey.getEncoded())
            .keys(this.keySet)
            .kid("1")
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .build();

        String jsonString = token.toString();

        JWK jwkKeyPair1 = this.keySet.get("1");
        JWK jwkKeyPair2 = this.keySet.get("2");

        assertJwEquals(
            "{\"typ\":\"JWT\",\"alg\":\"RS512\",\"keys\":[{\"n\":\""
                + jwkKeyPair2.getModulus()
                + "\",\"e\":\"AQAB\",\"kid\":\"2\",\"kty\":\"RSA\",\"use\":\"sig\"},{\"n\":\""
                + jwkKeyPair1.getModulus()
                + "\",\"e\":\"AQAB\",\"kid\":\"1\",\"kty\":\"RSA\",\"use\":\"sig\"}],\"kid\":\"1\"}.{\"jti\":\"1\",\"iss\":\"issuer\",\"sub\":\"subject\",\"aud\":\"audience\",\"exp\":123,\"iat\":456,\"nbf\":789}",
            jsonString);

        // here we temper the encoded token to fail invalid signature
        String tamperedToken = new StringBuilder(token.encode()).insert(3, "tampered").toString();

        new JWSBuilder().build(tamperedToken);
    }

    /**
     * Fail invalid key.
     */
    @Test(expected = JsonException.class)
    public void failInvalidKey() {
        PrivateKey privateKey = this.keyPair1.getPrivate();

        JWS token = new JWSBuilder()
            .rsa512(privateKey.getEncoded())
            .keys(this.keySet)
            .kid("2")
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .build();

        // token was signed with key 1 but is referencing key 2.
        new JWSBuilder().build(token.encode());
    }
}
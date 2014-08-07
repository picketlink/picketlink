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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonString;

import org.junit.Test;
import org.picketlink.json.JsonException;
import org.picketlink.json.jose.AbstractJWSBuilder;
import org.picketlink.json.jose.JWS;
import org.picketlink.json.jose.JWSBuilder;
import org.picketlink.json.jwt.JWT;

/**
 * @author Pedro Igor
 */
public class JWSHMACAPITestCase {

    /**
     * Test HMAC256 signature.
     */
    @Test
    public void testHMAC256Signature() {
        byte[] secretKey = new String("super_secret_key").getBytes();

        JWS token = new JWSBuilder()
            .hmac256(secretKey)
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .build();

        String jsonString = token.toString();

        assertEquals("{\"typ\":\"JWT\",\"alg\":\"HS256\"}.{\"jti\":\"1\",\"iss\":\"issuer\",\"sub\":\"subject\",\"aud\":\"audience\",\"exp\":123,\"iat\":456,\"nbf\":789}", jsonString);

        String jsonEncoded = token.encode();

        assertEquals(3, jsonEncoded.split("\\.").length);

        JWT parsedToken = new JWSBuilder().build(jsonEncoded, secretKey);

        assertNotNull(parsedToken);
        
        assertEquals("1", parsedToken.getId());
        assertEquals("issuer", parsedToken.getIssuer());
        assertEquals("subject", parsedToken.getSubject());
        assertArrayEquals(new String[] {"audience"}, parsedToken.getAudience().toArray());
        assertEquals(Integer.valueOf(123), parsedToken.getExpiration());
        assertEquals(Integer.valueOf(456), parsedToken.getIssuedAt());
        assertEquals(Integer.valueOf(789), parsedToken.getNotBefore());
    }

    /**
     * Test HMAC384 signature.
     */
    @Test
    public void testHMAC384Signature() {
        byte[] secretKey = new String("super_secret_key").getBytes();

        JWS token = new JWSBuilder()
            .hmac384(secretKey)
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .build();

        String jsonString = token.toString();

        assertEquals("{\"typ\":\"JWT\",\"alg\":\"HS384\"}.{\"jti\":\"1\",\"iss\":\"issuer\",\"sub\":\"subject\",\"aud\":\"audience\",\"exp\":123,\"iat\":456,\"nbf\":789}", jsonString);

        String jsonEncoded = token.encode();
        
        assertEquals(3, jsonEncoded.split("\\.").length);

        JWT parsedToken = new JWSBuilder().build(jsonEncoded, secretKey);

        assertNotNull(parsedToken);
        
        assertEquals("1", parsedToken.getId());
        assertEquals("issuer", parsedToken.getIssuer());
        assertEquals("subject", parsedToken.getSubject());
        assertArrayEquals(new String[] {"audience"}, parsedToken.getAudience().toArray());
        assertEquals(Integer.valueOf(123), parsedToken.getExpiration());
        assertEquals(Integer.valueOf(456), parsedToken.getIssuedAt());
        assertEquals(Integer.valueOf(789), parsedToken.getNotBefore());
    }

    /**
     * Test HMAC512 signature.
     */
    @Test
    public void testHMAC512Signature() {
        byte[] secretKey = new String("super_secret_key").getBytes();

        JWS token = new JWSBuilder()
            .hmac512(secretKey)
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .build();

        String jsonString = token.toString();

        assertEquals("{\"typ\":\"JWT\",\"alg\":\"HS512\"}.{\"jti\":\"1\",\"iss\":\"issuer\",\"sub\":\"subject\",\"aud\":\"audience\",\"exp\":123,\"iat\":456,\"nbf\":789}", jsonString);

        String jsonEncoded = token.encode();
        
        assertEquals(3, jsonEncoded.split("\\.").length);

        JWT parsedToken = new JWSBuilder().build(jsonEncoded, secretKey);

        assertNotNull(parsedToken);
        
        assertEquals("1", parsedToken.getId());
        assertEquals("issuer", parsedToken.getIssuer());
        assertEquals("subject", parsedToken.getSubject());
        assertArrayEquals(new String[] {"audience"}, parsedToken.getAudience().toArray());
        assertEquals(Integer.valueOf(123), parsedToken.getExpiration());
        assertEquals(Integer.valueOf(456), parsedToken.getIssuedAt());
        assertEquals(Integer.valueOf(789), parsedToken.getNotBefore());
    }

    /**
     * Test custom web token build.
     */
    @Test
    public void testCustomWebTokenBuild() {
        byte[] secretKey = new String("super_secret_key").getBytes();

        MyWebToken token = new MyWebToken.MyWebTokenBuilder()
            .hmac256(secretKey)
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .roles("maintainer", "profile") // here we define a custom claim
            .build();

        String jsonString = token.toString();

        assertEquals("{\"typ\":\"JWT\",\"alg\":\"HS256\"}.{\"jti\":\"1\",\"iss\":\"issuer\",\"sub\":\"subject\",\"aud\":\"audience\",\"exp\":123,\"iat\":456,\"nbf\":789,\"roles\":[\"maintainer\",\"profile\"]}", jsonString);

        String jsonEncoded = token.encode();
        
        assertEquals(3, jsonEncoded.split("\\.").length);

        MyWebToken parsedToken = new MyWebToken.MyWebTokenBuilder().build(jsonEncoded, secretKey);

        assertNotNull(parsedToken);
        
        assertEquals("1", parsedToken.getId());
        assertEquals("issuer", parsedToken.getIssuer());
        assertEquals("subject", parsedToken.getSubject());
        assertArrayEquals(new String[] {"audience"}, parsedToken.getAudience().toArray());
        assertEquals(Integer.valueOf(123), parsedToken.getExpiration());
        assertEquals(Integer.valueOf(456), parsedToken.getIssuedAt());
        assertEquals(Integer.valueOf(789), parsedToken.getNotBefore());
        assertArrayEquals(new String[] {"maintainer", "profile"}, parsedToken.getRoles().toArray());
    }

    /**
     * Test no signature.
     */
    @Test
    public void testNoSignature() {
        byte[] secretKey = new String("super_secret_key").getBytes();

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

        assertEquals("{\"typ\":\"JWT\",\"alg\":\"none\"}.{\"jti\":\"1\",\"iss\":\"issuer\",\"sub\":\"subject\",\"aud\":\"audience\",\"exp\":123,\"iat\":456,\"nbf\":789}", jsonString);

        String jsonEncoded = token.encode();

        assertEquals(2, jsonEncoded.split("\\.").length);

        JWT parsedToken = new JWSBuilder().build(jsonEncoded, secretKey);

        assertNotNull(parsedToken);
        
        assertEquals("1", parsedToken.getId());
        assertEquals("issuer", parsedToken.getIssuer());
        assertEquals("subject", parsedToken.getSubject());
        assertArrayEquals(new String[] {"audience"}, parsedToken.getAudience().toArray());
        assertEquals(Integer.valueOf(123), parsedToken.getExpiration());
        assertEquals(Integer.valueOf(456), parsedToken.getIssuedAt());
        assertEquals(Integer.valueOf(789), parsedToken.getNotBefore());
    }

    /**
     * Fail invalid secret key.
     */
    @Test(expected = RuntimeException.class)
    public void failInvalidSecretKey() {
        byte[] secretKey = new String("super_secret_key").getBytes();

        MyWebToken token = new MyWebToken.MyWebTokenBuilder()
            .hmac256(secretKey)
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .roles("maintainer", "profile")
            .build(); // here we define a custom claim

        String jsonEncoded = token.encode();

        byte[] invalidSecretKey = new String("invalid_secret_key").getBytes();

        new MyWebToken.MyWebTokenBuilder().build(jsonEncoded, invalidSecretKey);
    }

    /**
     * Fail invalid signature.
     */
    @Test(expected = JsonException.class)
    public void failInvalidSignature() {
        byte[] secretKey = new String("super_secret_key").getBytes();

        MyWebToken token = new MyWebToken.MyWebTokenBuilder()
            .hmac256(secretKey)
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .roles("maintainer", "profile")
            .build(); // here we define a custom claim

        String jsonEncoded = new StringBuilder(token.encode()).insert(3, "tampered").toString();

        new MyWebToken.MyWebTokenBuilder().build(jsonEncoded, secretKey);
    }

    public static class MyWebToken extends JWS {

        public static final String CLAIM_ROLES = "roles";

        public MyWebToken(JsonObject headers, JsonObject claims, byte[] key) {
            super(headers, claims, key);
        }

        public List<String> getRoles() {
            List<String> roles = new ArrayList<String>();

            for (JsonString string : getClaims().getJsonArray(CLAIM_ROLES).getValuesAs(JsonString.class)) {
                roles.add(string.getString());
            }

            return roles;
        }

        public static class MyWebTokenBuilder extends AbstractJWSBuilder<MyWebToken, MyWebTokenBuilder> {

            public MyWebTokenBuilder() {
                super(MyWebToken.class);
            }

            public MyWebTokenBuilder roles(String... roles) {
                JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

                for (String role : roles) {
                    arrayBuilder.add(role);
                }

                getClaimsBuilder().add(CLAIM_ROLES, arrayBuilder);

                return this;
            }
        }
    }
}

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

import org.junit.Test;
import org.picketlink.json.jwt.JWT;
import org.picketlink.json.jwt.JWTBuilder;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonString;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * The Class JWTAPITestCase.
 *
 * @author Pedro Igor
 */
public class JWTAPITestCase {

    /**
     * Test standard build.
     */
    @Test
    public void testStandardBuild() {
        JWT token = new JWTBuilder()
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .build();

        String jsonString = token.toString();

        assertEquals("{\"typ\":\"JWT\"}.{\"jti\":\"1\",\"iss\":\"issuer\",\"sub\":\"subject\",\"aud\":\"audience\",\"exp\":123,\"iat\":456,\"nbf\":789}", jsonString);

        String jsonEncoded = token.encode();

        JWT parsedToken = new JWTBuilder().build(jsonEncoded);

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
        MyWebToken token = new MyWebToken.MyWebTokenBuilder()
            .roles("maintainer", "profile") // here we define a custom claim
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .build();

        String jsonString = token.toString();

        assertEquals("{\"typ\":\"JWT\"}.{\"roles\":[\"maintainer\",\"profile\"],\"jti\":\"1\",\"iss\":\"issuer\",\"sub\":\"subject\",\"aud\":\"audience\",\"exp\":123,\"iat\":456,\"nbf\":789}", jsonString);

        String jsonEncoded = token.encode();

        MyWebToken parsedToken = new MyWebToken.MyWebTokenBuilder().build(jsonEncoded);

        assertNotNull(parsedToken);
        
        assertArrayEquals(new String[] {"maintainer", "profile"}, parsedToken.getRoles().toArray());
        assertEquals("1", parsedToken.getId());
        assertEquals("issuer", parsedToken.getIssuer());
        assertEquals("subject", parsedToken.getSubject());
        assertArrayEquals(new String[] {"audience"}, parsedToken.getAudience().toArray());
        assertEquals(Integer.valueOf(123), parsedToken.getExpiration());
        assertEquals(Integer.valueOf(456), parsedToken.getIssuedAt());
        assertEquals(Integer.valueOf(789), parsedToken.getNotBefore());
    }

    /**
     * Test custom claims.
     */
    @Test
    public void testCustomClaims() {
        JWT token = new JWTBuilder()
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .claim("roles", "maintainer", "profile")
            .claim("ints", 1, 2)
            .build();

        String jsonString = token.toString();
        
        assertEquals("{\"typ\":\"JWT\"}.{\"jti\":\"1\",\"iss\":\"issuer\",\"sub\":\"subject\",\"aud\":\"audience\",\"exp\":123,\"iat\":456,\"nbf\":789,\"roles\":[\"maintainer\",\"profile\"],\"ints\":[1,2]}", jsonString);

        String jsonEncoded = token.encode();

        JWT parsedToken = new JWTBuilder().build(jsonEncoded);

        assertNotNull(parsedToken);
        assertArrayEquals(new String[] {"maintainer", "profile"}, parsedToken.getClaimValues("roles").toArray());
        assertArrayEquals(new String[] {"1", "2"}, parsedToken.getClaimValues("ints").toArray());
        assertEquals("1", parsedToken.getClaim("ints"));
    }

    /**
     * Test multiple audience.
     */
    @Test
    public void testMultipleAudience() {
        JWT token = new JWTBuilder()
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("A", "B", "C")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .build();

        String jsonString = token.toString();

        assertEquals("{\"typ\":\"JWT\"}.{\"jti\":\"1\",\"iss\":\"issuer\",\"sub\":\"subject\",\"aud\":[\"A\",\"B\",\"C\"],\"exp\":123,\"iat\":456,\"nbf\":789}", jsonString);

        String jsonEncoded = token.encode();

        JWT parsedToken = new JWTBuilder().build(jsonEncoded);

        assertNotNull(parsedToken);
        
        assertEquals("1", parsedToken.getId());
        assertEquals("issuer", parsedToken.getIssuer());
        assertEquals("subject", parsedToken.getSubject());
        assertArrayEquals(new String[] {"A", "B", "C"}, parsedToken.getAudience().toArray());
        assertEquals(Integer.valueOf(123), parsedToken.getExpiration());
        assertEquals(Integer.valueOf(456), parsedToken.getIssuedAt());
        assertEquals(Integer.valueOf(789), parsedToken.getNotBefore());
    }

    /**
     * The Class MyWebToken.
     */
    public static class MyWebToken extends JWT {

        /** The Constant CLAIM_ROLES. */
        public static final String CLAIM_ROLES = "roles";

        /**
         * Instantiates a new my web token.
         *
         * @param headers the headers
         * @param claims the claims
         */
        public MyWebToken(JsonObject headers, JsonObject claims) {
            super(headers, claims);
        }

        /**
         * Gets the roles.
         *
         * @return the roles
         */
        public List<String> getRoles() {
            List<String> roles = new ArrayList<String>();

            for (JsonString string : getClaims().getJsonArray(CLAIM_ROLES).getValuesAs(JsonString.class)) {
                roles.add(string.getString());
            }

            return roles;
        }

        /**
         * The Class MyWebTokenBuilder.
         */
        public static class MyWebTokenBuilder extends JWTBuilder<MyWebToken, MyWebTokenBuilder> {

            /**
             * Instantiates a new my web token builder.
             */
            public MyWebTokenBuilder() {
                super(MyWebToken.class);
            }

            /**
             * Roles.
             *
             * @param roles the roles
             * @return the my web token builder
             */
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
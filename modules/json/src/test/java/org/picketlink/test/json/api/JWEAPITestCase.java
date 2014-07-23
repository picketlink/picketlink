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
import static org.picketlink.json.JsonConstants.RSA;
import static org.picketlink.json.JsonConstants.JWE.ALG_RSA1_5;
import static org.picketlink.json.JsonConstants.JWE.ALG_RSA_OAEP;
import static org.picketlink.json.JsonConstants.JWE.ALG_RSA_OAEP_256;
import static org.picketlink.json.JsonConstants.JWE.ENC_A128CBC_HS256;
import static org.picketlink.json.JsonConstants.JWE.ENC_A128GCM;
import static org.picketlink.json.JsonConstants.JWE.ENC_A192CBC_HS384;
import static org.picketlink.json.JsonConstants.JWE.ENC_A192GCM;
import static org.picketlink.json.JsonConstants.JWE.ENC_A256CBC_HS512;
import static org.picketlink.json.JsonConstants.JWE.ENC_A256GCM;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;
import org.picketlink.json.jose.JWE;
import org.picketlink.json.jose.JWEBuilder;
import org.picketlink.json.jose.JWK;
import org.picketlink.json.jose.JWKBuilder;
import org.picketlink.json.jose.JWKSet;
import org.picketlink.json.jose.crypto.JWEDecrypter;
import org.picketlink.json.jose.crypto.JWEEncrypter;
import org.picketlink.json.util.JsonUtil;

/**
 * The Class JWEAPITestCase.
 *
 * @author Giriraj Sharma
 */
public class JWEAPITestCase {

    /** The key set. */
    private JWKSet keySet;
    
    /** The key pair1. */
    private KeyPair keyPair1;
    
    /** The key pair2. */
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
            .keyAlgorithm(publicKey.getAlgorithm())
            .keyUse("sign")
            .build();

        this.keySet.add(rsaJWK);
    }

    /**
     * Test RSA JWE JSON.
     */
    @Test
    public void testRSAJWEJSON() {

        JWE token = new JWEBuilder()
            .keys(this.keySet)
            .algorithm(ALG_RSA_OAEP)
            .encryptionAlgorithm(ENC_A256GCM, 256)
            .compressionAlgorithm("DEF")
            .type("jwt")
            .contentType("jwe")
            .X509CertificateChain("cert1", "cert2", "cert3")
            .keyIdentifier("1")
            .build();

        String jsonString = token.toString();

        JWK jwkKeyPair1 = this.keySet.get("1");
        JWK jwkKeyPair2 = this.keySet.get("2");

        assertEquals(
            "{\"keys\":[{\"n\":\""
                + jwkKeyPair2.getModulus()
                + "\",\"e\":\"AQAB\",\"kid\":\"2\",\"kty\":\"RSA\",\"alg\":\"RSA\",\"use\":\"sign\"},{\"n\":\""
                + jwkKeyPair1.getModulus()
                + "\",\"e\":\"AQAB\",\"kid\":\"1\",\"kty\":\"RSA\",\"alg\":\"RSA\",\"use\":\"sign\"}],\"alg\":\"RSA-OAEP\",\"enc\":\"A256GCM\",\"cek_bitlength\":256,\"zip\":\"DEF\",\"typ\":\"jwt\",\"cty\":\"jwe\",\"x5c\":[\"cert1\",\"cert2\",\"cert3\"],\"kid\":\"1\"}",
            jsonString);
    }

    /**
     * Test ALG_RSA1_5.
     *
     * @throws ParseException the parse exception
     */
    @Test
    public void test_ALG_RSA1_5() throws ParseException {

        JWE jwe = new JWEBuilder()
            .algorithm(ALG_RSA1_5)
            .encryptionAlgorithm(ENC_A128GCM, 128)
            .compressionAlgorithm("DEF")
            .build();

        String payLoad = "{\"alg\": \"ALG_RSA1_5\",\"enc\": \"ENC_A128GCM\",\"zip\": \"DEF\"}";

        JWEEncrypter encrypter = new JWEEncrypter((RSAPublicKey) keyPair1.getPublic());
        String encryptedPayload = encrypter.encrypt(jwe, payLoad.getBytes());

        String[] cryptoPart = JsonUtil.split(encryptedPayload);

        JWEDecrypter decrypter = new JWEDecrypter((RSAPrivateKey) keyPair1.getPrivate());
        byte[] decryptedByteArray = decrypter.decrypt(jwe, cryptoPart[1], cryptoPart[2], cryptoPart[3], cryptoPart[4]);
        String decryptedPayload = new String(decryptedByteArray);

        assertEquals(payLoad, decryptedPayload);
    }

    /**
     * Test ALG_RSA_OAEP.
     *
     * @throws ParseException the parse exception
     */
    @Test
    public void test_ALG_RSA_OAEP() throws ParseException {

        JWE jwe = new JWEBuilder()
            .algorithm(ALG_RSA_OAEP)
            .encryptionAlgorithm(ENC_A192GCM, 192)
            .compressionAlgorithm("DEF")
            .build();

        String payLoad = "{\"alg\": \"ALG_RSA_OAEP\",\"enc\": \"ENC_A192GCM\",\"zip\": \"DEF\"}";

        JWEEncrypter encrypter = new JWEEncrypter((RSAPublicKey) keyPair1.getPublic());
        String encryptedPayload = encrypter.encrypt(jwe, payLoad.getBytes());

        String[] cryptoPart = JsonUtil.split(encryptedPayload);

        JWEDecrypter decrypter = new JWEDecrypter((RSAPrivateKey) keyPair1.getPrivate());
        byte[] decryptedByteArray = decrypter.decrypt(jwe, cryptoPart[1], cryptoPart[2], cryptoPart[3], cryptoPart[4]);
        String decryptedPayload = new String(decryptedByteArray);

        assertEquals(payLoad, decryptedPayload);
    }

    /**
     * Test ALG_RSA_OAEP_256.
     *
     * @throws ParseException the parse exception
     */
    @Test
    public void test_ALG_RSA_OAEP_256() throws ParseException {

        JWE jwe = new JWEBuilder()
            .algorithm(ALG_RSA_OAEP_256)
            .encryptionAlgorithm(ENC_A256GCM, 256)
            .compressionAlgorithm("DEF")
            .build();

        String payLoad = "{\"alg\": \"ALG_RSA_OAEP_256\",\"enc\": \"ENC_A256GCM\",\"zip\": \"DEF\"}";

        JWEEncrypter encrypter = new JWEEncrypter((RSAPublicKey) keyPair1.getPublic());
        String encryptedPayload = encrypter.encrypt(jwe, payLoad.getBytes());

        String[] cryptoPart = JsonUtil.split(encryptedPayload);

        JWEDecrypter decrypter = new JWEDecrypter((RSAPrivateKey) keyPair1.getPrivate());
        byte[] decryptedByteArray = decrypter.decrypt(jwe, cryptoPart[1], cryptoPart[2], cryptoPart[3], cryptoPart[4]);
        String decryptedPayload = new String(decryptedByteArray);

        assertEquals(payLoad, decryptedPayload);
    }
    
    /**
     * Test ALGRSA1_5_WITH_ENC_A128CBC_HS256.
     *
     * @throws ParseException the parse exception
     */
    @Test
    public void test_ALGRSA1_5_WITH_ENC_A128CBC_HS256() throws ParseException {

        JWE jwe = new JWEBuilder()
            .algorithm(ALG_RSA1_5)
            .encryptionAlgorithm(ENC_A128CBC_HS256, 256)
            .compressionAlgorithm("DEF")
            .build();

        String payLoad = "{\"alg\": \"ALG_RSA1_5\",\"enc\": \"ENC_A128CBC_HS256\",\"zip\": \"DEF\"}";

        JWEEncrypter encrypter = new JWEEncrypter((RSAPublicKey) keyPair1.getPublic());
        String encryptedPayload = encrypter.encrypt(jwe, payLoad.getBytes());

        String[] cryptoPart = JsonUtil.split(encryptedPayload);

        JWEDecrypter decrypter = new JWEDecrypter((RSAPrivateKey) keyPair1.getPrivate());
        byte[] decryptedByteArray = decrypter.decrypt(jwe, cryptoPart[1], cryptoPart[2], cryptoPart[3], cryptoPart[4]);
        String decryptedPayload = new String(decryptedByteArray);

        assertEquals(payLoad, decryptedPayload);
    }
    
    /**
     * Test ALGRSA_OAEP_WITH_ENC_A192CBC_HS384.
     *
     * @throws ParseException the parse exception
     * @throws NoSuchAlgorithmException 
     */
    @Test
    public void test_ALGRSA_OAEP_WITH_ENC_A192CBC_HS384() throws ParseException, NoSuchAlgorithmException {

        JWE jwe = new JWEBuilder()
            .algorithm(ALG_RSA_OAEP)
            .encryptionAlgorithm(ENC_A192CBC_HS384, 384)
            .compressionAlgorithm("DEF")
            .build();

        String payLoad = "{\"alg\": \"ALG_RSA_OAEP\",\"enc\": \"ENC_A192CBC_HS384\",\"zip\": \"DEF\"}";

        JWEEncrypter encrypter = new JWEEncrypter((RSAPublicKey) keyPair1.getPublic());
        String encryptedPayload = encrypter.encrypt(jwe, payLoad.getBytes());

        String[] cryptoPart = JsonUtil.split(encryptedPayload);

        JWEDecrypter decrypter = new JWEDecrypter((RSAPrivateKey) keyPair1.getPrivate());
        byte[] decryptedByteArray = decrypter.decrypt(jwe, cryptoPart[1], cryptoPart[2], cryptoPart[3], cryptoPart[4]);
        String decryptedPayload = new String(decryptedByteArray);

        assertEquals(payLoad, decryptedPayload);
    }
    
    /**
     * Test ALGRSA_OAEP256_WITH_ENC_A256CBC_HS512.
     *
     * @throws ParseException the parse exception
     */
    @Test
    public void test_ALGRSA_OAEP256_WITH_ENC_A256CBC_HS512() throws ParseException {

        JWE jwe = new JWEBuilder()
            .algorithm(ALG_RSA_OAEP_256)
            // Bit length 512 Causes javax.crypto.IllegalBlockSizeException: Data must not be longer than 62 bytes
            // at org.picketlink.json.jose.crypto.RSA_OAEP_256.encryptCEK(RSA_OAEP_256.java:61)
            //.encryptionAlgorithm(ENC_A256CBC_HS512, 512)
            .encryptionAlgorithm(ENC_A256CBC_HS512, 256)
            .compressionAlgorithm("DEF")
            .build();

        String payLoad = "{\"alg\": \"ALG_RSA_OAEP_256\",\"enc\": \"ENC_A256CBC_HS512\",\"zip\": \"DEF\"}";

        JWEEncrypter encrypter = new JWEEncrypter((RSAPublicKey) keyPair1.getPublic());
        String encryptedPayload = encrypter.encrypt(jwe, payLoad.getBytes());

        String[] cryptoPart = JsonUtil.split(encryptedPayload);

        JWEDecrypter decrypter = new JWEDecrypter((RSAPrivateKey) keyPair1.getPrivate());
        byte[] decryptedByteArray = decrypter.decrypt(jwe, cryptoPart[1], cryptoPart[2], cryptoPart[3], cryptoPart[4]);
        String decryptedPayload = new String(decryptedByteArray);

        assertEquals(payLoad, decryptedPayload);
    }
    
    /**
     * Test invalid private key.
     *
     * @throws ParseException the parse exception
     */
    @Test(expected = RuntimeException.class)
    public void test_INVALID_PRIVATE_KEY() throws ParseException {

        JWE jwe = new JWEBuilder()
            .algorithm(ALG_RSA_OAEP_256)
            .encryptionAlgorithm(ENC_A256GCM, 256)
            .compressionAlgorithm("DEF")
            .build();

        String payLoad = "{\"alg\": \"ALG_RSA_OAEP_256\",\"enc\": \"ENC_A256GCM\",\"zip\": \"DEF\"}";

        JWEEncrypter encrypter = new JWEEncrypter((RSAPublicKey) keyPair1.getPublic());
        String encryptedPayload = encrypter.encrypt(jwe, payLoad.getBytes());

        String[] cryptoPart = JsonUtil.split(encryptedPayload);

        JWEDecrypter decrypter = new JWEDecrypter((RSAPrivateKey) keyPair2.getPrivate());
        byte[] decryptedByteArray = decrypter.decrypt(jwe, cryptoPart[1], cryptoPart[2], cryptoPart[3], cryptoPart[4]);
        String decryptedPayload = new String(decryptedByteArray);

        assertEquals(payLoad, decryptedPayload);
    }

    /**
     * Test invalid serialization.
     *
     * @throws ParseException the parse exception
     */
    @Test(expected = ParseException.class)
    public void test_INVALID_SERIALIZATION_1() throws ParseException {

        JWE jwe = new JWEBuilder()
            .algorithm(ALG_RSA_OAEP_256)
            .encryptionAlgorithm(ENC_A256GCM, 256)
            .compressionAlgorithm("DEF")
            .build();

        String payLoad = "{\"alg\": \"ALG_RSA_OAEP_256\",\"enc\": \"ENC_A256GCM\",\"zip\": \"DEF\"}";

        JWEEncrypter encrypter = new JWEEncrypter((RSAPublicKey) keyPair1.getPublic());
        String encryptedPayload = encrypter.encrypt(jwe, payLoad.getBytes());

        encryptedPayload = encryptedPayload.substring(encryptedPayload.lastIndexOf('.'));

        String[] cryptoPart = JsonUtil.split(encryptedPayload);

        JWEDecrypter decrypter = new JWEDecrypter((RSAPrivateKey) keyPair1.getPrivate());
        byte[] decryptedByteArray = decrypter.decrypt(jwe, cryptoPart[1], cryptoPart[2], cryptoPart[3], cryptoPart[4]);
        String decryptedPayload = new String(decryptedByteArray);

        assertEquals(payLoad, decryptedPayload);
    }

    /**
     * Test invalid serialization.
     *
     * @throws ParseException the parse exception
     */
    @Test(expected = ParseException.class)
    public void test_INVALID_SERIALIZATION_2() throws ParseException {

        JWE jwe = new JWEBuilder()
            .algorithm(ALG_RSA_OAEP_256)
            .encryptionAlgorithm(ENC_A256GCM, 256)
            .compressionAlgorithm("DEF")
            .build();

        String payLoad = "{\"alg\": \"ALG_RSA_OAEP_256\",\"enc\": \"ENC_A256GCM\",\"zip\": \"DEF\"}";

        JWEEncrypter encrypter = new JWEEncrypter((RSAPublicKey) keyPair1.getPublic());
        String encryptedPayload = encrypter.encrypt(jwe, payLoad.getBytes());

        encryptedPayload = encryptedPayload.concat(".invalidExtraPart");

        String[] cryptoPart = JsonUtil.split(encryptedPayload);

        JWEDecrypter decrypter = new JWEDecrypter((RSAPrivateKey) keyPair1.getPrivate());
        byte[] decryptedByteArray = decrypter.decrypt(jwe, cryptoPart[1], cryptoPart[2], cryptoPart[3], cryptoPart[4]);
        String decryptedPayload = new String(decryptedByteArray);

        assertEquals(payLoad, decryptedPayload);
    }
}

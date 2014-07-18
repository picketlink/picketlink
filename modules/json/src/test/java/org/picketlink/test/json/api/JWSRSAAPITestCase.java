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
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.picketlink.json.JsonException;
import org.picketlink.json.jose.JWK;
import org.picketlink.json.jose.JWKBuilder;
import org.picketlink.json.jose.JWS;
import org.picketlink.json.jose.JWSBuilder;
/**
 * @author Pedro Igor
 */
public class JWSRSAAPITestCase {

    private KeyPair keyPair;
    private KeyPair anotherKeyPair;
//    private X509CertImpl certificate;

    @Before
    public void onBefore() throws Exception {
        this.keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        this.anotherKeyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

//        PrivateKey privkey = this.keyPair.getPrivate();
//        X509CertInfo info = new X509CertInfo();
//        Date from = new Date();
//        Date to = new Date(from.getTime() + 10 * 86400000l);
//        CertificateValidity interval = new CertificateValidity(from, to);
//        BigInteger sn = new BigInteger(64, new SecureRandom());
//        X500Name owner = new X500Name("CN=picketlink");
//
//        info.set(X509CertInfo.VALIDITY, interval);
//        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
//        info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(owner));
//        info.set(X509CertInfo.ISSUER, new CertificateIssuerName(owner));
//        info.set(X509CertInfo.KEY, new CertificateX509Key(this.keyPair.getPublic()));
//        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
//        AlgorithmId algo = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
//        info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));
//
//        // Sign the cert to identify the algorithm that's used.
//        this.certificate = new X509CertImpl(info);
//
//        String algorithm = Algorithm.RS512.getAlgorithm();
//
//        this.certificate.sign(privkey, algorithm);
//
//        // Update the algorithm, and resign.
//        algo = (AlgorithmId) this.certificate.get(X509CertImpl.SIG_ALG);
//        info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
//
//        this.certificate = new X509CertImpl(info);
//
//        this.certificate.sign(privkey, algorithm);
    }
    
    @Test
    public void testRSAPublicKey() {

        List<JWK> jwk = new ArrayList<JWK>();
        RSAPublicKey publicKey = (RSAPublicKey) this.keyPair.getPublic();

        JWK rsaJWK = new JWKBuilder()
            .modulus(publicKey.getModulus())
            .publicExponent(publicKey.getPublicExponent())
            .keyIdentifier("1")
            .keyType("RSA")
            .keyAlgorithm(publicKey.getAlgorithm())
            .keyUse("enc")
            .build();
        jwk.add(rsaJWK);

        JWK ecJWK = new JWKBuilder()
            .keyIdentifier("2")
            .keyType("EC")
            .keyAlgorithm("ES256")
            .keyOperations("sign", "verify")
            .curve("P-256")
            .build();
        jwk.add(ecJWK);

        JWK octetJWK = new JWKBuilder()
            .keyIdentifier("3")
            .keyType("oct")
            .keyAlgorithm("A128KW")
            .keyOperations("encrypt", "decrypt")
            .build();
        jwk.add(octetJWK);

        JWS token = new JWSBuilder()
            .kid(rsaJWK.getKeyIdentifier())
            .rsa256(this.keyPair.getPrivate().getEncoded())
            .keys(jwk)
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .build();

        String jsonString = token.toString();

        String jsonEncoded = token.encode();

        JWS parsedToken = null;
        try {
            parsedToken = new JWSBuilder().build(jsonEncoded, JWK.toRSAPublicKey(rsaJWK).getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve RSAPublicKey from JWK");
        }

        assertNotNull(parsedToken);
    }

    @Test
    public void testRSA256Signature() {

        List<JWK> jwk = new ArrayList<JWK>();
        RSAPublicKey publicKey = (RSAPublicKey) this.keyPair.getPublic();

        JWK rsaJWK = new JWKBuilder()
            .publicExponent(publicKey.getPublicExponent())
            .keyIdentifier("1")
            .keyType("RSA")
            .keyAlgorithm(publicKey.getAlgorithm())
            .keyUse("enc")
            .build();
        jwk.add(rsaJWK);

        JWK ecJWK = new JWKBuilder()
            .keyIdentifier("2")
            .keyType("EC")
            .keyAlgorithm("ES256")
            .keyOperations("sign", "verify")
            .curve("P-256")
            .build();
        jwk.add(ecJWK);

        JWK octetJWK = new JWKBuilder()
            .keyIdentifier("3")
            .keyType("oct")
            .keyAlgorithm("A128KW")
            .keyOperations("encrypt", "decrypt")
            .build();
        jwk.add(octetJWK);

        JWS token = new JWSBuilder()
            .kid(rsaJWK.getKeyIdentifier())
            .rsa256(this.keyPair.getPrivate().getEncoded())
            .keys(jwk)
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .build();

        String jsonString = token.toString();

        assertEquals(
            "{\"typ\":\"JWT\",\"alg\":\"RS256\",\"kid\":\"1\",\"keys\":[{\"e\":\"AQAB\",\"kid\":\"1\",\"kty\":\"RSA\",\"alg\":\"RSA\",\"use\":\"enc\"},{\"kid\":\"2\",\"kty\":\"EC\",\"alg\":\"ES256\",\"key_ops\":[\"sign\",\"verify\"],\"crv\":\"P-256\"},{\"kid\":\"3\",\"kty\":\"oct\",\"alg\":\"A128KW\",\"key_ops\":[\"encrypt\",\"decrypt\"]}]}.{\"jti\":\"1\",\"iss\":\"issuer\",\"sub\":\"subject\",\"aud\":\"audience\",\"exp\":123,\"iat\":456,\"nbf\":789}",
            jsonString);

        String jsonEncoded = token.encode();

        JWS parsedToken = new JWSBuilder().build(jsonEncoded, this.keyPair.getPublic().getEncoded());

        assertNotNull(parsedToken);
    }

    @Test
    public void testRSA384Signature() {

        List<JWK> jwk = new ArrayList<JWK>();
        RSAPublicKey publicKey = (RSAPublicKey) this.keyPair.getPublic();

        JWK rsaJWK = new JWKBuilder()
            .publicExponent(publicKey.getPublicExponent())
            .keyIdentifier("1")
            .keyType("RSA")
            .keyAlgorithm(publicKey.getAlgorithm())
            .keyUse("enc")
            .build();
        jwk.add(rsaJWK);

        JWK ecJWK = new JWKBuilder()
            .keyIdentifier("2")
            .keyType("EC")
            .keyAlgorithm("ES256")
            .keyOperations("encrypt", "decrypt")
            .curve("P-256")
            .build();
        jwk.add(ecJWK);

        JWS token = new JWSBuilder()
            .kid(rsaJWK.getKeyIdentifier())
            .rsa384(this.keyPair.getPrivate().getEncoded())
            .keys(jwk)
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .build();

        String jsonString = token.toString();

        assertEquals(
            "{\"typ\":\"JWT\",\"alg\":\"RS384\",\"kid\":\"1\",\"keys\":[{\"e\":\"AQAB\",\"kid\":\"1\",\"kty\":\"RSA\",\"alg\":\"RSA\",\"use\":\"enc\"},{\"kid\":\"2\",\"kty\":\"EC\",\"alg\":\"ES256\",\"key_ops\":[\"encrypt\",\"decrypt\"],\"crv\":\"P-256\"}]}.{\"jti\":\"1\",\"iss\":\"issuer\",\"sub\":\"subject\",\"aud\":\"audience\",\"exp\":123,\"iat\":456,\"nbf\":789}",
            jsonString);

        String jsonEncoded = token.encode();

        JWS parsedToken = new JWSBuilder().build(jsonEncoded, this.keyPair.getPublic().getEncoded());

        assertNotNull(parsedToken);
    }

    @Test
    public void testRSA512Signature() {

        List<JWK> jwk = new ArrayList<JWK>();
        RSAPublicKey publicKey = (RSAPublicKey) this.keyPair.getPublic();

        JWK rsaJWK = new JWKBuilder()
            .publicExponent(publicKey.getPublicExponent())
            .keyIdentifier("1")
            .keyType("RSA")
            .keyAlgorithm(publicKey.getAlgorithm())
            .keyUse("enc")
            .build();
        jwk.add(rsaJWK);

        JWS token = new JWSBuilder()
            .kid(rsaJWK.getKeyIdentifier())
            .rsa512(this.keyPair.getPrivate().getEncoded())
            .keys(jwk)
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .build();

        String jsonString = token.toString();

        assertEquals(
            "{\"typ\":\"JWT\",\"alg\":\"RS512\",\"kid\":\"1\",\"keys\":[{\"e\":\"AQAB\",\"kid\":\"1\",\"kty\":\"RSA\",\"alg\":\"RSA\",\"use\":\"enc\"}]}.{\"jti\":\"1\",\"iss\":\"issuer\",\"sub\":\"subject\",\"aud\":\"audience\",\"exp\":123,\"iat\":456,\"nbf\":789}",
            jsonString);

        String jsonEncoded = token.encode();

        JWS parsedToken = new JWSBuilder().build(jsonEncoded, this.keyPair.getPublic().getEncoded());

        assertNotNull(parsedToken);
    }

    @Test(expected = JsonException.class)
    public void failInvalidSignature() {

        List<JWK> jwk = new ArrayList<JWK>();
        RSAPublicKey publicKey = (RSAPublicKey) this.keyPair.getPublic();

        JWK rsaJWK = new JWKBuilder()
            .publicExponent(publicKey.getPublicExponent())
            .keyIdentifier("1")
            .keyType("RSA")
            .keyAlgorithm(publicKey.getAlgorithm())
            .keyUse("enc")
            .build();
        jwk.add(rsaJWK);

        JWS token = new JWSBuilder()
            .kid(rsaJWK.getKeyIdentifier())
            .rsa256(this.keyPair.getPrivate().getEncoded())
            .keys(jwk)
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .build();

        // here we define a custom claim
        String jsonEncoded = new StringBuilder(token.encode()).insert(3, "tampered").toString();

        new JWSBuilder().build(jsonEncoded, this.keyPair.getPublic().getEncoded());
    }

    @Test(expected = RuntimeException.class)
    public void failInvalidKey() {

        List<JWK> jwk = new ArrayList<JWK>();
        RSAPublicKey publicKey = (RSAPublicKey) this.keyPair.getPublic();

        JWK rsaJWK = new JWKBuilder()
            .publicExponent(publicKey.getPublicExponent())
            .keyIdentifier("1")
            .keyType("RSA")
            .keyAlgorithm(publicKey.getAlgorithm())
            .keyUse("enc")
            .build();
        jwk.add(rsaJWK);

        JWS token = new JWSBuilder()
            .kid(rsaJWK.getKeyIdentifier())
            .rsa256(this.anotherKeyPair.getPrivate().getEncoded())
            .keys(jwk)
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .build();

        // here we define a custom claim
        String jsonEncoded = token.encode();

        new JWSBuilder().build(jsonEncoded, this.keyPair.getPublic().getEncoded());
    }
}
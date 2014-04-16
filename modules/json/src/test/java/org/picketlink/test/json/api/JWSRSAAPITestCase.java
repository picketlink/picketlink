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

import org.junit.Before;
import org.junit.Test;
import org.picketlink.json.JsonException;
import org.picketlink.json.jose.JWS;
import org.picketlink.json.jose.JWSBuilder;
import org.picketlink.json.jose.crypto.Algorithm;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateIssuerName;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateSubjectName;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.Date;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Pedro Igor
 */
public class JWSRSAAPITestCase {

    private KeyPair keyPair;
    private KeyPair anotherKeyPair;
    private X509CertImpl certificate;

    @Before
    public void onBefore() throws Exception {
        this.keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        this.anotherKeyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

        PrivateKey privkey = this.keyPair.getPrivate();
        X509CertInfo info = new X509CertInfo();
        Date from = new Date();
        Date to = new Date(from.getTime() + 10 * 86400000l);
        CertificateValidity interval = new CertificateValidity(from, to);
        BigInteger sn = new BigInteger(64, new SecureRandom());
        X500Name owner = new X500Name("CN=picketlink");

        info.set(X509CertInfo.VALIDITY, interval);
        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
        info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(owner));
        info.set(X509CertInfo.ISSUER, new CertificateIssuerName(owner));
        info.set(X509CertInfo.KEY, new CertificateX509Key(this.keyPair.getPublic()));
        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
        AlgorithmId algo = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
        info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));

        // Sign the cert to identify the algorithm that's used.
        this.certificate = new X509CertImpl(info);

        String algorithm = Algorithm.RS512.getAlgorithm();

        this.certificate.sign(privkey, algorithm);

        // Update the algorith, and resign.
        algo = (AlgorithmId) this.certificate.get(X509CertImpl.SIG_ALG);
        info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);

        this.certificate = new X509CertImpl(info);

        this.certificate.sign(privkey, algorithm);
    }

    @Test
    public void testRSA256Signature() {
        JWS token = new JWSBuilder()
            .rsa256(this.keyPair.getPrivate().getEncoded())
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .build();

        String jsonString = token.toString();

        assertEquals("{\"typ\":\"JWT\",\"alg\":\"RS256\"}.{\"jti\":\"1\",\"iss\":\"issuer\",\"sub\":\"subject\",\"aud\":\"audience\",\"exp\":123,\"iat\":456,\"nbf\":789}", jsonString);

        String jsonEncoded = token.encode();

        JWS parsedToken = new JWSBuilder().build(jsonEncoded, this.keyPair.getPublic().getEncoded());

        assertNotNull(parsedToken);
    }

    @Test
    public void testRSA384Signature() {
        JWS token = new JWSBuilder()
            .rsa384(this.keyPair.getPrivate().getEncoded())
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .build();

        String jsonString = token.toString();

        assertEquals("{\"typ\":\"JWT\",\"alg\":\"RS384\"}.{\"jti\":\"1\",\"iss\":\"issuer\",\"sub\":\"subject\",\"aud\":\"audience\",\"exp\":123,\"iat\":456,\"nbf\":789}", jsonString);

        String jsonEncoded = token.encode();

        JWS parsedToken = new JWSBuilder().build(jsonEncoded, this.keyPair.getPublic().getEncoded());

        assertNotNull(parsedToken);
    }

    @Test
    public void testRSA512Signature() {
        JWS token = new JWSBuilder()
            .rsa512(this.keyPair.getPrivate().getEncoded())
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .build();

        String jsonString = token.toString();

        assertEquals("{\"typ\":\"JWT\",\"alg\":\"RS512\"}.{\"jti\":\"1\",\"iss\":\"issuer\",\"sub\":\"subject\",\"aud\":\"audience\",\"exp\":123,\"iat\":456,\"nbf\":789}", jsonString);

        String jsonEncoded = token.encode();

        JWS parsedToken = new JWSBuilder().build(jsonEncoded, this.keyPair.getPublic().getEncoded());

        assertNotNull(parsedToken);
    }

    @Test(expected = JsonException.class)
    public void failInvalidSignature() {
        JWS token = new JWSBuilder()
            .rsa256(this.anotherKeyPair.getPrivate().getEncoded())
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .build(); // here we define a custom claim

        String jsonEncoded = new StringBuilder(token.encode()).insert(3, "tampered").toString();

        new JWSBuilder().build(jsonEncoded, this.keyPair.getPublic().getEncoded());
    }

    @Test(expected = RuntimeException.class)
    public void failInvalidKey() {
        JWS token = new JWSBuilder()
            .rsa256(this.anotherKeyPair.getPrivate().getEncoded())
            .id("1")
            .issuer("issuer")
            .subject("subject")
            .audience("audience")
            .expiration(123)
            .issuedAt(456)
            .notBefore(789)
            .build(); // here we define a custom claim

        String jsonEncoded = token.encode();

        new JWSBuilder().build(jsonEncoded, this.keyPair.getPublic().getEncoded());
    }
}

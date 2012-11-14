/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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

package org.picketlink.identity.xmlsec.w3.xmldsig;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.util.Base64;
import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;

/**
 * <p>
 * Java class for RSAKeyValueType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="RSAKeyValueType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Modulus" type="{http://www.w3.org/2000/09/xmldsig#}CryptoBinary"/>
 *         &lt;element name="Exponent" type="{http://www.w3.org/2000/09/xmldsig#}CryptoBinary"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public class RSAKeyValueType implements KeyValueType {
    protected byte[] modulus;
    protected byte[] exponent;

    /**
     * Gets the value of the modulus property.
     *
     * @return possible object is byte[]
     */
    public byte[] getModulus() {
        return modulus;
    }

    /**
     * Sets the value of the modulus property.
     *
     * @param value allowed object is byte[]
     */
    public void setModulus(byte[] value) {
        this.modulus = ((byte[]) value);
    }

    /**
     * Gets the value of the exponent property.
     *
     * @return possible object is byte[]
     */
    public byte[] getExponent() {
        return exponent;
    }

    /**
     * Sets the value of the exponent property.
     *
     * @param value allowed object is byte[]
     */
    public void setExponent(byte[] value) {
        this.exponent = ((byte[]) value);
    }

    /**
     * Convert to the JDK representation of a RSA Public Key
     * @return
     * @throws ProcessingException
     */
    public RSAPublicKey convertToPublicKey() throws ProcessingException{
        BigInteger bigModulus = new BigInteger(1, massage(Base64.decode(new String(modulus))));
        BigInteger bigEx = new BigInteger(1, massage(Base64.decode(new String(exponent))));

        try {
            KeyFactory rsaKeyFactory = KeyFactory.getInstance("rsa");
            RSAPublicKeySpec kspec = new RSAPublicKeySpec(bigModulus,bigEx);
            return (RSAPublicKey) rsaKeyFactory.generatePublic(kspec);
        } catch (Exception e) {
            throw new ProcessingException(e);
        }
    }

    /**
     * Convert to the JDK representation of a RSA Private Key
     * @return
     * @throws ProcessingException
     */
    public RSAPrivateKey convertToPrivateKey() throws ProcessingException{
        BigInteger bigModulus = new BigInteger(1, massage(Base64.decode(new String(modulus))));
        BigInteger bigEx = new BigInteger(1, massage(Base64.decode(new String(exponent))));

        try {
            KeyFactory rsaKeyFactory = KeyFactory.getInstance("rsa");
            RSAPrivateKeySpec kspec = new RSAPrivateKeySpec(bigModulus,bigEx);
            return (RSAPrivateKey) rsaKeyFactory.generatePrivate(kspec);
        } catch (Exception e) {
            throw new ProcessingException(e);
        }
    }

    public String toString(){
        String prefix = WSTrustConstants.XMLDSig.DSIG_PREFIX;
        String colon = ":";
        String left = "<";
        String right = ">";
        String slash = "/";

        StringBuilder sb = new StringBuilder();

        sb.append(left).append(prefix).append(colon).append(WSTrustConstants.XMLDSig.RSA_KEYVALUE).append(right);

        sb.append(left).append(prefix).append(colon).append(WSTrustConstants.XMLDSig.MODULUS).append(right);
        sb.append(new String(getModulus()));
        sb.append(left).append(slash).append(prefix).append(colon).append(WSTrustConstants.XMLDSig.MODULUS).append(right);

        sb.append(left).append(prefix).append(colon).append(WSTrustConstants.XMLDSig.EXPONENT).append(right);
        sb.append(new String(getExponent()));
        sb.append(left).append(slash).append(prefix).append(colon).append(WSTrustConstants.XMLDSig.EXPONENT).append(right);

        sb.append(left).append(slash).append(prefix).append(colon).append(WSTrustConstants.XMLDSig.RSA_KEYVALUE).append(right);
        return sb.toString();
    }

    private byte[] massage(byte[] byteArray){
        if (byteArray[0] == 0){
            byte[] substring = new byte[byteArray.length - 1];
            System.arraycopy(byteArray, 1, substring, 0, byteArray.length - 1);
            return substring;
        }
        return byteArray;
    }
}
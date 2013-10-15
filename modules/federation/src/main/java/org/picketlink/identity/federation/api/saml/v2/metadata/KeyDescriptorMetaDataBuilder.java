/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.identity.federation.api.saml.v2.metadata;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.util.StringUtil;
import org.picketlink.identity.federation.saml.v2.metadata.KeyDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.KeyTypes;
import org.picketlink.identity.xmlsec.w3.xmlenc.EncryptionMethodType;
import org.picketlink.identity.xmlsec.w3.xmlenc.EncryptionMethodType.EncryptionMethod;
import org.w3c.dom.Element;

import java.math.BigInteger;

/**
 * MetaDataBuilder for the KeyDescriptor
 *
 * @author Anil.Saldhana@redhat.com
 * @since Apr 20, 2009
 */
public class KeyDescriptorMetaDataBuilder {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    /**
     * Create a Key Descriptor
     *
     * @param keyInfo
     * @param algorithm
     * @param keySize
     * @param isSigningKey Whether the key is for signing
     * @param isEncryptionKey Whether the key is for encryption
     *
     * @return
     *
     * @throws {@link IllegalArgumentException} when keyinfo is null
     * @throws {@link IllegalArgumentException} when both the parameters "isSigningKey" and "isEncryptionKey" are same
     */
    public static KeyDescriptorType createKeyDescriptor(Element keyInfo, String algorithm, int keySize, boolean isSigningKey,
                                                        boolean isEncryptionKey) {
        if (keyInfo == null)
            throw logger.nullArgumentError("keyInfo");

        if (isSigningKey == isEncryptionKey)
            throw logger.shouldNotBeTheSameError("Only one of isSigningKey and isEncryptionKey should be true");

        KeyDescriptorType keyDescriptor = new KeyDescriptorType();

        if (StringUtil.isNotNull(algorithm)) {
            EncryptionMethodType encryptionMethod = new EncryptionMethodType(algorithm);

            encryptionMethod.setEncryptionMethod(new EncryptionMethod(BigInteger.valueOf(keySize), null));

            keyDescriptor.addEncryptionMethod(encryptionMethod);
        }

        if (isSigningKey)
            keyDescriptor.setUse(KeyTypes.SIGNING);
        if (isEncryptionKey)
            keyDescriptor.setUse(KeyTypes.ENCRYPTION);

        keyDescriptor.setKeyInfo(keyInfo);

        return keyDescriptor;
    }

    /**
     * Create a key descriptor that specifies an algorithm but does not specify whether the key is for signing or
     * encryption
     *
     * @param keyInfo
     * @param algorithm
     * @param keySize
     *
     * @return
     */
    public static KeyDescriptorType createKeyDescriptor(Element keyInfo, String algorithm, int keySize) {
        if (keyInfo == null)
            throw logger.nullArgumentError("keyInfo");
        KeyDescriptorType keyDescriptor = new KeyDescriptorType();

        if (StringUtil.isNotNull(algorithm)) {
            EncryptionMethodType encryptionMethod = new EncryptionMethodType(algorithm);

            encryptionMethod.setEncryptionMethod(new EncryptionMethod(BigInteger.valueOf(keySize), null));

            keyDescriptor.addEncryptionMethod(encryptionMethod);
        }
        keyDescriptor.setKeyInfo(keyInfo);

        return keyDescriptor;
    }
}
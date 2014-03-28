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
package org.picketlink.json.enc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.json.PicketLinkJSONMessages;

/**
 * <p>
 * Approved Alternative 1 : Concatenation Key Derivation Function
 * </p>
 * <p>
 * Document: Recommendation for Pair-Wise Key Establishment Schemes Using Discrete Logarithm Cryptography
 * </p>
 * <p>
 * Location: http://csrc.nist.gov/publications/PubsSPs.html SP 800-56A
 * </p>
 *
 * @author anil saldhana
 * @since Jul 27, 2012
 */
public class ConcatenationKeyDerivation {
    private final long MAX_HASH_INPUTLEN = Long.MAX_VALUE;
    private final long UNSIGNED_INTEGER_MAX_VALUE = 4294967295L;
    private MessageDigest md;

    public ConcatenationKeyDerivation(String hashAlg) throws ProcessingException {
        try {
            md = MessageDigest.getInstance(hashAlg);
        } catch (NoSuchAlgorithmException e) {
            throw PicketLinkJSONMessages.MESSAGES.processingException(e);
        }
    }

    public byte[] concatKDF(byte[] z, int keyDataLen, byte[] algorithmID, byte[] partyUInfo, byte[] partyVInfo,
                            byte[] suppPubInfo, byte[] suppPrivInfo) {
        int hashLen = md.getDigestLength() * 8;

        if (keyDataLen % 8 != 0) {
            throw PicketLinkJSONMessages.MESSAGES.keyDataLenError();
        }

        if (keyDataLen > (long) hashLen * UNSIGNED_INTEGER_MAX_VALUE) {
            throw PicketLinkJSONMessages.MESSAGES.keyDataLenLarge();
        }
        if (algorithmID == null) {
            throw PicketLinkJSONMessages.MESSAGES.invalidNullArgument("algorithmID");
        }

        if (partyUInfo == null) {
            throw PicketLinkJSONMessages.MESSAGES.invalidNullArgument("partyUInfo");
        }

        if (partyVInfo == null) {
            throw PicketLinkJSONMessages.MESSAGES.invalidNullArgument("partyVInfo");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(algorithmID);
            baos.write(partyUInfo);
            baos.write(partyVInfo);
            if (suppPubInfo != null) {
                baos.write(suppPubInfo);
            }
            if (suppPrivInfo != null) {
                baos.write(suppPrivInfo);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] otherInfo = baos.toByteArray();
        return concatKDF(z, keyDataLen, otherInfo);
    }

    /**
     * Generate a KDF
     *
     * @param z shared secret
     * @param keyDataLen
     * @param otherInfo
     * @return
     */
    public byte[] concatKDF(byte[] z, int keyDataLen, byte[] otherInfo) {
        byte[] key = new byte[keyDataLen];

        int hashLen = md.getDigestLength();
        int reps = keyDataLen / hashLen;

        if (reps > UNSIGNED_INTEGER_MAX_VALUE) {
            throw new IllegalArgumentException("Key derivation failed");
        }

        // First check on the overall hash length
        int counter = 1;
        byte[] fourByteInt = convertIntegerToFourBytes(counter);

        if ((fourByteInt.length + z.length + otherInfo.length) * 8 > MAX_HASH_INPUTLEN) {
            throw PicketLinkJSONMessages.MESSAGES.hashLengthTooLarge();
        }

        for (int i = 0; i <= reps; i++) {
            md.reset();
            md.update(convertIntegerToFourBytes(i + 1));
            md.update(z);
            md.update(otherInfo);

            byte[] hash = md.digest();
            if (i < reps) {
                System.arraycopy(hash, 0, key, hashLen * i, hashLen);
            } else {
                System.arraycopy(hash, 0, key, hashLen * i, keyDataLen % hashLen);
            }
        }
        return key;
    }

    private byte[] convertIntegerToFourBytes(int i) {
        byte[] res = new byte[4];
        res[0] = (byte) (i >>> 24);
        res[1] = (byte) ((i >>> 16) & 0xFF);
        res[2] = (byte) ((i >>> 8) & 0xFF);
        res[3] = (byte) (i & 0xFF);
        return res;
    }
}

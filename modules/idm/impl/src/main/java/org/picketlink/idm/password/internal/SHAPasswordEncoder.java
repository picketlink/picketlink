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

package org.picketlink.idm.password.internal;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.picketlink.common.util.Base64;
import org.picketlink.idm.IDMMessages;
import org.picketlink.idm.password.PasswordEncoder;
import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * <p>
 * {@link PasswordEncoder} that uses SHA to encode passwords. You can always change the SHA strength by specifying a valid
 * integer when creating a new instance.
 * </p>
 * <p>Passwords are returned with a Base64 encoding.</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class SHAPasswordEncoder implements PasswordEncoder {

    private int strength;

    public SHAPasswordEncoder(int strength) {
        this.strength = strength;
    }

    @Override
    public String encode(String rawPassword) {
        MessageDigest messageDigest = getMessageDigest();

        String encodedPassword = null;

        try {
            byte[] digest = messageDigest.digest(rawPassword.getBytes("UTF-8"));
            encodedPassword = Base64.encodeBytes(digest);
        } catch (UnsupportedEncodingException e) {
            throw MESSAGES.credentialCouldNotEncodePassword(e);
        }

        return encodedPassword;
    }

    @Override
    public boolean verify(String rawPassword, String encodedPassword) {
        return encode(rawPassword).equals(encodedPassword);
    }

    protected final MessageDigest getMessageDigest() throws IllegalArgumentException {
        String algorithm = "SHA-" + this.strength;

        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw IDMMessages.MESSAGES.credentialInvalidEncodingAlgorithm(algorithm, this, e);
        }
    }

    public int getStrength() {
        return this.strength;
    }
}

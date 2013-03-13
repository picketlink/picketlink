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

package org.picketlink.common.password;

import org.picketlink.common.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <p>
 * {@link PasswordEncoder} that uses SHA to created a salted hash the password. Passwords are returned with a Base64 encoding.
 * </p>
 * <p>
 * The provided password is salted before the encoding. The salt is stored as an user's attribute with name
 * <code>PASSWORD_SALT_USER_ATTRIBUTE</code>.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class SHASaltedPasswordEncoder implements PasswordEncoder {


    private int strength;

    public SHASaltedPasswordEncoder(int strength) {
        this.strength = strength;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.idm.PasswordEncoder#encodePassword(java.lang.String, java.lang.Object)
     */
    @Override
    public String encodePassword(String salt, String rawPassword) {
        MessageDigest messageDigest = getMessageDigest();

        String encodedPassword = null;

        try {
            byte[] digest = messageDigest.digest(saltPassword(rawPassword, salt).getBytes("UTF-8"));
            encodedPassword = Base64.encodeBytes(digest);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Could not encode password");
        }

        return encodedPassword;
    }

    /**
     * <p>
     * Salt the password with the specified salt value.
     * </p>
     *
     * @param rawPassword
     * @param salt
     * @return
     */
    private String saltPassword(String rawPassword, String salt) {
        return rawPassword + salt.toString();
    }

    protected final MessageDigest getMessageDigest() throws IllegalArgumentException {
        String algorithm = "SHA-" + this.strength;

        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("No such algorithm: " + algorithm);
        }
    }

}

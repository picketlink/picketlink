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

package org.picketlink.idm.password.internal;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.picketlink.common.util.Base64;
import org.picketlink.idm.password.PasswordEncoder;

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
 *
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

        byte[] encodedPassword = null;

        try {
            encodedPassword = messageDigest.digest(saltPassword(rawPassword, salt).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error encoding password", e);
        }

        return Base64.encodeBytes(encodedPassword);
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

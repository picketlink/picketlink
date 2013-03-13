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

package org.picketlink.idm.credential;

import org.picketlink.common.password.PasswordEncoder;
import org.picketlink.common.password.SHASaltedPasswordEncoder;

/**
 * Represents a text-based password credential
 *
 * @author Shane Bryzak
 */
public class Password {

    private char[] value = new char[]{};
    private PasswordEncoder encoder = new SHASaltedPasswordEncoder(512);

    public Password(char[] value) {
        this.value = value;
    }

    public Password(char[] value, PasswordEncoder encoder) {
        this.value = value;
        this.encoder = encoder;
    }

    public Password(String str) {
        this.value = str != null ? str.toCharArray() : value;
    }

    public char[] getValue() {
        return value;
    }

    public PasswordEncoder getEncoder() {
        return encoder;
    }

    public void clear() {
        for (int i = 0; i < value.length; i++) {
            value[i] = 0x00;
        }
        value = null;
    }
}

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


import org.picketlink.idm.IDMMessages;
import static org.picketlink.common.util.StringUtil.isNullOrEmpty;

/**
 * <p>This class can be used to update TOTP credentials. Credentials can be updated providing a password/secret combination
 * or only a secret.</p>
 * <p>If using a password/secret combination, indicates that both password and secret should be updated. If only a secret
 * is provided, the password will not be updated.</p>
 *
 * @author anil saldhana
 * @since Dec 31, 2012
 */
public class TOTPCredential extends Password {

    private final String secret;
    private String device;

    public TOTPCredential(String secret) {
        this((String) null, secret);
    }

    public TOTPCredential(String password, String secret) {
        super(password);

        if (isNullOrEmpty(secret)) {
            throw IDMMessages.MESSAGES.nullArgument("TOTP secret.");
        }

        this.secret = secret;
    }

    public String getSecret() {
        return this.secret;
    }

    public String getDevice() {
        return this.device;
    }

    public void setDevice(String device) {
        this.device = device;
    }
}
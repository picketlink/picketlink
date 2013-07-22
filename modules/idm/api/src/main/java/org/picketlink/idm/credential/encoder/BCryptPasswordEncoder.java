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
package org.picketlink.idm.credential.encoder;

import org.picketlink.idm.credential.util.BCrypt;

/**
 * Implementation of {@link PasswordEncoder} based on BCrypt
 * @author Anil Saldhana
 * @since June 18, 2013
 */
public class BCryptPasswordEncoder implements PasswordEncoder{
    int logRounds = 12;

    /**
     * Create {@link BCryptPasswordEncoder}
     * @param logRounds default is 12. Range is 4 to 31
     */
    public BCryptPasswordEncoder(int logRounds){
        this.logRounds = logRounds;
    }
    @Override
    public String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(logRounds));
    }

    @Override
    public boolean verify(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}
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

package org.picketlink.authentication;


/**
 * <p>Thrown during the authentication process to indicate that the agent being validated is disabled.</p> 
 * 
 * @author Pedro Igor
 * 
 */
public class LockedAccountException extends AuthenticationException {

    private static final long serialVersionUID = -1555087586933373287L;

    public LockedAccountException(String message) {
        super(message);
    }
    
}

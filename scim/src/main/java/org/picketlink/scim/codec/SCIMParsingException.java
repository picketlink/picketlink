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
package org.picketlink.scim.codec;

import java.security.GeneralSecurityException;

/**
 * Parsing Exception
 * @author anil saldhana
 * @since Apr 8, 2013
 */
public class SCIMParsingException extends GeneralSecurityException {
    private static final long serialVersionUID = 2499431723101384910L;

    public SCIMParsingException() {
        super();
    }

    public SCIMParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public SCIMParsingException(String msg) {
        super(msg);
    }

    public SCIMParsingException(Throwable cause) {
        super(cause);
    }
}
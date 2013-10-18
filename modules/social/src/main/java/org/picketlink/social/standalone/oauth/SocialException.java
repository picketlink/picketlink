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

package org.picketlink.social.standalone.oauth;

import org.picketlink.common.exceptions.PicketLinkException;

/**
 * Exception when calling some operations on Social network (OAuth provider)
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SocialException extends PicketLinkException {

    private static final long serialVersionUID = -7034897190745766989L;

    // Specify error code
    private final SocialExceptionCode exceptionCode;

    public SocialException(SocialExceptionCode exceptionCode, String message) {
        super(message);
        this.exceptionCode = exceptionCode;
    }

    public SocialException(SocialExceptionCode exceptionCode, String message, Throwable cause) {
        super(message, cause);
        this.exceptionCode = exceptionCode;
    }

    @Override
    public String getMessage() {
        return exceptionCode + ": " + super.getMessage();
    }
}

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
package org.picketlink.identity.federation.core.wstrust.exceptions;

import java.security.GeneralSecurityException;

/**
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 */
public class WSTrustGeneralException extends GeneralSecurityException {

    private static final long serialVersionUID = -6855476286470782334L;

    public WSTrustGeneralException() {
        super();
    }

    public WSTrustGeneralException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public WSTrustGeneralException(final String msg) {
        super(msg);
    }

    public WSTrustGeneralException(final Throwable cause) {
        super(cause);
    }

}

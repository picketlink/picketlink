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
package org.picketlink.oauth.grants;

import org.picketlink.oauth.messages.ErrorResponse;
import org.picketlink.oauth.messages.ErrorResponse.ErrorResponseCode;

/**
 * Base class for the grants
 *
 * @author anil saldhana
 * @since Mar 5, 2013
 */
public abstract class OAuthGrant {

    protected ErrorResponseCode errorCode;

    protected String errorDescription;

    protected String errorURI;
    protected String state;

    public abstract void validate();

    public OAuthGrant setErrorResponseCode(ErrorResponseCode erc) {
        this.errorCode = erc;
        return this;
    }

    public OAuthGrant setErrorDescription(String errorDesc) {
        this.errorDescription = errorDesc;
        return this;
    }

    public OAuthGrant setErrorURI(String errorURI) {
        this.errorURI = errorURI;
        return this;
    }

    public ErrorResponse error() {
        ErrorResponse response = new ErrorResponse();
        response.setErrorDescription(errorDescription);
        response.setErrorURI(errorURI);
        response.setError(errorCode);
        response.setState(state);
        return response;
    }

}
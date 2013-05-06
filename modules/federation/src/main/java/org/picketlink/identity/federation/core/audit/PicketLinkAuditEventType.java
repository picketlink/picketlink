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
package org.picketlink.identity.federation.core.audit;

/**
 * An enumeration indicating the type of audit event
 *
 * @author anil saldhana
 */
public enum PicketLinkAuditEventType {
    CREATED_ASSERTION,
    ERROR_RESPONSE_TO_SP,
    ERROR_SIG_VALIDATION,
    ERROR_TRUSTED_DOMAIN,
    EXPIRED_ASSERTION,
    GENERATED_ROLES,
    INVALIDATE_HTTP_SESSION,
    LOGIN_INIT,
    LOGIN_COMPLETE,
    REQUEST_TO_IDP,
    RESPONSE_FROM_IDP,
    REQUEST_FROM_IDP,
    RESPONSE_TO_SP,
    TOKEN_ISSUED;
}
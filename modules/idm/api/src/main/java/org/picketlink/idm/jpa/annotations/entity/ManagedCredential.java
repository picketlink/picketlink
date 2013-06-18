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
package org.picketlink.idm.jpa.annotations.entity;

import org.picketlink.idm.credential.spi.CredentialStorage;

/**
 * This annotation is applied to an entity class to indicate that it contains managed
 * credential-related state.  The supportedClasses member may be used to specify exactly
 * which credential classes are stored in the entity, allowing multiple entities to be used
 * to store a variety of credential types.
 *
 * @author Shane Bryzak
 */
public @interface ManagedCredential {
    Class<? extends CredentialStorage>[] supportedClasses() default {};
}

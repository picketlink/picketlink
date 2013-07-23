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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.picketlink.idm.credential.storage.CredentialStorage;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>This annotation is applied to an entity class to indicate that it contains managed
 * credential-related state.</p>
 *
 * @author Shane Bryzak
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface ManagedCredential {

    /**
     * <p>(Optional) Defines which credential storage types are supported and mapped for an entity. If not specified
     * the entity will support any credential storage type.</p>
     *
     * @return
     */
    Class<? extends CredentialStorage>[] value() default {};

}

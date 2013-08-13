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

package org.picketlink.annotations;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Qualifies a bean, injection or producer field as a PicketLink resource.</p>
 *
 * <p>It is usually used to configure a specific behavior for the extensions points provided by PicketLink. You
 * can use this annotation with types, field or methods. If used in a field or method this usually means
 * you're producing or injecting a specific PicketLink enabled bean.</p>
 *
 * <p>As an example, when using PicketLink IDM backed by a JPA identity store, you must produce a <code>EntityManager</code>
 * using this annotation.</p>
 *
 * <p>Another usage example is when providing a custom {@link org.picketlink.authentication.Authenticator}.</p>
 *
 * @author Shane Bryzak
 *
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface PicketLink {

}

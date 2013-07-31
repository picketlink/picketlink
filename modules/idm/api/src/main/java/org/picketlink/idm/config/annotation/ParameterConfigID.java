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

package org.picketlink.idm.config.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation can be used for parameters in {@link org.picketlink.idm.config.Builder} methods. It represents how will be
 * particular parameter identified in configuration (For example in XML configuration).
 *
 * If this annotation is not present, the configID of parameter will be the name of this parameter
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@Target({PARAMETER})
@Documented
@Retention(RUNTIME)
public @interface ParameterConfigID {

    String name();

}

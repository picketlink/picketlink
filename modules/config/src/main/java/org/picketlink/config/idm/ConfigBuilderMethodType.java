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

package org.picketlink.config.idm;

import java.util.Collections;
import java.util.Map;

/**
 * This entry represents one method call to IDM Configuration builder
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ConfigBuilderMethodType {

    // methodId is either method name or value of MethodConfigID annotation
    private final String methodId;

    private final Map<String, String> methodParameters;

    public ConfigBuilderMethodType(String methodId, Map<String, String> methodParameters) {
        this.methodId = methodId;
        this.methodParameters = Collections.unmodifiableMap(methodParameters);
    }
    public String getMethodId() {
        return methodId;
    }

    public Map<String, String> getMethodParameters() {
        return methodParameters;
    }
}

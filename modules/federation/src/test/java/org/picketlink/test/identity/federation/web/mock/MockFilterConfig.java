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
package org.picketlink.test.identity.federation.web.mock;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Mock Filter Config
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 8, 2009
 */
public class MockFilterConfig implements FilterConfig {

    private Map<String, String> params = new HashMap<String, String>();
    private ServletContext context = null;

    public MockFilterConfig(ServletContext ctx) {
        this.context = ctx;
    }

    public void addInitParameter(String key, String val) {
        params.put(key, val);
    }

    public String getFilterName() {
        throw new RuntimeException("NYI");
    }

    public String getInitParameter(String arg0) {
        return params.get(arg0);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Enumeration getInitParameterNames() {
        throw new RuntimeException("NYI");
    }

    public ServletContext getServletContext() {
        return context;
    }
}
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

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Mock Servlet Config
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 7, 2009
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class MockServletConfig implements ServletConfig {

    private ServletContext context;

    private Map<String, String> params = new HashMap<String, String>();

    public MockServletConfig(ServletContext context) {
        this.context = context;
    }

    public void addInitParameter(String key, String value) {
        params.put(key, value);
    }

    public String getInitParameter(String arg0) {
        return params.get(arg0);
    }

    public Enumeration getInitParameterNames() {
        return new Enumeration() {
            private Iterator iter = params.entrySet().iterator();

            public boolean hasMoreElements() {
                return iter.hasNext();
            }

            public Object nextElement() {
                Entry<String, String> entry = (Entry<String, String>) iter.next();
                return entry.getValue();
            }
        };
    }

    public ServletContext getServletContext() {
        return this.context;
    }

    public String getServletName() {

        throw new RuntimeException("NYI");
    }

}

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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * Mock HttpSession
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 7, 2009
 */
@SuppressWarnings({"deprecation", "unchecked", "rawtypes"})
public class MockHttpSession implements HttpSession {

    private boolean valid = true;

    private Map<String, Object> attribs = new HashMap<String, Object>();

    private String id = UUID.randomUUID().toString();

    private ServletContext context;

    public boolean isInvalidated() {
        return valid == false;
    }

    public Object getAttribute(String arg0) {
        return attribs.get(arg0);
    }

    public Enumeration getAttributeNames() {
        return new Enumeration() {
            private Iterator iter = attribs.entrySet().iterator();

            public boolean hasMoreElements() {
                return iter.hasNext();
            }

            public Object nextElement() {
                Entry<String, Object> entry = (Entry<String, Object>) iter.next();
                return entry.getValue();
            }
        };
    }

    public long getCreationTime() {
        return 0;
    }

    public String getId() {
        return id;
    }

    public long getLastAccessedTime() {
        return 0;
    }

    public int getMaxInactiveInterval() {
        return 0;
    }

    public void setServletContext(ServletContext servletContext) {
        this.context = servletContext;
    }

    public ServletContext getServletContext() {
        return this.context;
    }

    public HttpSessionContext getSessionContext() {

        throw new RuntimeException("NYI");
    }

    public Object getValue(String arg0) {
        throw new RuntimeException("NYI");
    }

    public String[] getValueNames() {
        throw new RuntimeException("NYI");
    }

    public void invalidate() {
        this.valid = false;
    }

    public boolean isNew() {
        if (this.valid == false)
            throw new IllegalStateException("Session already invalidated");

        return false;
    }

    public void putValue(String arg0, Object arg1) {
        if (this.valid == false)
            throw new IllegalStateException("Session already invalidated");
    }

    public void removeAttribute(String arg0) {
        if (this.valid == false)
            throw new IllegalStateException("Session already invalidated");

        this.attribs.remove(arg0);
    }

    public void removeValue(String arg0) {
        if (this.valid == false)
            throw new IllegalStateException("Session already invalidated");
    }

    public void setAttribute(String arg0, Object arg1) {
        if (this.valid == false)
            throw new IllegalStateException("Session already invalidated");

        this.attribs.put(arg0, arg1);
    }

    public void setMaxInactiveInterval(int arg0) {
    }
}
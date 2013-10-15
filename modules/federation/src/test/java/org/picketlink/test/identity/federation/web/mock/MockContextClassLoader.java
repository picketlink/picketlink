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

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Mock TCL
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 7, 2009
 */
public class MockContextClassLoader extends URLClassLoader {

    private String profile;

    private ClassLoader delegate;

    public MockContextClassLoader(URL[] urls) {
        super(urls);
    }

    public void setDelegate(ClassLoader tcl) {
        this.delegate = tcl;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        if (profile == null)
            throw new RuntimeException("null profile");
        InputStream is = super.getResourceAsStream(name);
        if (is == null)
            is = delegate.getResourceAsStream(profile + "/" + name);
        return is;
    }

    @Override
    public URL getResource(String name) {
        if (profile == null)
            throw new RuntimeException("null profile");
        URL url = null;
        try {
            url = super.getResource(profile + "/" + name);
        } catch (Exception e) {
        }
        if (url == null)
            url = delegate.getResource(profile + "/" + name);
        return url;
    }
}
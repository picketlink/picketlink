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
package org.picketlink.test.oauth.server.endpoint;

import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.junit.After;
import org.picketbox.test.ldap.LDAPTestUtil;
import org.picketlink.oauth.PicketLinkOAuthApplication;

import java.io.File;
import java.net.URL;

/**
 * Base class for the endpoint test cases
 *
 * @author anil saldhana
 * @since Sep 13, 2012
 */
public class EndpointTestBase extends EmbeddedWebServerBase {

    protected LDAPTestUtil testUtil = null;

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        if (testUtil != null) {
            testUtil.tearDown();
        }
        Thread.sleep(1000); // 1sec
    }

    @Override
    protected void establishUserApps() {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        if (tcl == null) {
            tcl = getClass().getClassLoader();
        }

        final String WEBAPPDIR = "oauth";

        final String CONTEXTPATH = "/*";

        // for localhost:port/admin/index.html and whatever else is in the webapp directory
        final URL warUrl = tcl.getResource(WEBAPPDIR);
        final String warUrlString = warUrl.toExternalForm();

        // WebAppContext context = new WebAppContext(warUrlString, CONTEXTPATH);

        WebAppContext context = createWebApp(CONTEXTPATH, warUrlString);

        context.setExtraClasspath(warUrlString + "/..");

        context.setContextPath("/");
        ServletHolder servletHolder = new ServletHolder(new HttpServletDispatcher());
        servletHolder.setInitParameter("javax.ws.rs.Application", PicketLinkOAuthApplication.class.getName());
        context.addServlet(servletHolder, "/*");

        // context.setParentLoaderPriority(true);

        server.setHandler(context);
        if (needLDAP()) {
            // Deal with LDAP Server
            try {
                deleteApacheDSTmp();
                testUtil = new LDAPTestUtil();
                testUtil.setup();
                testUtil.createBaseDN("jboss", "dc=jboss,dc=org");
                testUtil.importLDIF("ldap/users.ldif");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Override if the test case needs LDAP Support
     *
     * @return
     */
    protected boolean needLDAP() {
        return false;
    }

    protected void deleteApacheDSTmp() {
        String tempDir = System.getProperty("java.io.tmpdir");
        System.out.println("java.io.tmpdir=" + tempDir);

        System.out.println("Going to delete the server-work directory");
        File workDir = new File(tempDir + "/server-work");
        if (workDir != null) {
            recursiveDeleteDir(workDir);
        }
    }

    protected boolean recursiveDeleteDir(File dirPath) {
        if (dirPath.exists()) {
            File[] files = dirPath.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    recursiveDeleteDir(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        if (dirPath.exists())
            return dirPath.delete();
        else
            return true;
    }
}
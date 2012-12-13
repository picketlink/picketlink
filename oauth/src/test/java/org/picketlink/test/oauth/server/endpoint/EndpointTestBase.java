/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketlink.test.oauth.server.endpoint;

import java.io.File;
import java.net.URL;

import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.junit.After;
import org.picketbox.test.ldap.LDAPTestUtil;
import org.picketlink.oauth.PicketLinkOAuthApplication;

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
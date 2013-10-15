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
package org.picketlink.identity.federation.core.factories;

import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextFactory;
import org.picketlink.common.ErrorCodes;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Iterator;

/**
 * Privileged blocks
 *
 * @author <a href="mmoyses@redhat.com">Marcus Moyses</a>
 * @version $Revision: 1 $
 */
class SecurityActions {

    static SecurityContext createSecurityContext() throws PrivilegedActionException {
        SecurityManager sm = System.getSecurityManager();

        if (sm != null) {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<SecurityContext>() {
                public SecurityContext run() throws Exception {
                    return SecurityContextFactory.createSecurityContext("CLIENT");
                }
            });
        } else {
            try {
                return SecurityContextFactory.createSecurityContext("CLIENT");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    static MBeanServer getJBossMBeanServer() {
        SecurityManager sm = System.getSecurityManager();

        if (sm != null) {
            return AccessController.doPrivileged(new PrivilegedAction<MBeanServer>() {
                public MBeanServer run() {
                    // Differences in JBAS5.1, 6.0 with the "jboss" mbean server.
                    MBeanServer cached = null;

                    for (Iterator<MBeanServer> i = MBeanServerFactory.findMBeanServer(null).iterator(); i.hasNext(); ) {
                        MBeanServer server = i.next();

                        String defaultDomain = server.getDefaultDomain();

                        if (defaultDomain != null) {
                            if (defaultDomain.contains("Default"))
                                cached = server;

                            if (defaultDomain.equals("jboss")) {
                                return server;
                            }
                        }
                    }
                    if (cached != null)
                        return cached; // We did not find one with jboss but there is "DefaultDomain" which is the norm in AS6
                    throw new IllegalStateException(ErrorCodes.NULL_VALUE + "No 'jboss' MBeanServer found!");
                }
            });
        } else {
            // Differences in JBAS5.1, 6.0 with the "jboss" mbean server.
            MBeanServer cached = null;

            for (Iterator<MBeanServer> i = MBeanServerFactory.findMBeanServer(null).iterator(); i.hasNext(); ) {
                MBeanServer server = i.next();

                String defaultDomain = server.getDefaultDomain();

                if (defaultDomain != null) {
                    if (defaultDomain.contains("Default"))
                        cached = server;

                    if (defaultDomain.equals("jboss")) {
                        return server;
                    }
                }
            }
            if (cached != null)
                return cached; // We did not find one with jboss but there is "DefaultDomain" which is the norm in AS6

            throw new IllegalStateException(ErrorCodes.NULL_VALUE + "No 'jboss' MBeanServer found!");
        }
    }
}
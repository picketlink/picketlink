/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.identity.federation.core.util;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Security;

import org.picketlink.identity.federation.PicketLinkLogger;
import org.picketlink.identity.federation.PicketLinkLoggerFactory;

/**
 * Utility dealing with the Santuario (XMLSec) providers registration for PicketLink
 *
 * @author alessio.soldano@jboss.com
 * @since 07-May-2012
 */
public class ProvidersUtil {
    
    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    /**
     * No-op call such that the default system properties are set
     */
    public static synchronized void ensure() {
        AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            public Boolean run() {
                // register Apache Santuario 1.5.x XMLDSig version
                addXMLDSigRI();
                // register BC provider if available (to have additional encryption algorithms, etc.)
                addJceProvider("BC", "org.bouncycastle.jce.provider.BouncyCastleProvider");
                return true;
            }
        });
    }

    private static void addXMLDSigRI() {
        try {
            Class<?> clazz = SecurityActions
                    .loadClass(XMLSignatureUtil.class, "org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI");
            if (clazz == null)
                throw logger.classNotLoadedError("org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI");
            addJceProvider("ApacheXMLDSig", (Provider) clazz.newInstance());
        } catch (Throwable t) {
            // ignore - may be a NoClassDefFound if XMLDSigRI isn't avail
            return;
        }
    }

    /**
     * Add a new JCE security provider to use for PicketLink.
     *
     * @param name The name string of the provider (this may not be the real name of the provider)
     * @param provider A subclass of <code>java.security.Provider</code>
     *
     * @return Returns the actual name of the provider that was loaded
     */
    private static String addJceProvider(String name, Provider provider) {
        Provider currentProvider = Security.getProvider(name);
        if (currentProvider == null) {
            try {
                //
                // Install the provider after the SUN provider (see WSS-99)
                // Otherwise fall back to the old behaviour of inserting
                // the provider in position 2. For AIX, install it after
                // the IBMJCE provider.
                //
                int ret = 0;
                Provider[] provs = Security.getProviders();
                for (int i = 0; i < provs.length; i++) {
                    if ("SUN".equals(provs[i].getName()) || "IBMJCE".equals(provs[i].getName())) {
                        ret = Security.insertProviderAt(provider, i + 2);
                        break;
                    }
                }
                if (ret == 0) {
                    ret = Security.insertProviderAt(provider, 2);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("The provider " + provider.getName() + " - " + provider.getVersion() + " was added at position: "
                            + ret);
                }
                return provider.getName();
            } catch (Throwable t) {
                if (logger.isDebugEnabled()) {
                    logger.jceProviderCouldNotBeLoaded(name, t);
                }
                return null;
            }
        }
        return currentProvider.getName();
    }

    private static String addJceProvider(String name, String className) {
        Provider currentProvider = Security.getProvider(name);
        if (currentProvider == null) {
            try {
                // Class<? extends Provider> clazz = Loader.loadClass(className, false, Provider.class);
                Class<? extends Provider> clazz = Class.forName(className).asSubclass(Provider.class);
                Provider provider = clazz.newInstance();
                return addJceProvider(name, provider);
            } catch (Throwable t) {
                if (logger.isDebugEnabled()) {
                    logger.jceProviderCouldNotBeLoaded(name, t);
                }
                return null;
            }
        }
        return currentProvider.getName();
    }
}
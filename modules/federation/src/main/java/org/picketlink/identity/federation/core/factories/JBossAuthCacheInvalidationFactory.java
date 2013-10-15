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

import org.jboss.logging.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.security.Principal;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * <p>
 * A factory that is used to obtain an expiration policy of type {@link TimeCacheExpiry}
 * </p>
 * <p>
 * Primarily used to expire the SAML Principal in the JAAS Subject cached in the JBoss Auth Cache.
 * </p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Feb 7, 2011
 */
public class JBossAuthCacheInvalidationFactory {

    /**
     * Get an instance of {@link TimeCacheExpiry}
     *
     * @return
     */
    public static TimeCacheExpiry getCacheExpiry() {
        return ExpiringPrincipalCacheInvalidation.get();
    }

    public interface TimeCacheExpiry {

        /**
         * Register a Principal that has an expiry at {@link Date}
         *
         * @param securityDomain the security domain under which the principal may be cached in a subject
         * @param expiry when to expire the principal and hence the subject
         * @param principal the principal which needs to be expired
         */
        void register(String securityDomain, Date expiry, Principal principal);
    }

    protected static class ExpiringPrincipalCacheInvalidation implements TimeCacheExpiry {

        private static Logger log = Logger.getLogger(ExpiringPrincipalCacheInvalidation.class);

        private final boolean trace = log.isTraceEnabled();

        protected static ExpiringPrincipalCacheInvalidation _instance = null;

        protected static String objectName = "jboss.security:service=JaasSecurityManager";

        protected static Timer timer = new Timer();

        protected ExpiringPrincipalCacheInvalidation() {
        }

        protected static ExpiringPrincipalCacheInvalidation get() {
            if (_instance == null)
                _instance = new ExpiringPrincipalCacheInvalidation();
            return _instance;
        }

        protected static void setObjectName(String oName) {
            objectName = oName;
        }

        public void register(final String securityDomain, final Date expiry, final Principal principal) {
            try {
                timer.purge();
            } catch (Exception e) {
                if (trace) {
                    log.trace("Exception in purging timer tasks:", e);
                }
            }
            try {
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            ObjectName on = new ObjectName(objectName);
                            MBeanServer server = SecurityActions.getJBossMBeanServer();
                            Object[] obj = new Object[]{securityDomain, principal};
                            String[] sig = new String[]{"java.lang.String", "java.security.Principal"};

                            // Flush the Authentication Cache
                            server.invoke(on, "flushAuthenticationCache", obj, sig);
                        } catch (Exception e) {
                            if (trace) {
                                log.trace("Exception in scheduling timer:", e);
                            }
                        }
                    }
                }, expiry);
            } catch (Exception e) {
                if (trace) {
                    log.trace("Exception in scheduling timer:", e);
                }
            }
        }
    }
}
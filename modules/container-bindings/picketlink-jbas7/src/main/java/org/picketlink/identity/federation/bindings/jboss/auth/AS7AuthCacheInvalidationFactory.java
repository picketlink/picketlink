package org.picketlink.identity.federation.bindings.jboss.auth;

import java.security.Principal;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.naming.InitialContext;

import org.jboss.security.CacheableManager;
import org.jboss.security.SecurityConstants;
import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.identity.federation.core.factories.JBossAuthCacheInvalidationFactory.TimeCacheExpiry;

public class AS7AuthCacheInvalidationFactory {

    public static TimeCacheExpiry getCacheExpiry() {
        return AS7ExpiringPrincipalCacheInvalidation.get();
    }

    protected static class AS7ExpiringPrincipalCacheInvalidation implements TimeCacheExpiry {

        private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

        protected static Timer timer = new Timer();

        protected static AS7ExpiringPrincipalCacheInvalidation _instance = null;

        protected AS7ExpiringPrincipalCacheInvalidation() {
        }

        protected static AS7ExpiringPrincipalCacheInvalidation get() {
            if (_instance == null)
                _instance = new AS7ExpiringPrincipalCacheInvalidation();
            return _instance;
        }

        public void register(final String securityDomain, final Date expiry, final Principal principal) {
            try {
                timer.purge();
            } catch (Exception e) {
                logger.trace("Exception in purging timer tasks:", e);
            }
            try {
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            String lookupDomain = securityDomain;
                            if (lookupDomain.startsWith(SecurityConstants.JAAS_CONTEXT_ROOT) == false)
                                lookupDomain = SecurityConstants.JAAS_CONTEXT_ROOT + "/" + lookupDomain;

                            // lookup the JBossCachedAuthManager.
                            InitialContext context = new InitialContext();
                            CacheableManager manager = (CacheableManager) context.lookup(lookupDomain);

                            // Flush the Authentication Cache
                            manager.flushCache(principal);
                        } catch (Exception e) {
                            logger.trace("Exception in scheduling timer:", e);
                        }
                    }
                }, expiry);
            } catch (Exception e) {
                logger.trace("Exception in scheduling timer:", e);
            }
        }
    }
}

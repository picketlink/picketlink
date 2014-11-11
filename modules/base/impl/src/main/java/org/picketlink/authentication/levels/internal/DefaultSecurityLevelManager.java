package org.picketlink.authentication.levels.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.picketlink.annotations.PicketLink;
import org.picketlink.authentication.levels.Level;
import org.picketlink.authentication.levels.SecurityLevelManager;
import org.picketlink.authentication.levels.SecurityLevelResolver;
import org.picketlink.authentication.levels.annotations.DefaultSecurityLevel;

/**
 * Resolves security level from all resolvers.
 *
 * @author Michal Trnka
 */
@ApplicationScoped
public class DefaultSecurityLevelManager implements SecurityLevelManager {

    private Level defaultSecurityLevel;

    @Inject
    @PicketLink
    @DefaultSecurityLevel
    private Instance<Level> levelInstance;

    @Inject
    @PicketLink
    @Any
    private Instance<SecurityLevelResolver> resolverInstances;

    @Inject
    private AsynchronousResolverProcessor asynchronousResolver;


    @PostConstruct
    public void init() {
        if (levelInstance.isUnsatisfied()) {
            defaultSecurityLevel = new DefaultLevel(DEFAULT_SECURITY_LEVEL);
        } else {
            defaultSecurityLevel = levelInstance.get();
        }
    }

    public Level resolveSecurityLevel() {
        Level highestLevel = defaultSecurityLevel;
        Set<Future<Level>> set = new HashSet<Future<Level>>();

        for (SecurityLevelResolver resolver : resolverInstances) {
            set.add(asynchronousResolver.processResolver(resolver));
        }

        for (Future<Level> result : set) {
            Level level = null;
            try {
                level = result.get();
            } catch (Exception e) {
                set = null;
                highestLevel = defaultSecurityLevel;
                return synchronousResolve();
            }

            if (highestLevel.compareTo(level)<0) {
                highestLevel = level;
            }
        }
        return highestLevel;
    }

    private Level synchronousResolve() {
        Level highestLevel = defaultSecurityLevel;

        for (SecurityLevelResolver resolver : resolverInstances) {
            Level level = resolver.resolve();
            if (highestLevel.compareTo(level) < 0) {
                highestLevel = level;
            }
        }

        return highestLevel;
    }
}

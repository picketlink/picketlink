package org.picketlink.authentication.levels.internal;

import org.picketlink.authentication.levels.Level;
import org.picketlink.authentication.levels.SecurityLevelManager;
import org.picketlink.authentication.levels.SecurityLevelResolver;
import org.picketlink.authentication.levels.annotations.DefaultSecurityLevel;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Resolves security level from all resolvers.
 *
 * @author Michal Trnka
 */
@ApplicationScoped
public class DefaultSecurityLevelManager implements SecurityLevelManager {

    private Level defaultSecurityLevel;

    @Inject
    @DefaultSecurityLevel
    private Instance<Level> levelInstance;

    @Inject
    @Any
    private Instance<SecurityLevelResolver> resolverInstances;

    @Inject
    public void init() {
        if (levelInstance.isUnsatisfied()) {
            defaultSecurityLevel = new DefaultLevel(DEFAULT_SECURITY_LEVEL);
        } else {
            defaultSecurityLevel = levelInstance.get();
        }
    }

    public Level resolveSecurityLevel() {
        //TODO: support async level resolution
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

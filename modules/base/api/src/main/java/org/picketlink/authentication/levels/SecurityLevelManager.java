package org.picketlink.authentication.levels;

/**
 * Resolves security level from given resolvers.
 *
 * @author Michal Trnka
 */
public interface SecurityLevelManager {

    int DEFAULT_SECURITY_LEVEL = 1;

    /**
     * Resolves current Level for the user. It will return the highest Level resolved.
     *
     * @return Representation of the current Level for the user
     */
    Level resolveSecurityLevel();

}

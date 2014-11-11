package org.picketlink.authentication.levels;

/**
 * Interface for creating own security level resolvers. Application can have multiple resolvers.
 *
 * @author Michal Trnka
 *
 */
public interface SecurityLevelResolver {

    /**
     * Resolves the Level
     *
     * @return Instance of the resolved Level or null if the Level can not be resolved
     */
    Level resolve();
}

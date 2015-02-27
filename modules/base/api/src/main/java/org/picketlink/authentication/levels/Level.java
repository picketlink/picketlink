package org.picketlink.authentication.levels;

import java.io.Serializable;

/**
 * <p>Representation of the security level in the application</p>
 *
 * @author Michal Trnka
 */
public interface Level extends Comparable<Level>, Serializable {

    /**
     * Compares current level to a given level
     */
    @Override
    int compareTo(Level level);
}

package org.picketlink.authentication.levels;

/**
 * <p>Interface for the LevelFactories</p>
 *
 * @author Michal Trnka
 */
public interface LevelFactory {

    /**
     * Created Level instance from a string. Used for example in EL or for creating of the Level from annotations.
     *
     * @param level Representation of the level
     */
    Level createLevel(String level);
}

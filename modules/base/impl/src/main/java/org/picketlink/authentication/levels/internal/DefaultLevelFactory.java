package org.picketlink.authentication.levels.internal;

import org.picketlink.authentication.levels.Level;

import org.picketlink.authentication.levels.LevelFactory;
/**
 * Factory for the DefaultLevel
 *
 * @author Michal Trnka
 */
public class DefaultLevelFactory implements LevelFactory{

    @Override
    public Level createLevel(String level) {
        return new DefaultLevel(Integer.parseInt(level));
    }

}

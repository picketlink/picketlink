package org.picketlink.authentication.levels.internal;

import org.picketlink.authentication.levels.Level;
import org.picketlink.authentication.levels.SecurityLevelsMismatchException;

/**
 * Default representation of the Level
 *
 * @author Michal Trnka
 */
public class DefaultLevel implements Level {

    private static final long serialVersionUID = -874421685922107606L;

    int value;

    public DefaultLevel(int val){
        value = val;
    }

    @Override
    public int compareTo(Level level) {
        if(level == null){
            return 1;
        }
        else if(level instanceof DefaultLevel){
            DefaultLevel defLevel = (DefaultLevel)level;
            return Integer.compare(value, defLevel.getValue());
        }else{
            throw new SecurityLevelsMismatchException("More instances of SecurityLevel in the application");
        }
    }

    public int getValue(){
        return value;
    }

    public String toString(){
        return ""+value;
    }

}

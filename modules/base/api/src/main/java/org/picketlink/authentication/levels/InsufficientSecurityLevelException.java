package org.picketlink.authentication.levels;

import org.picketlink.common.exceptions.PicketLinkException;

/**
 * <p>Thrown when the action which is invoked needs higher security level.</p>
 *
 * @author Michal Trnka
 */
public class InsufficientSecurityLevelException extends PicketLinkException{

    private static final long serialVersionUID = 101667538978437287L;

    private Level level;

    public InsufficientSecurityLevelException(Level level, String msg){
        super(msg);
        this.level = level;
    }

    /**
     * @return level which was required
     */
    public Level getLevel(){
        return level;
    }
}

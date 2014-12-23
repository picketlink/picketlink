package org.picketlink.authentication.levels;

import org.picketlink.common.exceptions.PicketLinkException;

/**
 * <p>Thrown when comparing levels and they do not match.</p>
 *
 * @author Michal Trnka
 */
public class SecurityLevelsMismatchException extends PicketLinkException{
    private static final long serialVersionUID = 898667538978437098L;

    public SecurityLevelsMismatchException(String msg){
        super(msg);
    }
}

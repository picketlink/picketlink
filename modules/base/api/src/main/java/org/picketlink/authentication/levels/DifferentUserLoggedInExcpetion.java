package org.picketlink.authentication.levels;

import org.picketlink.authentication.AuthenticationException;

/**
 * <p>
 * Thrown during the authentication process to indicate that an user trying to raise his level is
 * different from the currently logged one.
 * </p>
 *
 * @author Michal Trnka
 */
public class DifferentUserLoggedInExcpetion extends AuthenticationException {

    private static final long serialVersionUID = 155666586933387287L;

    public DifferentUserLoggedInExcpetion(String message) {
        super(message);
    }
}

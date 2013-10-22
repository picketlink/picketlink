package org.picketlink.common.logging;

import org.jboss.logging.Logger;

/**
 * <p>Factory that creates {@link Log} instances.</p>
 *
 * <p>Classes should always prefer to use this class to obtain logger instances.</p>
 *
 * @author Pedro Igor
 */
public class LogFactory {

    /**
     * <p>Create an {@link Log} instance.</p>
     *
     * @param logClass Usually an interface that defines logging messages and methods.
     * @param category The logger category.
     *
     * @return
     */
    public static <T extends Log> T getLog(Class<T> logClass, String category) {
        return Logger.getMessageLogger(logClass, category);
    }

}

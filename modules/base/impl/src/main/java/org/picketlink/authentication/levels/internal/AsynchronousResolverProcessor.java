package org.picketlink.authentication.levels.internal;

import java.util.concurrent.Future;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;

import org.picketlink.authentication.levels.Level;
import org.picketlink.authentication.levels.SecurityLevelResolver;

/**
 * Used in {@link DefaultSecurityLevelManager} for processing resolvers asynchronously
 *
 * @author Michal Trnka
 *
 */
@Asynchronous
public class AsynchronousResolverProcessor{

    public Future<Level> processResolver(SecurityLevelResolver resolver) {
        Future<Level> result = new AsyncResult<Level>(resolver.resolve());
        return result;
    }
}

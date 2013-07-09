package org.picketlink.idm;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface IdentityContextFactory {
    IdentityContext createIdentityContext();
    void close();

}

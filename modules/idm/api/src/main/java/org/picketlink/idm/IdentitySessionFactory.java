package org.picketlink.idm;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface IdentitySessionFactory {
    IdentitySession createIdentitySession();
    void close();

}

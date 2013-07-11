package org.picketlink.idm.spi;

import org.picketlink.idm.IdentitySession;

/**
 * SPI Interface used to control transactions on resource-local identity stores
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface IdentitySessionHandler {
    void begin(IdentitySession context, IdentityStore<?> store);
    void commit(IdentitySession context, IdentityStore<?> store);
    void rollback(IdentitySession context, IdentityStore<?> store);
    void setRollbackOnly(IdentitySession context, IdentityStore<?> store);

    void initialize(IdentitySession identityContext, SecurityContext context, IdentityStore<?> store);

    void initialize(IdentitySession context, IdentityStore<?> store);
    void close(IdentitySession context, IdentityStore<?> store);

    void initialize();
    void close();
}

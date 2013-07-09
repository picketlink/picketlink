package org.picketlink.idm.spi;

import org.picketlink.idm.IdentityContext;

/**
 * SPI Interface used to control transactions on resource-local identity stores
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface IdentityContextHandler {
    void begin(IdentityContext context, IdentityStore<?> store);
    void commit(IdentityContext context, IdentityStore<?> store);
    void rollback(IdentityContext context, IdentityStore<?> store);
    void setRollbackOnly(IdentityContext context, IdentityStore<?> store);

    void initialize(IdentityContext identityContext, SecurityContext context, IdentityStore<?> store);

    void initialize(IdentityContext context, IdentityStore<?> store);
    void close(IdentityContext context, IdentityStore<?> store);

    void initialize();
    void close();
}

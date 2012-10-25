package org.picketlink.idm.spi;

import javax.persistence.EntityManager;

/**
 * Extends the IdentityStoreSession interface with EntityManager support for use within JPA based IdentityStore implementations
 * 
 * @author Shane Bryzak
 *
 */
public interface JPAIdentityStoreSession extends IdentityStoreSession {
    EntityManager getEntityManager();
}

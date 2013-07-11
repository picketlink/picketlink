package org.picketlink.idm.jpa.internal;

import org.picketlink.idm.IdentitySession;
import org.picketlink.idm.spi.IdentitySessionHandler;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.SecurityContext;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ResourceLocalJpaIdentitySessionHandler implements IdentitySessionHandler {

    protected EntityManagerFactory emf;

    private static class PropertyKey {
        private String name;
        private IdentityStore store;

        private PropertyKey(String name, IdentityStore store) {
            this.name = name;
            this.store = store;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PropertyKey that = (PropertyKey) o;

            if (!name.equals(that.name)) return false;
            if (!store.equals(that.store)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + store.hashCode();
            return result;
        }
    }

    public ResourceLocalJpaIdentitySessionHandler(String persistentUnit) {
        this.emf = Persistence.createEntityManagerFactory(persistentUnit);
    }

    public ResourceLocalJpaIdentitySessionHandler(EntityManagerFactory emf) {
        this.emf = emf;
    }

    protected EntityManager getEntityManager(IdentitySession context, IdentityStore store) {
        PropertyKey key = new PropertyKey(EntityManager.class.getName(), store);
        return (EntityManager)context.getProperties().get(key);

    }

    @Override
    public void begin(IdentitySession context, IdentityStore<?> store) {
       getEntityManager(context, store).getTransaction().begin();
    }

    @Override
    public void commit(IdentitySession context, IdentityStore<?> store) {
        getEntityManager(context, store).getTransaction().commit();
    }

    @Override
    public void rollback(IdentitySession context, IdentityStore<?> store) {
        getEntityManager(context, store).getTransaction().rollback();
    }

    @Override
    public void setRollbackOnly(IdentitySession context, IdentityStore<?> store) {
        getEntityManager(context, store).getTransaction().setRollbackOnly();

    }

    @Override
    public void initialize(IdentitySession identityContext, SecurityContext context, IdentityStore<?> store) {
        EntityManager entityManager = getEntityManager(identityContext, store);
        context.setParameter(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER, entityManager);
    }

    @Override
    public void initialize(IdentitySession context, IdentityStore<?> store) {
        EntityManager em = emf.createEntityManager();
        PropertyKey key = new PropertyKey(EntityManager.class.getName(), store);
        context.getProperties().put(key, em);

    }

    @Override
    public void close(IdentitySession context, IdentityStore<?> store) {
        getEntityManager(context, store).close();
    }

    @Override
    public void initialize() {

    }

    @Override
    public void close() {
        emf.close();
    }
}

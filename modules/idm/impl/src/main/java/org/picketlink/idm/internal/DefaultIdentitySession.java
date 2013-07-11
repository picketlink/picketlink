package org.picketlink.idm.internal;

import org.picketlink.idm.IdentitySession;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.IdentityTransaction;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Tier;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.SecurityContext;
import org.picketlink.idm.spi.SecurityContextFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class DefaultIdentitySession implements IdentitySession {
    protected IdentityConfiguration identityConfig;
    protected SecurityContextFactory contextFactory;
    protected DefaultStoreFactory2 storeFactory;
    protected Map<Object, Object> properties = new HashMap<Object, Object>();
    protected IdentityStoreConfiguration partitionStore;
    protected List<IdentityStore> storesWithHandlers = new ArrayList<IdentityStore>();
    protected IdentityTransaction identityTransaction = new IdentityTransaction() {
        protected boolean setRollback;
        protected boolean active;

        @Override
        public void begin() {
            active = true;
            for (IdentityStore store : storesWithHandlers) {
                store.getConfig().getIdentitySessionHandler().begin(DefaultIdentitySession.this, store);
            }
        }

        @Override
        public void commit() {
            active = false;
            for (IdentityStore store : storesWithHandlers) {
                store.getConfig().getIdentitySessionHandler().commit(DefaultIdentitySession.this, store);
            }

        }

        @Override
        public void rollback() {
            active = false;
            for (IdentityStore store : storesWithHandlers) {
                store.getConfig().getIdentitySessionHandler().rollback(DefaultIdentitySession.this, store);
            }
        }

        @Override
        public void setRollbackOnly() {
            setRollback = true;
            for (IdentityStore store : storesWithHandlers) {
                store.getConfig().getIdentitySessionHandler().setRollbackOnly(DefaultIdentitySession.this, store);
            }

        }

        @Override
        public boolean getRollbackOnly() {
            return setRollback;
        }

        @Override
        public boolean isActive() {
            return active;
        }
    };
    protected static final Class[] IDENTITY_MANAGER_PROXY_INTERFACES = new Class[]{IdentityManager.class};

    protected class IdmProxyHandler implements InvocationHandler {

        protected IdentityManager delegate;

        public IdmProxyHandler(IdentityManager delegate) {
            this.delegate = delegate;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            boolean wasActive = identityTransaction.isActive();
            if (!wasActive) {
                identityTransaction.begin();
            }
            Object rtn = null;
            Throwable exc = null;
            try {
                rtn = method.invoke(delegate, args);
                if (!wasActive) identityTransaction.commit();
                return rtn;
            } catch (InvocationTargetException e) {
                exc = e.getCause();
            } catch (Throwable e) {
                exc = e;
            }
            if (!wasActive) {
                identityTransaction.rollback();
            } else {
                identityTransaction.setRollbackOnly();
            }
            throw exc;
        }
    }

    public DefaultIdentitySession(IdentityConfiguration identityConfig, SecurityContextFactory contextFactory, DefaultStoreFactory2 storeFactory) {
        this.identityConfig = identityConfig;
        this.contextFactory = contextFactory;
        this.storeFactory = storeFactory;
        this.partitionStore = storeFactory.getPartitionStoreConfig();
        for (IdentityStoreConfiguration config : identityConfig.getConfiguredStores()) {
            if (config.getIdentitySessionHandler() != null) {
                storesWithHandlers.add(storeFactory.createIdentityStore(config, null));
            }
        }
        for (IdentityStore store : storesWithHandlers) {
            store.getConfig().getIdentitySessionHandler().initialize(this, store);
        }

    }

    @Override
    public IdentityTransaction getTransaction() {
        return identityTransaction;
    }

    @Override
    public Map<Object, Object> getProperties() {
        return properties;
    }

    @Override
    public IdentityManager createIdentityManager(Partition partition) {
        if (partition == null) {
            throw MESSAGES.nullArgument("Partition");
        }

        try {
            SecurityContext context = contextFactory.createContext(partition);

            initSecurityContext(context);
            IdentityManager delegate = new DefaultIdentityManager(context, storeFactory);
            return (IdentityManager)Proxy.newProxyInstance(IdentityManager.class.getClassLoader(), IDENTITY_MANAGER_PROXY_INTERFACES, new IdmProxyHandler(delegate) );
        } catch (Exception e) {
            throw MESSAGES.couldNotCreateContextualIdentityManager(partition);
        }
    }

    protected void initSecurityContext(SecurityContext context) {
        context.setParameter(IdentitySession.class.getName(), this);
        for (IdentityStore store : storesWithHandlers) {
            store.getConfig().getIdentitySessionHandler().initialize(this, context, store);
        }
    }

    @Override
    public IdentityManager defaultIdentityManager() {
        Realm defaultRealm = findRealm(Realm.DEFAULT_REALM);

        if (defaultRealm == null) {
            throw MESSAGES.configurationDefaultRealmNotDefined();
        }

        return createIdentityManager(defaultRealm);
    }

    @Override
    public Realm createRealm(String name) {
        boolean wasActive = identityTransaction.isActive();
        if (!wasActive) {
            identityTransaction.begin();
        }
        try {
            SecurityContext context = contextForPartitionManagement();
            Realm realm = storeFactory.createRealm(context, name);
            if (!wasActive) identityTransaction.commit();
            return realm;
        } catch (RuntimeException e) {
            if (!wasActive) {
                identityTransaction.rollback();
            } else {
                identityTransaction.setRollbackOnly();
            }
            throw e;
        }
    }

    @Override
    public Tier createTier(String name) {
        boolean wasActive = identityTransaction.isActive();
        if (!wasActive) {
            identityTransaction.begin();
        }
        try {
            SecurityContext context = contextForPartitionManagement();
            Tier tier = storeFactory.createTier(context, name);
            if (!wasActive) identityTransaction.commit();
            return tier;
        } catch (RuntimeException e) {
            if (!wasActive) {
                identityTransaction.rollback();
            } else {
                identityTransaction.setRollbackOnly();
            }
            throw e;
        }
    }

    protected SecurityContext contextForPartitionManagement() {
        SecurityContext context = contextFactory.createContext(null);
        initSecurityContext(context);
        return context;
    }

    protected SecurityContext contextForPartitionManagement(Partition partition) {
        SecurityContext context = contextFactory.createContext(partition);
        initSecurityContext(context);
        return context;
    }


    @Override
    public Realm findRealm(String name) {
        boolean wasActive = identityTransaction.isActive();
        if (!wasActive) {
            identityTransaction.begin();
        }
        try {
            SecurityContext context = contextForPartitionManagement();
            Realm realm = storeFactory.findRealm(context, name);
            if (!wasActive) identityTransaction.commit();
            return realm;
        } catch (RuntimeException e) {
            if (!wasActive) {
                identityTransaction.rollback();
            } else {
                identityTransaction.setRollbackOnly();
            }
            throw e;
        }
    }

    @Override
    public Tier findTier(String name) {
        boolean wasActive = identityTransaction.isActive();
        if (!wasActive) {
            identityTransaction.begin();
        }
        try {
            SecurityContext context = contextForPartitionManagement();
            Tier tier = storeFactory.findTier(context, name);
            if (!wasActive) identityTransaction.commit();
            return tier;
        } catch (RuntimeException e) {
            if (!wasActive) {
                identityTransaction.rollback();
            } else {
                identityTransaction.setRollbackOnly();
            }
            throw e;
        }
    }

    @Override
    public void deleteRealm(Realm realm) {
        boolean wasActive = identityTransaction.isActive();
        if (!wasActive) {
            identityTransaction.begin();
        }
        try {
            SecurityContext context = contextForPartitionManagement(realm);
            storeFactory.deleteRealm(context, realm);
            if (!wasActive) identityTransaction.commit();
        } catch (RuntimeException e) {
            if (!wasActive) {
                identityTransaction.rollback();
            } else {
                identityTransaction.setRollbackOnly();
            }
            throw e;
        }

    }

    @Override
    public void deleteTier(Tier tier) {
        boolean wasActive = identityTransaction.isActive();
        if (!wasActive) {
            identityTransaction.begin();
        }
        try {
            SecurityContext context = contextForPartitionManagement(tier);
            storeFactory.deleteTier(context, tier);
            if (!wasActive) identityTransaction.commit();
        } catch (RuntimeException e) {
            if (!wasActive) {
                identityTransaction.rollback();
            } else {
                identityTransaction.setRollbackOnly();
            }
            throw e;
        }

    }

    @Override
    public void close() {
        for (IdentityStore store : storesWithHandlers) {
            store.getConfig().getIdentitySessionHandler().close(this, store);
        }
    }
}

package org.picketlink.idm.credential.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.CredentialHandlerFactory;
import org.picketlink.idm.credential.spi.annotations.SupportsCredentials;
import org.picketlink.idm.credential.spi.annotations.SupportsStores;
import org.picketlink.idm.jpa.internal.JPAPlainTextPasswordCredentialHandler;
import org.picketlink.idm.ldap.internal.LDAPPlainTextPasswordCredentialHandler;
import org.picketlink.idm.spi.IdentityStore;

/**
 * A basic implementation of CredentialHandlerFactory that is pre-configured with the built-in
 * CredentialHandlers, and allows registration of additional handlers. 
 *  
 * @author Shane Bryzak
 */
public class DefaultCredentialHandlerFactory implements CredentialHandlerFactory {

    /**
     * 
     */
    Set<CredentialHandler> defaultHandlers = new HashSet<CredentialHandler>();

    /**
     * 
     */
    Set<CredentialHandler> registeredHandlers = new HashSet<CredentialHandler>();
    
    private Map<Class<? extends CredentialHandler>, List<Class<? extends IdentityStore>>> additionalStores = new HashMap<Class<? extends CredentialHandler>, List<Class<? extends IdentityStore>>>();


    public DefaultCredentialHandlerFactory() {
        defaultHandlers.add(new JPAPlainTextPasswordCredentialHandler());
        defaultHandlers.add(new LDAPPlainTextPasswordCredentialHandler());
        defaultHandlers.add(new X509CertificateCredentialHandler());
    }

    /**
     * Register the specified credential handler
     * 
     * @param handler
     */
    public void registerHandler(CredentialHandler handler) {
        registeredHandlers.add(handler);
    }
    
    /**
     * Register the specified {@link IdentityStore} to support the given {@link CredentialHandler}. 
     * 
     * @param handler
     */
    public void registerIdentityStore(Class<? extends CredentialHandler> handler, Class<? extends IdentityStore> identityStore) {
        List<Class<? extends IdentityStore>> supportedStores = this.additionalStores.get(handler);
        
        if (supportedStores == null) {
            supportedStores = new ArrayList<Class<? extends IdentityStore>>();
            this.additionalStores.put(handler, supportedStores);
        }
        
        supportedStores.add(identityStore);
    }

    @Override
    public CredentialHandler getCredentialValidator(Class<? extends Credentials> credentialsClass, Class<? extends IdentityStore> identityStoreClass) {
        for (CredentialHandler handler : registeredHandlers) {
            if (handlerSupports(handler, credentialsClass, identityStoreClass)) {
                return handler;
            }
        }

        for (CredentialHandler handler : defaultHandlers) {
            if (handlerSupports(handler, credentialsClass, identityStoreClass)) {
                return handler;
            }
        }
        
        return null;
    }

    @Override
    public CredentialHandler getCredentialUpdater(Class<?> credentialClass, Class<? extends IdentityStore> identityStoreClass) {
        for (CredentialHandler handler : registeredHandlers) {
            if (handlerSupports(handler, credentialClass, identityStoreClass)) {
                return handler;
            }
        }

        for (CredentialHandler handler : defaultHandlers) {
            if (handlerSupports(handler, credentialClass, identityStoreClass)) {
                return handler;
            }
        }

        return null;
    }

    private boolean handlerSupports(CredentialHandler handler, Class<?> credentialClass, 
            Class<? extends IdentityStore> identityStoreClass) {
        SupportsCredentials sc = handler.getClass().getAnnotation(SupportsCredentials.class);
        SupportsStores ss = handler.getClass().getAnnotation(SupportsStores.class);
        
        if (sc == null || ss == null) {
            return false;
        }
        
        for (Class<?> cls : sc.value()) {
            if (cls.equals(credentialClass)) {
                for (Class<? extends IdentityStore> isc : ss.value()) {
                    if (isc.equals(identityStoreClass)) {
                        return true;
                    }
                    
                    if (this.additionalStores.containsKey(handler.getClass())) {
                        List<Class<? extends IdentityStore>> stores = this.additionalStores.get(handler.getClass());
                        
                        for (Class<? extends IdentityStore> storeClass : stores) {
                            if (storeClass.equals(identityStoreClass)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

}

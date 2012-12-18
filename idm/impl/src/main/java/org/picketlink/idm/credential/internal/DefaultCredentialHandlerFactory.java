package org.picketlink.idm.credential.internal;

import java.util.HashSet;
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
                }
            }
        }

        return false;
    }

}

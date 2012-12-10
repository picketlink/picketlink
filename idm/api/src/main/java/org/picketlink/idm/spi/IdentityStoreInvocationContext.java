package org.picketlink.idm.spi;

import java.util.HashMap;
import java.util.Map;

import org.picketlink.idm.IdentityCache;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.CredentialHandlerFactory;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Tier;

/**
 * Used to pass contextual state to an IdentityStore during an identity management operation.
 * 
 * @author Shane Bryzak
 *
 */
public class IdentityStoreInvocationContext {

    /**
     * 
     */
    private IdentityCache cache;

    /**
     * 
     */
    private EventBridge eventBridge;

    /**
     * 
     */
    private Realm realm;

    /**
     * 
     */
    private Tier tier;

    /**
     * 
     */
    private CredentialHandlerFactory credentialHandlerFactory;

    /**
     * 
     */
    private Map<String,Object> parameters = new HashMap<String,Object>();

    public IdentityStoreInvocationContext(IdentityCache cache, EventBridge eventBridge, 
            CredentialHandlerFactory factory) {
        this.cache = cache;
        this.eventBridge = eventBridge;
        this.credentialHandlerFactory = factory;
    }

    /**
     * Returns a CredentialHandler instance capable of validating a credential of the specified
     * Credentials class, for the specified IdentityStore class
     * 
     * @param credentialsClass
     * @param identityStoreClass
     * @return
     */
    public CredentialHandler getCredentialValidator(Class<? extends Credentials> credentialsClass, 
            Class<? extends IdentityStore> identityStoreClass) {
        return credentialHandlerFactory.getCredentialValidator(credentialsClass, identityStoreClass);
    }

    /**
     * Returns a CredentialHandler instance capable of updating a credential of the specified
     * Credentials class, for the specified IdentityStore class
     * 
     * @param credentialClass
     * @param identityStoreClass
     * @return
     */
    public CredentialHandler getCredentialUpdater(Class<?> credentialClass, 
            Class<? extends IdentityStore> identityStoreClass) {
        return credentialHandlerFactory.getCredentialUpdater(credentialClass, identityStoreClass);
    }

    /**
     * Returns the cache for this invocation context.
     * 
     * @return
     */
    public IdentityCache getCache() {
        return cache;
    }

    /**
     * Returns the parameter value with the specified name
     * 
     * @return
     */
    public Object getParameter(String paramName) {
        return parameters.get(paramName);
    }

    /**
     * Returns a boolean indicating whether the parameter with the specified name has been set
     * 
     * @param paramName
     * @return
     */
    public boolean isParameterSet(String paramName) {
        return parameters.containsKey(paramName);
    }

    /**
     * Sets a parameter value
     * 
     * @param paramName
     * @param value
     */
    public void setParameter(String paramName, Object value) {
        parameters.put(paramName, value);
    }

    /**
     * 
     * @return
     */
    public EventBridge getEventBridge() {
        return eventBridge;
    }

    /**
     * Return the active Realm for this context
     * 
     * @return
     */
    public Realm getRealm() {
        return realm;
    }

    /**
     * Sets the active Realm for this context
     * 
     * @param realm
     */
    public void setRealm(Realm realm) {
        this.realm = realm;
    }

    /**
     * Return the active Tier for this context
     * 
     * @return
     */
    public Tier getTier() {
        return tier;
    }

    /**
     * Sets the active Tier for this context
     * 
     * @param tier
     */
    public void setTier(Tier tier) {
        this.tier = tier;
    }
}

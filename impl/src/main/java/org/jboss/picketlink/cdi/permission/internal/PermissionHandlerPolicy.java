package org.jboss.picketlink.cdi.permission.internal;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.picketlink.cdi.permission.PermissionHandler;
import org.jboss.picketlink.cdi.permission.annotations.Identifier;

/**
 * Manages a set of PermissionHandler instances that overall define a "policy" for 
 * how persistent resource permissions are managed.
 *
 * @author Shane Bryzak
 */
@ApplicationScoped
public class PermissionHandlerPolicy 
{
    private Map<Class<?>, PermissionHandler> handlers = new ConcurrentHashMap<Class<?>, PermissionHandler>();

    private Set<PermissionHandler> registeredHandlers = new HashSet<PermissionHandler>();

    @Inject
    public void create() 
    {
        if (registeredHandlers.isEmpty()) 
        {
            registeredHandlers.add(new EntityPermissionHandler());
            registeredHandlers.add(new ClassPermissionHandler());
        }
    }

    public String getGeneratedIdentifier(Object resource) 
    {
        if (resource instanceof String) 
        {
            return (String) resource;
        }

        PermissionHandler strategy = getStrategyForResource(resource);

        return strategy != null ? strategy.getGeneratedIdentifier(resource) : null;
    }
    
    public Map<String,Object> lookupResources(Collection<String> identifiers, Collection<Object> loadedResources)
    {
        Map<String,Object> resources = new HashMap<String,Object>();
        
        Map<String,Object> loadedIdentifiers = new HashMap<String,Object>();
        
        if (loadedResources != null && !loadedResources.isEmpty())
        {
            for (Object resource: loadedResources)
            {
                PermissionHandler strategy = getStrategyForResource(resource);
                
                if (strategy != null)
                {
                    String identifier = strategy.getGeneratedIdentifier(resource);
                    if (!loadedIdentifiers.containsKey(identifier))
                    {
                        loadedIdentifiers.put(identifier, resource);
                    }
                }
            }
        }
        
        for (String identifier : identifiers)
        {
            if (loadedIdentifiers.containsKey(identifier))
            {
                resources.put(identifier, loadedIdentifiers.get(identifier));
            }
            else
            {
                PermissionHandler strategy = getStrategyForIdentifier(identifier);
                if (strategy != null)
                {
                    Object resource = strategy.lookupResource(identifier);
                    if (resource != null)
                    {
                        resources.put(identifier, resource);    
                    }
                }
           }
        }
        
        return resources;
    }
    
    public Serializable getNaturalIdentifier(Object resource)
    {
        PermissionHandler strategy = getStrategyForResource(resource);
        return strategy != null ? strategy.getNaturalIdentifier(resource) : null;
    }
    
    private PermissionHandler getStrategyForIdentifier(String identifier)
    {
        for (PermissionHandler strategy : handlers.values())
        {
            if (strategy.canLoadResource(identifier))
            {
                return strategy;
            }
        }
        
        for (PermissionHandler strategy : registeredHandlers)
        {
            if (strategy.canLoadResource(identifier))
            {
                return strategy;
            }
        }
        
        return null;
    }
    
    private PermissionHandler getStrategyForResource(Object resource)
    {
        PermissionHandler strategy = handlers.get(resource.getClass());

        if (strategy == null) {
            if (resource.getClass().isAnnotationPresent(Identifier.class)) {
                Class<? extends PermissionHandler> strategyClass =
                        resource.getClass().getAnnotation(Identifier.class).value();

                if (strategyClass != PermissionHandler.class) {
                    try {
                        strategy = strategyClass.newInstance();
                        handlers.put(resource.getClass(), strategy);
                    } catch (Exception ex) {
                        throw new RuntimeException("Error instantiating IdentifierStrategy for object " + resource, ex);
                    }
                }
            }

            for (PermissionHandler s : registeredHandlers) {
                if (s.canHandle(resource.getClass())) {
                    strategy = s;
                    handlers.put(resource.getClass(), strategy);
                    break;
                }
            }
        }
        
        return strategy;
    }

    public Set<PermissionHandler> getRegisteredHandlers() {
        return registeredHandlers;
    }

    public void setRegisteredHandlers(Set<PermissionHandler> registeredHandlers) {
        this.registeredHandlers = registeredHandlers;
    }
}
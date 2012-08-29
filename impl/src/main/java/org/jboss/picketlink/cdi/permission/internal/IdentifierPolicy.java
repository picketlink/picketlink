package org.jboss.picketlink.cdi.permission.internal;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.picketlink.cdi.permission.IdentifierStrategy;
import org.jboss.picketlink.cdi.permission.annotations.Identifier;

/**
 * A policy for the generation of resource "identifiers" - unique Strings that identify a specific
 * resource.  A policy can consist of numerous identifier strategies, each with the
 * ability to generate identifiers for specific classes of resource.
 *
 * @author Shane Bryzak
 */
@ApplicationScoped
public class IdentifierPolicy 
{
    private Map<Class<?>, IdentifierStrategy> strategies = new ConcurrentHashMap<Class<?>, IdentifierStrategy>();

    private Set<IdentifierStrategy> registeredStrategies = new HashSet<IdentifierStrategy>();

    @Inject
    public void create() 
    {
        if (registeredStrategies.isEmpty()) 
        {
            registeredStrategies.add(new EntityIdentifierStrategy());
            registeredStrategies.add(new ClassIdentifierStrategy());
        }
    }

    public String getIdentifier(Object resource) 
    {
        if (resource instanceof String) 
        {
            return (String) resource;
        }

        IdentifierStrategy strategy = getStrategyForResource(resource);

        return strategy != null ? strategy.getIdentifier(resource) : null;
    }
    
    public Serializable getIdentifierValue(Object resource)
    {
        IdentifierStrategy strategy = getStrategyForResource(resource);
        return strategy != null ? strategy.getIdentifierValue(resource) : null;
    }
    
    private IdentifierStrategy getStrategyForResource(Object resource)
    {
        IdentifierStrategy strategy = strategies.get(resource.getClass());

        if (strategy == null) {
            if (resource.getClass().isAnnotationPresent(Identifier.class)) {
                Class<? extends IdentifierStrategy> strategyClass =
                        resource.getClass().getAnnotation(Identifier.class).value();

                if (strategyClass != IdentifierStrategy.class) {
                    try {
                        strategy = strategyClass.newInstance();
                        strategies.put(resource.getClass(), strategy);
                    } catch (Exception ex) {
                        throw new RuntimeException("Error instantiating IdentifierStrategy for object " + resource, ex);
                    }
                }
            }

            for (IdentifierStrategy s : registeredStrategies) {
                if (s.canIdentify(resource.getClass())) {
                    strategy = s;
                    strategies.put(resource.getClass(), strategy);
                    break;
                }
            }
        }
        
        return strategy;
    }

    public Set<IdentifierStrategy> getRegisteredStrategies() {
        return registeredStrategies;
    }

    public void setRegisteredStrategies(Set<IdentifierStrategy> registeredStrategies) {
        this.registeredStrategies = registeredStrategies;
    }
}
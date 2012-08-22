package org.jboss.picketlink.cdi.permission.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.spi.PersistenceProvider;

import org.jboss.picketlink.cdi.internal.util.Strings;
import org.jboss.picketlink.cdi.permission.IdentifierStrategy;
import org.jboss.picketlink.cdi.permission.annotations.Identifier;

/**
 * An Identifier strategy for entity-based permission checks
 *
 * @author Shane Bryzak
 */
public class EntityIdentifierStrategy implements IdentifierStrategy 
{
    private Map<Class<?>, String> identifierNames = new ConcurrentHashMap<Class<?>, String>();

    @Inject PersistenceProvider persistenceProvider;
    @Inject Instance<EntityManager> entityManager;

    public boolean canIdentify(Class<?> resourceClass) 
    {
        return resourceClass.isAnnotationPresent(Entity.class);
    }

    public String getIdentifier(Object target) 
    {
        return String.format("%s:%s", getIdentifierName(target.getClass()), 
                // FIXME need to return the correct Id value for the entity
                null);
//          persistenceProvider.getId(target, entityManager.get()).toString());
    }

    private String getIdentifierName(Class<?> cls) 
    {
        if (!identifierNames.containsKey(cls)) {
            String name = null;

            if (cls.isAnnotationPresent(Identifier.class)) 
            {
                Identifier identifier = (Identifier) cls.getAnnotation(Identifier.class);
                if (!Strings.isEmpty(identifier.name())) 
                {
                    name = identifier.name();
                }
            }

            if (name == null) 
            {
                name = cls.getName().substring(cls.getName().lastIndexOf('.') + 1);
            }

            identifierNames.put(cls, name);
            return name;
        }

        return identifierNames.get(cls);
    }
}

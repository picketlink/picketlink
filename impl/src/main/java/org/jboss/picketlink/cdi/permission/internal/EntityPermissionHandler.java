package org.jboss.picketlink.cdi.permission.internal;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;

import org.jboss.picketlink.cdi.internal.util.Strings;
import org.jboss.picketlink.cdi.internal.util.properties.Property;
import org.jboss.picketlink.cdi.internal.util.properties.query.AnnotatedPropertyCriteria;
import org.jboss.picketlink.cdi.internal.util.properties.query.PropertyQueries;
import org.jboss.picketlink.cdi.internal.util.properties.query.PropertyQuery;
import org.jboss.picketlink.cdi.permission.PermissionHandler;
import org.jboss.picketlink.cdi.permission.annotations.Identifier;

/**
 * An Identifier strategy for entity-based permission checks
 *
 * @author Shane Bryzak
 */
public class EntityPermissionHandler extends BaseAbstractPermissionHandler implements PermissionHandler 
{
    private Map<Class<?>, String> identifierNames = new ConcurrentHashMap<Class<?>, String>();
    private Map<Class<?>, Property<Serializable>> identifierProperties = new ConcurrentHashMap<Class<?>, Property<Serializable>>();

    @Inject Instance<EntityManager> entityManager;

    public boolean canHandle(Class<?> resourceClass) 
    {
        return resourceClass.isAnnotationPresent(Entity.class);
    }

    public String getGeneratedIdentifier(Object resource) 
    {
        return String.format("%s:%s", getIdentifierName(resource.getClass()),
                getNaturalIdentifier(resource));
    }
    
    public Serializable getNaturalIdentifier(Object resource)
    {
        Class<?> resourceClass = resource.getClass();
        
        if (!identifierProperties.containsKey(resourceClass))
        {
            PropertyQuery<Serializable> pq = PropertyQueries.createQuery(resource.getClass());
            pq.addCriteria(new AnnotatedPropertyCriteria(Id.class));
            identifierProperties.put(resourceClass, pq.getSingleResult());
        }
        
        Property<Serializable> p = identifierProperties.get(resourceClass);
        
        return p.getValue(resource);
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

    @Override
    public boolean canLoadResource(String identifier) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Object lookupResource(String identifier) {
        // TODO Auto-generated method stub
        return null;
    }
}

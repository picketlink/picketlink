package org.picketlink.cdi.permission.internal;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.Dependent;

import org.picketlink.cdi.permission.annotations.PermissionsHandledBy;
import org.picketlink.cdi.permission.spi.PermissionHandler;

/**
 * An Identifier strategy for class-based permission checks
 *
 * @author Shane Bryzak
 */
@Dependent
public class ClassPermissionHandler extends BaseAbstractPermissionHandler implements PermissionHandler 
{
    private Map<Class<?>, String> identifierNames = new ConcurrentHashMap<Class<?>, String>();

    public boolean canHandle(Class<?> resourceClass) 
    {
        return Class.class.equals(resourceClass);
    }

    public String getGeneratedIdentifier(Object resource) 
    {
        if (!(resource instanceof Class<?>)) 
        {
            throw new IllegalArgumentException("Resource [" + resource + "] must be instance of Class");
        }

        return getIdentifierName((Class<?>) resource);
    }

    public Serializable getNaturalIdentifier(Object resource) 
    {
        // The identifier value is the same as getIdentifier()
        return getGeneratedIdentifier(resource);
    }    
    
    private String getIdentifierName(Class<?> cls) 
    {
        if (!identifierNames.containsKey(cls)) 
        {
            String name = null;

            if (cls.isAnnotationPresent(PermissionsHandledBy.class)) 
            {
                PermissionsHandledBy handledBy = (PermissionsHandledBy) cls.getAnnotation(PermissionsHandledBy.class);
                if (handledBy.name() != null && !"".equals(handledBy.name().trim())) 
                {
                    name = handledBy.name();
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

package org.picketlink.idm.permission.acl.internal;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Entity;

import org.picketlink.idm.permission.internal.BaseAbstractPermissionHandler;

/**
 *
 * @author Shane Bryzak
 *
 */
public class EntityPermissionHandler extends BaseAbstractPermissionHandler {

    private Map<Class<?>, String> identifierNames = new ConcurrentHashMap<Class<?>, String>();

    @Override
    public boolean canHandle(Class<?> resourceClass) {
        return resourceClass.isAnnotationPresent(Entity.class);
    }

    @Override
    public boolean canLoadResource(String identifier) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getGeneratedIdentifier(Object resource) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Serializable getNaturalIdentifier(Object resource) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object lookupResource(String identifier) {
        // TODO Auto-generated method stub
        return null;
    }

    private String getIdentifierName(Class<?> cls) {
        if (!identifierNames.containsKey(cls)) {
            String name = null;

            /*if (cls.isAnnotationPresent(Identifier.class)) {
                Identifier identifier = (Identifier) cls.getAnnotation(Identifier.class);
                if (!Strings.isEmpty(identifier.name())) {
                    name = identifier.name();
                }
            }*/

            if (name == null) {
                name = cls.getName().substring(cls.getName().lastIndexOf('.') + 1);
            }

            identifierNames.put(cls, name);
            return name;
        }

        return identifierNames.get(cls);
    }

}

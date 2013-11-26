package org.picketlink.idm.permission.acl.spi;

import java.io.Serializable;
import java.lang.annotation.Annotation;

/**
 *
 * @author Shane Bryzak
 *
 */
public class EntityPermissionHandler extends BaseAbstractPermissionHandler {

    private Class<? extends Annotation> entityAnnotationClass = null;

    public EntityPermissionHandler() {
        try {
            entityAnnotationClass = (Class<? extends Annotation>) Class.forName("javax.persistence.Entity");
        } catch (ClassNotFoundException ex) {
            // Entity permissions not supported
        }
    }

    @Override
    public boolean canHandle(Class<?> resourceClass) {
        return entityAnnotationClass != null && resourceClass.isAnnotationPresent(entityAnnotationClass);
    }

    @Override
    public Serializable getIdentifier(Object resource) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<?> unwrapResourceClass(Object resource) {
        // TODO Auto-generated method stub
        return null;
    }


}

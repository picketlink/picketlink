package org.picketlink.idm.permission.acl.internal;

import java.io.Serializable;

import javax.persistence.Entity;

import org.picketlink.idm.permission.internal.BaseAbstractPermissionHandler;

/**
 *
 * @author Shane Bryzak
 *
 */
public class EntityPermissionHandler extends BaseAbstractPermissionHandler {

    @Override
    public boolean canHandle(Class<?> resourceClass) {
        return resourceClass.isAnnotationPresent(Entity.class);
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

package org.picketlink.idm.permission.spi;

import java.util.List;
import java.util.Set;

import org.picketlink.idm.permission.Permission;

/**
 * Permission Store interface
 *
 * @author Shane Bryzak
 *
 */
public interface PermissionStore {
    List<Permission> listPermissions(Object resource);

    List<Permission> listPermisisons(Object resource, String permission);

    List<Permission> listPermissions(Set<Object> resources, String permission);

    boolean grantPermission(Permission permission);

    boolean grantPermissions(List<Permission> permissions);

    boolean revokePermission(Permission permission);

    boolean revokePermissions(List<Permission> permissions);

    List<String> listAvailablePermissions(Object resource);

    void clearPermissions(Object resource);
}

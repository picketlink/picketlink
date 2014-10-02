package org.picketlink.idm;

import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.permission.Permission;

import java.io.Serializable;
import java.util.List;

/**
 * Manages all Permission Management related operations.
 *
 * @author Shane Bryzak
 */
public interface PermissionManager {

    /**
     * Return a list of all permissions for the specified resource.
     *
     * @param resource
     * @return
     */
    List<Permission> listPermissions(Object resource);

    /**
     * <p>Returns a list of all {@link org.picketlink.idm.permission.Permission} for the given {@link org.picketlink.idm.model.IdentityType}.</p>
     *
     * @param identityType
     * @return
     */
    List<Permission> listPermissions(IdentityType identityType);

    /**
     * Returns a list of all Permissions for the specified resource identifier
     *
     * @param resourceClass
     * @param identifier
     * @return
     */
    List<Permission> listPermissions(Class<?> resourceClass, Serializable identifier);

    /**
     * Return a list of all permissions for the specified resource, with the specified operation
     *
     * @param resource
     * @param operation
     * @return
     */
    List<Permission> listPermissions(Object resource, String operation);

    /**
     * Returns a list of all Permissions for the specified resource identifier, with the specified operation
     *
     * @param resource
     * @param operation
     *
     * @return
     */
    List<Permission> listPermissions(Class<?> resource, String operation);

    /**
     * Returns a list of all Permissions for the specified resource identifier, with the specified operation
     *
     * @param resourceClass
     * @param identifier
     * @param operation
     * @return
     */
    List<Permission> listPermissions(Class<?> resourceClass, Serializable identifier, String operation);

    /**
     * Grant the specified permission
     *
     * @param assignee
     * @param resource
     * @param operation
     *
     * @return boolean returns true if the permission was granted successfully
     */
    void grantPermission(IdentityType assignee, Object resource, String operation);

    /**
     * Revoke the specified permission
     *
     * @param resource
     * @param operation
     * @return
     */
    void revokePermission(IdentityType assignee, Object resource, String operation);

    /**
     * Revoke the specified permission
     *
     * @param resourceclass
     * @return
     */
    void revokePermission(IdentityType assignee, Class<?> resourceclass, String operation);

    /**
     * Remove all permissions for the specified resource
     *
     * @param resource
     */
    void clearPermissions(Object resource);
}

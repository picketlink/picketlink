package org.picketlink.permission;

import java.util.List;
import java.util.Set;

import org.picketlink.idm.model.IdentityType;
import org.picketlink.permission.spi.PermissionStore;

/**
 * API for querying object permissions
 * 
 * @author Shane Bryzak
 *
 */
public class PermissionQuery
{
    private Object resource;    
    private Set<Object> resources;
    private int offset;
    private int limit;
    private IdentityType recipient;
    
    private PermissionStore permissionStore;
    
    public PermissionQuery(PermissionStore permissionStore)
    {
        this.permissionStore = permissionStore;
    }
    
    public Object getResource()
    {
        return resource;
    }
    
    public PermissionQuery setResource(Object resource)
    {
        this.resource = resource;
        this.resources = null;
        return this;
    }
    
    public Set<Object> getResources()
    {
        return resources;
    }
    
    public PermissionQuery setResources(Set<Object> resources)
    {
        this.resources = resources;
        this.resource = null;
        return this;
    }
    
    public int getOffset() {
        return offset;
    }
    
    public PermissionQuery setOffset(int offset) {
        this.offset = offset;
        return this;
    }
    
    public int getLimit() {
        return limit;
    }
    
    public PermissionQuery setLimit(int limit) {
        this.limit = limit;
        return this;
    }
    
    public IdentityType getRecipient()
    {
        return recipient;
    }
    
    public PermissionQuery setRecipient(IdentityType recipient)
    {
        this.recipient = recipient;
        return this;
    }
    
    public List<Permission> getResultList() 
    {
        return permissionStore.getPermissions(this);
    }    
}

package org.picketlink.cdi.permission;

import java.util.List;
import java.util.Set;

import org.picketlink.cdi.permission.spi.PermissionStore;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.query.Range;

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
    private Range range;
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
    
    public Range getRange()
    {
        return range;
    }
    
    public PermissionQuery setRange(Range range)
    {
        this.range = range;
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

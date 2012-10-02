package org.picketlink.cdi.permission;

import org.picketlink.idm.model.IdentityType;

/**
 * Represents a specific permission grant for a domain object 
 * 
 * @author Shane Bryzak
 *
 */
public class Permission
{
    private Object resource;
    private IdentityType recipient;
    private String permission;
    
    public Permission(Object resource, IdentityType recipient, String permission)
    {
        this.resource = resource;
        this.recipient = recipient;
        this.permission = permission;
    }
    
    public Object getResource()
    {
        return resource;
    }
    
    public IdentityType getRecipient()
    {
        return recipient;        
    }
    
    public String getPermission()
    {
        return permission;
    }
}

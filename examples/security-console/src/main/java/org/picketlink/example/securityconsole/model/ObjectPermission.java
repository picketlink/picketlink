package org.jboss.picketlink.example.securityconsole.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.jboss.picketlink.cdi.permission.annotations.ACLIdentifier;
import org.jboss.picketlink.cdi.permission.annotations.ACLPermission;
import org.jboss.picketlink.cdi.permission.annotations.ACLRecipient;
import org.jboss.picketlink.cdi.permission.annotations.ACLStore;

@ACLStore
@Entity
public class ObjectPermission implements Serializable 
{
    private static final long serialVersionUID = 2386489292457994509L;
    
    @Id @GeneratedValue
    private Long id;
    
    @ACLIdentifier
    private String identifier;
    
    @ACLRecipient
    private String recipient;
    
    @ACLPermission
    private String permission;
    
    public Long getId()
    {
        return id;
    }
    
    public void setId(Long id)
    {
        this.id = id;
    }
    
    public String getIdentifier()
    {
        return identifier;
    }
    
    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }
    
    public String getRecipient()
    {
        return recipient;
    }
    
    public void setRecipient(String recipient)
    {
        this.recipient = recipient;
    }
    
    public String getPermission()
    {
        return permission;
    }
    
    public void setPermission(String permission)
    {
        this.permission = permission;
    }
}

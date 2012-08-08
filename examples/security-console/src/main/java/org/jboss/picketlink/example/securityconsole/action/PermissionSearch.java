package org.jboss.picketlink.example.securityconsole.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.Model;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ejb.Stateful;
import javax.inject.Inject;

import org.jboss.picketlink.cdi.permission.Permission;
import org.jboss.picketlink.cdi.permission.PermissionManager;
import org.jboss.picketlink.example.securityconsole.model.Customer;

@Stateful
@Model
public class PermissionSearch 
{
    private static final List<String> ENTITY_TYPES = Arrays.asList(new String[] {"Customer", "Project"});
    
    private String entityType = ENTITY_TYPES.get(0);
    
    @PersistenceContext
    private EntityManager em;
    
    private Object resource;
    private List<Permission> permissions;
    
    @Inject
    private PermissionManager permissionManager;
    
    public String getEntityType()
    {
        return entityType;
    }
    
    public void setEntityType(String entityType)
    {
        this.entityType = entityType;
    }
    
    public List<String> getEntityTypes()
    {
        return ENTITY_TYPES;
    }
    
    public List<Customer> getCustomers()
    {
        return em.createQuery("select C from Customer C").getResultList();
    }
    
    public Object getResource()
    {
        return resource;
    }
    
    public void setResource(Object resource)
    {
        this.resource = resource;
    }
    
    public List<Permission> getPermissions()
    {
        if (permissions == null && resource != null)
        {
            permissions = permissionManager.createPermissionQuery()
                    .setResource(resource)
                    .getResultList();
            
        }
        return permissions;
    }
}

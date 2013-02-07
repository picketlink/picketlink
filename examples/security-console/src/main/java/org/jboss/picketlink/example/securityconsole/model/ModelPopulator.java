package org.jboss.picketlink.example.securityconsole.model;

import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.permission.Permission;
import org.picketlink.permission.PermissionManager;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Populates the database with default values
 *
 */
@Stateless
public @Named class ModelPopulator 
{
    @PersistenceContext
    private EntityManager em;
    
    @Inject
    private PermissionManager pm;
    
    public void populate()
    {
        Customer c = new Customer();
        c.setFirstName("Shane");
        c.setLastName("Bryzak");
        em.persist(c);
        
        User u = new SimpleUser("shane");
        
        pm.grantPermission(new Permission(c, u, "read"));
        
        c = new Customer();
        c.setFirstName("John");
        c.setLastName("Smith");
        em.persist(c);
        
        
    }
}

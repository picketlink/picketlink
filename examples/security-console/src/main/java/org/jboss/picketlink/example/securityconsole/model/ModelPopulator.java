package org.jboss.picketlink.example.securityconsole.model;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.picketlink.cdi.permission.Permission;
import org.jboss.picketlink.cdi.permission.PermissionManager;
import org.jboss.picketlink.idm.model.SimpleUser;
import org.jboss.picketlink.idm.model.User;

/**
 * Populates the database with default values
 *
 */
@Stateless
public @Named class ModelPopulator 
{
    @PersistenceContext
    private EntityManager em;
    
    @Inject PermissionManager pm;
    
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

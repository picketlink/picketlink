package org.picketlink.test.permission.resource;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.picketlink.annotations.PicketLink;

@RequestScoped
public class Resources {
    
    @SuppressWarnings("unused")
    @Produces
    @PicketLink
    @PersistenceContext(unitName = "jpa-permission-store-tests")
    private EntityManager picketLinkEntityManager;
    
    @SuppressWarnings("unused")
    @PersistenceContext(unitName = "jpa-permission-store-tests")
    private EntityManager entityManager;

    @Produces
    public EntityManager produceEntityManager() {
        return entityManager;
    }
}

package org.jboss.picketlink.example.securityconsole.util;

import javax.ejb.Stateless;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class EntityManagerProducer 
{
    @Produces
    @PersistenceContext
    private EntityManager em;
}

/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors. 
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketlink.identity.federation.core.sts.registry;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.core.ErrorCodes;

/**
 * <p>
 * {@code JPABasedRevocationRegistry} is a revocation registry implementation that uses JPA to store the ids of the
 * revoked (canceled) security tokens on a database. By default, the JPA configuration has the name {@code picketlink-sts}
 * but a different configuration name can be specified through the constructor that takes a {@code String} as a parameter.
 * </p>
 * <p>
 * NOTE: this implementation doesn't keep any cache of the security token ids. It performs a JPA query every time the
 * {@code isRevoked(String id)} method is called. Many JPA providers have internal caching mechanisms that can keep the
 * data in the cache synchronized with the database and avoid unnecessary trips to the database. This makes this registry
 * a good choice for clustered environments as any changes to the revocation table made by a node will be visible to
 * the other nodes.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class JPABasedRevocationRegistry implements RevocationRegistry
{

   private static Logger logger = Logger.getLogger(JPABasedRevocationRegistry.class);

   private final EntityManagerFactory factory;

   /**
    * <p>
    * Creates an instance of {@code JPABasedRevocationRegistry} that uses the default {@code picketlink-sts} JPA 
    * configuration to persist the ids of the canceled security tokens.
    * </p>
    */
   public JPABasedRevocationRegistry()
   {
      this("picketlink-sts");
   }

   /**
    * <p>
    * Creates an instance of {@code JPABasedRevocationRegistry} that uses the specified JPA configuration to persist
    * the ids of the canceled security tokens.
    * </p>
    * 
    * @param configuration a {@code String} representing the JPA configuration name to be used.
    */
   public JPABasedRevocationRegistry(String configuration)
   {
      if (configuration == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "JPA configuration name");
      this.factory = Persistence.createEntityManagerFactory(configuration);
   }

   /*
    * (non-Javadoc)
    * @see org.picketlink.identity.federation.core.wstrust.plugins.RevocationRegistry#isRevoked(java.lang.String, java.lang.String)
    */
   public boolean isRevoked(String tokenType, String id)
   {
      // try to locate a RevokedToken entity with the specified id.
      EntityManager manager = this.factory.createEntityManager();
      Object object = manager.find(RevokedToken.class, id);
      manager.close();

      return object != null;
   }

   /*
    * (non-Javadoc)
    * @see org.picketlink.identity.federation.core.wstrust.plugins.RevocationRegistry#revokeToken(java.lang.String, java.lang.String)
    */
   public void revokeToken(String tokenType, String id)
   {
      // if a RevokedToken entity with the specified id doesn't exist in the database, create one and insert it.
      EntityManager manager = this.factory.createEntityManager();
      if (manager.find(RevokedToken.class, id) != null)
      {
         if (logger.isDebugEnabled())
            logger.debug("Token with id=" + id + " has already been cancelled");
      }
      else
      {
         RevokedToken revokedToken = new RevokedToken(tokenType, id);
         EntityTransaction transaction = manager.getTransaction();
         transaction.begin();
         manager.persist(revokedToken);
         transaction.commit();
      }
      manager.close();
   }
}
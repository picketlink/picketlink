/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.picketlink.test.integration;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.picketlink.annotations.PicketLink;

/**
 * <p>
 * Base class for test cases that require JPA integration.
 * </p>
 * <p>
 * As Arquillian Weld container does not provide a good support to JPA, test cases can rely on this base class in order to
 * use JPA with Weld deployments.
 * </p>
 * 
 * @author Pedro Igor
 * 
 */
public abstract class AbstractJPADeploymentTestCase extends AbstractArquillianTestCase {

    private static EntityManagerFactory emf;

    @Inject
    @PicketLink
    private EntityManager entityManager;

    public static WebArchive createDeployment(Class<?>... toAdd) {
        WebArchive archive = ArchiveUtils.create(toAdd);

        archive.addClass(Resources.class);

        return archive;
    }

    @Before
    public void onBefore() throws Exception {
        emf = Persistence.createEntityManagerFactory("jpa-store-default-schema");
        this.entityManager.getTransaction().begin();
    }

    @After
    public void onAfter() {
        this.entityManager.getTransaction().commit();
        this.entityManager.close();
        emf.close();
    }

    @ApplicationScoped
    public static class Resources {

        @PicketLink
        @Produces
        @RequestScoped
        public EntityManager produceEntityManager() {
            return emf.createEntityManager();

        }
    }
}

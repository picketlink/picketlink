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

package org.picketlink.idm.internal.jpa;

import javax.persistence.EntityManager;

/**
 * <p>
 * This class provides a template method to execute operations on the {@link EntityManager} instance. It already provides some
 * exception handling, logging and gives more control about how the {@link EntityManager} is used.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class JPATemplate {

    private EntityManager entityManager;

    /**
     * <p>
     * Executes the specified {@link JPACallback}.
     * </p>
     *
     * @param callback
     * @return
     */
    public Object execute(JPACallback callback) {
        try {
            return callback.execute(this.entityManager);
        } catch (Exception e) {
            // TODO: how to handle exceptions
            // TODO: logging
            throw new RuntimeException("Error while executing operation on JPA Identity Store.", e);
        }
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

}

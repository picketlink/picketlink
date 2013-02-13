/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.picketlink.example.securityconsole.model;

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
 */
@Stateless
public
@Named
class ModelPopulator {
    @PersistenceContext
    private EntityManager em;

    @Inject
    PermissionManager pm;

    public void populate() {
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

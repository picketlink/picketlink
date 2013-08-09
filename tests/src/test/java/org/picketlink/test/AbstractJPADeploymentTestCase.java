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
package org.picketlink.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.picketlink.annotations.PicketLink;
import org.picketlink.test.idm.config.CustomConfigurationTestCase;
import org.picketlink.test.util.ArchiveUtils;

/**
 * @author pedroigor
 */
public abstract class AbstractJPADeploymentTestCase extends AbstractArquillianTestCase {

    public static WebArchive deploy(Class<?>... classesToAdd) {
        List<Class> classes = new ArrayList<Class>(Arrays.asList(classesToAdd));

        classes.add(AbstractJPADeploymentTestCase.class);
        classes.add(AbstractArquillianTestCase.class);

        WebArchive archive = ArchiveUtils.create(classes.toArray(new Class[classes.size()]));

        archive.addAsResource(new File(CustomConfigurationTestCase.class.getResource("/META-INF/persistence.xml").getFile()), "META-INF/persistence.xml");
        ArchiveUtils.addDependency(archive, "org.picketlink:picketlink-idm-simple-schema:" + ArchiveUtils.getCurrentProjectVersion());

        return archive;
    }

    @ApplicationScoped
    public static class JPAConfiguration {

        @PersistenceContext
        private EntityManager entityManager;
        @Inject
        private UserTransaction userTransaction;

        @Produces
        @PicketLink
        public EntityManager produceTransactionalEntityManager() throws Exception {
            if (this.userTransaction.getStatus() != Status.STATUS_ACTIVE) {
                this.userTransaction.begin();
            }

            return this.entityManager;
        }
    }

}

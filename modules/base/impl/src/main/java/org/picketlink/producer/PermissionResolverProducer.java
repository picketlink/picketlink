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

package org.picketlink.producer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.picketlink.idm.permission.spi.PermissionResolver;
import org.picketlink.idm.permission.spi.PermissionVoter;

/**
 * Defines the producer method for the application's PermissionResolver instance
 *
 * @author Shane Bryzak
 */
@ApplicationScoped
public class PermissionResolverProducer {

    private PermissionResolver resolver;

    @Inject
    public void init(Instance<PermissionVoter> votersInstance) {
        List<PermissionVoter> voters = new ArrayList<PermissionVoter>();

        if (!votersInstance.isUnsatisfied()) {
            Iterator<PermissionVoter> voterIterator = votersInstance.iterator();
            while (voterIterator.hasNext()) {
                voters.add(voterIterator.next());
            }
        }

        resolver = new PermissionResolver(voters);
    }


    @Produces
    public PermissionResolver createPermissionResolver() {
        return resolver;
    }
}

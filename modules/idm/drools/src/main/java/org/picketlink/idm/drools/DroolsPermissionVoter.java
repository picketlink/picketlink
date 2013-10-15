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

package org.picketlink.idm.drools;

import java.io.Serializable;

import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.permission.spi.PermissionVoter;


/**
 *
 * @author Shane Bryzak
 *
 */
public class DroolsPermissionVoter implements PermissionVoter {

    private KieBase securityRules;

    public DroolsPermissionVoter(KieBase securityRules) {
        this.securityRules = securityRules;
    }

    @Override
    public VotingResult hasPermission(IdentityType recipient, Object resource, String operation) {
        KieSession session = securityRules.newKieSession();

        session.fireAllRules();
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VotingResult hasPermission(IdentityType recipient, Class<?> resourceClass, Serializable identifier, String operation) {
        return VotingResult.NOT_APPLICABLE;
    }

}
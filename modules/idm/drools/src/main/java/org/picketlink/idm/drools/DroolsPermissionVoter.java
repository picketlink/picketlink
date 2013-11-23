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
 * A PermissionVoter implementation that uses Drools to provide rule-based permission checks. A
 * PermissionCheck object is created and inserted into the Drools session object, upon which all
 * rules are then fired.
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
        VotingResult result = VotingResult.NOT_APPLICABLE;

        KieSession session = securityRules.newKieSession();

        PermissionCheck check = new PermissionCheck(resource, operation);

        session.insert(recipient);
        session.insert(check);
        session.fireAllRules();

        if (check.isGranted()) {
            result = VotingResult.ALLOW;
        }

        return result;
    }

    /**
     * Rule-based permission checks only work with the actual resource instance, not with the identifier.
     */
    @Override
    public VotingResult hasPermission(IdentityType recipient, Class<?> resourceClass, Serializable identifier, String operation) {
        return VotingResult.NOT_APPLICABLE;
    }

}
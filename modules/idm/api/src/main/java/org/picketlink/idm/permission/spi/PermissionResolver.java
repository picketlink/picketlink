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

package org.picketlink.idm.permission.spi;

import java.io.Serializable;
import java.util.List;

import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.permission.spi.PermissionVoter.VotingResult;

/**
 * Iterates through the configured PermissionVoter instances to determine whether a resource permission
 * is to be allowed or denied.
 *
 * @author Shane Bryzak
 */
public class PermissionResolver {
    private final List<PermissionVoter> voters;

    public PermissionResolver(List<PermissionVoter> voters) {
        this.voters = voters;
    }

    public boolean resolvePermission(IdentityType recipient, Object resource, String operation) {
        boolean permit = false;

        for (PermissionVoter voter : voters) {
            VotingResult result = voter.hasPermission(recipient, resource, operation);
            if (VotingResult.ALLOW.equals(result)) {
                permit = true;
            }
            else if (VotingResult.DENY.equals(result)) {
                return false;
            }
        }

        return permit;
    }

    public boolean resolvePermission(IdentityType recipient, Class<?> resourceClass, Serializable identifier, String operation) {
        boolean permit = false;

        for (PermissionVoter voter : voters) {
            VotingResult result = voter.hasPermission(recipient, resourceClass, identifier, operation);
            if (VotingResult.ALLOW.equals(result)) {
                permit = true;
            }
            else if (VotingResult.DENY.equals(result)) {
                return false;
            }
        }

        return permit;
    }
}

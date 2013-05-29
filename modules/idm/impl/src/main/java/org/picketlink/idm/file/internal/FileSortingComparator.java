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

package org.picketlink.idm.file.internal;

import org.picketlink.idm.internal.util.IDMUtil;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;

import java.util.Comparator;

/**
 * Comparator for sorting identity objects according to given query parameters
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class FileSortingComparator<T extends IdentityType> implements Comparator<T> {

    private IdentityQuery<T> identityQuery;

    public FileSortingComparator(IdentityQuery<T> identityQuery) {
        this.identityQuery = identityQuery;
    }

    @Override
    public int compare(T o1, T o2) {
        QueryParameter[] params = identityQuery.getSortParameters();

        if (params == null || params.length == 0) {
            params = IDMUtil.getDefaultParamsForSorting(o1.getClass());
        }

        int sortResult = 0;
        for (QueryParameter queryParameter : params) {
            sortResult = sortByQueryParameter(queryParameter, o1, o2);
            if (sortResult != 0) {

                // Negate result if descending order is required
                if (!identityQuery.isSortAscending()) {
                    return -sortResult;
                }

                return sortResult;
            }
        }

        return sortResult;
    }

    protected int sortByQueryParameter(QueryParameter queryParameter, T o1, T o2) {
        // TODO: Maybe it should be rewritten to have something more pluggable (possibility for user to plug his own comparators for custom query parameters)
        if (queryParameter.equals(IdentityType.ID)) {
            return o1.getId().compareTo(o2.getId());
        } else if (queryParameter.equals(IdentityType.ENABLED)) {
            return Boolean.valueOf(o1.isEnabled()).compareTo(o2.isEnabled());
        } else if (queryParameter.equals(IdentityType.CREATED_DATE)) {
            return o1.getCreatedDate().compareTo(o2.getCreatedDate());
        } else if (queryParameter.equals(IdentityType.EXPIRY_DATE)) {
            return o1.getExpirationDate().compareTo(o2.getExpirationDate());
        }

        if (Agent.class.isInstance(o1) && Agent.class.isInstance(o2)) {
            Agent a1 = (Agent)o1;
            Agent a2 = (Agent)o2;
            if (queryParameter.equals(Agent.LOGIN_NAME)) {
                return a1.getLoginName().compareTo(a2.getLoginName());
            }

            if (o1 instanceof User) {
                User u1 = (User)o1;
                User u2 = (User)o2;
                if (queryParameter.equals(User.FIRST_NAME)) {
                    return u1.getFirstName().compareTo(u2.getFirstName());
                } else if (queryParameter.equals(User.LAST_NAME)) {
                    return u1.getLastName().compareTo(u2.getLastName());
                } else if (queryParameter.equals(User.EMAIL)) {
                    return u1.getEmail().compareTo(u2.getEmail());
                }
            }
        } else if (Group.class.isInstance(o1) && Group.class.isInstance(o2)) {
            Group g1 = (Group)o1;
            Group g2 = (Group)o2;
            if (queryParameter.equals(Group.NAME)) {
                return g1.getName().compareTo(g2.getName());
            }
        } else if (Role.class.isInstance(o1) && Role.class.isInstance(o2)) {
            Role r1 = (Role)o1;
            Role r2 = (Role)o2;
            if (queryParameter.equals(Role.NAME)) {
                return r1.getName().compareTo(r2.getName());
            }
        }

        return -1;
    }
}

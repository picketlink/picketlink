/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.picketlink.idm.file.internal;

import java.util.Comparator;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.internal.util.IDMUtil;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;

/**
 * Comparator for sorting identity objects according to given query parameters
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class FileSortingComparator<T extends IdentityType> implements Comparator<T> {

    private IdentityQuery<T> identityQuery;

    public FileSortingComparator(IdentityQuery identityQuery) {
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

        if (o1 instanceof Agent) {
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
        } else if (o1 instanceof Group) {
            Group g1 = (Group)o1;
            Group g2 = (Group)o2;
            if (queryParameter.equals(Group.NAME)) {
                return g1.getName().compareTo(g2.getName());
            }
        } else if (o1 instanceof Role) {
            Role r1 = (Role)o1;
            Role r2 = (Role)o2;
            if (queryParameter.equals(Role.NAME)) {
                return r1.getName().compareTo(r2.getName());
            }
        }

        throw new IdentityManagementException("Unknown query parameter " + queryParameter + " for comparing objects " + o1 + " and " + o2);
    }
}

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
package org.picketlink.test.authorization;

import org.picketlink.authorization.annotations.Restrict;

/**
 * @author Pedro Igor
 */
public class ELProtectedBean {

    @Restrict("#{identity.loggedIn}")
    public void protectedFromUnauthenticatedUsers() throws IllegalAccessException {

    }

    @Restrict("#{isLoggedIn()}")
    public void protectedFromUnauthenticatedUsersFunction() {

    }

    @Restrict("#{hasPermission('profile','read')}")
    public void protectedWithResourcePermission() {

    }

    @Restrict("#{hasPermission('profile','write')}")
    public void protectedWithResourceWithoutPermission() {

    }

    @Restrict("#{hasRole('Tester')}")
    public void protectedWithRequiredRole() {

    }

    @Restrict("#{hasRole('Invalid Role')}")
    public void protectedWithRequiredInvalidRole() {

    }

    @Restrict("#{isMember('QA')}")
    public void protectedWithRequiredGroup() {

    }

    @Restrict("#{isMember('Invalid Group')}")
    public void protectedWithRequiredInvalidGroup() {

    }

    @Restrict("#{isMember('QA') and hasRole('Tester')}")
    public void protectedWithRequiredMemberAndRole() {

    }

    @Restrict("#{isMember('QA') and hasRole('Invalid Role')}")
    public void protectedWithRequiredMemberAndInvalidRole() {

    }

    @Restrict("#{hasPartition('default')}")
    public void protectedWithRequiredPartitionName() {

    }

    @Restrict("#{hasPartition('invalid partition')}")
    public void protectedWithInvalidPartitionName() {

    }

    @Restrict("#{hasAttribute('someAttribute')}")
    public void protectedWithAttribute() {

    }

    @Restrict("#{hasAttribute('invalidAttribute')}")
    public void protectedWithInvalidAttribute() {

    }

    @Restrict("#{identity.account != null}")
    public void protectedWithValidAccountExpression() {

    }

    @Restrict("#{identity.account.partition.name == 'default'}")
    public void protectedWithValidPartitionExpression() {

    }

    @Restrict("#{identity.account.partition.name != 'default'}")
    public void protectedWithInvalidPartitionExpression() {

    }

    @Restrict("#{identity.account.attributes['someAttribute'] != null}")
    public void protectedWithValidAccountAttributeExpression() {

    }

    @Restrict("#{identity.account.attributes['someAttribute'] == 'someValue'}")
    public void protectedWithValidAccountAttributeValueExpression() {

    }

    @Restrict("#{identity.account.attributes['someAttribute'] == 'invalidValue'}")
    public void protectedWithInvalidAccountAttributeValueExpression() {

    }

}

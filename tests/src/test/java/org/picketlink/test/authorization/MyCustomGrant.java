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

import org.picketlink.idm.model.AbstractAttributedType;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.annotation.RelationshipStereotype;
import org.picketlink.idm.model.annotation.StereotypeProperty;

import static org.picketlink.idm.model.annotation.RelationshipStereotype.Stereotype.GRANT;
import static org.picketlink.idm.model.annotation.StereotypeProperty.Property.RELATIONSHIP_GRANT_ASSIGNEE;
import static org.picketlink.idm.model.annotation.StereotypeProperty.Property.RELATIONSHIP_GRANT_ROLE;

/**
 * @author Pedro Igor
 */
@RelationshipStereotype(GRANT)
public class MyCustomGrant extends AbstractAttributedType implements Relationship {

    private MyCustomRole myCustomRole;
    private Account myCustomAssignee;

    private MyCustomGrant() {
        this(null, null);
    }

    public MyCustomGrant(Account myCustomAssignee, MyCustomRole myCustomRole) {
        this.myCustomAssignee = myCustomAssignee;
        this.myCustomRole = myCustomRole;
    }

    @StereotypeProperty(RELATIONSHIP_GRANT_ROLE)
    public MyCustomRole getMyCustomRole() {
        return this.myCustomRole;
    }

    public void setMyCustomRole(MyCustomRole myCustomRole) {
        this.myCustomRole = myCustomRole;
    }

    @StereotypeProperty(RELATIONSHIP_GRANT_ASSIGNEE)
    public Account getMyCustomAssignee() {
        return this.myCustomAssignee;
    }

    public void setMyCustomAssignee(Account myCustomAssignee) {
        this.myCustomAssignee = myCustomAssignee;
    }
}

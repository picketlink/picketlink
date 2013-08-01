package org.picketlink.idm.jpa.model.sample.complex;

import org.picketlink.idm.model.AbstractIdentityType;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.model.annotation.Unique;

/**
 * @author pedroigor
 */
public class OrganizationUnit extends AbstractIdentityType {

    @AttributeProperty
    @Unique
    private String name;

    @AttributeProperty
    private OrganizationUnit parent;

    public OrganizationUnit() {
    }

    public OrganizationUnit(String name) {
        this(name, null);
    }

    public OrganizationUnit(String name, OrganizationUnit parent) {
        this.name = name;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public OrganizationUnit getParent() {
        return parent;
    }

    public void setParent(final OrganizationUnit parent) {
        this.parent = parent;
    }
}

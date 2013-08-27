package org.picketlink.test.idm.relationship;


import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;
import org.picketlink.idm.jpa.model.sample.simple.RelationshipTypeEntity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;

/**
 * @author  Pedro Igor
 */
@IdentityManaged (CustomRelationshipTestCase.CustomRelationship.class)
@Entity
public class CustomRelationshipTypeEntity implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @AttributeValue
    private String attributeA;

    @AttributeValue
    private Long attributeB;

    @AttributeValue
    private boolean attributeC;

    @OwnerReference
    @ManyToOne
    private RelationshipTypeEntity owner;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAttributeA() {
        return attributeA;
    }

    public void setAttributeA(final String attributeA) {
        this.attributeA = attributeA;
    }

    public Long getAttributeB() {
        return attributeB;
    }

    public void setAttributeB(final Long attributeB) {
        this.attributeB = attributeB;
    }

    public boolean isAttributeC() {
        return attributeC;
    }

    public void setAttributeC(final boolean attributeC) {
        this.attributeC = attributeC;
    }

    public RelationshipTypeEntity getOwner() {
        return owner;
    }

    public void setOwner(final RelationshipTypeEntity owner) {
        this.owner = owner;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!getClass().isInstance(obj)) {
            return false;
        }

        CustomRelationshipTypeEntity other = (CustomRelationshipTypeEntity) obj;

        return getId() != null && other.getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getId() != null ? getId().hashCode() : 0);
        return result;
    }

}

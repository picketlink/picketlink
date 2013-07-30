package org.picketlink.test.idm.partition;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;
import org.picketlink.idm.jpa.model.sample.simple.PartitionTypeEntity;

@IdentityManaged(CustomPartitionTestCase.CustomPartition.class)
@Entity
public class CustomPartitionEntity implements Serializable {

    @AttributeValue
    private String attributeA;

    @AttributeValue
    private Long attributeB;

    @AttributeValue
    private int attributeC;

    @OneToOne
    @Id
    @OwnerReference
    private PartitionTypeEntity partitionTypeEntity;

    public PartitionTypeEntity getPartitionTypeEntity() {
        return partitionTypeEntity;
    }

    public void setPartitionTypeEntity(PartitionTypeEntity partitionTypeEntity) {
        this.partitionTypeEntity = partitionTypeEntity;
    }

    public String getAttributeA() {
        return attributeA;
    }

    public void setAttributeA(String attributeA) {
        this.attributeA = attributeA;
    }

    public Long getAttributeB() {
        return attributeB;
    }

    public void setAttributeB(Long attributeB) {
        this.attributeB = attributeB;
    }

    public int getAttributeC() {
        return attributeC;
    }

    public void setAttributeC(int attributeC) {
        this.attributeC = attributeC;
    }
}
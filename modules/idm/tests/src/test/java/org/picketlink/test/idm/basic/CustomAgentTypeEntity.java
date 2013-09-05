package org.picketlink.test.idm.basic;

import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;
import org.picketlink.idm.jpa.model.sample.simple.AccountTypeEntity;

import javax.persistence.Entity;

/**
 * @author pedroigor
 */
@IdentityManaged (CustomAgentManagementTestCase.CustomAgent.class)
@Entity
public class CustomAgentTypeEntity extends AccountTypeEntity {

    @AttributeValue
    private String customProperty;

    public String getCustomProperty() {
        return customProperty;
    }

    public void setCustomProperty(final String customProperty) {
        this.customProperty = customProperty;
    }
}

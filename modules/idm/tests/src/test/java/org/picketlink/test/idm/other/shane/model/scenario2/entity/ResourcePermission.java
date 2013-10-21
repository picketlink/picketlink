package org.picketlink.test.idm.other.shane.model.scenario2.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.picketlink.idm.jpa.annotations.PermissionOperation;
import org.picketlink.idm.jpa.annotations.PermissionAssignee;
import org.picketlink.idm.jpa.annotations.PermissionResourceClass;
import org.picketlink.idm.jpa.annotations.PermissionResourceIdentifier;
import org.picketlink.idm.jpa.annotations.entity.PermissionManaged;

/**
 * This entity stores general resource permissions
 *
 * @author Shane Bryzak
 */
@PermissionManaged
@Entity
public class ResourcePermission implements Serializable {

    private static final long serialVersionUID = -5728457124164068127L;

    @Id @GeneratedValue
    private Long id;

    @PermissionAssignee
    private String recipient;

    @PermissionResourceClass
    private String resourceClass;

    @PermissionResourceIdentifier
    private String resourceIdentifier;

    @PermissionOperation
    private String grants;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getResourceClass() {
        return resourceClass;
    }

    public void setResourceClass(String resourceClass) {
        this.resourceClass = resourceClass;
    }

    public String getResourceIdentifier() {
        return resourceIdentifier;
    }

    public void setResourceIdentifier(String resourceIdentifier) {
        this.resourceIdentifier = resourceIdentifier;
    }

    public String getGrants() {
        return grants;
    }

    public void setGrants(String grants) {
        this.grants = grants;
    }
}

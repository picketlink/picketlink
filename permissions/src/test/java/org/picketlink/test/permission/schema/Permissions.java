package org.picketlink.test.permission.schema;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.picketlink.permission.annotations.ACLIdentifier;
import org.picketlink.permission.annotations.ACLPermission;
import org.picketlink.permission.annotations.ACLRecipient;
import org.picketlink.permission.annotations.ACLResourceClass;
import org.picketlink.permission.annotations.ACLStore;

/**
 * Test permissions entity
 * 
 * @author Shane Bryzak
 *
 */
@Entity
@ACLStore(ACLStore.GENERAL.class)
public class Permissions {

    @Id @GeneratedValue 
    private Long id;

    @ACLIdentifier
    private String resourceIdentifier;

    @ACLRecipient
    private String recipient;

    @ACLPermission
    private String permission;

    @ACLResourceClass
    private String resourceClass;

    public String getResourceIdentifier() {
        return resourceIdentifier;
    }

    public void setResourceIdentifier(String resourceIdentifier) {
        this.resourceIdentifier = resourceIdentifier;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getResourceClass() {
        return resourceClass;
    }

    public void setResourceClass(String resourceClass) {
        this.resourceClass = resourceClass;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

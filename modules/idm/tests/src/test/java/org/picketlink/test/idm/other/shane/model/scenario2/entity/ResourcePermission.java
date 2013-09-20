package org.picketlink.test.idm.other.shane.model.scenario2.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.picketlink.idm.jpa.annotations.PermissionGrant;
import org.picketlink.idm.jpa.annotations.PermissionRecipient;
import org.picketlink.idm.jpa.annotations.PermissionResourceClass;
import org.picketlink.idm.jpa.annotations.entity.PermissionManaged;

/**
 * This entity stores general resource permissions
 *
 * @author Shane Bryzak
 */
@PermissionManaged
@Entity
public class ResourcePermission implements Serializable {

    @Id @GeneratedValue
    private Long id;

    @PermissionRecipient
    private String recipient;

    @PermissionResourceClass
    private String resource;

    @PermissionGrant
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

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getGrants() {
        return grants;
    }

    public void setGrants(String grants) {
        this.grants = grants;
    }
}

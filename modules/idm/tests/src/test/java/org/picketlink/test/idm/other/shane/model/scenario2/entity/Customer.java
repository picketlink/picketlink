package org.picketlink.test.idm.other.shane.model.scenario2.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.picketlink.idm.permission.annotations.AllowedPermission;
import org.picketlink.idm.permission.annotations.AllowedPermissions;

/**
 * Sample model entity for which we'll assign permissions
 *
 * @author Shane Bryzak
 */
@Entity
@AllowedPermissions({
    @AllowedPermission(mask = 1, operation = "READ"),
    @AllowedPermission(mask = 2, operation = "UPDATE"),
    @AllowedPermission(mask = 4, operation = "DELETE")
})
public class Customer implements Serializable {

    private static final long serialVersionUID = 2595163271352308849L;

    public static final String PERMISSION_READ = "READ";
    public static final String PERMISSION_UPDATE = "UPDATE";
    public static final String PERMISSION_DELETE = "DELETE";

    @Id @GeneratedValue
    private Long id;

    public String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

package org.picketlink.test.idm.other.shane.model.scenario2.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.picketlink.idm.permission.annotations.AllowedOperation;
import org.picketlink.idm.permission.annotations.AllowedOperations;

/**
 * Sample model entity for which we'll assign permissions
 *
 * @author Shane Bryzak
 */
@Entity
@AllowedOperations({
    @AllowedOperation(value = "CREATE", mask = 1, classOperation = true),
    @AllowedOperation(value = "READ", mask = 2),
    @AllowedOperation(value = "UPDATE", mask = 4),
    @AllowedOperation(value = "DELETE", mask = 8)
})
public class Customer implements Serializable {

    private static final long serialVersionUID = 2595163271352308849L;

    public static final String PERMISSION_CREATE = "CREATE";
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

package org.picketlink.idm.model;

/**
 * A Realm defines a boundary for certain identity state.  Users, Groups and standard Roles 
 * are unique within a Realm.
 * 
 * @author Shane Bryzak
 */
public class Realm {
    private String name;

    public Realm(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

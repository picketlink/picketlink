package org.picketlink.idm.model;

/**
 * A Realm defines a boundary for certain identity state.  Users, Groups and standard Roles 
 * are unique within a Realm.
 * 
 * @author Shane Bryzak
 */
public class Realm implements Partition {

    private static final long serialVersionUID = -638755196631131758L;

    public static final String DEFAULT_REALM = "default";

    public static final String KEY_PREFIX = "REALM://";

    private String name;

    public Realm(String name) {
        if (name == null) {
            throw new InstantiationError("Realm name must not be null");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getKey() {
        return String.format("%s%s", KEY_PREFIX, name);
    }

    // TODO implement hashCode() and equals() methods
}

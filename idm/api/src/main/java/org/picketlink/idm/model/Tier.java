package org.picketlink.idm.model;

/**
 * A hierarchical abstraction representing a partitioned set or subset of services, for which
 * specialized Roles and Groups may be created.
 * 
 * @author Shane Bryzak
 */
public class Tier implements Partition {
    private String id;
    private String description;
    private Tier parent;

    public Tier(String id, String description, Tier parent) {
        this.id = id;
        this.description = description;
        this.parent = parent;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Tier getParent() {
        return parent;
    }

    // TODO implement hashCode() and equals() methods
}

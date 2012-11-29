package org.picketlink.idm.model;

/**
 * A hierarchical abstraction representing a partitioned set or subset of services, for which
 * specialized Roles and Groups may be created.
 * 
 * @author Shane Bryzak
 */
public class Tier implements Partition {

    private static final long serialVersionUID = 7797059334915537276L;

    private static final String KEY_PREFIX = "TIER://";

    private String id;
    private String description;
    private Tier parent;

    public Tier(String id, String description, Tier parent) {
        if (id == null) {
            throw new InstantiationError("Tier id must not be null");
        }

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

    @Override
    public String getKey() {
        return String.format("%s%s", KEY_PREFIX, id);
    }

    // TODO implement hashCode() and equals() methods
}

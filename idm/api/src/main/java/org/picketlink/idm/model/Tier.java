package org.picketlink.idm.model;

/**
 * A hierarchical abstraction representing a partitioned set or subset of services, for which
 * specialized Roles and Groups may be created.
 * 
 * @author Shane Bryzak
 */
public class Tier implements Partition {

    private static final long serialVersionUID = 7797059334915537276L;

    public static final String KEY_PREFIX = "TIER://";

    private String id;
    private String name;
    private String description;
    private Tier parent;

    public Tier(String name) {
        if (name == null) {
            throw new InstantiationError("Tier name must not be null");
        }

        this.name = name;
    }
    
    public Tier(String name, String description, Tier parent) {
        this(name);
        this.description = description;
        this.parent = parent;
    }

    public Tier(String name, Tier applicationTier) {
        this(name, null, applicationTier);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }

    public Tier getParent() {
        return parent;
    }
    
    @Override
    public String getKey() {
        return String.format("%s%s", KEY_PREFIX, name);
    }

    public void setParent(Tier parent) {
        this.parent = parent;
    }

    // TODO implement hashCode() and equals() methods
}

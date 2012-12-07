package org.picketlink.idm.model;

/**
 * Simple implementation of the Agent interface
 * 
 * @author Shane Bryzak
 */
public class SimpleAgent extends AbstractIdentityType implements Agent {
    private static final long serialVersionUID = -7418037050013416323L;

    private String id;

    public SimpleAgent(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getKey() {
        return String.format("%s%s", KEY_PREFIX, id);
    }
}

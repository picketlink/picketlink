package org.picketlink.idm.model;

import java.io.Serializable;

/**
 * Represents an attribute value, a type of metadata that can be associated with an IdentityType
 * 
 * @author Shane Bryzak
 *
 * @param <T>
 */
public class Attribute<T extends Serializable> {
    private String name;
    private T value;
    private boolean readOnly = false;
    private boolean unique = false;

    public Attribute(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public Attribute(String name, T value, boolean readOnly) {
        this(name, value);
        this.readOnly = readOnly;
    }

    public Attribute(String name, T value, boolean readOnly, boolean unique) {
        this(name, value, readOnly);
        this.unique = unique;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setValue(T value) {
        this.value = value;
    }
}



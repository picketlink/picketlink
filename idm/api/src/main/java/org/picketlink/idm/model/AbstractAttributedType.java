package org.picketlink.idm.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for all AttributedType implementations
 * 
 * @author Shane Bryzak
 *
 */
public abstract class AbstractAttributedType implements AttributedType {
    private static final long serialVersionUID = -6118293036241099199L;

    private Map<String, Attribute<? extends Serializable>> attributes = 
            new HashMap<String, Attribute<? extends Serializable>>();

    public void setAttribute(Attribute<? extends Serializable> attribute) {
        attributes.put(attribute.getName(), attribute);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @SuppressWarnings("unchecked")
    public <T extends Serializable> Attribute<T> getAttribute(String name) {
        return (Attribute<T>) attributes.get(name);
    }

    public Collection<Attribute<? extends Serializable>> getAttributes() {
        return java.util.Collections.unmodifiableCollection(attributes.values());
    }
}

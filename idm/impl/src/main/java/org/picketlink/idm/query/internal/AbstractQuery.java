package org.picketlink.idm.query.internal;

import java.util.HashMap;
import java.util.Map;

import org.picketlink.idm.query.Range;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class AbstractQuery<T> {

    private String name;
    private Map<String, String[]> attributeFilters = new HashMap<String, String[]>();
    private boolean enabled = true;
    private boolean sortAscending;

    public T reset() {
        return (T) this;
    }

    public T getImmutable() {
        return (T) this;
    }

    public T setName(String name) {
        this.name = name;
        return (T) this;
    }

    public String getName() {
        return this.name;
    }

    public T setAttributeFilter(String name, String[] values) {
        this.attributeFilters.put(name, values);
        return (T) this;
    }

    public T setEnabled(boolean enabled) {
        this.enabled = enabled;
        return (T) this;
    }

    public boolean getEnabled() {
        return this.enabled;
    }

    public Map<String, String[]> getAttributeFilters() {
        return this.attributeFilters;
    }

    public void setRange(Range range) {

    }

    public Range getRange() {
        return null;
    }

    public T sort(boolean ascending) {
        this.sortAscending = ascending;
        return (T) this;
    }

    public IdentityStoreInvocationContext getInvocationContext(IdentityStore store) {
        // FIXME implement this
        return null;
    }

}
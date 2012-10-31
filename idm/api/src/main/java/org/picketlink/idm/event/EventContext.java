package org.picketlink.idm.event;

import java.util.HashMap;
import java.util.Map;

/**
 * The event context may be used to pass arbitrary state to event observers
 *
 * @author Shane Bryzak
 */
public class EventContext {
    private Map<String,Object> context;

    public Object getValue(String name) {
        return context != null ? context.get(name) : null;
    }

    public void setValue(String name, Object value) {
        if (context == null) {
            context = new HashMap<String,Object>();
        }
        context.put(name, value);
    }

    public boolean contains(String name) {
        return context != null && context.containsKey(name);
    }

    public boolean isEmpty() {
        return context == null || context.isEmpty();
    }
}

package org.picketlink.idm.query;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;

/**
 *
 * @author Shane Bryzak
 */
public class RelationshipCriteria {
    private Map<String,IdentityType> criteria = new HashMap<String,IdentityType>();

    private Class<? extends Relationship> relationshipClass;

    public RelationshipCriteria(Class<? extends Relationship> relationshipClass) {
        this.relationshipClass = relationshipClass;
    }

    public void addCriteria(String propertyName, IdentityType identity) {
        criteria.put(propertyName, identity);
    }

    public Class<? extends Relationship> getRelationshipClass() {
        return relationshipClass;
    }

    public Set<String> getPropertyNames() {
        return criteria.keySet();
    }

    public IdentityType getCriteria(String propertyName) {
        return criteria.get(propertyName);
    }
}

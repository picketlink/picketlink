package org.picketlink.idm.query.internal;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.spi.IdentityStore;

/**
 * Default IdentityQuery implementation.
 * 
 * @author Shane Bryzak
 *
 * @param <T>
 */
public class DefaultRelationshipQuery<T extends Relationship> implements RelationshipQuery<T> {

    private Map<QueryParameter, Object[]> parameters = new LinkedHashMap<QueryParameter, Object[]>();
    private IdentityStore<?> identityStore;
    private Class<T> relationshipType;
    private int offset;
    private int limit;
    
    public DefaultRelationshipQuery(Class<T> relationshipType, IdentityStore<?> identityStore) {
        this.identityStore = identityStore;
        this.relationshipType = relationshipType;
    }
    
    @Override
    public RelationshipQuery<T> setParameter(QueryParameter param, Object... value) {
        parameters.put(param, value);
        return this;
    }
    
    @Override
    public Class<T> getRelationshipType() {
        return relationshipType;
    }

    @Override
    public Map<QueryParameter, Object[]> getParameters() {
        return parameters;
    }

    @Override
    public int getLimit() {
        return limit;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public List<T> getResultList() {
        return this.identityStore.fetchQueryResults(this);

    }

    @Override
    public int getResultCount() {
        return this.identityStore.countQueryResults(this);
    }

    @Override
    public RelationshipQuery<T> setOffset(int offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public RelationshipQuery<T> setLimit(int limit) {
        this.limit = limit;
        return this;
    }
}

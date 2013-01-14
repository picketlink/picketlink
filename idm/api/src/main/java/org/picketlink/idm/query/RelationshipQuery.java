package org.picketlink.idm.query;

import java.util.List;
import java.util.Map;

import org.picketlink.idm.model.Relationship;

/**
 * Used to query identity relationships 
 * 
 * @author Shane Bryzak
 */
public interface RelationshipQuery<T extends Relationship> {
    RelationshipQuery<T> setOffset(int offset);

    RelationshipQuery<T> setLimit(int limit);

    RelationshipQuery<T> setParameter(QueryParameter param, Object... value);

    Map<QueryParameter, Object[]> getParameters();
    
    Object[] getParameter(QueryParameter queryParameter);

    Class<T> getRelationshipType();
    
    int getOffset();

    int getLimit();

    List<T> getResultList();

    int getResultCount();
}

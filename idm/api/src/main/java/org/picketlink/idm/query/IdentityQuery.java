package org.picketlink.idm.query;

import java.util.List;
import java.util.Map;

import org.picketlink.idm.model.IdentityType;

/**
 * Unified identity query API
 * 
 * @author Shane Bryzak
 */
public interface IdentityQuery<T extends IdentityType> {
//    public enum Operator { equals, notEquals, greaterThan, lessThan };

    IdentityQuery<T> setOffset(int offset);

    IdentityQuery<T> setLimit(int limit);

    IdentityQuery<T> setParameter(QueryParameter param, Object... value);

    Class<T> getIdentityType();

    Map<QueryParameter, Object[]> getParameters();

    int getOffset();

    int getLimit();

//    IdentityQuery<T> setParameter(QueryParameter param, Operator operator, Object... value);

    List<T> getResultList();

   /**
    * Count of all query results. It takes into account query parameters, but it doesn't take into account pagination
    * parameter like offset and limit
    *
    * @return count of all query results
    */
    int getResultCount();
}

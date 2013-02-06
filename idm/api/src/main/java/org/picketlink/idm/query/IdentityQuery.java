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

    IdentityQuery<T> setOffset(int offset);

    IdentityQuery<T> setLimit(int limit);

    /**
     * Parameters used to sort the results. First parameter has biggest priority.
     * For example: setSortParameter(User.LAST_NAME, User.FIRST_NAME) means that results will be sorted primarily by lastName
     * and firstName will be used to sort only records with same lastName
     *
     * @param sortParameters parameters to specify sort criteria
     * @return this query
     */
    IdentityQuery<T> setSortParameters(QueryParameter... sortParameters);

    /**
     * @see #setSortParameters(QueryParameter...)
     */
    QueryParameter[] getSortParameters();

    /**
     * Specify if sorting will be ascending (true) or descending (false)
     * @param sortAscending to specify if sorting will be ascending or descending
     * @return this query
     */
    IdentityQuery<T> setSortAscending(boolean sortAscending);

    /**
     * @return true if sorting will be ascending
     * @see #setSortAscending(boolean)
     */
    boolean isSortAscending();

    IdentityQuery<T> setParameter(QueryParameter param, Object... value);

    Class<T> getIdentityType();

    Map<QueryParameter, Object[]> getParameters();
    
    Object[] getParameter(QueryParameter queryParameter);
    
    Map<QueryParameter, Object[]> getParameters(Class<?> type);

    int getOffset();

    int getLimit();

    List<T> getResultList();

   /**
    * Count of all query results. It takes into account query parameters, but it doesn't take into account pagination
    * parameter like offset and limit
    *
    * @return count of all query results
    */
    int getResultCount();
}

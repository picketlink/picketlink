package org.picketlink.idm.query;

import java.util.List;

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

//    IdentityQuery<T> setParameter(QueryParameter param, Operator operator, Object... value);

    List<T> getResultList();
}

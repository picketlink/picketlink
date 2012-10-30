package org.picketlink.idm.query;

import java.util.List;

import org.picketlink.idm.model.IdentityType;

/**
 * Unified identity query API
 * 
 * @author Shane Bryzak
 */
public interface IdentityQuery<T extends IdentityType> {
    public enum Param {
        // General parameters
        id, key, created, expired, enabled, 

        // User parameters
        firstName, lastName, email,

        // Group and Role parameters
        name,

        // Group parameters
        parent,

        // Membership
        memberOf};

    public enum Operator { equals, notEquals, greaterThan, lessThan };
    
    IdentityQuery<T> setOffset(int offset);

    IdentityQuery<T> setLimit(int limit);

    IdentityQuery<T> setParameter(Param param, Object value);

    IdentityQuery<T> setParameter(Param param, Operator operator, Object value);

    IdentityQuery<T> setAttributeParameter(String attributeName, Object value);

    IdentityQuery<T> setAttributeParameter(String attributeName, Operator operator, Object value);

    List<T> getResultList();
}

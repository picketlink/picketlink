package org.picketlink.idm.query.internal;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.picketlink.idm.ldap.internal.LDAPIdentityStore;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.spi.IdentityStore;

/**
 * Default IdentityQuery implementation.
 * 
 * @author Shane Bryzak
 *
 * @param <T>
 */
public class DefaultIdentityQuery<T extends IdentityType> implements IdentityQuery<T> {

    private Map<QueryParameter, Object[]> parameters = new LinkedHashMap<QueryParameter, Object[]>();
    private IdentityStore<?> identityStore;
    private Class<T> identityType;
    
    public DefaultIdentityQuery(Class<T> identityType, IdentityStore<?> identityStore) {
        this.identityStore = identityStore;
        this.identityType = identityType;
    }
    
    @Override
    public IdentityQuery<T> setParameter(QueryParameter param, Object... value) {
        parameters.put(param, value);
        return this;
    }

    /*@Override
    public IdentityQuery<T> setParameter(QueryParameter param, Operator operator, Object value) {
        // TODO Auto-generated method stub
        return null;
    }*/

    @Override
    public List<T> getResultList() {
        return ((LDAPIdentityStore) this.identityStore).fetchQueryResults(this.identityType, this.parameters);

    }

    @Override
    public IdentityQuery<T> setOffset(int offset) {
        return null;
    }

    @Override
    public IdentityQuery<T> setLimit(int limit) {
        return null;
    }
}

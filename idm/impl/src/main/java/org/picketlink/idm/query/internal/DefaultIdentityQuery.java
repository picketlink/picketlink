package org.picketlink.idm.query.internal;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.naming.ldap.LdapContext;

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
    private int offset;
    private int limit;
    private LdapContext context;
    private byte[] cookie;
    
    public DefaultIdentityQuery(Class<T> identityType, IdentityStore<?> identityStore) {
        this.identityStore = identityStore;
        this.identityType = identityType;
    }
    
    @Override
    public IdentityQuery<T> setParameter(QueryParameter param, Object... value) {
        parameters.put(param, value);
        return this;
    }

    @Override
    public Class<T> getIdentityType() {
        return identityType;
    }

    @Override
    public Map<QueryParameter, Object[]> getParameters() {
        return parameters;
    }
    
    @Override
    public Object[] getParameter(QueryParameter queryParameter) {
        return this.parameters.get(queryParameter);
    }
    
    @Override
    public Map<QueryParameter, Object[]> getParameters(Class<?> type) {
        Map<QueryParameter, Object[]> typedParameters = new HashMap<QueryParameter, Object[]>();
        
        Set<Entry<QueryParameter, Object[]>> entrySet = this.parameters.entrySet();
        
        for (Entry<QueryParameter, Object[]> entry : entrySet) {
            if (type.isInstance(entry.getKey())) {
                typedParameters.put(entry.getKey(), entry.getValue());
            }
        }
        
        return typedParameters;
    }

    @Override
    public int getLimit() {
        return limit;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    /*@Override
    public IdentityQuery<T> setParameter(QueryParameter param, Operator operator, Object value) {
        // TODO Auto-generated method stub
        return null;
    }*/

    @Override
    public List<T> getResultList() {
        return this.identityStore.fetchQueryResults(this);
    }

    @Override
    public int getResultCount() {
        return this.identityStore.countQueryResults(this);
    }

    @Override
    public IdentityQuery<T> setOffset(int offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public IdentityQuery<T> setLimit(int limit) {
        this.limit = limit;
        return this;
    }

}

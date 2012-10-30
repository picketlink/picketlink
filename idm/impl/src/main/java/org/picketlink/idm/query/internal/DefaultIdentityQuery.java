package org.picketlink.idm.query.internal;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;

/**
 * Default IdentityQuery implementation.
 * 
 * @author Shane Bryzak
 *
 * @param <T>
 */
public class DefaultIdentityQuery<T extends IdentityType> implements IdentityQuery<T> {


    @Override
    public IdentityQuery<T> setParameter(QueryParameter param, Object value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IdentityQuery<T> setParameter(QueryParameter param, Operator operator, Object value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<T> getResultList() {

        // This is a bit hacky, we might need to actually pass in the type to the constructor
        ParameterizedType parameterizedType = (ParameterizedType)getClass()
                .getGenericSuperclass();
        Class type = (Class) parameterizedType.getActualTypeArguments()[0];

        // TODO Auto-generated method stub
        return null;

    }

    @Override
    public IdentityQuery<T> setOffset(int offset) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IdentityQuery<T> setLimit(int limit) {
        // TODO Auto-generated method stub
        return null;
    }
}

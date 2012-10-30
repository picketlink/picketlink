package org.picketlink.idm.query.internal;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.Range;

/**
 * Default IdentityQuery implementation.
 * 
 * @author Shane Bryzak
 *
 * @param <T>
 */
public class DefaultIdentityQuery<T extends IdentityType> implements IdentityQuery<T> {

    @Override
    public IdentityQuery<T> reset() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IdentityQuery<T> setParameter(org.picketlink.idm.query.IdentityQuery.Param param, Object value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IdentityQuery<T> setParameter(org.picketlink.idm.query.IdentityQuery.Param param,
            org.picketlink.idm.query.IdentityQuery.Operator operator, Object value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IdentityQuery<T> setAttributeParameter(String attributeName, Object value) {
        // TODO Auto-generated method stub


        return null;
    }

    @Override
    public IdentityQuery<T> setAttributeParameter(String attributeName,
            org.picketlink.idm.query.IdentityQuery.Operator operator, Object value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IdentityQuery<T> setRange(Range range) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<T> getResultList() {
        ParameterizedType parameterizedType = (ParameterizedType)getClass()
                .getGenericSuperclass();
        Class type = (Class) parameterizedType.getActualTypeArguments()[0];

        // TODO Auto-generated method stub
        return null;

    }

}

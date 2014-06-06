package org.picketlink.test.authorization.permission.action;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.picketlink.test.authorization.permission.schema.Foo;

@Stateless
public class FooAction {

    @Inject EntityManager entityManager;

    public Foo createFoo(String value) {
        Foo foo = new Foo();
        foo.setValue(value);
        entityManager.persist(foo);
        return foo;
    }

}

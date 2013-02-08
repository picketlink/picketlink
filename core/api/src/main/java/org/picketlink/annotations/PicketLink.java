package org.picketlink.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * This qualifier annotation is used to provide an EntityManager instance to the Identity Management API
 * when access to a database is required for JPAIdentityStore.
 * 
 * @author Shane Bryzak
 *
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface PicketLink {

}

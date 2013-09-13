package org.picketlink.idm.jpa.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks the property of an entity bean that represents the resource for which a permission is granted.  The value may
 * either be the String-based resource identifier, or in the case of an entity bean resource it may be the natural primary key
 * value for that entity, but only in the case that the @PermissionManaged annotation specifies a resourceClasses member value
 * with a single value being that of the entity bean class.  This second use case is to facilitate natural join queries between
 * the entity bean and its associated permission table.
 *
 * @author Shane Bryzak
 *
 */
@Target({METHOD, FIELD})
@Documented
@Retention(RUNTIME)
@Inherited
public @interface PermissionResource {

}

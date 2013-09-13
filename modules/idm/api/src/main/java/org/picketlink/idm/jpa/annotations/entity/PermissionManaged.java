package org.picketlink.idm.jpa.annotations.entity;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation is applied to an entity bean to indicate that it is used to store permission
 * state. If the resourceClasses member value is set, then only permissions for resources of the
 * specified classes will be stored in this table.
 *
 * @author Shane Bryzak
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface PermissionManaged {
    Class<?>[] resourceClasses() default {};
}

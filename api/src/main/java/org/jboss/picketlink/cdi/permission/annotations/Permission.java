package org.jboss.picketlink.cdi.permission.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies an allowable permission action for the target class, and allows for an optional bit mask
 * value for mapping the permission action to a persistent store
 *
 * @author Shane Bryzak
 */
@Target({TYPE})
@Documented
@Retention(RUNTIME)
@Inherited
public @interface Permission {
    String action();

    long mask() default 0L;
}

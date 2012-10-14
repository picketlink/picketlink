package org.picketlink.permission.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.picketlink.permission.spi.PermissionHandler;

/**
 * Configures the Permission Handler to use for instance-based permissions.  The specified class
 * should implement the PermissionHandler interface.
 *
 * @author Shane Bryzak
 */
@Target({TYPE})
@Documented
@Retention(RUNTIME)
@Inherited
public @interface PermissionsHandledBy {
    Class<? extends PermissionHandler> value() default PermissionHandler.class;

    String name() default "";
}
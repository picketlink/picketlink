package org.jboss.picketlink.cdi.permission.annotations;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 * 
 * @author Shane Bryzak
 *
 */
@Target({ PARAMETER })
@Retention(RUNTIME)
@Documented
public @interface RequiresPermission
{
    String value() default "";
}

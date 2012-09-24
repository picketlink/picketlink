package org.picketlink.cdi.permission.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.deltaspike.security.api.authorization.annotation.SecurityBindingType;

/**
 * 
 * @author Shane Bryzak
 *
 */
@Target({ METHOD })
@Retention(RUNTIME)
@Documented
@SecurityBindingType
public @interface CheckPermissions
{

}

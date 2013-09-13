package org.picketlink.idm.jpa.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks the property of an entity bean that represents the permissions granted for a resource.  This value will
 * either be a bitmask integer (for resources annotated with @AllowedPermissions) or a comma-separated list of
 * arbitrary permission String values
 *
 * @author Shane Bryzak
 *
 */
@Target({METHOD, FIELD})
@Documented
@Retention(RUNTIME)
@Inherited
public @interface PermissionGrant {

}

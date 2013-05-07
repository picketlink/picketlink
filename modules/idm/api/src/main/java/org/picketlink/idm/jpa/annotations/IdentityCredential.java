package org.picketlink.idm.jpa.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation should be applied to a single entity bean of an application to mark it as
 * holding credential state for an identity
 *
 * @author Shane Bryzak
 */
@Target({TYPE})
@Documented
@Retention(RUNTIME)
@Inherited
public @interface IdentityCredential {

}

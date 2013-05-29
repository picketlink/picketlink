package org.picketlink.idm.jpa.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks the partition property of an identity type entity
 *
 * @author Shane Bryzak
 */
@Target({METHOD, FIELD})
@Documented
@Retention(RUNTIME)
@Inherited
public @interface IdentityPartition {

}

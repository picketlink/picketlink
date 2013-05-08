package org.picketlink.idm.jpa.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks the parent group property of a identity type entity (for group identities only),
 * the parent partition property of a partition entity (for tier partitions only),
 * the parent identity type for a credential entity, the parent identity for an attribute,
 * the parent relationship for a relationship attribute, the parent credential for a credential attribute,
 * or the parent relationship for a relationship identity.
 *
 * @author Shane Bryzak
 */
@Target({METHOD, FIELD})
@Documented
@Retention(RUNTIME)
@Inherited
public @interface Parent {

}

package org.picketlink.idm.jpa.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks the attribute value property of an identity attribute entity, a relationship attribute entity
 * or credential attribute entity
 *
 * @author Shane Bryzak
 */
@Target({METHOD, FIELD})
@Documented
@Retention(RUNTIME)
@Inherited
public @interface AttributeValue {
    String name() default "";
    boolean lazyLoaded() default false;
}

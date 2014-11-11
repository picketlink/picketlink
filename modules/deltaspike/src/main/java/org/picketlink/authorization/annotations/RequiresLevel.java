package org.picketlink.authorization.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;

import org.apache.deltaspike.security.api.authorization.SecurityBindingType;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>Defines an authorization check where only users with same or higher level then specified are allowed to invoke method</p>
 *
 * <p>The constraint is validated before method invocation.</p>
 *
 * @author  Michal Trnka
 */
@Target({TYPE, METHOD, FIELD})
@Retention(RUNTIME)
@Documented
@SecurityBindingType
public @interface RequiresLevel {

    /**
     * Text representation of the Level
     *
     * @return
     */
    @Nonbinding
    String value() default "";
}

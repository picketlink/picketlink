package org.picketlink.test.idm;

import org.picketlink.test.idm.testers.IdentityConfigurationTester;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * @author pedroigor
 */
@Target({METHOD, TYPE})
@Documented
@Retention(RUNTIME)
@Inherited
public @interface Configuration {

    Class<? extends IdentityConfigurationTester>[] exclude() default {};
    Class<? extends IdentityConfigurationTester>[] include() default {};

}

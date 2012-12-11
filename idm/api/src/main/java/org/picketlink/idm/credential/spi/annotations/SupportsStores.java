package org.picketlink.idm.credential.spi.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.picketlink.idm.spi.IdentityStore;

/**
 * This annotation is used to declare which identity store types are supported
 * by a particular CredentialHandler implementation.
 *
 * @author Shane Bryzak
 */
@Target({TYPE})
@Documented
@Retention(RUNTIME)
@Inherited
public @interface SupportsStores {
    Class<? extends IdentityStore>[] value();
}

/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketlink.annotations;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Stereotype;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>Defines a {@link javax.enterprise.inject.Stereotype} that may be used to indicate that a stateless {@link org.picketlink.Identity}
 * is expected. In this case, the authentication state for an user is not shared between different requests. So any authentication data will
 * be lost once the request processing is finished..</p>
 *
 * <p>By default, the {@link org.picketlink.Identity} bean provides a stateful behavior, where the authentication state is shared between
 * different requests from the same user. This annotation can be used as a switch to disable the stateful and enable the stateless nature
 * of the {@link org.picketlink.Identity} bean.</p>
 *
 * <p>In order to enable the stateless behavior of the {@link org.picketlink.Identity} bean, you need to configure your <code>beans.xml</code> as follows:</p>
 *
 * <pre>
 *     {@code
 *     <beans>
 *         <alternatives>
 *             <stereotype>org.picketlink.annotations.StatelessIdentity</stereotype>
 *         </alternatives>
 *     </beans>
 *     }
 * </pre>
 *
 * @author Pedro Igor
 *
 * @see org.picketlink.StatelessIdentityExtension
 */
@Alternative
@Stereotype
@Retention(RUNTIME)
@Target(TYPE)
@RequestScoped
public @interface StatelessIdentity {

}

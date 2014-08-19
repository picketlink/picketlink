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
package org.picketlink.http.test.config;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import java.lang.reflect.Field;

/**
 * @author Pedro Igor
 */
public class HttpSecurityConfigExtension implements Extension {

    public <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> event) {
        AnnotatedType<T> tp = event.getAnnotatedType();

        if (tp.isAnnotationPresent(HttpSecurity.class)) {
            if (!tp.getJavaClass().isInterface()) {
                throw new RuntimeException("ViewConfig annotation should only be applied to interfaces, and [" + tp.getJavaClass()
                    + "] is not an interface.");
            } else {
                for (Class<?> clazz : tp.getJavaClass().getClasses()) {
                    for (Field enumm : clazz.getFields()) {
                        System.out.println(enumm);
                    }
                }
            }
        }
    }

}

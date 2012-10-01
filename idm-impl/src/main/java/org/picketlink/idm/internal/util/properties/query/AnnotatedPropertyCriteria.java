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
package org.picketlink.idm.internal.util.properties.query;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * A criteria that matches a property based on its annotations
 *
 * @see PropertyCriteria
 */
public class AnnotatedPropertyCriteria implements PropertyCriteria {
    private final Class<? extends Annotation> annotationClass;

    public AnnotatedPropertyCriteria(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public boolean fieldMatches(Field f) {
        return f.isAnnotationPresent(annotationClass);
    }

    public boolean methodMatches(Method m) {
        return m.isAnnotationPresent(annotationClass);
    }

}

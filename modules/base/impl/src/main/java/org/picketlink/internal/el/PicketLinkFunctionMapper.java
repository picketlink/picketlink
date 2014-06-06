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
package org.picketlink.internal.el;

import javax.el.FunctionMapper;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>{@link javax.el.FunctionMapper} for built-in EL functions.</p>
 *
 * @author Pedro Igor
 */
public class PicketLinkFunctionMapper extends FunctionMapper {

    private static final String DEFAULT_NAMESPACE = "p";
    private static final Map<String,Method> builtInFunctions = new HashMap<String, Method>();

    static {
        try {
            addFunction("isLoggedIn", ELFunctionMethods.class.getMethod("isLoggedIn"));
            addFunction("hasPermission", ELFunctionMethods.class.getMethod("hasPermission", new Class[]{Object.class, String.class}));
            addFunction("hasRole", ELFunctionMethods.class.getMethod("hasRole", new Class[]{String.class}));
            addFunction("hasPartition", ELFunctionMethods.class.getMethod("hasPartition", new Class[]{String.class}));
            addFunction("hasAttribute", ELFunctionMethods.class.getMethod("hasAttribute", String.class));
            addFunction("isMember", ELFunctionMethods.class.getMethod("isMember", new Class[]{String.class}));
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Error configuring built-in EL functions.", e);
        }
    }

    private final Map<String,Method> userDefinedFunctions = new HashMap<String, Method>();

    @Override
    public Method resolveFunction(String prefix, String localName) {
        if (prefix.length() == 0) {
            prefix = DEFAULT_NAMESPACE;
        }

        String key = prefix + ":" + localName;
        Method function = builtInFunctions.get(key);

        if (function == null) {
            function = this.userDefinedFunctions.get(key);
        }

        return function;
    }

    private static void addFunction(String name, Method functionMethod) {
        builtInFunctions.put(DEFAULT_NAMESPACE + ":" + name, functionMethod);
    }
}

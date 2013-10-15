/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.picketlink.config.idm;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.reflection.Reflections;
import org.picketlink.config.PicketLinkConfigParser;
import org.picketlink.config.federation.PicketLinkType;
import org.picketlink.config.idm.resolver.PropertyResolverMapper;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.Builder;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.config.annotation.MethodConfigID;
import org.picketlink.idm.config.annotation.ParameterConfigID;

/**
 * Creating IDM runtime from parsed XML configuration
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class XMLConfigurationProvider {

    public static final ClassLoader[] IDM_CLASSLOADERS = {IdentityManager.class.getClassLoader(), XMLConfigurationProvider.class.getClassLoader()};

    /**
     * Create and initialize IdentityConfigurationBuilder and fill it with content from XML configuration
     *
     * @param inputStream stream with XML configuration
     *
     * @return initialized builder
     */
    public IdentityConfigurationBuilder readIDMConfiguration(InputStream inputStream) {
        IDMType idmConfiguration = parseIDMConfiguration(inputStream);
        return readIDMConfigurationFromIDMType(idmConfiguration);
    }

    public IDMType parseIDMConfiguration(InputStream inputStream) {
        try {
            PicketLinkConfigParser parser = new PicketLinkConfigParser();
            PicketLinkType plType = (PicketLinkType) parser.parse(inputStream);
            return plType.getIdmType();
        } catch (ParsingException pe) {
            throw new SecurityConfigurationException("Could not parse picketlink configuration", pe);
        }
    }

    public IdentityConfigurationBuilder readIDMConfigurationFromIDMType(IDMType idmType) {
        IdentityConfigurationBuilder idmConfigBuilder = new IdentityConfigurationBuilder();

        Builder<?> currentBuilder = idmConfigBuilder;

        for (ConfigBuilderMethodType configBuilderMethodType : idmType.getBuilderMethods()) {
            String methodId = configBuilderMethodType.getMethodId();
            Map<String, String> methodParams = configBuilderMethodType.getMethodParameters();
            Method builderMethod = getBuilderMethod(currentBuilder.getClass(), methodId, methodParams);

            Object[] parameters = getMethodParameters(builderMethod, methodParams);
            currentBuilder = Reflections.invokeMethod(builderMethod, Builder.class, currentBuilder, parameters);
        }

        return idmConfigBuilder;
    }

    protected Method getBuilderMethod(Class<?> builderClass, String methodId, Map<String, String> methodParams) {
        for (Class<?> c = builderClass; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                Method candidate = null;

                // Check MethodConfigID annotation first
                MethodConfigID methodConfigID = m.getAnnotation(MethodConfigID.class);
                if (methodConfigID != null) {
                    if (methodId.equals(methodConfigID.name())) {
                        candidate = m;
                    }
                }

                // Check method name
                if (methodId.equals(m.getName())) {
                    candidate = m;
                }

                if (candidate != null) {
                    // Parameters sanity check
                    Class<?>[] params = m.getParameterTypes();
                    int paramsCount = params.length;
                    int requiredParamsCount = methodParams.size();
                    // We likely have correct method if params sizes are the same
                    if (paramsCount == requiredParamsCount) {
                        return candidate;
                        // Otherwise if last parameter is array (varargs), we can have more parameters provided from configuration
                    } else if (requiredParamsCount + 1 >= paramsCount && paramsCount >= 1 && params[paramsCount - 1].isArray()) {
                        return candidate;
                    }
                }
            }
        }

        throw new SecurityConfigurationException("Not found method " + methodId + " with required params " + methodParams + " on object " + builderClass);
    }

    protected Object[] getMethodParameters(Method builderMethod, Map<String, String> unparsedParameters) {
        Object[] params = new Object[unparsedParameters.size()];
        Class<?>[] paramTypes = builderMethod.getParameterTypes();

        boolean[] paramsResolved = new boolean[unparsedParameters.size()];
        for (int i = 0; i < unparsedParameters.size(); i++) {
            paramsResolved[i] = false;
        }

        // First look for parameter with ParameterConfigID annotation
        Map<String, Integer> paramConfigAnnotationIndexes = getParamConfigIdAnnotationIndexes(builderMethod);
        for (Map.Entry<String, Integer> paramEntry : paramConfigAnnotationIndexes.entrySet()) {
            String paramName = paramEntry.getKey();
            int paramIndex = paramEntry.getValue();

            String unparsedParamValue = unparsedParameters.get(paramName);
            if (unparsedParamValue == null) {
                throw new SecurityConfigurationException("No value found for parameter " + paramName + " in params " + unparsedParameters);
            }
            if (paramIndex >= paramTypes.length) {
                throw new SecurityConfigurationException("Index too big. paramName: " + paramName + ", paramIndex: " + paramIndex + ", paramTypes length: " + paramTypes.length);
            }

            Class<?> expectedParamType = paramTypes[paramIndex];
            Object paramValue = PropertyResolverMapper.getInstance().resolveProperty(unparsedParamValue, expectedParamType);
            params[paramIndex] = paramValue;
            paramsResolved[paramIndex] = true;
        }

        // Now look for parameters, which don't have ParameterConfigID annotation
        int paramIndex = 0;
        for (Map.Entry<String, String> current : unparsedParameters.entrySet()) {
            String paramName = current.getKey();
            String unparsedParamValue = current.getValue();

            // Already processed during annotation processing
            if (paramConfigAnnotationIndexes.containsKey(paramName)) {
                continue;
            }

            // Find first unresolved parameter
            while (paramsResolved[paramIndex] == true) {
                paramIndex++;
            }

            Class<?> expectedParamType;
            // Handle the case when last parameter of current builder method is 'varargs' parameters
            if (paramIndex >= paramTypes.length - 1 && paramTypes[paramTypes.length - 1].isArray()) {
                expectedParamType = paramTypes[paramTypes.length - 1].getComponentType();
            } else {
                expectedParamType = paramTypes[paramIndex];
            }

            Object paramValue = PropertyResolverMapper.getInstance().resolveProperty(unparsedParamValue, expectedParamType);
            params[paramIndex] = paramValue;
            paramsResolved[paramIndex] = true;
        }

        // Handle the case when last parameter of current builder method is 'varargs' parameters. Parameters needs to be converted, so that last parameters are wrapped into array
        if (paramTypes.length > 0 && paramTypes[paramTypes.length - 1].isArray()) {
            params = varargsConvert(params, paramTypes.length, paramTypes[paramTypes.length - 1].getComponentType());
        }

        return params;
    }

    /**
     * Return mapping of names of ParameterConfigID to indexes of current parameter.
     *
     * Example: For method like:
     * public String test2(@ParameterConfigID(name="firstArg") String firstArg, @ParameterConfigID(name="secondArg")
     * String secondArg, Object... lastArgs);
     *
     * The result will be map(("firstArg" -> 0),("secondArg" -> 1))
     */
    private Map<String, Integer> getParamConfigIdAnnotationIndexes(Method builderMethod) {
        Map<String, Integer> paramConfigAnnotationIndexes = new HashMap<String, Integer>();
        Annotation[][] annotations = builderMethod.getParameterAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Annotation[] currentParamAnnotations = annotations[i];
            for (Annotation currentAnnotation : currentParamAnnotations) {
                if (currentAnnotation instanceof ParameterConfigID) {
                    String paramAnnotationName = ((ParameterConfigID) currentAnnotation).name();
                    paramConfigAnnotationIndexes.put(paramAnnotationName, i);
                    break;
                }
            }
        }

        return paramConfigAnnotationIndexes;
    }

    /**
     * Convert parameters to be passed to varargs method, so that last parameter will be array.
     *
     * Example: We have method myMethod(String param1, Integer param2, String... param3), which means that
     * expectedParamsLength=3 and arrayType=String.class
     * and we have params array like {"String1", 23, "String2", "String3"} .
     * Then result will be array like {"String1", 23 {"String2", "String3"}}
     *
     * @param params params from XML configuration
     * @param expectedParamsLength length of result array (Declared number of method parameters)
     * @param arrayType Type of one item in varargs array
     *
     * @return converted array with last parameter as array (this last parameter represents varargs argument)
     */
    private Object[] varargsConvert(Object[] params, int expectedParamsLength, Class<?> arrayType) {
        Object[] result = new Object[expectedParamsLength];
        int normalParamsLength = expectedParamsLength - 1;
        int varargsArrayLength = params.length - normalParamsLength;
        Object[] varargsArray = (Object[]) Array.newInstance(arrayType, varargsArrayLength);

        // First copy previous (non-varargs) parameters
        for (int i = 0; i < normalParamsLength; i++) {
            result[i] = params[i];
        }

        // Now fill varargs array
        for (int i = 0; i < varargsArrayLength; i++) {
            varargsArray[i] = params[i + normalParamsLength];
        }

        // Add varargs as last parameter
        result[normalParamsLength] = varargsArray;

        return result;
    }
}

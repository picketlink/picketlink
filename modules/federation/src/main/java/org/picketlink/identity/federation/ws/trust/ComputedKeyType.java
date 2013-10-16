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
package org.picketlink.identity.federation.ws.trust;

public class ComputedKeyType {

    private String algorithm;

    /**
     * <p>
     * Creates an instance of {@code ComputedKeyType}.
     * </p>
     */
    public ComputedKeyType() {
    }

    /**
     * <p>
     * Creates an instance of {@code ComputedKeyType} with the specified algorithm.
     * </p>
     *
     * @param algorithm the computed key algorithm.
     */
    public ComputedKeyType(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * <p>
     * Obtains the algorithm used to compute the shared secret key.
     * </p>
     *
     * @return a {@code String} representing the computed key algorithm.
     */
    public String getAlgorithm() {
        return this.algorithm;
    }

    /**
     * <p>
     * Sets the algorithm used to compute the shared secret key.
     * </p>
     *
     * @param algorithm a {@code String} representing the computed key algorithm.
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
}

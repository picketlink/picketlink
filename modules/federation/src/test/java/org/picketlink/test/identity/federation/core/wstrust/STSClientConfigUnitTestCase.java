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
package org.picketlink.test.identity.federation.core.wstrust;

import junit.framework.TestCase;
import org.picketlink.identity.federation.core.wstrust.STSClientConfig;
import org.picketlink.identity.federation.core.wstrust.STSClientConfig.Builder;

/**
 * Unit test for {@link WSTrustClientConfig}.
 *
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 */
public class STSClientConfigUnitTestCase extends TestCase {

    final String serviceName = "PicketLinkSTS";
    final String portName = "PicketLinkSTSPort";
    final String endpointAddress = "http://localhost:8080/picketlink-sts/PicketLinkSTS";
    final String username = "admin";
    final String password = "admin";

    public void testBuild() {
        final Builder builder = new STSClientConfig.Builder();
        final STSClientConfig config = builder.serviceName(serviceName).portName(portName).endpointAddress(endpointAddress)
                .username(username).password(password).build();
        assertAllProperties(config);
    }

    public void testBuildFromConfigPropertiesFile() {
        final Builder builder = new STSClientConfig.Builder("wstrust/sts-client.properties");
        assertAllProperties(builder.build());
    }

    public void testBuildFromConfigPropertiesFileOverridePassword() {
        final Builder builder = new STSClientConfig.Builder("wstrust/sts-client.properties");
        assertAllProperties(builder.build());

        final String overriddenPassword = "newPassword";
        builder.password(overriddenPassword);
        final STSClientConfig config = builder.build();
        assertEquals(overriddenPassword, config.getPassword());
    }

    private void assertAllProperties(final STSClientConfig config) {
        assertEquals(serviceName, config.getServiceName());
        assertEquals(portName, config.getPortName());
        assertEquals(endpointAddress, config.getEndPointAddress());
        assertEquals(username, config.getUsername());
        assertEquals(password, config.getPassword());

    }

}

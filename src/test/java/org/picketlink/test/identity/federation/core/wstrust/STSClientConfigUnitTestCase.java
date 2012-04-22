/*
 * JBoss, Home of Professional Open Source Copyright 2009, Red Hat Middleware
 * LLC, and individual contributors by the @authors tag. See the copyright.txt
 * in the distribution for a full listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.picketlink.test.identity.federation.core.wstrust;

import junit.framework.TestCase;

import org.picketlink.identity.federation.core.wstrust.STSClientConfig;
import org.picketlink.identity.federation.core.wstrust.STSClientConfig.Builder;

/**
 * Unit test for {@link WSTrustClientConfig}.
 * 
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 * 
 */
public class STSClientConfigUnitTestCase extends TestCase
{
    final String serviceName = "PicketLinkSTS";
    final String portName = "PicketLinkSTSPort";
    final String endpointAddress = "http://localhost:8080/picketlink-sts/PicketLinkSTS";
    final String username = "admin";
    final String password = "admin";
    
    public void testBuild()
    {
        final Builder builder = new STSClientConfig.Builder();
        final STSClientConfig config = builder.serviceName(serviceName).portName(portName).endpointAddress(endpointAddress).username(username).password(password).build();
        assertAllProperties(config);
    }
    
    public void testBuildFromConfigPropertiesFile()
    {
        final Builder builder = new STSClientConfig.Builder("wstrust/sts-client.properties");
        assertAllProperties(builder.build());
    }
    
    public void testBuildFromConfigPropertiesFileOverridePassword()
    {
        final Builder builder = new STSClientConfig.Builder("wstrust/sts-client.properties");
        assertAllProperties(builder.build());
        
        final String overriddenPassword = "newPassword";
        builder.password(overriddenPassword);
        final STSClientConfig config = builder.build();
        assertEquals(overriddenPassword, config.getPassword());
    }
    
    private void assertAllProperties(final STSClientConfig config)
    {
        assertEquals(serviceName, config.getServiceName());
        assertEquals(portName, config.getPortName());
        assertEquals(endpointAddress, config.getEndPointAddress());
        assertEquals(username, config.getUsername());
        assertEquals(password, config.getPassword());
        
    }
    
}

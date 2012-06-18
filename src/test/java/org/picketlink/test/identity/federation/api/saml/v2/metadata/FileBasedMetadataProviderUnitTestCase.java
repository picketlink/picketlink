/*
 * JBoss, a division of Red Hat
 * Copyright 2012, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.picketlink.test.identity.federation.api.saml.v2.metadata;

import junit.framework.TestCase;
import org.picketlink.identity.federation.core.interfaces.IMetadataProvider;
import org.picketlink.identity.federation.core.saml.md.providers.AbstractFileBasedMetadataProvider;
import org.picketlink.identity.federation.core.saml.md.providers.FileBasedEntitiesMetadataProvider;
import org.picketlink.identity.federation.core.saml.md.providers.FileBasedEntityMetadataProvider;
import org.picketlink.identity.federation.core.util.CoreConfigUtil;
import org.picketlink.identity.federation.saml.v2.metadata.EndpointType;
import org.picketlink.identity.federation.saml.v2.metadata.EntitiesDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.SPSSODescriptorType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.picketlink.identity.federation.core.util.StringUtil.isNotNull;
import static junit.framework.Assert.assertEquals;

/**
 * <p>Unit test for {@link FileBasedEntityMetadataProvider} and {@link FileBasedEntitiesMetadataProvider}</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class FileBasedMetadataProviderUnitTestCase extends TestCase {

    public void testFileBasedEntityMetadataProvider() {
        FileBasedEntityMetadataProvider metadataProvider = new FileBasedEntityMetadataProvider();
        EntityDescriptorType metadata = getMetadata(metadataProvider, "saml2/metadata/sp-entitydescriptor.xml");

        assertEquals(metadata.getEntityID(), "https://service.example.org/shibboleth");
        SPSSODescriptorType spSSODescriptor = CoreConfigUtil.getSPDescriptor(metadata);
        assertNull(spSSODescriptor.isAuthnRequestsSigned());
        assertEquals(spSSODescriptor.getSingleLogoutService().size(), 4);
    }

    public void testFileBasedEntitiesMetadataProvider() {
        FileBasedEntitiesMetadataProvider metadataProvider = new FileBasedEntitiesMetadataProvider();
        EntitiesDescriptorType metadata = getMetadata(metadataProvider, "saml2/metadata/sp-two-salesforce-googleapps.xml");

        List<Object> descriptors = metadata.getEntityDescriptor();
        for (Object descriptorType : descriptors) {
            if (!(descriptorType instanceof EntityDescriptorType)) {
                fail("Wrong type: " + descriptorType.getClass());
            }

            EntityDescriptorType entDescriptorType = (EntityDescriptorType)descriptorType;

            if ("https://saml.salesforce.com".equals(entDescriptorType.getEntityID())) {
                SPSSODescriptorType spDescriptor = CoreConfigUtil.getSPDescriptor(entDescriptorType);

                assertTrue(spDescriptor.isAuthnRequestsSigned());
                List<EndpointType> logoutEndpoints = spDescriptor.getSingleLogoutService();
                assertNotNull(logoutEndpoints);
                assertEquals(logoutEndpoints.size(), 1);
                EndpointType endpoint = logoutEndpoints.get(0);
                assertEquals("https://login.salesforce.com/saml/logout-request.jsp?saml=MgoTx78aEPkEM4eGV5ZzptlliwIVkRkOWYKlqXQq2StV_sLo0EiRqKYtIc",
                      endpoint.getLocation().toASCIIString());
                assertEquals("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST", endpoint.getBinding().toASCIIString());
            }
            else if ("google.com/a/somedomain.com".equals(entDescriptorType.getEntityID())) {
                SPSSODescriptorType spDescriptor = CoreConfigUtil.getSPDescriptor(entDescriptorType);

                assertFalse(spDescriptor.isAuthnRequestsSigned());
                List<EndpointType> logoutEndpoints = spDescriptor.getSingleLogoutService();
                assertNotNull(logoutEndpoints);
                assertEquals(logoutEndpoints.size(), 0);
            }
            else {
                fail("Wrong entityID: " + entDescriptorType.getEntityID());
            }
        }
    }

    private <T> T getMetadata(IMetadataProvider<T> metadataProvider, String filePath) {
        Map<String, String> options = new HashMap<String, String>();
        options.put(AbstractFileBasedMetadataProvider.FILENAME_KEY, filePath);
        metadataProvider.init(options);

        String fileInjectionStr = metadataProvider.requireFileInjection();
        if (isNotNull(fileInjectionStr)) {
            metadataProvider.injectFileStream(Thread.currentThread().getContextClassLoader().getResourceAsStream(fileInjectionStr));
        }

        return metadataProvider.getMetaData();
    }
}

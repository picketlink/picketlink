/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.test.identity.federation.bindings.jboss.auth;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.jboss.security.identity.RoleGroup;
import org.jboss.security.mapping.MappingProvider;
import org.jboss.security.mapping.MappingResult;
import org.picketlink.identity.federation.bindings.jboss.auth.SAML20TokenRoleAttributeProvider;
import org.picketlink.identity.federation.bindings.jboss.auth.mapping.STSGroupMappingProvider;
import org.picketlink.identity.federation.bindings.jboss.auth.mapping.STSPrincipalMappingProvider;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.wstrust.auth.AbstractSTSLoginModule;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType.ASTChoiceType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType.STSubType;
import org.w3c.dom.Element;

/**
 * <p>
 * This {@code TestCase} tests the functionalities of {@code STSPrincipalMappingProvider} and {@code STSGroupMappingProvider}.
 * </p>
 * 
 * @author <a href="mailto:Babak@redhat.com">Babak Mozaffari</a>
 */
public class STSMappingProviderUnitTestCase extends TestCase
{

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
   }

   /**
    * <p>
    * Tests that {@code STSGroupMappingProvider} correctly maps and returns a {@code RoleGroup}
    * </p>
    * 
    * @throws Exception if an error occurs while running the test.
    */
   public void testSTSGroupMappingProvider() throws Exception
   {
      String roleAttributeName = "roleAttributeName";
      String role1 = "userRole1";
      String role2 = "userRole2";

      AssertionType assertion = new AssertionType("ID_SOME", XMLTimeUtil.getIssueInstant());
      AttributeStatementType attributeStatementType = new AttributeStatementType();
      assertion.addStatement(attributeStatementType);
      AttributeType attributeType = new AttributeType(roleAttributeName);
      attributeStatementType.addAttribute(new ASTChoiceType(attributeType));
      attributeType.addAttributeValue(role1);
      attributeType.addAttributeValue(role2);

      MappingResult<RoleGroup> mappingResult = new MappingResult<RoleGroup>();
      Map<String, Object> contextMap = new HashMap<String, Object>();
      contextMap.put("token-role-attribute-name", roleAttributeName);
      contextMap.put(AbstractSTSLoginModule.SHARED_TOKEN, SAMLUtil.toElement(assertion));

      MappingProvider<RoleGroup> mappingProvider = new STSGroupMappingProvider();
      mappingProvider.init(contextMap);
      mappingProvider.setMappingResult(mappingResult);
      mappingProvider.performMapping(contextMap, null);

      RoleGroup roleGroup = mappingResult.getMappedObject();
      assertNotNull("Unexpected null mapped role", roleGroup);
      assertEquals("RoleGroup name has unexpected value", SAML20TokenRoleAttributeProvider.JBOSS_ROLE_PRINCIPAL_NAME,
            roleGroup.getRoleName());
      assertEquals("RoleGroup has unexpected first role", role1, roleGroup.getRoles().get(0).getRoleName());
      assertEquals("RoleGroup has unexpected second role", role2, roleGroup.getRoles().get(1).getRoleName());
   }

   /**
    * <p>
    * Tests that {@code STSPrincipalMappingProvider} correctly maps and returns a {@code Principal}
    * </p>
    * 
    * @throws Exception if an error occurs while running the test.
    */
   public void testSTSPrincipalMappingProvider() throws Exception
   {
      String userId = "babak";

      AssertionType assertion = new AssertionType("ID_SOME", XMLTimeUtil.getIssueInstant());
      SubjectType subjectType = new SubjectType();
      assertion.setSubject(subjectType);
      //QName name = new QName(WSTrustConstants.SAML2_ASSERTION_NS, "NameID");
      NameIDType nameIDType = new NameIDType();
      nameIDType.setValue(userId);
      STSubType subType = new STSubType();
      subType.addBaseID(nameIDType);

      subjectType.setSubType(subType);
      /*JAXBElement<NameIDType> jaxbElement = new JAXBElement<NameIDType>(name, declaredType, JAXBElement.GlobalScope.class, nameIDType);
      subjectType.getContent().add(jaxbElement);*/

      MappingResult<Principal> mappingResult = new MappingResult<Principal>();
      Map<String, Object> contextMap = new HashMap<String, Object>();
      Element assertionElement = SAMLUtil.toElement(assertion);
      contextMap.put(AbstractSTSLoginModule.SHARED_TOKEN, assertionElement);

      MappingProvider<Principal> mappingProvider = new STSPrincipalMappingProvider();
      mappingProvider.init(contextMap);
      mappingProvider.setMappingResult(mappingResult);
      mappingProvider.performMapping(contextMap, null);

      Principal principal = mappingResult.getMappedObject();
      assertNotNull("Unexpected null mapped principal", principal);
      assertEquals("Principal has unexpected value", userId, principal.getName());
   }
}

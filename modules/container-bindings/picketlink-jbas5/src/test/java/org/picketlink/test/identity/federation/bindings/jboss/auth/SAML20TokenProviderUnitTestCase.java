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

import java.net.URI;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import junit.framework.TestCase;

import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;
import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.plugins.JBossSecurityContext;
import org.picketlink.identity.federation.bindings.jboss.auth.SAML20TokenRoleAttributeProvider;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.wstrust.StandardSecurityToken;
import org.picketlink.identity.federation.core.wstrust.WSTrustRequestContext;
import org.picketlink.identity.federation.core.wstrust.WSTrustUtil;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAML20TokenProvider;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.assertion.AudienceRestrictionType;
import org.picketlink.identity.federation.saml.v2.assertion.ConditionsType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.StatementAbstractType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectConfirmationType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType.ASTChoiceType;
import org.w3c.dom.Element;

/**
 * <p>
 * This {@code TestCase} tests the functionalities of the {@code SAML20TokenRoleAttributeProvider} class.
 * </p>
 * 
 * @author <a href="mailto:Babak@redhat.com">Babak Mozaffari</a>
 */
public class SAML20TokenProviderUnitTestCase extends TestCase
{

   private SAML20TokenProvider provider;

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      this.provider = new SAML20TokenProvider();
      Map<String, String> properties = new HashMap<String, String>();
      properties.put("AttributeProvider", SAML20TokenRoleAttributeProvider.class.getName());
      properties.put(SAML20TokenRoleAttributeProvider.class.getName() + ".tokenRoleAttributeName", "roleAttributeName");
      provider.initialize(properties);
   }

   /**
    * <p>
    * Tests the inclusion of the roles attributes in a SAMLV2.0 Assertion.
    * </p>
    * 
    * @throws Exception if an error occurs while running the test.
    */
   public void testIssueSAMLV20Token() throws Exception
   {
      // create a WSTrustRequestContext with a simple WS-Trust request.
      RequestSecurityToken request = new RequestSecurityToken();
      request.setLifetime(WSTrustUtil.createDefaultLifetime(3600000));
      request.setAppliesTo(WSTrustUtil.createAppliesTo("http://services.testcorp.org/provider2"));
      request.setTokenType(URI.create(SAMLUtil.SAML2_TOKEN_TYPE));

      SecurityContext securityContext = new JBossSecurityContext("jmx-console");
      SecurityContextAssociation.setSecurityContext(securityContext);

      Principal principal = new SimplePrincipal("bmozaffa");
      SimpleGroup group = new SimpleGroup(SAML20TokenRoleAttributeProvider.JBOSS_ROLE_PRINCIPAL_NAME);
      group.addMember(new SimplePrincipal("myTestRole"));
      Subject newSubject = new Subject();
      newSubject.getPrincipals().add(principal);
      newSubject.getPrincipals().add(group);
      SecurityContextAssociation.getSecurityContext().getUtil().createSubjectInfo(principal, null, newSubject);
      
      WSTrustRequestContext context = new WSTrustRequestContext(request, principal);
      context.setTokenIssuer("PicketLinkSTS");

      // call the SAML token provider and check the generated token.
      this.provider.issueToken(context);
      assertNotNull("Unexpected null security token", context.getSecurityToken());
      
      SecurityContextAssociation.clearSecurityContext();

      Element assertionElement = (Element) context.getSecurityToken().getTokenValue();
      
      SAMLParser samlParser = new SAMLParser();
      AssertionType assertion = (AssertionType) samlParser.parse( DocumentUtil.getNodeAsStream(assertionElement));
      
      /*JAXBContext jaxbContext = JAXBContext.newInstance("org.picketlink.identity.federation.saml.v2.assertion");
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      JAXBElement<?> parsedElement = (JAXBElement<?>) unmarshaller.unmarshal((Element) context.getSecurityToken()
            .getTokenValue());
      assertNotNull("Unexpected null element", parsedElement);
      assertEquals("Unexpected element type", AssertionType.class, parsedElement.getDeclaredType());

      AssertionType assertion = (AssertionType) parsedElement.getValue();*/
      StandardSecurityToken securityToken = (StandardSecurityToken) context.getSecurityToken();
      assertEquals("Unexpected token id", securityToken.getTokenID(), assertion.getID());
      assertEquals("Unexpected token issuer", "PicketLinkSTS", assertion.getIssuer().getValue());

      // check the contents of the assertion conditions.
      ConditionsType conditions = assertion.getConditions();
      assertNotNull("Unexpected null conditions", conditions);
      assertNotNull("Unexpected null value for NotBefore attribute", conditions.getNotBefore());
      assertNotNull("Unexpected null value for NotOnOrAfter attribute", conditions.getNotOnOrAfter());
      assertEquals("Unexpected number of conditions", 1, conditions.getConditions()
            .size());
      assertTrue("Unexpected condition type",
            conditions.getConditions().get(0) instanceof AudienceRestrictionType);
      AudienceRestrictionType restrictionType = (AudienceRestrictionType) conditions
            .getConditions().get(0);
      assertNotNull("Unexpected null audience list", restrictionType.getAudience());
      assertEquals("Unexpected number of audience elements", 1, restrictionType.getAudience().size());
      assertEquals("Unexpected audience value", "http://services.testcorp.org/provider2", restrictionType.getAudience()
            .get(0).toString() );

      // check the contents of the assertion subject.
      SubjectType subject = assertion.getSubject();
      assertNotNull("Unexpected null subject", subject);

      NameIDType nameID = (NameIDType) subject.getSubType().getBaseID();
      assertEquals("Unexpected name id qualifier", "urn:picketlink:identity-federation", nameID.getNameQualifier());
      assertEquals("Unexpected name id", "bmozaffa", nameID.getValue());
      SubjectConfirmationType confirmation = (SubjectConfirmationType) subject.getConfirmation().get(0);
      assertEquals("Unexpected confirmation method", SAMLUtil.SAML2_BEARER_URI, confirmation.getMethod());
      
      StatementAbstractType statementAbstractType = assertion.getStatements().iterator().next() ;
      assertNotNull("Unexpected null StatementAbstractType", statementAbstractType);
      assertTrue("Unexpected type instead of AttributeStatement: " + statementAbstractType.getClass().getSimpleName(), statementAbstractType instanceof AttributeStatementType);
      AttributeStatementType attributeStatement = (AttributeStatementType)statementAbstractType;
      List<ASTChoiceType> attributes = attributeStatement.getAttributes();
      assertFalse("Unexpected empty list of attributes", attributes.isEmpty());
      assertEquals("Unexpected number of attributes", 1, attributes.size());
      Object attributeObject = attributes.iterator().next();
      ASTChoiceType astChoice = (ASTChoiceType) attributeObject;
      AttributeType attribute = astChoice.getAttribute();
      /*assertTrue("Unexpected type instead of AttributeStatement: " + attributeObject.getClass().getSimpleName(), attributeObject instanceof AttributeType);
      AttributeType attribute = (AttributeType)attributeObject;*/
      assertEquals("Unexpected name for the role attribute", "roleAttributeName", attribute.getName() );
      assertEquals("Unexpected number of roles", 1, attribute.getAttributeValue().size());
      assertEquals("Unexpected user role", "myTestRole", attribute.getAttributeValue().get(0));
   }
}

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
package org.picketlink.identity.federation.core.wstrust.handlers;

import static javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;

import org.junit.Before;
import org.junit.Test;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.util.SOAPUtil;
import org.picketlink.identity.federation.core.wstrust.STSClient;
import org.picketlink.identity.federation.core.wstrust.STSClientConfig.Builder;
import org.w3c.dom.Element;

/**
 * Unit test for {@link STSSaml20Handler}.
 * </p>
 * 
 * When running this unit test 'java.endorsed.dirs' must be set.
 * For example, if you are using Eclipse then you can set this in the
 * run configuration as a VM argument:
 * -Djava.endorsed.dirs=${project_loc}/src/test/resources/endorsed
 * 
 * This is not required when running the test through maven as this 
 * same setting exists in pom.xml.
 * 
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 * 
 */
public class STSSaml20HandlerTestCase
{
   private SOAPMessageContext soapMessageContext;

   private SOAPMessage soapMessage;

   private STSClient wsTrustClient;

   private STSSaml20Handler samlHandler;

   @Test
   public void handleMessageValidToken() throws Exception
   {
      when(wsTrustClient.validateToken((any(Element.class)))).thenReturn(true);

      final SOAPHeaderElement securityHeader = addSecurityHeader(soapMessage.getSOAPHeader());
      addSecurityAssertionElement(securityHeader);

      when(soapMessageContext.get(MESSAGE_OUTBOUND_PROPERTY)).thenReturn(false);
      when(soapMessageContext.getMessage()).thenReturn(soapMessage);

      boolean result = samlHandler.handleMessage(soapMessageContext);
      assertTrue(result);
   }

   @Test
   public void handleMessageInValidToken() throws Exception
   {
      when(wsTrustClient.validateToken((any(Element.class)))).thenReturn(false);

      final SOAPHeaderElement securityHeader = addSecurityHeader(soapMessage.getSOAPHeader());
      addSecurityAssertionElement(securityHeader);

      when(soapMessageContext.get(MESSAGE_OUTBOUND_PROPERTY)).thenReturn(false);
      when(soapMessageContext.getMessage()).thenReturn(soapMessage);
      try
      {
         samlHandler.handleMessage(soapMessageContext);
         fail("handleMessage should have thrown an exception");
      }
      catch (final Exception e)
      {
         assertTrue(e instanceof SOAPFaultException);
         assertSoapFaultString(e, "The security token could not be authenticated or authorized");
      }
   }

   @Test
   public void handleMessageMissingSecurityToken() throws Exception
   {
      when(soapMessageContext.get(MESSAGE_OUTBOUND_PROPERTY)).thenReturn(false);
      when(soapMessageContext.getMessage()).thenReturn(soapMessage);
      try
      {
         samlHandler.handleMessage(soapMessageContext);
         fail("handleMessage should have thrown a exception!");
      }
      catch (final Exception e)
      {
         assertTrue(e instanceof SOAPFaultException);
         assertSoapFaultString(e, ErrorCodes.NULL_VALUE + "No security token could be found in the SOAP Header");
      }
   }

   @Test
   public void handleMessageInvalidSecurityToken() throws Exception
   {
      when(wsTrustClient.validateToken((any(Element.class)))).thenReturn(false);

      final SOAPHeaderElement securityHeader = addSecurityHeader(soapMessage.getSOAPHeader());
      addSecurityAssertionElement(securityHeader);

      when(soapMessageContext.get(MESSAGE_OUTBOUND_PROPERTY)).thenReturn(false);
      when(soapMessageContext.getMessage()).thenReturn(soapMessage);
      try
      {
         samlHandler.handleMessage(soapMessageContext);
         fail("handleMessage should have thrown a exception!");
      }
      catch (final Exception e)
      {
         assertTrue(e instanceof SOAPFaultException);
         assertSoapFaultString(e, "The security token could not be authenticated or authorized");
      }
   }

   @Test
   public void usernamePasswordFromSOAPMessageContext() throws Exception
   {
      when(wsTrustClient.validateToken((any(Element.class)))).thenReturn(true);

      final SOAPHeaderElement securityHeader = addSecurityHeader(soapMessage.getSOAPHeader());
      addSecurityAssertionElement(securityHeader);

      when(soapMessageContext.get(MESSAGE_OUTBOUND_PROPERTY)).thenReturn(false);
      when(soapMessageContext.getMessage()).thenReturn(soapMessage);

      when(soapMessageContext.get(STSSecurityHandler.USERNAME_MSG_CONTEXT_PROPERTY)).thenReturn("Fletch");
      when(soapMessageContext.get(STSSecurityHandler.PASSWORD_MSG_CONTEXT_PROPERTY)).thenReturn("letmein");

      samlHandler.handleMessage(soapMessageContext);

      assertEquals("Fletch", samlHandler.getConfigBuilder().getUsername());
      assertEquals("letmein", samlHandler.getConfigBuilder().getPassword());
   }

   @Test
   public void handleMessageOutbound()
   {
      when(soapMessageContext.get(MESSAGE_OUTBOUND_PROPERTY)).thenReturn(true);
      assertTrue(new STSSaml20Handler().handleMessage(soapMessageContext));
   }

   @Before
   public void setUp()
   {
      // Create a Mock for WSTrustClient.
      wsTrustClient = mock(STSClient.class);

      samlHandler = new FakeSamlHandler(wsTrustClient);
      samlHandler.setConfigFile("wstrust/auth/jboss-sts-client.properties");
      // Simulate the WS Engine calling @PostConstruct.
      samlHandler.parseSTSConfig();

      soapMessageContext = mock(SOAPMessageContext.class);

      try
      {
         soapMessage = SOAPUtil.create();
      }
      catch (SOAPException e)
      {
         e.printStackTrace();
         fail(e.getMessage());
      }
   }

   private SOAPHeaderElement addSecurityHeader(final SOAPHeader soapHeader) throws SOAPException
   {
      final QName securityQName = samlHandler.getSecurityElementQName();
      final SOAPHeaderElement securityHeader = soapHeader.addHeaderElement(new QName(securityQName.getNamespaceURI(),
            securityQName.getLocalPart(), "wsse"));
      soapHeader.addChildElement(securityHeader);
      return securityHeader;
   }

   private SOAPElement addSecurityAssertionElement(final SOAPHeaderElement securityHeader) throws SOAPException
   {
      final QName tokenElementQName = this.samlHandler.getTokenElementQName();
      final SOAPElement tokenElement = securityHeader.addChildElement(new QName(tokenElementQName.getNamespaceURI(),
            tokenElementQName.getLocalPart(), "saml"));
      return securityHeader.addChildElement(tokenElement);
   }

   private void assertSoapFaultString(final Exception e, final String str)
   {
      SOAPFaultException soapFaultException = (SOAPFaultException) e;
      SOAPFault fault = soapFaultException.getFault();
      assertEquals(str, fault.getFaultString());
   }

   private class FakeSamlHandler extends STSSaml20Handler
   {
      private final STSClient stsClient;

      public FakeSamlHandler(final STSClient stsClient)
      {
         this.stsClient = stsClient;
      }

      @Override
      protected STSClient createSTSClient(Builder builder) throws ParsingException
      {
         return stsClient;
      }
   }
}

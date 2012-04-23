/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.test.trust.jbossws.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.security.acl.Group;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;
import org.jboss.security.SimplePrincipal;
import org.junit.Test;
import org.picketlink.identity.federation.core.util.SOAPUtil;
import org.picketlink.trust.jbossws.handler.SAML2Handler;

/**
 * Unit test the {@link SAML2Handler}
 * @author Anil.Saldhana@redhat.com
 * @since Jul 12, 2011
 */
public class SAML2HandlerUnitTestCase
{
   @Test
   public void testIn() throws Exception
   {
      DelegatingHandler handler = new DelegatingHandler(); 
      SAML2HandlerUnitTestCaseMessageContext msgContext = new SAML2HandlerUnitTestCaseMessageContext();
      SOAPMessage soapMessage = get();
      
      System.setProperty("picketlink.rolekey", "Role,Roles,Membership");
      msgContext.setMessage(soapMessage);
      handler.handleInbound(msgContext);
      
      SecurityContext securityContext = SecurityContextAssociation.getSecurityContext();
      assertNotNull(securityContext);
      assertEquals("admin", securityContext.getUtil().getUserName());
      Subject subject = securityContext.getUtil().getSubject();
      Set<Group> groups = subject.getPrincipals(Group.class);
      assertEquals(1, groups.size());
      Group grp = groups.iterator().next();
      assertTrue(grp.isMember(new SimplePrincipal("meco")));
   }
   
   private SOAPMessage get() throws SOAPException, IOException
   {
      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("jbossws/xml/wsse-saml.xml");
      SOAPMessage soapMessage = SOAPUtil.getSOAPMessage(is);
      return soapMessage;
   }
   
   private static class DelegatingHandler extends SAML2Handler
   { 
      @Override
      protected boolean handleInbound(MessageContext msgContext)
      { 
         return super.handleInbound(msgContext);
      } 
   }
   
   private static class SAML2HandlerUnitTestCaseMessageContext implements SOAPMessageContext
   { 
      private Map<String,Object> map = new HashMap<String, Object>();
      
      private Map<String,Scope> scopes = new HashMap<String, MessageContext.Scope>();
      
      private SOAPMessage msg;
      
      public int size()
      { 
         return 0;
      }

      public boolean isEmpty()
      { 
         return false;
      }

      public boolean containsKey(Object key)
      { 
         return map.containsKey(key);
      }

      public boolean containsValue(Object value)
      { 
         return map.containsValue(value);
      }

      public Object get(Object key)
      { 
         return map.get(key);
      }

      public Object put(String key, Object value)
      { 
         return map.put(key, value);
      }

      public Object remove(Object key)
      { 
         return map.remove(key);
      }

      public void putAll(Map<? extends String, ? extends Object> m)
      { 
         map.putAll(m);
      }

      public void clear()
      { 
         map.clear();
      }

      public Set<String> keySet()
      { 
         return map.keySet();
      }

      public Collection<Object> values()
      { 
         return map.values();
      }

      public Set<java.util.Map.Entry<String, Object>> entrySet()
      { 
         return map.entrySet();
      }

      public Scope getScope(String arg0)
      { 
         return scopes.get(arg0);
      }

      public void setScope(String arg0, Scope arg1)
      { 
         scopes.put(arg0, arg1);
      }

      public Object[] getHeaders(QName arg0, JAXBContext arg1, boolean arg2)
      { 
         throw new RuntimeException("NYI"); 
      }

      public SOAPMessage getMessage()
      { 
         return msg;
      }

      public Set<String> getRoles()
      {
         throw new RuntimeException("NYI");
      }

      public void setMessage(SOAPMessage arg0)
      {
         msg =arg0;
      }
   }
}
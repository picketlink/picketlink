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
package org.picketlink.identity.federation.core.wstrust.auth;

import java.util.HashMap;
import java.util.Map;

import org.picketlink.identity.federation.core.saml.v2.factories.JBossSAMLBaseFactory;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.w3c.dom.Element;

/**
 * Test util methods.
 * 
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 *
 */
public final class Util
{
   private Util()
   {
   }

   public static Element createSamlToken() throws Exception
   {
      String id = "ID+" + JBossSAMLBaseFactory.createUUID();
      final AssertionType assertionType = new AssertionType(id, XMLTimeUtil.getIssueInstant());
      return SAMLUtil.toElement(assertionType);
   }

   public static Map<String, String> allOptions()
   {
      Map<String, String> options = new HashMap<String, String>();
      options.put(AbstractSTSLoginModule.STS_CONFIG_FILE, "wstrust/auth/jboss-sts-client.properties");
      return options;
   }

}

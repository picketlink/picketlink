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
package org.picketlink.identity.federation.core.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the schemas for PicketLink
 * @author Anil.Saldhana@redhat.com
 * @since Jun 30, 2011
 */
public class SchemaManagerUtil
{
   public static List<String> getXMLSchemas()
   {
      List<String> list = new ArrayList<String>();

      list.add("schema/w3c/xmlschema/xml.xsd");
      return list;
   }

   public static List<String> getXMLDSig()
   {
      List<String> list = new ArrayList<String>();

      list.add("schema/w3c/xmldsig/xmldsig-core-schema.xsd");
      return list;
   }

   public static List<String> getXMLEnc()
   {
      List<String> list = new ArrayList<String>();

      list.add("schema/w3c/xmlenc/xenc-schema.xsd");
      return list;
   }

   public static List<String> getXACMLSchemas()
   {
      List<String> list = new ArrayList<String>();

      list.add("schema/access_control-xacml-2.0-policy-schema-os.xsd");
      list.add("schema/access_control-xacml-2.0-context-schema-os.xsd");
      return list;
   }

   public static List<String> getSAML2Schemas()
   {
      List<String> list = new ArrayList<String>();

      list.add("schema/saml/v2/saml-schema-assertion-2.0.xsd");
      list.add("schema/saml/v2/saml-schema-protocol-2.0.xsd");
      list.add("schema/saml/v2/saml-schema-metadata-2.0.xsd");
      list.add("schema/saml/v2/saml-schema-x500-2.0.xsd");
      list.add("schema/saml/v2/saml-schema-authn-context-2.0.xsd");
      list.add("schema/saml/v2/saml-schema-authn-context-types-2.0.xsd");
      list.add("schema/saml/v2/saml-schema-xacml-2.0.xsd");
      list.add("schema/saml/v2/access_control-xacml-2.0-saml-assertion-schema-os.xsd");
      list.add("schema/saml/v2/access_control-xacml-2.0-saml-protocol-schema-os.xsd");
      return list;
   }

   public static List<String> getSAML11Schemas()
   {
      List<String> list = new ArrayList<String>();

      list.add("schema/saml/v1/saml-schema-assertion-1.0.xsd");
      list.add("schema/saml/v1/oasis-sstc-saml-schema-assertion-1.1.xsd");
      list.add("schema/saml/v1/saml-schema-protocol-1.1.xsd");
      return list;
   }

   public static List<String> getWSTrustSchemas()
   {
      List<String> list = new ArrayList<String>();

      list.add("schema/wstrust/v1_3/ws-trust-1.3.xsd");
      list.add("schema/wstrust/v1_3/oasis-200401-wss-wssecurity-secext-1.0.xsd");
      list.add("schema/wstrust/v1_3/oasis-200401-wss-wssecurity-utility-1.0.xsd");
      list.add("schema/wstrust/v1_3/ws-policy.xsd");
      list.add("schema/wstrust/v1_3/ws-addr.xsd");
      return list;
   }

   public static List<String> getSchemas()
   {
      List<String> list = new ArrayList<String>();
      list.addAll(getXMLSchemas());
      list.addAll(getXMLDSig());
      list.addAll(getXMLEnc());
      list.addAll(getSAML2Schemas());
      list.addAll(getSAML11Schemas());
      list.addAll(getXACMLSchemas());
      list.addAll(getWSTrustSchemas());
      return list;
   }
}
/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.trust.jbossws;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Jason T. Greene
 */
public class Util
{
   public static int count = 0;
   
   public static String assignWsuId(Element element)
   {
      String id = element.getAttributeNS(Constants.WSU_NS, Constants.ID);

      if (id == null || id.length() < 1)
      {
         id = generateId();
         element.setAttributeNS(Constants.WSU_NS, Constants.WSU_ID, id);
         addNamespace(element, Constants.WSU_PREFIX, Constants.WSU_NS);
      }

      return id;
   }

   public static Element getFirstChildElement(Node node)
   {
      Node child = node.getFirstChild();
      while (child != null && child.getNodeType() != Node.ELEMENT_NODE)
         child = child.getNextSibling();

      return (Element)child;
   }

   public static Element getNextSiblingElement(Element element)
   {
      Node sibling = element.getNextSibling();
      while (sibling != null && sibling.getNodeType() != Node.ELEMENT_NODE)
         sibling = sibling.getNextSibling();

      return (Element)sibling;
   }

   public static Element getPreviousSiblingElement(Element element)
   {
      Node sibling = element.getPreviousSibling();
      while (sibling != null && sibling.getNodeType() != Node.ELEMENT_NODE)
         sibling = sibling.getPreviousSibling();

      return (Element)sibling;
   }

   public static Element findElement(Element root, String localName, String namespace)
   {
      return findElement(root, new QName(namespace, localName));
   }

   public static Element findElement(Element root, QName name)
   {
      // Here lies your standard recusive DFS.....
      if (matchNode(root, name))
         return root;

      // Search children
      for (Node child = root.getFirstChild(); child != null; child = child.getNextSibling())
      {
         if (child.getNodeType() != Node.ELEMENT_NODE)
            continue;

         Node possibleMatch = findElement((Element)child, name);
         if (possibleMatch != null)
            return (Element)possibleMatch;
      }

      return null;
   }

   public static List<Node> findAllElements(Element root, QName name, boolean local)
   {
      List<Node> list = new ArrayList<Node>();
      if (matchNode(root, name, local))
         list.add(root);

      for (Node child = root.getFirstChild(); child != null; child = child.getNextSibling())
      {
         if (child.getNodeType() != Node.ELEMENT_NODE)
            continue;

         list.addAll(findAllElements((Element) child, name, local));
      }

      return list;
   }

   public static Element findElementByWsuId(Element root, String id)
   {
      // Here lies another standard recusive DFS.....
      if (id.equals(getWsuId(root)))
         return root;

      // Search children
      for (Node child = root.getFirstChild(); child != null; child = child.getNextSibling())
      {
         if (child.getNodeType() != Node.ELEMENT_NODE)
            continue;

         Node possibleMatch = findElementByWsuId((Element)child, id);
         if (possibleMatch != null)
            return (Element)possibleMatch;
      }

      return null;
   }

   public static Element findOrCreateSoapHeader(Element envelope)
   {
      String prefix = envelope.getPrefix();
      String uri = envelope.getNamespaceURI();
      QName name = new QName(uri, "Header");
      Element header = findElement(envelope, name);
      if (header == null)
      {
         header = envelope.getOwnerDocument().createElementNS(uri, prefix + ":Header");
         envelope.insertBefore(header, envelope.getFirstChild());
      }

      return header;
   }

   public static String getWsuId(Element element)
   {
      if (element.hasAttributeNS(Constants.WSU_NS, Constants.ID))
         return element.getAttributeNS(Constants.WSU_NS, Constants.ID);

      if (element.hasAttribute(Constants.ID))
      {
         String ns = element.getNamespaceURI();
         if (Constants.XML_SIGNATURE_NS.equals(ns) || Constants.XML_ENCRYPTION_NS.equals(ns))
            return element.getAttribute(Constants.ID);
      }

      return null;
   }

   public static boolean equalStrings(String string1, String string2)
   {
      if (string1 == null && string2 == null)
         return true;

      return string1 != null && string1.equals(string2);
   }

   public static boolean matchNode(Node node, QName name)
   {
      return matchNode(node, name, false);
   }

   public static boolean matchNode(Node node, QName name, boolean local)
   {
      return equalStrings(node.getLocalName(), name.getLocalPart())
          && (local || equalStrings(node.getNamespaceURI(), name.getNamespaceURI()));
   }

   public static String generateId()
   {
      return generateId("element");
   }

   public static void addNamespace(Element element, String prefix, String uri)
   {
      element.setAttributeNS(Constants.XMLNS_NS, "xmlns:" + prefix, uri);
   }

   public static String generateId(String prefix)
   {
      StringBuilder id = new StringBuilder();
      long time = System.currentTimeMillis();

      // reasonably gaurantee uniqueness
      synchronized (Util.class)
      {
         count++;
      }

      id.append(prefix).append("-").append(count).append("-").append(time).append("-").append(id.hashCode());

      return id.toString();
   }
}

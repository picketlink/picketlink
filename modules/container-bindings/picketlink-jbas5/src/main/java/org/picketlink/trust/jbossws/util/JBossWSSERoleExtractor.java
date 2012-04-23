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
package org.picketlink.trust.jbossws.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Given a jboss-wsse.xml file, extract the roles
 * @author Anil.Saldhana@redhat.com
 * @since Apr 11, 2011
 */
public class JBossWSSERoleExtractor
{  
   public static final String UNCHECKED = "unchecked";
   
   /**
    * <p>
    * Given the jboss-wsse.xml inputstream, return the configured roles
    * </p>
    * <p>
    * Note that the <unchecked/> setting will yield a role of unchecked.
    * So special handling needs to be done by the caller.
    * </p>
    * @param is
    * @param portName optionally pass in a portName
    * @return a {@link List} of role names
    */
   public static List<String> getRoles(InputStream is, String portName, String operationName) throws ProcessingException
   {
      List<String> roles = new ArrayList<String>();
      try
      {
         Document doc = DocumentUtil.getDocument(is);
         NodeList nl = doc.getElementsByTagName("port");
         if( nl != null )
         {
            int len = nl.getLength();
            if( len > 0)
            {
               Node portNode = getNamedNode(nl, portName);
               if( portNode != null)
               {
                  roles.addAll( getRoles(portNode, operationName));
                  return roles;
               }
            } 
            return getDefaultRoles(doc.getDocumentElement());
         }
      }
      catch (ProcessingException e)
      {
         throw e;
      }
      catch( Exception e1)
      {
         throw new ProcessingException(e1);
      }
      return roles;
   }
   
   private static Node getNamedNode( NodeList nl, String portName)
   {
      int len = nl.getLength();
      for( int i = 0; i < len; i++)
      {
         Node n = nl.item(i);
         if( n.getNodeType() == Node.ELEMENT_NODE)
         {
            Node name = n.getAttributes().getNamedItem("name");
            if( portName.equals(name.getNodeValue()))
               return n;
         }
      }
      return null;
   }
   
   private static List<String> getRoles(Node node, String operationName) throws ProcessingException
   {
      List<String> roles = new ArrayList<String>(); 
      
      Element elem = (Element) node;
      //First check for operations
      NodeList ops = elem.getElementsByTagName("operation");
      if(ops.getLength() > 0 )
      {
         Node opNode = getNamedNode( ops, operationName);
         if( opNode != null)
            return getDefaultRoles((Element) opNode);
         return roles;
      }
      NodeList nl = elem.getElementsByTagName("authorize");
      if( nl != null )
      {
         int len = nl.getLength();

         if( len > 1 )
            throw new ProcessingException( ErrorCodes.PROCESSING_EXCEPTION + "More than one authorize element");
         Node authorize = nl.item(0);
         roles.addAll(getRolesFromAuthorize((Element) authorize));
      } 
      return roles;
   }
   
   private static List<String> getDefaultRoles(Element root) throws ProcessingException
   { 
      List<String> roles = new ArrayList<String>();
      NodeList children = root.getChildNodes();
      if( children != null )
      {
         int len  = children.getLength();
         //Go down tree and if you hit port, return
         for( int i = 0 ; i <len ; i++ )
         {
            Node n = children.item(i);
            if(n.getNodeType() == Node.ELEMENT_NODE)
            {
               Element newNode = (Element) n;
               if( newNode.getNodeName().equals("port"))
                  return roles;
               else if( newNode.getNodeName().equals("authorize"))
                  return getRolesFromAuthorize(newNode);
               else
                  roles = getDefaultRoles(newNode);
            }
         } 
      }
      
      return validate(roles);
   }
   
   private static List<String> validate( List<String> roles) throws ProcessingException
   {
      //Validate that we do not have unchecked and roles
      if(roles.contains(UNCHECKED) && roles.size() > 1)
         throw new ProcessingException(ErrorCodes.PROCESSING_EXCEPTION + "unchecked and role(s) cannot be together");
      return roles;
   }
   
   private static List<String> getRolesFromAuthorize( Element authorize) throws ProcessingException
   {
      List<String> roles = new ArrayList<String>(); 
      NodeList children = authorize.getChildNodes();

      int len = children.getLength();
      for( int i = 0 ; i < len; i++ )
      {
         Node child = children.item(i);
         if( child instanceof Element)
         {
            String nodeName = child.getNodeName();
            if( "unchecked".equals( nodeName) )
            {
               roles.add(nodeName); 
            } 
            else if("role".equals(nodeName))
            {
               roles.add(child.getChildNodes().item(0).getNodeValue());
            }
         } 
      }
      return validate(roles);
   }
}
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
package org.picketlink.identity.federation.core.parsers;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.parsers.wsa.WSAddressingParser;
import org.picketlink.identity.federation.core.parsers.wsp.WSPolicyParser;
import org.picketlink.identity.federation.core.parsers.wsse.WSSecurityParser;
import org.picketlink.identity.federation.core.parsers.wst.WSTCancelTargetParser;
import org.picketlink.identity.federation.core.parsers.wst.WSTRenewTargetParser;
import org.picketlink.identity.federation.core.parsers.wst.WSTRequestSecurityTokenCollectionParser;
import org.picketlink.identity.federation.core.parsers.wst.WSTRequestSecurityTokenParser;
import org.picketlink.identity.federation.core.parsers.wst.WSTValidateTargetParser;
import org.picketlink.identity.federation.core.parsers.wst.WSTrustOnBehalfOfParser;
import org.picketlink.identity.federation.core.parsers.wst.WSTrustParser;

/**
 * <p>
 * A Controller that can act as the front door
 * for parsing or when you need to locate a parser
 * that is capable of parsing a {@code QName}
 * <p>
 * @author Anil.Saldhana@redhat.com
 * @since Oct 14, 2010
 */
public class ParserController
{
   private static List<ParserNamespaceSupport> parsers = new ArrayList<ParserNamespaceSupport>();
   
   private static RuntimePermission PARSER_PERM = new RuntimePermission ( "org.picketlink.parser.permission" );
   
   
   static
   {

      add( new SAMLParser() );
      add( new WSTrustParser() ); 
      
      add( new WSSecurityParser() );
      add( new WSPolicyParser() );
      add( new WSAddressingParser() );
      
      
      add( new WSTrustOnBehalfOfParser() );
      add( new WSTValidateTargetParser() );
      add( new WSTRenewTargetParser() );
      add( new WSTCancelTargetParser() );
      add( new WSTRequestSecurityTokenParser() );
      add( new WSTRequestSecurityTokenCollectionParser() ); 
   };
   
   /**
    * <p>
    * Add an {@code ParserNamespaceSupport} parser
    * </p>
    * 
    * <p>
    * Under a Java security manager, the following run time permission is required.
    * "org.picketlink.parser.permission"
    * </p>
    * @param parser
    */
   public static void add( ParserNamespaceSupport parser )
   {
      if( System.getSecurityManager() != null )
      {
         System.getSecurityManager().checkPermission( PARSER_PERM );
      }
      
      parsers.add( 0, parser ); 
   }
   
   /**
    * Get an {@code ParserNamespaceSupport} that supports parsing the qname
    * @param qname
    * @return A supporting parser or null
    */
   public static ParserNamespaceSupport get( QName qname )
   {
      int size = parsers.size();
      if( size  > 0 )
      {
         for( ParserNamespaceSupport parser : parsers )
         {
            if( parser.supports(qname) )
               return parser;
         }
      } 
      return null;
   }
   
   /**
    * <p>
    * Clear the registered parsers.
    * <b>Note:</b> You really need to have a reason to perform this operation.
    * Once you have cleared the parsers, you have the opportunity to register
    * new parsers with {@code #add(ParserNamespaceSupport)} call.
    * </p>
    * 
    * <p>
    * Under a Java security manager, the following run time permission is required.
    * "org.picketlink.parser.permission"
    * </p>
    */
   public static void clearAll()
   {
      if( System.getSecurityManager() != null )
      {
         System.getSecurityManager().checkPermission( PARSER_PERM );
      }
      
      parsers.clear();
   }
}
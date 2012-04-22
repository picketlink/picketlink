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
package org.picketlink.identity.federation.core.saml.v2.holders;

/**
 * Holder containing the information about a destination
 * @author Anil.Saldhana@redhat.com
 * @since Jul 24, 2009
 */
public class DestinationInfoHolder
{
   private String destination;
   private String samlMessage;
   private String relayState;
   
   /**
    * Create an holder
    * @param destination The destination where the post will be sent
    * @param samlMessage SAML Message
    * @param relayState
    */
   public DestinationInfoHolder(String destination, String samlMessage, String relayState)
   { 
      this.destination = destination;
      this.samlMessage = samlMessage;
      this.relayState = relayState;
   } 

   public String getDestination()
   {
      return destination;
   } 

   public String getSamlMessage()
   {
      return samlMessage;
   }
   
   public String getRelayState()
   {
      return relayState;
   }
}

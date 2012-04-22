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
package org.picketlink.identity.federation.web.core;

/**
 * A stack to register and deregister participants
 * in the identity federation
 * @author Anil.Saldhana@redhat.com
 * @since Feb 25, 2011
 */
public interface IdentityParticipantStack
{

   /**
    * Peek at the most recent participant in the session
    * @param sessionID
    * @return
    */
   String peek(String sessionID);

   /**
    * Remove the most recent participant in the session
    * @param sessionID
    * @return
    */
   String pop(String sessionID);

   /**
    * Register a participant in a session
    * @param sessionID
    * @param participant
    */
   void register(String sessionID, String participant, boolean postBinding);

   /**
    * For a given identity session, return the number of participants
    * @param sessionID
    * @return
    */
   int getParticipants(String sessionID);

   /**
    * Register a participant as in transit in a logout interaction
    * @param sessionID
    * @param participant
    * @return
    */
   boolean registerTransitParticipant(String sessionID, String participant);

   /**
    * Deregister a participant as in transit in a logout interaction
    * @param sessionID
    * @param participant
    * @return
    */
   boolean deRegisterTransitParticipant(String sessionID, String participant);

   /**
    * Return the number of participants in transit
    * @param sessionID
    * @return
    */
   int getNumOfParticipantsInTransit(String sessionID);

   /**
    * <p>
    * For a particular participant, indicate whether it supports
    * POST or REDIRECT binding.
    * </p>
    * <p>
    * <b>NOTE:</b> true: POST, false: REDIRECT, null: does not exist
    * </p>
    * @param participant
    * @return
    */
   Boolean getBinding(String participant);

   /**
    * The total number of sessions active
    * @return
    */
   int totalSessions();
   
   /**
    * Create a session
    * @param id
    */
   void createSession( String id );
   
   /**
    * Remove a session
    * @param id
    */
   void removeSession( String id );

}
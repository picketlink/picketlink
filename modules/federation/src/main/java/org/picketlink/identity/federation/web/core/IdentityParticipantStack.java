/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.identity.federation.web.core;

/**
 * A stack to register and deregister participants in the identity federation
 *
 * @author Anil.Saldhana@redhat.com
 * @since Feb 25, 2011
 */
public interface IdentityParticipantStack {

    /**
     * Peek at the most recent participant in the session
     *
     * @param sessionID
     *
     * @return
     */
    String peek(String sessionID);

    /**
     * Remove the most recent participant in the session
     *
     * @param sessionID
     *
     * @return
     */
    String pop(String sessionID);

    /**
     * Register a participant in a session
     *
     * @param sessionID
     * @param participant
     */
    void register(String sessionID, String participant, boolean postBinding);

    /**
     * For a given identity session, return the number of participants
     *
     * @param sessionID
     *
     * @return
     */
    int getParticipants(String sessionID);

    /**
     * Register a participant as in transit in a logout interaction
     *
     * @param sessionID
     * @param participant
     *
     * @return
     */
    boolean registerTransitParticipant(String sessionID, String participant);

    /**
     * Deregister a participant as in transit in a logout interaction
     *
     * @param sessionID
     * @param participant
     *
     * @return
     */
    boolean deRegisterTransitParticipant(String sessionID, String participant);

    /**
     * Return the number of participants in transit
     *
     * @param sessionID
     *
     * @return
     */
    int getNumOfParticipantsInTransit(String sessionID);

    /**
     * <p>
     * For a particular participant, indicate whether it supports POST or REDIRECT binding.
     * </p>
     * <p>
     * <b>NOTE:</b> true: POST, false: REDIRECT, null: does not exist
     * </p>
     *
     * @param participant
     *
     * @return
     */
    Boolean getBinding(String participant);

    /**
     * The total number of sessions active
     *
     * @return
     */
    int totalSessions();

    /**
     * Create a session
     *
     * @param id
     */
    void createSession(String id);

    /**
     * Remove a session
     *
     * @param id
     */
    void removeSession(String id);

}
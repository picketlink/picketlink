/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.picketlink.test.identity.federation.bindings.authenticators.idp;

import org.picketlink.identity.federation.web.core.IdentityParticipantStack;
import org.picketlink.identity.federation.web.core.IdentityServer;
import org.picketlink.identity.federation.web.core.IdentityServer.STACK;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class TestIdentityParticipantStack implements IdentityParticipantStack {

    private static IdentityServer.STACK delegate = createDelegate();
    
    private static STACK createDelegate() {
        return new IdentityServer.STACK();
    }
    
    public static IdentityParticipantStack getDelegate() {
        return delegate;
    }

    public static void reset() {
        delegate = createDelegate(); 
    }

    /**
     * @return
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return delegate.hashCode();
    }

    /**
     * @param sessionID
     * @return
     * @see org.picketlink.identity.federation.web.core.IdentityServer.STACK#peek(java.lang.String)
     */
    public String peek(String sessionID) {
        return delegate.peek(sessionID);
    }

    /**
     * @param sessionID
     * @return
     * @see org.picketlink.identity.federation.web.core.IdentityServer.STACK#pop(java.lang.String)
     */
    public String pop(String sessionID) {
        return delegate.pop(sessionID);
    }

    /**
     * @param sessionID
     * @param participant
     * @param postBinding
     * @see org.picketlink.identity.federation.web.core.IdentityServer.STACK#register(java.lang.String, java.lang.String, boolean)
     */
    public void register(String sessionID, String participant, boolean postBinding) {
        delegate.register(sessionID, participant, postBinding);
    }

    /**
     * @param obj
     * @return
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    /**
     * @param sessionID
     * @return
     * @see org.picketlink.identity.federation.web.core.IdentityServer.STACK#getParticipants(java.lang.String)
     */
    public int getParticipants(String sessionID) {
        return delegate.getParticipants(sessionID);
    }

    /**
     * @param sessionID
     * @param participant
     * @return
     * @see org.picketlink.identity.federation.web.core.IdentityServer.STACK#registerTransitParticipant(java.lang.String, java.lang.String)
     */
    public boolean registerTransitParticipant(String sessionID, String participant) {
        return delegate.registerTransitParticipant(sessionID, participant);
    }

    /**
     * @param sessionID
     * @param participant
     * @return
     * @see org.picketlink.identity.federation.web.core.IdentityServer.STACK#deRegisterTransitParticipant(java.lang.String, java.lang.String)
     */
    public boolean deRegisterTransitParticipant(String sessionID, String participant) {
        return delegate.deRegisterTransitParticipant(sessionID, participant);
    }

    /**
     * @param sessionID
     * @return
     * @see org.picketlink.identity.federation.web.core.IdentityServer.STACK#getNumOfParticipantsInTransit(java.lang.String)
     */
    public int getNumOfParticipantsInTransit(String sessionID) {
        return delegate.getNumOfParticipantsInTransit(sessionID);
    }

    /**
     * @param participant
     * @return
     * @see org.picketlink.identity.federation.web.core.IdentityServer.STACK#getBinding(java.lang.String)
     */
    public Boolean getBinding(String participant) {
        return delegate.getBinding(participant);
    }

    /**
     * @return
     * @see org.picketlink.identity.federation.web.core.IdentityServer.STACK#totalSessions()
     */
    public int totalSessions() {
        return delegate.totalSessions();
    }

    /**
     * @param id
     * @see org.picketlink.identity.federation.web.core.IdentityServer.STACK#createSession(java.lang.String)
     */
    public void createSession(String id) {
        delegate.createSession(id);
    }

    /**
     * @param id
     * @see org.picketlink.identity.federation.web.core.IdentityServer.STACK#removeSession(java.lang.String)
     */
    public void removeSession(String id) {
        delegate.removeSession(id);
    }

    /**
     * @return
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return delegate.toString();
    }    
    
}

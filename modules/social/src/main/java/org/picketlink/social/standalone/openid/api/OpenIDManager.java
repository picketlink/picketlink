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
package org.picketlink.social.standalone.openid.api;

import org.openid4java.association.AssociationException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;
import org.openid4java.consumer.InMemoryNonceVerifier;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.sreg.SRegRequest;
import org.picketlink.social.standalone.openid.api.exceptions.OpenIDAssociationException;
import org.picketlink.social.standalone.openid.api.exceptions.OpenIDConsumerException;
import org.picketlink.social.standalone.openid.api.exceptions.OpenIDDiscoveryException;
import org.picketlink.social.standalone.openid.api.exceptions.OpenIDLifeCycleException;
import org.picketlink.social.standalone.openid.api.exceptions.OpenIDMessageException;
import org.picketlink.social.standalone.openid.api.exceptions.OpenIDProtocolException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * OpenID Manager for consumers
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jul 6, 2009
 */
public class OpenIDManager {

    public enum CONST {
        OPENID("openid"), OPENID_CLAIMED("openid-claimed"), OPENID_DISC("openid-discovered");

        private String val;

        CONST(String val) {
            this.val = val;
        }

        public String get() {
            return this.val;
        }
    }

    private OpenIDRequest request = null;

    private ConsumerManager consumerManager = null;

    private String userString = null;

    public OpenIDManager(OpenIDRequest theReq) {
        this.request = theReq;
        consumerManager = new ConsumerManager();
        consumerManager.setAssociations(new InMemoryConsumerAssociationStore());
        consumerManager.setNonceVerifier(new InMemoryNonceVerifier(5000));
        userString = request.getURL();
    }

    /**
     * Set the user string
     *
     * @param userString
     */
    public void setUserString(String userString) {
        this.userString = userString;
    }

    /**
     * Get the OpenID Request
     *
     * @return
     */
    public OpenIDRequest getOpenIDRequest() {
        return this.request;
    }

    @SuppressWarnings("unchecked")
    public OpenIDProviderList discoverProviders() throws OpenIDDiscoveryException, OpenIDConsumerException {
        // perform discovery on the user-supplied identifier
        List<DiscoveryInformation> discoveries;
        try {
            discoveries = consumerManager.discover(userString);
        } catch (DiscoveryException e1) {
            throw new OpenIDDiscoveryException(e1);
        }

        return new OpenIDProviderList(discoveries);
    }

    /**
     * Associate with a list of open id providers
     *
     * @param adapter Protocol adapter (such as http)
     * @param listOfProviders (a list of providers from discovery)
     *
     * @return
     *
     * @throws OpenIDConsumerException
     * @throws OpenIDLifeCycleException
     */
    public OpenIDProviderInformation associate(OpenIDProtocolAdapter adapter, OpenIDProviderList listOfProviders)
            throws OpenIDConsumerException, OpenIDLifeCycleException {
        OpenIDLifecycle lifeCycle = null;

        if (adapter instanceof OpenIDLifecycle) {
            lifeCycle = (OpenIDLifecycle) adapter;
        }
        List<DiscoveryInformation> discoveries = listOfProviders.get();

        if (discoveries.size() == 0)
            throw new OpenIDConsumerException("No open id endpoints discovered");

        // attempt to associate with the OpenID provider
        // and retrieve one service endpoint for authentication
        DiscoveryInformation discovered = consumerManager.associate(discoveries);

        // store the discovery information in the user's session for later use
        // leave out for stateless operation / if there is no session
        if (lifeCycle != null) {
            OpenIDLifecycleEvent ev = new OpenIDLifecycleEvent(OpenIDLifecycleEvent.TYPE.SESSION, OpenIDLifecycleEvent.OP.ADD, CONST.OPENID_DISC.get(), discovered);
            lifeCycle.handle(ev);
        }
        return new OpenIDProviderInformation(discovered);
    }

    /**
     * Authenticate an user with the provider
     *
     * @param adapter protocol adapter
     * @param providerInfo Information about a provider derived from discovery process
     *
     * @return
     *
     * @throws OpenIDDiscoveryException
     * @throws OpenIDConsumerException
     * @throws OpenIDMessageException
     * @throws OpenIDProtocolException
     */
    @SuppressWarnings("unchecked")
    public boolean authenticate(OpenIDProtocolAdapter adapter, OpenIDProviderInformation providerInfo)
            throws OpenIDDiscoveryException, OpenIDConsumerException, OpenIDMessageException, OpenIDProtocolException {
        DiscoveryInformation discovered = providerInfo.get();

        // obtain a AuthRequest message to be sent to the OpenID provider
        try {
            AuthRequest authReq = consumerManager.authenticate(discovered, adapter.getReturnURL());

            // Attribute Exchange example: fetching the 'email' attribute
            FetchRequest fetch = FetchRequest.createFetchRequest();
            SRegRequest sregReq = SRegRequest.createFetchRequest();

            OpenIDAttributeMap amap = adapter.getAttributeMap();

            if ("1".equals(amap.get("nickname"))) {
                // fetch.addAttribute("nickname",
                // "http://schema.openid.net/contact/nickname", false);
                sregReq.addAttribute("nickname", false);
            }

            if ("1".equals(amap.get("email"))) {
                fetch.addAttribute("email", OpenIDConstants.EMAIL.url(), false);
                sregReq.addAttribute("email", false);
            }

            if ("1".equals(amap.get("fullname"))) {
                fetch.addAttribute("fullname", OpenIDConstants.FULLNAME.url(), false);
                sregReq.addAttribute("fullname", false);
            }
            if ("1".equals(amap.get("dob"))) {
                fetch.addAttribute("dob", OpenIDConstants.DOB.url(), true);
                sregReq.addAttribute("dob", false);
            }

            if ("1".equals(amap.get("gender"))) {
                fetch.addAttribute("gender", OpenIDConstants.GENDER.url(), false);
                sregReq.addAttribute("gender", false);
            }

            if ("1".equals(amap.get("postcode"))) {
                fetch.addAttribute("postcode", OpenIDConstants.POSTCODE.url(), false);
                sregReq.addAttribute("postcode", false);
            }

            if ("1".equals(amap.get("country"))) {
                fetch.addAttribute("country", OpenIDConstants.COUNTRY.url(), false);
                sregReq.addAttribute("country", false);
            }

            if ("1".equals(amap.get("language"))) {
                fetch.addAttribute("language", OpenIDConstants.LANGUAGE.url(), false);
                sregReq.addAttribute("language", false);
            }

            if ("1".equals(amap.get("timezone"))) {
                fetch.addAttribute("timezone", OpenIDConstants.TIMEZONE.url(), false);
                sregReq.addAttribute("timezone", false);
            }

            // attach the extension to the authentication request
            if (!sregReq.getAttributes().isEmpty()) {
                authReq.addExtension(sregReq);
            }

            if (!discovered.isVersion2()) {
                // Option 1: GET HTTP-redirect to the OpenID Provider endpoint
                // The only method supported in OpenID 1.x
                // redirect-URL usually limited ~2048 bytes
                adapter.sendToProvider(1, authReq.getDestinationUrl(true), null);
                return true;
            } else {
                // Option 2: HTML FORM Redirection (Allows payloads >2048 bytes)
                adapter.sendToProvider(2, authReq.getDestinationUrl(false), authReq.getParameterMap());
            }
        } catch (MessageException e) {
            throw new OpenIDMessageException(e);
        } catch (ConsumerException e) {
            throw new OpenIDConsumerException(e);
        }
        return false;
    }

    /**
     * Verify a previously authenticated user with the provider
     *
     * @param adapter protocol adapter
     * @param parameterMap request parameters
     * @param receivedURL url where the response will be received
     *
     * @return
     *
     * @throws OpenIDMessageException
     * @throws OpenIDDiscoveryException
     * @throws OpenIDAssociationException
     * @throws OpenIDLifeCycleException
     */
    public boolean verify(OpenIDProtocolAdapter adapter, Map<String, String> parameterMap, String receivedURL)
            throws OpenIDMessageException, OpenIDDiscoveryException, OpenIDAssociationException, OpenIDLifeCycleException {
        OpenIDLifecycle lifeCycle = null;

        if (adapter instanceof OpenIDLifecycle) {
            lifeCycle = (OpenIDLifecycle) adapter;
        }
        ParameterList responselist = new ParameterList(parameterMap);

        if (lifeCycle == null)
            throw new IllegalStateException("Lifecycle not found");

        DiscoveryInformation discovered = (DiscoveryInformation) lifeCycle.getAttributeValue(CONST.OPENID_DISC.get());

        // verify the response; ConsumerManager needs to be the same
        // (static) instance used to place the authentication request
        try {
            VerificationResult verification = this.consumerManager.verify(receivedURL, responselist, discovered);

            // examine the verification result and extract the verified identifier
            Identifier verified = verification.getVerifiedId();
            if (verified != null) {
                AuthSuccess authSuccess = (AuthSuccess) verification.getAuthResponse();

                // Create an lifecycle event array
                OpenIDLifecycleEvent[] eventArr = new OpenIDLifecycleEvent[]{
                        /** Store the id **/
                        new OpenIDLifecycleEvent(OpenIDLifecycleEvent.TYPE.SESSION, OpenIDLifecycleEvent.OP.ADD, CONST.OPENID.get(), authSuccess.getIdentity()),

                        /** Store the claimed **/
                        new OpenIDLifecycleEvent(OpenIDLifecycleEvent.TYPE.SESSION, OpenIDLifecycleEvent.OP.ADD, CONST.OPENID_CLAIMED.get(), authSuccess.getClaimed()),

                        /** Indicate success **/
                        new OpenIDLifecycleEvent(OpenIDLifecycleEvent.TYPE.SUCCESS, null, null, null)};
                lifeCycle.handle(eventArr);
                return true;
            }
        } catch (MessageException e) {
            throw new OpenIDMessageException(e);
        } catch (DiscoveryException e) {
            throw new OpenIDDiscoveryException(e);
        } catch (AssociationException e) {
            throw new OpenIDAssociationException(e);
        }

        return false;
    }

    /**
     * Log an user out from an openid provider
     *
     * @param adapter protocol adapter
     *
     * @throws OpenIDLifeCycleException
     */
    public void logout(OpenIDProtocolAdapter adapter) throws OpenIDLifeCycleException {
        OpenIDLifecycle lifeCycle = null;

        if (adapter instanceof OpenIDLifecycle) {
            lifeCycle = (OpenIDLifecycle) adapter;
        }
        if (lifeCycle != null) {
            lifeCycle.handle(new OpenIDLifecycleEvent(OpenIDLifecycleEvent.TYPE.SESSION, OpenIDLifecycleEvent.OP.REMOVE, CONST.OPENID.get(), null));
            lifeCycle.handle(new OpenIDLifecycleEvent(OpenIDLifecycleEvent.TYPE.SESSION, OpenIDLifecycleEvent.OP.REMOVE, CONST.OPENID_CLAIMED.get(), null));
        }
    }

    /**
     * Information about a provider from the discovery process
     */
    public static class OpenIDProviderInformation {

        private DiscoveryInformation discovered;

        OpenIDProviderInformation(DiscoveryInformation di) {
            this.discovered = di;
        }

        DiscoveryInformation get() {
            return this.discovered;
        }
    }

    /**
     * List of OpenID providers
     */
    public static class OpenIDProviderList {

        private List<DiscoveryInformation> providers = null;

        OpenIDProviderList(List<DiscoveryInformation> providers) {
            this.providers = providers;
        }

        void addProvider(DiscoveryInformation provider) {
            this.providers.add(provider);
        }

        List<DiscoveryInformation> get() {
            return Collections.unmodifiableList(providers);
        }

        public int size() {
            return this.providers != null ? providers.size() : 0;
        }
    }
}

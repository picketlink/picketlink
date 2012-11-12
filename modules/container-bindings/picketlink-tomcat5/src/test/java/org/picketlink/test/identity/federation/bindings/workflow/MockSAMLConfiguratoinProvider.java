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

package org.picketlink.test.identity.federation.bindings.workflow;

import org.picketlink.identity.federation.core.config.AuthPropertyType;
import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.config.KeyProviderType;
import org.picketlink.identity.federation.core.config.KeyValueType;
import org.picketlink.identity.federation.core.config.PicketLinkType;
import org.picketlink.identity.federation.core.config.ProviderType;
import org.picketlink.identity.federation.core.config.SPType;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.handler.config.Handler;
import org.picketlink.identity.federation.core.handler.config.Handlers;
import org.picketlink.identity.federation.web.config.AbstractSAMLConfigurationProvider;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class MockSAMLConfiguratoinProvider extends AbstractSAMLConfigurationProvider {
    
    private ProviderType providerType;

    public MockSAMLConfiguratoinProvider(ProviderType providerType) {
        this.providerType = providerType;
    }

    @Override
    public SPType getSPConfiguration() throws ProcessingException {
        configureDefaultKeyProvider();
        return (SPType) this.providerType;
    }

    private void configureDefaultKeyProvider() {
        this.providerType.setKeyProvider(new KeyProviderType());
        this.providerType.getKeyProvider().setClassName("org.picketlink.identity.federation.core.impl.KeyStoreKeyManager");

        this.providerType.getKeyProvider().add(createAuthProperty("KeyStoreURL", "keystore/jbid_test_keystore.jks"));
        this.providerType.getKeyProvider().add(createAuthProperty("KeyStorePass", "store123"));
        this.providerType.getKeyProvider().add(createAuthProperty("SigningKeyPass", "test123"));
        this.providerType.getKeyProvider().add(createAuthProperty("SigningKeyAlias", "servercert"));

        this.providerType.getKeyProvider().add(createKeyProperty("localhost", "servercert"));
    }

    private KeyValueType createKeyProperty(String key, String value) {
        KeyValueType kv = new KeyValueType();

        kv.setKey(key);
        kv.setValue(value);
        return kv;
    }

    private AuthPropertyType createAuthProperty(String key, String value) {
        AuthPropertyType kv = new AuthPropertyType();

        kv.setKey(key);
        kv.setValue(value);
        return kv;
    }

    private Handler createHandler(String clazz) {
        Handler handler = new Handler();

        handler.setClazz(clazz);

        return handler;
    }

    @Override
    public PicketLinkType getPicketLinkConfiguration() throws ProcessingException {
        PicketLinkType picketLinkType = new PicketLinkType();
        
        picketLinkType.setIdpOrSP(this.providerType);
        
        picketLinkType.setHandlers(new Handlers());

        picketLinkType.getHandlers().add(createHandler("org.picketlink.identity.federation.web.handlers.saml2.SAML2LogOutHandler"));
        picketLinkType.getHandlers().add(createHandler("org.picketlink.identity.federation.web.handlers.saml2.SAML2AuthenticationHandler"));
        picketLinkType.getHandlers().add(createHandler("org.picketlink.identity.federation.web.handlers.saml2.RolesGenerationHandler"));
        picketLinkType.getHandlers().add(createHandler("org.picketlink.identity.federation.web.handlers.saml2.SAML2SignatureGenerationHandler"));
        picketLinkType.getHandlers().add(createHandler("org.picketlink.identity.federation.web.handlers.saml2.SAML2SignatureValidationHandler"));

        return picketLinkType;
    }

    @Override
    public IDPType getIDPConfiguration() throws ProcessingException {
        configureDefaultKeyProvider();
        return (IDPType) this.providerType;
    }
    
}

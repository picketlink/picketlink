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
/**
 * Author: coluccelli@redhat.com
 */
package org.picketlink.identity.federation.core.saml.md.providers;

import org.picketlink.config.federation.KeyValueType;
import org.picketlink.config.federation.PicketLinkType;
import org.picketlink.config.federation.ProviderType;
import org.picketlink.config.federation.handler.Handler;
import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.constants.JBossSAMLConstants;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.interfaces.IMetadataProvider;
import org.picketlink.identity.federation.saml.v2.metadata.AttributeConsumingServiceType;
import org.picketlink.identity.federation.saml.v2.metadata.EndpointType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.SPSSODescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.IndexedEndpointType;
import org.picketlink.identity.federation.saml.v2.metadata.RequestedAttributeType;
import org.picketlink.identity.federation.saml.v2.metadata.LocalizedNameType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;

import java.io.InputStream;
import java.net.URI;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Metadata provider for SP
 *
 * Author: coluccelli@redhat.com
 *
 */
public class SPMetadataProvider extends AbstractMetadataProvider implements
        IMetadataProvider<EntityDescriptorType> {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();
    private static final String ENTITY_ID_KEY="EntityId";
    private static final String PROTOCOL = "urn:oasis:names:tc:SAML:2.0:protocol";
    private static final String ATTRIBUTE_KEYS = "ATTRIBUTE_KEYS";
    private static final String SERVICE_NAME = "ServiceName" ;
    private String entityId;
    private String logoutPage;
    private String bindingUri;
    private String serviceUrl;
    private String logoutResponseLocation;
    private String serviceName;
    private String nameIdFormat;
    private PicketLinkType picketLinkType;

    @Override
    public void init(Map<String, String> options) {
        super.init(options);
        entityId = options.get(ENTITY_ID_KEY);
        if (entityId == null)
            throw logger.optionNotSet("EntityId");
        ProviderType providerType = MetadataProviderUtils.getProviderType(picketLinkType);
        //Add parameters from picket-link.xml
        String bindingURI = MetadataProviderUtils.getBindingURI(providerType);
        if (bindingURI == null) throw new RuntimeException("bindingURI cannot be null");

        logoutPage = MetadataProviderUtils.getLogoutURL(providerType);
        logoutResponseLocation = MetadataProviderUtils.getLogoutResponseLocation(providerType);
        bindingUri = bindingURI;
        serviceUrl = MetadataProviderUtils.getServiceURL(providerType);
        serviceName = options.get(SERVICE_NAME);
        nameIdFormat = getNameIdFormat();

    }

    @Override
    public EntityDescriptorType getMetaData() {
        ArrayList<String> protocols = new ArrayList<String>();
        protocols.add(PROTOCOL);
        SPSSODescriptorType spSSO = new SPSSODescriptorType(protocols);
        spSSO.setAuthnRequestsSigned(true);
        spSSO.setWantAssertionsSigned(true);
        if (bindingUri!=null && logoutPage != null) {
            EndpointType endpointType = new EndpointType(URI.create(bindingUri), URI.create(logoutPage));
            endpointType.setResponseLocation(URI.create(logoutResponseLocation));
            spSSO.addSingleLogoutService(endpointType);
        }
        IndexedEndpointType assertionConsumerSvc = new IndexedEndpointType(URI.create(bindingUri), URI.create(serviceUrl));
        assertionConsumerSvc.setIsDefault(true);
        spSSO.addAssertionConsumerService(assertionConsumerSvc);
        if (serviceName != null) {
            spSSO.addAttributeConsumerService(getAttributeConsumerService());
            if(nameIdFormat != null) {
                spSSO.addNameIDFormat(nameIdFormat);
            }
        }
        EntityDescriptorType.EDTDescriptorChoiceType edtDescChoice = new EntityDescriptorType.EDTDescriptorChoiceType(spSSO);
        EntityDescriptorType.EDTChoiceType edtChoice = EntityDescriptorType.EDTChoiceType.oneValue(edtDescChoice);

        EntityDescriptorType entityDescriptor = new EntityDescriptorType(entityId);
        entityDescriptor.addChoiceType(edtChoice);
        return entityDescriptor;
    }

    private AttributeConsumingServiceType getAttributeConsumerService() {
        try {
            Handler attributeHandler = MetadataProviderUtils.getHandler(picketLinkType,
                    "org.picketlink.identity.federation.web.handlers.saml2.SAML2AttributeHandler");
            List<KeyValueType> options = attributeHandler.getOption();
            ArrayList<String> attributeVals = new ArrayList<String>();
            for(KeyValueType option:options)
                if (option.getKey().equals(ATTRIBUTE_KEYS)){
                    for(String str:option.getValue().split(","))
                        attributeVals.add(str);
                }

            AttributeConsumingServiceType attributeConsumingService = new AttributeConsumingServiceType(0);
            for(String attributeVal:attributeVals){
                RequestedAttributeType requestedAttributeType = new RequestedAttributeType(attributeVal);
                requestedAttributeType.setIsRequired(true);
                requestedAttributeType.setNameFormat(JBossSAMLURIConstants.ATTRIBUTE_FORMAT_BASIC.get());
                attributeConsumingService.addRequestedAttribute(requestedAttributeType);
            }

            LocalizedNameType serviceLocName = new LocalizedNameType(JBossSAMLConstants.LANG_EN.get());
            serviceLocName.setValue(serviceName);
            attributeConsumingService.addServiceName(serviceLocName);

            attributeConsumingService.setIsDefault(true);

            return attributeConsumingService;
        } catch (ParsingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getNameIdFormat() {
        try {

            Handler authHandler = MetadataProviderUtils.getHandler(picketLinkType,
                    "org.picketlink.identity.federation.web.handlers.saml2.SAML2AuthenticationHandler");
            List<KeyValueType> options = authHandler.getOption();
            for(KeyValueType option:options) {
                if (option.getKey().equals(GeneralConstants.NAMEID_FORMAT)){
                    return option.getValue();
                }
            }

            return null;

        } catch (ParsingException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPicketLinkConf(PicketLinkType picketLinkType) {
        this.picketLinkType = picketLinkType;
    }

    @Override
    public void injectFileStream(InputStream fileStream) {
    }

    @Override
    public boolean isMultiple() {
        return false;
    }

    @Override
    public String requireFileInjection() {
        return null;
    }

    @Override
    public void injectSigningKey(PublicKey publicKey) {
    }

    @Override
    public void injectEncryptionKey(PublicKey publicKey) {
    }


}

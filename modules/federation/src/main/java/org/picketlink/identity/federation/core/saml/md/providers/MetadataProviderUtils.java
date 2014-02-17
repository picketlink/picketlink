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
package org.picketlink.identity.federation.core.saml.md.providers;


import org.picketlink.config.federation.PicketLinkType;
import org.picketlink.config.federation.ProviderType;
import org.picketlink.config.federation.SPType;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.config.federation.handler.Handler;
import org.picketlink.config.federation.handler.Handlers;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.web.util.ConfigurationUtil;

import java.io.InputStream;

/**
 * Author: coluccelli@redhat.com
 */

public class MetadataProviderUtils {

    public static String getLogoutURL(ProviderType providerType) {
        if (providerType instanceof SPType){
            SPType spType = (SPType) providerType;
            return spType.getLogoutUrl();
        }
        return null;
    }

    public static String getServiceURL(ProviderType providerType) {
        if (providerType instanceof SPType){
            SPType spType = (SPType) providerType;
            return spType.getServiceURL();
        }
        //TODO: Add support for IDP
        return null;
    }

    public static String getBindingURI(ProviderType providerType) {
        if (providerType instanceof SPType){//TODO: Add support for IDP
            SPType spType = (SPType) providerType;
            if (spType.getBindingType().equals("POST"))
                return JBossSAMLURIConstants.SAML_HTTP_POST_BINDING.get();
            if (spType.getBindingType().equals("REDIRECT"))
                return JBossSAMLURIConstants.SAML_HTTP_REDIRECT_BINDING.get();
        }
        return null;
    }
    public static String getLogoutResponseLocation(ProviderType providerType){
         if (providerType instanceof SPType){
            SPType spType = (SPType) providerType;
            return spType.getLogoutResponseLocation();
        }
        return null;
    }

    public static PicketLinkType getPicketLinkConf(InputStream is){
        try {
            return ConfigurationUtil.getConfiguration(is);
        } catch (ParsingException e) {
            throw new RuntimeException(e);        }
    }

    public static ProviderType getProviderType(PicketLinkType picketLinkConfiguration) {
        ProviderType providerType  = null;
        if (picketLinkConfiguration != null)
            providerType = picketLinkConfiguration.getIdpOrSP();

        return providerType;
    }

    public static Handler getHandler(PicketLinkType picketLinkType, String handlerName) throws ParsingException {
        Handlers handlers = picketLinkType.getHandlers();
        for (Handler h:handlers.getHandler()){
            if (h.getClazz().equals(handlerName)) return h;
        }
        return null;
    }

}

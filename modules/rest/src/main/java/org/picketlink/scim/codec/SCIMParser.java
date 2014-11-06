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
package org.picketlink.scim.codec;

import org.codehaus.jackson.JsonGenerator.Feature;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.picketlink.scim.model.v11.SCIMGroups;
import org.picketlink.scim.model.v11.SCIMResource;
import org.picketlink.scim.model.v11.SCIMUser;
import org.picketlink.scim.model.v11.ServiceProviderConfiguration;

import java.io.IOException;
import java.io.InputStream;

/**
 * Parser for SCIM
 *
 * @author anil saldhana
 * @since Apr 8, 2013
 */
public class SCIMParser {
    private ObjectMapper mapper = new ObjectMapper();

    public SCIMParser() {
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
    }

    /**
     * Configure the {@link ObjectMapper}
     *
     * @param feature
     * @param flag
     */
    public void configure(Feature feature, boolean flag) {
        mapper.configure(feature, flag);
    }

    /**
     * Parse {@link User}
     *
     * @param is
     * @return
     * @throws SCIMParsingException
     */
    public SCIMUser parseUser(InputStream is) throws SCIMParsingException {
        try {
            return mapper.readValue(is, SCIMUser.class);
        } catch (JsonParseException e) {
            throw new SCIMParsingException(e);
        } catch (JsonMappingException e) {
            throw new SCIMParsingException(e);
        } catch (IOException e) {
            throw new SCIMParsingException(e);
        }

    }

    /**
     * Parse {@link Groups}
     *
     * @param is
     * @return
     * @throws SCIMParsingException
     */
    public SCIMGroups parseGroup(InputStream is) throws SCIMParsingException {
        try {
            return mapper.readValue(is, SCIMGroups.class);
        } catch (JsonParseException e) {
            throw new SCIMParsingException(e);
        } catch (JsonMappingException e) {
            throw new SCIMParsingException(e);
        } catch (IOException e) {
            throw new SCIMParsingException(e);
        }

    }

    /**
     * Parse {@link Resource}
     *
     * @param is
     * @return
     * @throws SCIMParsingException
     */
    public SCIMResource parseResource(InputStream is) throws SCIMParsingException {
        try {
            return mapper.readValue(is, SCIMResource.class);
        } catch (JsonParseException e) {
            throw new SCIMParsingException(e);
        } catch (JsonMappingException e) {
            throw new SCIMParsingException(e);
        } catch (IOException e) {
            throw new SCIMParsingException(e);
        }

    }

    /**
     * Parse {@link ServiceProviderConfiguration}
     *
     * @param is
     * @return
     * @throws SCIMParsingException
     */
    public ServiceProviderConfiguration parseServiceProviderConfiguration(InputStream is) throws SCIMParsingException {
        try {
            return mapper.readValue(is, ServiceProviderConfiguration.class);
        } catch (JsonParseException e) {
            throw new SCIMParsingException(e);
        } catch (JsonMappingException e) {
            throw new SCIMParsingException(e);
        } catch (IOException e) {
            throw new SCIMParsingException(e);
        }
    }
}
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

import org.picketlink.common.util.StaxParserUtil;
import org.picketlink.identity.federation.core.parsers.saml.metadata.SAMLEntitiesDescriptorParser;
import org.picketlink.identity.federation.saml.v2.metadata.EntitiesDescriptorType;

/**
 * File based provider that handles multiple entities
 *
 * @author Anil.Saldhana@redhat.com
 * @since Apr 21, 2009
 */
public class FileBasedEntitiesMetadataProvider extends AbstractFileBasedMetadataProvider<EntitiesDescriptorType> {

    /**
     * @see org.picketlink.identity.federation.core.interfaces.IMetadataProvider#getMetaData()
     */
    public EntitiesDescriptorType getMetaData() {
        if (this.metadataFileStream == null)
            throw logger.injectedValueMissing("Metadata file");

        try {
            SAMLEntitiesDescriptorParser parser = new SAMLEntitiesDescriptorParser();
            return (EntitiesDescriptorType) parser.parse(StaxParserUtil.getXMLEventReader(metadataFileStream));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isMultiple() {
        return true;
    }
}
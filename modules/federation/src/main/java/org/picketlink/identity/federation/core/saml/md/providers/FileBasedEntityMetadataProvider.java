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

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.util.StaxParserUtil;
import org.picketlink.identity.federation.core.interfaces.IMetadataProvider;
import org.picketlink.identity.federation.core.parsers.saml.metadata.SAMLEntityDescriptorParser;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;

import java.io.InputStream;
import java.security.PublicKey;
import java.util.Map;

/**
 * A file based metadata provider that just looks for the passed in file name
 *
 * @author Anil.Saldhana@redhat.com
 * @since Apr 21, 2009
 */
public class FileBasedEntityMetadataProvider extends AbstractMetadataProvider implements
        IMetadataProvider<EntityDescriptorType> {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    private static final String FILENAME_KEY = "FileName";

    private String fileName;

    private InputStream metadataFileStream;

    @SuppressWarnings("unused")
    private PublicKey encryptionKey;

    @SuppressWarnings("unused")
    private PublicKey signingKey;

    @Override
    public void init(Map<String, String> options) {
        super.init(options);
        fileName = options.get(FILENAME_KEY);
        if (fileName == null)
            throw logger.optionNotSet("FileName");
    }

    /**
     * @see IMetadataProvider#getMetaData()
     */
    public EntityDescriptorType getMetaData() {
        if (this.metadataFileStream == null)
            throw logger.injectedValueMissing("Metadata file");

        try {
            SAMLEntityDescriptorParser parser = new SAMLEntityDescriptorParser();
            return (EntityDescriptorType) parser.parse(StaxParserUtil.getXMLEventReader(metadataFileStream));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see IMetadataProvider#isMultiple()
     */
    public boolean isMultiple() {
        return false;
    }

    public void injectEncryptionKey(PublicKey publicKey) {
        this.encryptionKey = publicKey;
    }

    public void injectFileStream(InputStream fileStream) {
        this.metadataFileStream = fileStream;
    }

    public void injectSigningKey(PublicKey publicKey) {
        this.signingKey = publicKey;
    }

    public String requireFileInjection() {
        return this.fileName;
    }
}
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
package org.picketlink.identity.federation.web.config;

import org.picketlink.common.ErrorCodes;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.config.federation.IDPType;
import org.picketlink.config.federation.SPType;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.util.CoreConfigUtil;
import org.picketlink.identity.federation.saml.v2.metadata.EntitiesDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;

import java.io.InputStream;
import java.util.ArrayList;

import static org.picketlink.common.util.StringUtil.isNullOrEmpty;

/**
 * <p>Base {@link org.picketlink.identity.federation.web.util.SAMLConfigurationProvider} class providing some common
 * functionality when parsing SAML Metadata files for service providers.</p>
 *
 * @author Pedro Igor
 */
public abstract class AbstractSPMetadataConfigurationProvider extends AbstractSAMLConfigurationProvider {

    public static final String DEFAULT_SP_MD_FILE = "sp-metadata.xml";
    public static final String DEFAULT_IDP_MD_FILE = "idp-metadata.xml";

    private String spMetadataLocation;
    private String idpMetadataLocation;

    @Override
    public IDPType getIDPConfiguration() throws ProcessingException {
        throw new RuntimeException(ErrorCodes.ILLEGAL_METHOD_CALLED);
    }

    @Override
    public SPType getSPConfiguration() throws ProcessingException {
        SPType spType = null;

        if (fileAvailable()) {
            try {
                spType = CoreConfigUtil.getSPConfiguration(parseMDFile(), getBindingURI());
            } catch (ParsingException e) {
                throw logger.processingError(e);
            } catch (ConfigurationException ce) {
                throw logger.processingError(ce);
            }
        }

        importFromPicketLinkConfiguration(spType);

        return spType;
    }

    protected abstract String getBindingURI();

    private EntitiesDescriptorType parseMDFile() throws ParsingException {
        Object spMetadata = parseSPMetadata();
        Object idpMetadata = parseIdPMetadata();
        EntitiesDescriptorType entities;

        if (EntitiesDescriptorType.class.isInstance(spMetadata)) {
            entities = (EntitiesDescriptorType) spMetadata;

            // if a IdP metadata is provided and if SP metadata provides multiple entities we search for any IDPSSODescriptor element to remove/replace it.
            if (idpMetadata != null) {
                removeIdPDescriptor(entities);
            }
        } else {
            entities = new EntitiesDescriptorType();
            entities.addEntityDescriptor(spMetadata);
        }

        if (idpMetadata != null) {
            entities.addEntityDescriptor(idpMetadata);
        }

        return entities;
    }

    private void removeIdPDescriptor(EntitiesDescriptorType entities) {
        for (Object descriptorType : new ArrayList<Object>(entities.getEntityDescriptor())) {
            for (EntityDescriptorType.EDTChoiceType choiceType : ((EntityDescriptorType) descriptorType).getChoiceType()) {
                for (EntityDescriptorType.EDTDescriptorChoiceType descriptorChoiceType : choiceType.getDescriptors()) {
                    if (descriptorChoiceType.getIdpDescriptor() != null) {
                        entities.removeEntityDescriptor(descriptorType);
                    }
                }
            }
        }
    }

    private Object parseSPMetadata() throws ParsingException {
        InputStream spMetadataStream = SecurityActions.loadStream(getClass(), getSpMetadataLocation());

        if (spMetadataStream == null) {
            throw logger.nullValueError(getSpMetadataLocation());
        }

        return new SAMLParser().parse(spMetadataStream);
    }

    private Object parseIdPMetadata() throws ParsingException {
        EntityDescriptorType idpEntityDescriptor = null;

        if (!isNullOrEmpty(getIdpMetadataLocation())) {
            InputStream configStream = SecurityActions.loadStream(getClass(), getIdpMetadataLocation());

            if (configStream != null) {
                Object idpParsedType = new SAMLParser().parse(configStream);

                if (EntitiesDescriptorType.class.isInstance(idpParsedType)) {
                    idpEntityDescriptor = (EntityDescriptorType) ((EntitiesDescriptorType) idpParsedType).getEntityDescriptor().get(0);
                } else {
                    idpEntityDescriptor = (EntityDescriptorType) idpParsedType;
                }
            }
        }

        return idpEntityDescriptor;
    }

    private void importFromPicketLinkConfiguration(SPType spType) {
        if (configParsedSPType != null) {
            spType.importFrom(configParsedSPType);
        }
    }

    private boolean fileAvailable() {
        InputStream is = SecurityActions.loadStream(getClass(), getSpMetadataLocation());
        return is != null;
    }

    public String getSpMetadataLocation() {
        if (this.spMetadataLocation == null) {
            this.spMetadataLocation = DEFAULT_SP_MD_FILE;
        }

        return this.spMetadataLocation;
    }

    public void setSpMetadataLocation(String spMetadataLocation) {
        this.spMetadataLocation = spMetadataLocation;
    }

    public String getIdpMetadataLocation() {
        if (this.idpMetadataLocation == null) {
            this.idpMetadataLocation = DEFAULT_IDP_MD_FILE;
        }

        return this.idpMetadataLocation;
    }

    public void setIdpMetadataLocation(String idpMetadataLocation) {
        this.idpMetadataLocation = idpMetadataLocation;
    }
}

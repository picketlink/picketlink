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
package org.picketlink.identity.federation.core.saml.v2.metadata.store;

import org.picketlink.common.ErrorCodes;
import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.StaxParserUtil;
import org.picketlink.common.util.StaxUtil;
import org.picketlink.common.util.StringUtil;
import org.picketlink.identity.federation.core.constants.PicketLinkFederationConstants;
import org.picketlink.identity.federation.core.parsers.saml.metadata.SAMLEntityDescriptorParser;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLMetadataWriter;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType.EDTChoiceType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType.EDTDescriptorChoiceType;
import org.picketlink.identity.federation.saml.v2.metadata.IDPSSODescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.SPSSODescriptorType;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * File based metadata store that uses the ${user.home}/jbid-store location to persist the data
 *
 * @author Anil.Saldhana@redhat.com
 * @since Apr 27, 2009
 */
public class FileBasedMetadataConfigurationStore implements IMetadataConfigurationStore {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    private String userHome = null;

    private String baseDirectory = null;

    public FileBasedMetadataConfigurationStore() {
        bootstrap();
    }

    /**
     * @see {@code IMetadataConfigurationStore#bootstrap()}
     */
    public void bootstrap() {
        userHome = SecurityActions.getSystemProperty("user.home");
        if (userHome == null)
            throw logger.systemPropertyMissingError("user.home");

        StringBuilder builder = new StringBuilder(userHome);
        builder.append(PicketLinkFederationConstants.FILE_STORE_DIRECTORY);
        baseDirectory = builder.toString();

        File plStore = new File(baseDirectory);
        if (plStore.exists() == false) {
            logger.trace(plStore.getPath() + " does not exist. Hence creating.");
            plStore.mkdir();
        }
    }

    /**
     * @see IMetadataConfigurationStore#getIdentityProviderID()
     */
    public Set<String> getIdentityProviderID() {
        Set<String> identityProviders = new HashSet<String>();

        Properties idp = new Properties();

        StringBuilder builder = new StringBuilder(baseDirectory);
        builder.append(PicketLinkFederationConstants.IDP_PROPERTIES);

        File identityProviderFile = new File(builder.toString());
        if (identityProviderFile.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(identityProviderFile);
                idp.load(fis);
                String listOfIDP = (String) idp.get("IDP");
                if (StringUtil.isNotNull(listOfIDP)) {
                    identityProviders.addAll(StringUtil.tokenize(listOfIDP));
                }
            } catch (Exception e) {
                logger.samlMetaDataIdentityProviderLoadingError(e);
            } finally {
                if(fis != null){
                    try {
                        fis.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return identityProviders;
    }

    /**
     * @see IMetadataConfigurationStore#getServiceProviderID()
     */
    public Set<String> getServiceProviderID() {
        Set<String> serviceProviders = new HashSet<String>();

        Properties sp = new Properties();
        StringBuilder builder = new StringBuilder(baseDirectory);
        builder.append(PicketLinkFederationConstants.SP_PROPERTIES);

        File serviceProviderFile = new File(builder.toString());

        if (serviceProviderFile.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(serviceProviderFile);
                sp.load(fis);
                String listOfSP = (String) sp.get("SP");

                // Comma separated list
                StringTokenizer st = new StringTokenizer(listOfSP, ",");
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    serviceProviders.add(token);
                }
            } catch (Exception e) {
                logger.samlMetaDataServiceProviderLoadingError(e);
            } finally {
                if(fis != null){
                    try {
                        fis.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return serviceProviders;
    }

    /**
     * @see IMetadataConfigurationStore#load(String)
     */
    public EntityDescriptorType load(String id) throws IOException {
        File persistedFile = validateIdAndReturnMDFile(id);

        SAMLEntityDescriptorParser parser = new SAMLEntityDescriptorParser();
        try {
            FileInputStream fileIn = new FileInputStream(persistedFile);
            try {
                XMLEventReader reader = StaxParserUtil.getXMLEventReader(fileIn);
                try {
                    return (EntityDescriptorType) parser.parse(reader);
                }
                finally {
                    reader.close();
                }
            }
            finally {
                fileIn.close();
            }
        } catch (ParsingException e) {
            throw new RuntimeException(e);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see IMetadataConfigurationStore#persist(EntityDescriptorType, String)
     */
    public void persist(EntityDescriptorType entity, String id) throws IOException {
        File persistedFile = validateIdAndReturnMDFile(id);

        try {
            FileOutputStream fileOut = new FileOutputStream(persistedFile);
            try {
                XMLStreamWriter streamWriter = StaxUtil.getXMLStreamWriter(fileOut);
                try {
                    SAMLMetadataWriter writer = new SAMLMetadataWriter(streamWriter);
                    writer.writeEntityDescriptor(entity);
                }
                finally {
                    streamWriter.close();
                }
            }
            finally {
                fileOut.close();
            }
        } catch (ProcessingException e) {
            throw new RuntimeException(e);
        } catch (XMLStreamException e){
            throw new RuntimeException(e);
        }

        logger.trace("Persisted entity descriptor into " + persistedFile.getPath());

        // Process the EDT
        List<EDTChoiceType> edtChoiceTypeList = entity.getChoiceType();
        for (EDTChoiceType edtChoiceType : edtChoiceTypeList) {
            List<EDTDescriptorChoiceType> edtDescriptorChoiceTypeList = edtChoiceType.getDescriptors();
            for (EDTDescriptorChoiceType edtDesc : edtDescriptorChoiceTypeList) {
                IDPSSODescriptorType idpSSO = edtDesc.getIdpDescriptor();
                if (idpSSO != null) {
                    addIdentityProvider(id);
                }
                SPSSODescriptorType spSSO = edtDesc.getSpDescriptor();
                if (spSSO != null) {
                    addServiceProvider(id);
                }
            }
        }
    }

    /**
     * @see IMetadataConfigurationStore#delete(String)
     */
    public void delete(String id) {
        File persistedFile = validateIdAndReturnMDFile(id);

        if (persistedFile.exists())
            persistedFile.delete();
    }

    /**
     * @throws IOException
     * @throws ClassNotFoundException
     * @see IMetadataConfigurationStore#loadTrustedProviders(String)
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> loadTrustedProviders(String id) throws IOException, ClassNotFoundException {
        File trustedFile = validateIdAndReturnTrustedProvidersFile(id);
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(trustedFile));
            Map<String, String> trustedMap = (Map<String, String>) ois.readObject();
            return trustedMap;
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException ioe) {
                }
            }
        }
    }

    /**
     * @throws IOException
     * @see IMetadataConfigurationStore#persistTrustedProviders(Map)
     */
    public void persistTrustedProviders(String id, Map<String, String> trusted) throws IOException {
        File trustedFile = validateIdAndReturnTrustedProvidersFile(id);
        ObjectOutputStream oos = null;

        try {
            oos = new ObjectOutputStream(new FileOutputStream(trustedFile));
            oos.writeObject(trusted);
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException ioe) {
                }
            }
        }

        logger.trace("Persisted trusted map into " + trustedFile.getPath());
    }

    /**
     * @see IMetadataConfigurationStore#deleteTrustedProviders(String)
     */
    public void deleteTrustedProviders(String id) {
        File persistedFile = validateIdAndReturnTrustedProvidersFile(id);

        if (persistedFile.exists())
            persistedFile.delete();
    }

    private File validateIdAndReturnMDFile(String id) {
        String serializationExtension = PicketLinkFederationConstants.SERIALIZATION_EXTENSION;

        if (id == null)
            throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "id");
        if (!id.endsWith(serializationExtension))
            id += serializationExtension;

        StringBuilder builder = new StringBuilder(baseDirectory);
        builder.append("/").append(id);

        return new File(builder.toString());
    }

    private File validateIdAndReturnTrustedProvidersFile(String id) {
        if (id == null)
            throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "id");

        id += "-trusted" + PicketLinkFederationConstants.SERIALIZATION_EXTENSION;

        StringBuilder builder = new StringBuilder(baseDirectory);
        builder.append("/").append(id);

        return new File(builder.toString());
    }

    private void addServiceProvider(String id) {
        Properties sp = new Properties();

        StringBuilder builder = new StringBuilder(baseDirectory);
        builder.append(PicketLinkFederationConstants.SP_PROPERTIES);

        File serviceProviderFile = new File(builder.toString());

        FileInputStream fileInputStream = null;
        try {
            if (serviceProviderFile.exists() == false)
                serviceProviderFile.createNewFile();

            fileInputStream = new FileInputStream(serviceProviderFile);
            sp.load(fileInputStream);
            String listOfSP = (String) sp.get("SP");
            if (listOfSP == null) {
                listOfSP = id;
            } else {
                listOfSP += "," + id;
            }
            sp.put("SP", listOfSP);

            sp.store(new FileWriter(serviceProviderFile), "");
        } catch (Exception e) {
            logger.samlMetaDataServiceProviderLoadingError(e);
        } finally {
            if(fileInputStream != null){
                try{
                    fileInputStream.close();
                } catch (IOException ioe){
                }
            }
        }
    }

    private void addIdentityProvider(String id) {
        Properties idp = new Properties();

        StringBuilder builder = new StringBuilder(baseDirectory);
        builder.append(PicketLinkFederationConstants.IDP_PROPERTIES);

        File idpProviderFile = new File(builder.toString());

        FileInputStream fileInputStream = null;
        try {
            if (idpProviderFile.exists() == false)
                idpProviderFile.createNewFile();

            fileInputStream = new FileInputStream(idpProviderFile);
            idp.load(fileInputStream);
            String listOfIDP = (String) idp.get("IDP");
            if (listOfIDP == null) {
                listOfIDP = id;
            } else {
                listOfIDP += "," + id;
            }
            idp.put("IDP", listOfIDP);

            idp.store(new FileWriter(idpProviderFile), "");
        } catch (Exception e) {
            logger.samlMetaDataIdentityProviderLoadingError(e);
        } finally {
            if(fileInputStream != null){
                try{
                    fileInputStream.close();
                } catch (IOException ioe){
                }
            }
        }
    }

    /**
     * @see {@code IMetadataConfigurationStore#cleanup()}
     */
    public void cleanup() {
    }
}
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

package org.picketlink.identity.federation.web.servlets.saml;

import static org.picketlink.common.util.StringUtil.isNotNull;

//import java.io.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.picketlink.config.federation.AuthPropertyType;
import org.picketlink.config.federation.KeyProviderType;
import org.picketlink.config.federation.KeyValueType;
import org.picketlink.config.federation.MetadataProviderType;
import org.picketlink.config.federation.PicketLinkType;
import org.picketlink.config.federation.ProviderType;
import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.ErrorCodes;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.constants.JBossSAMLConstants;
import org.picketlink.common.util.StaxUtil;

import org.picketlink.identity.federation.api.saml.v2.metadata.KeyDescriptorMetaDataBuilder;
import org.picketlink.identity.federation.api.util.KeyUtil;
import org.picketlink.identity.federation.core.interfaces.IMetadataProvider;
import org.picketlink.identity.federation.core.interfaces.TrustKeyManager;
import org.picketlink.identity.federation.core.saml.md.providers.MetadataProviderUtils;
import org.picketlink.identity.federation.core.saml.md.providers.SPMetadataProvider;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLMetadataWriter;
import org.picketlink.identity.federation.core.util.CoreConfigUtil;
import org.picketlink.identity.federation.core.util.XMLSignatureUtil;
import org.picketlink.identity.federation.saml.v2.metadata.AttributeAuthorityDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.AuthnAuthorityDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.EntitiesDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType.EDTDescriptorChoiceType;
import org.picketlink.identity.federation.saml.v2.metadata.IDPSSODescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.KeyDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.PDPDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.RoleDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.SPSSODescriptorType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Metadata servlet for the SP
 *
 * Author: coluccelli@redhat.com
 *
 */
public class MetadataServletSP extends HttpServlet {

    private static final long serialVersionUID = 1L;

    //private static Logger log = Logger.getLogger(MetadataServletSP.class);
    private static final PicketLinkLogger log = PicketLinkLoggerFactory.getLogger();

    private final boolean trace = log.isTraceEnabled();

    private String configFileLocation = GeneralConstants.CONFIG_FILE_LOCATION;

    private transient MetadataProviderType metadataProviderType = null;

    private transient IMetadataProvider<?> metadataProvider = null;

    private transient EntitiesDescriptorType entitiesDescriptor;

    private transient EntityDescriptorType entityDescriptor;

    private String signingAlias = null;

    private String encryptingAlias = null;

    private TrustKeyManager keyManager;

    @SuppressWarnings("rawtypes")
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        ServletContext context = config.getServletContext();
        String configL = config.getInitParameter("configFile");
        if (isNotNull(configL))
            configFileLocation = configL;
        if (trace)
            log.trace("Config File Location=" + configFileLocation);
        InputStream is = context.getResourceAsStream(configFileLocation);
        if (is == null)
            throw new ServletException(ErrorCodes.RESOURCE_NOT_FOUND + configFileLocation + " missing");

        // Look for signing alias
        signingAlias = config.getInitParameter("signingAlias");
        encryptingAlias = config.getInitParameter("encryptingAlias");


        PicketLinkType picketLinkType = MetadataProviderUtils.getPicketLinkConf(is);
        ProviderType providerType = MetadataProviderUtils.getProviderType(picketLinkType);

        metadataProviderType = providerType.getMetaDataProvider();
        String fqn = metadataProviderType.getClassName();
        Class<?> clazz = SecurityActions.loadClass(getClass(), fqn);
        try {
            metadataProvider = (IMetadataProvider) clazz.newInstance();
        } catch (InstantiationException e) {
            throw new ServletException(e);
        } catch (IllegalAccessException e) {
            throw new ServletException(e);
        }
        List<KeyValueType> keyValues = metadataProviderType.getOption();
        Map<String, String> options = new HashMap<String, String>();
        if (keyValues != null) {
            for (KeyValueType kvt : keyValues)
                options.put(kvt.getKey(), kvt.getValue());
        }


        //inject inputStream and other provider-specific properties
        String fileInjectionStr = metadataProvider.requireFileInjection();
        if (isNotNull(fileInjectionStr)) {
            metadataProvider.injectFileStream(context.getResourceAsStream(fileInjectionStr));
        }else if (metadataProvider instanceof SPMetadataProvider){
            ((SPMetadataProvider)metadataProvider).setPicketLinkConf(picketLinkType);
        }

        metadataProvider.init(options);

        Object metadata = metadataProvider.getMetaData();
        if (metadata instanceof EntitiesDescriptorType) {
            entitiesDescriptor = (EntitiesDescriptorType) metadata;
        }else if (metadata instanceof EntityDescriptorType) {
            entityDescriptor = (EntityDescriptorType) metadata;
        } else {
            throw new ServletException(ErrorCodes.PARSING_ERROR+"Invalid metadata type");
        }

        // Get the trust manager information
        KeyProviderType keyProvider = providerType.getKeyProvider();
        signingAlias = keyProvider.getSigningAlias();
        String keyManagerClassName = keyProvider.getClassName();
        if (keyManagerClassName == null)
            throw new ServletException(ErrorCodes.NULL_VALUE + "KeyManager class name");

        clazz = SecurityActions.loadClass(getClass(), keyManagerClassName);

        try{
            this.keyManager = (TrustKeyManager) clazz.newInstance();

            List<AuthPropertyType> authProperties = CoreConfigUtil.getKeyProviderProperties(keyProvider);
            keyManager.setAuthProperties(authProperties);

            Certificate cert = keyManager.getCertificate(signingAlias);
            Element keyInfo = KeyUtil.getKeyInfo(cert);

            // TODO: Assume just signing key for now
            KeyDescriptorType keyDescriptor = KeyDescriptorMetaDataBuilder.createKeyDescriptor(keyInfo, null, 0, true, false);

            if (entitiesDescriptor != null)
                updateKeyDescriptors(entitiesDescriptor, keyDescriptor);
            else{
                updateKeyDescriptor(entityDescriptor,keyDescriptor);

            }
            // encryption
            if (encryptingAlias == null)
                encryptingAlias = signingAlias;
            cert = keyManager.getCertificate(encryptingAlias);
            keyInfo = KeyUtil.getKeyInfo(cert);

            keyDescriptor = KeyDescriptorMetaDataBuilder.createKeyDescriptor(keyInfo, null, 0, false, true);
            if (entitiesDescriptor != null)
                updateKeyDescriptors(entitiesDescriptor, keyDescriptor);
                //TODO: IMPLEMENT FOREACH (entityDescriptor) signAndAddAttribs
            else{
                updateKeyDescriptor(entityDescriptor,keyDescriptor);
                signAndAddAttribs(entityDescriptor);
            }

        }catch(Exception e){
            throw  new ServletException(e);
        }

    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType(JBossSAMLConstants.METADATA_MIME.get());
        OutputStream os = resp.getOutputStream();

        try {
            XMLStreamWriter streamWriter = StaxUtil.getXMLStreamWriter(os);
            SAMLMetadataWriter writer = new SAMLMetadataWriter(streamWriter);
            if (entitiesDescriptor != null)
                writer.writeEntitiesDescriptor(entitiesDescriptor);
            else
                writer.writeEntityDescriptor(entityDescriptor);

        } catch (ProcessingException e) {
            throw new ServletException(e);
        }
        /*
         * JAXBElement<?> jaxbEl = MetaDataBuilder.getObjectFactory().createEntityDescriptor(metadata); try {
         * MetaDataBuilder.getMarshaller().marshal(jaxbEl , os); } catch (Exception e) { throw new RuntimeException(e); }
         */
    }

    private void signAndAddAttribs(EntityDescriptorType entityDescriptor) throws ServletException{
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLStreamWriter streamWriter = StaxUtil.getXMLStreamWriter(baos);
            SAMLMetadataWriter writer = new SAMLMetadataWriter(streamWriter);
            writer.writeEntityDescriptor(entityDescriptor);
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(baos.toByteArray()));
            KeyPair keyPair = new KeyPair(null, keyManager.getSigningKey());
            //Sign doc
            Element spssoDesc = doc.getDocumentElement();
            XMLSignatureUtil.sign(spssoDesc,spssoDesc.getFirstChild(),keyPair,DigestMethod.SHA1,
                    SignatureMethod.RSA_SHA1,"",(X509Certificate) keyManager.getCertificate(signingAlias));
            //extract Signature
            entityDescriptor.setSignature(extractSignatureFromDoc(spssoDesc));

        } catch (Exception e) {
            throw new ServletException(e);
        }

    }

    private Element extractSignatureFromDoc(Element doc) {
        return (Element) doc.getFirstChild();

    }

    private String getStringFromDocument(Element doc) throws TransformerException {
        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);
        return writer.toString();
    }




    private void updateKeyDescriptors(EntitiesDescriptorType entityId, KeyDescriptorType keyD){
        List<Object> entities =  entityId.getEntityDescriptor();
        for (Object obj : entities){
            updateKeyDescriptor((EntityDescriptorType) obj,keyD);

        }

    }

    private void updateKeyDescriptor(EntityDescriptorType entityD, KeyDescriptorType keyD) {
        List<EDTDescriptorChoiceType> objs = entityD.getChoiceType().get(0).getDescriptors();
        if (objs != null) {
            for (EDTDescriptorChoiceType choiceTypeDesc : objs) {
                AttributeAuthorityDescriptorType attribDescriptor = choiceTypeDesc.getAttribDescriptor();
                if (attribDescriptor != null)
                    attribDescriptor.addKeyDescriptor(keyD);
                AuthnAuthorityDescriptorType authnDescriptor = choiceTypeDesc.getAuthnDescriptor();
                if (authnDescriptor != null)
                    authnDescriptor.addKeyDescriptor(keyD);
                IDPSSODescriptorType idpDescriptor = choiceTypeDesc.getIdpDescriptor();
                if (idpDescriptor != null)
                    idpDescriptor.addKeyDescriptor(keyD);
                PDPDescriptorType pdpDescriptor = choiceTypeDesc.getPdpDescriptor();
                if (pdpDescriptor != null)
                    pdpDescriptor.addKeyDescriptor(keyD);
                RoleDescriptorType roleDescriptor = choiceTypeDesc.getRoleDescriptor();
                if (roleDescriptor != null)
                    roleDescriptor.addKeyDescriptor(keyD);
                SPSSODescriptorType spDescriptorType = choiceTypeDesc.getSpDescriptor();
                if (spDescriptorType != null)
                    spDescriptorType.addKeyDescriptor(keyD);

            }
        }
    }
}
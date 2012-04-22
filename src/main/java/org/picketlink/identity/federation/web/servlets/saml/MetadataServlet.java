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
package org.picketlink.identity.federation.web.servlets.saml;

import static org.picketlink.identity.federation.core.util.StringUtil.isNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamWriter;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.api.saml.v2.metadata.KeyDescriptorMetaDataBuilder;
import org.picketlink.identity.federation.api.util.KeyUtil;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.config.AuthPropertyType;
import org.picketlink.identity.federation.core.config.KeyProviderType;
import org.picketlink.identity.federation.core.config.KeyValueType;
import org.picketlink.identity.federation.core.config.MetadataProviderType;
import org.picketlink.identity.federation.core.config.ProviderType;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.interfaces.IMetadataProvider;
import org.picketlink.identity.federation.core.interfaces.TrustKeyManager;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLMetadataWriter;
import org.picketlink.identity.federation.core.util.CoreConfigUtil;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.core.util.XMLEncryptionUtil;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType.EDTDescriptorChoiceType;
import org.picketlink.identity.federation.saml.v2.metadata.KeyDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.RoleDescriptorType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.util.ConfigurationUtil;
import org.w3c.dom.Element;

/**
 * Metadata servlet for the IDP/SP
 * @author Anil.Saldhana@redhat.com
 * @since Apr 22, 2009
 */
public class MetadataServlet extends HttpServlet
{
   private static final long serialVersionUID = 1L;

   private static Logger log = Logger.getLogger(MetadataServlet.class);

   private final boolean trace = log.isTraceEnabled();

   private String configFileLocation = GeneralConstants.CONFIG_FILE_LOCATION;

   private transient MetadataProviderType metadataProviderType = null;

   private transient IMetadataProvider<?> metadataProvider = null;

   private transient EntityDescriptorType metadata;

   private String signingAlias = null;

   private String encryptingAlias = null;

   private TrustKeyManager keyManager;

   @SuppressWarnings("rawtypes")
   @Override
   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);
      try
      {
         ServletContext context = config.getServletContext();
         String configL = config.getInitParameter("configFile");
         if (isNotNull(configL))
            configFileLocation = configL;
         if (trace)
            log.trace("Config File Location=" + configFileLocation);
         InputStream is = context.getResourceAsStream(configFileLocation);
         if (is == null)
            throw new RuntimeException(ErrorCodes.RESOURCE_NOT_FOUND + configFileLocation + " missing");

         //Look for signing alias
         signingAlias = config.getInitParameter("signingAlias");
         encryptingAlias = config.getInitParameter("encryptingAlias");

         ProviderType providerType = ConfigurationUtil.getIDPConfiguration(is);
         metadataProviderType = providerType.getMetaDataProvider();
         String fqn = metadataProviderType.getClassName();
         Class<?> clazz = SecurityActions.loadClass(getClass(), fqn);
         metadataProvider = (IMetadataProvider) clazz.newInstance();
         List<KeyValueType> keyValues = metadataProviderType.getOption();
         Map<String, String> options = new HashMap<String, String>();
         if (keyValues != null)
         {
            for (KeyValueType kvt : keyValues)
               options.put(kvt.getKey(), kvt.getValue());
         }
         metadataProvider.init(options);
         if (metadataProvider.isMultiple())
            throw new RuntimeException(ErrorCodes.NOT_IMPLEMENTED_YET + "Multiple Entities not currently supported");

         /**
          * Since a metadata provider does not have access to the servlet context.
          * It may be difficult to get to the resource from the TCL.
          */
         String fileInjectionStr = metadataProvider.requireFileInjection();
         if (isNotNull(fileInjectionStr))
         {
            metadataProvider.injectFileStream(context.getResourceAsStream(fileInjectionStr));
         }

         metadata = (EntityDescriptorType) metadataProvider.getMetaData();

         //Get the trust manager information
         KeyProviderType keyProvider = providerType.getKeyProvider();
         signingAlias = keyProvider.getSigningAlias();
         String keyManagerClassName = keyProvider.getClassName();
         if (keyManagerClassName == null)
            throw new RuntimeException(ErrorCodes.NULL_VALUE + "KeyManager class name");

         clazz = SecurityActions.loadClass(getClass(), keyManagerClassName);
         this.keyManager = (TrustKeyManager) clazz.newInstance();

         List<AuthPropertyType> authProperties = CoreConfigUtil.getKeyProviderProperties(keyProvider);
         keyManager.setAuthProperties(authProperties);

         Certificate cert = keyManager.getCertificate(signingAlias);
         Element keyInfo = KeyUtil.getKeyInfo(cert);

         //TODO: Assume just signing key for now
         KeyDescriptorType keyDescriptor = KeyDescriptorMetaDataBuilder.createKeyDescriptor(keyInfo, null, 0, true,
               false);

         updateKeyDescriptor(metadata, keyDescriptor);

         //encryption
         if (this.encryptingAlias != null)
         {
            cert = keyManager.getCertificate(encryptingAlias);
            keyInfo = KeyUtil.getKeyInfo(cert);
            String certAlgo = cert.getPublicKey().getAlgorithm();
            keyDescriptor = KeyDescriptorMetaDataBuilder.createKeyDescriptor(keyInfo,
                  XMLEncryptionUtil.getEncryptionURL(certAlgo), XMLEncryptionUtil.getEncryptionKeySize(certAlgo),
                  false, true);
            updateKeyDescriptor(metadata, keyDescriptor);
         }
      }
      catch (Exception e)
      {
         log.error("Exception in starting servlet:", e);
         throw new ServletException(ErrorCodes.PROCESSING_EXCEPTION + "Unable to start servlet");
      }

   }

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      resp.setContentType(JBossSAMLConstants.METADATA_MIME.get());
      OutputStream os = resp.getOutputStream();

      try
      {
         XMLStreamWriter streamWriter = StaxUtil.getXMLStreamWriter(os);
         SAMLMetadataWriter writer = new SAMLMetadataWriter(streamWriter);
         writer.writeEntityDescriptor(metadata);
      }
      catch (ProcessingException e)
      {
         throw new ServletException(e);
      }
      /*
      JAXBElement<?> jaxbEl = MetaDataBuilder.getObjectFactory().createEntityDescriptor(metadata);
      try
      {
         MetaDataBuilder.getMarshaller().marshal(jaxbEl , os);
      } 
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }*/
   }

   private void updateKeyDescriptor(EntityDescriptorType entityD, KeyDescriptorType keyD)
   {
      List<EDTDescriptorChoiceType> objs = entityD.getChoiceType().get(0).getDescriptors();
      if (objs != null)
      {
         for (EDTDescriptorChoiceType roleD : objs)
         {
            RoleDescriptorType roleDescriptor = roleD.getRoleDescriptor();
            roleDescriptor.addKeyDescriptor(keyD);
         }
      }
   }
}
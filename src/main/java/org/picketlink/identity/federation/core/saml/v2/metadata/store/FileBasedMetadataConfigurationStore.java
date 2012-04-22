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
package org.picketlink.identity.federation.core.saml.v2.metadata.store;

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

import javax.xml.stream.XMLStreamWriter;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.constants.PicketLinkFederationConstants;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.parsers.saml.metadata.SAMLEntityDescriptorParser;
import org.picketlink.identity.federation.core.parsers.util.StaxParserUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLMetadataWriter;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.core.util.StringUtil;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType.EDTChoiceType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType.EDTDescriptorChoiceType;
import org.picketlink.identity.federation.saml.v2.metadata.IDPSSODescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.SPSSODescriptorType;

/**
 * File based metadata store that uses
 * the ${user.home}/jbid-store location to
 * persist the data
 * @author Anil.Saldhana@redhat.com
 * @since Apr 27, 2009
 */
public class FileBasedMetadataConfigurationStore implements IMetadataConfigurationStore
{
   private static Logger log = Logger.getLogger(FileBasedMetadataConfigurationStore.class);

   private final boolean trace = log.isTraceEnabled();

   private String userHome = null;

   private String baseDirectory = null;

   public FileBasedMetadataConfigurationStore()
   {
      bootstrap();
   }

   /**
    * @see {@code IMetadataConfigurationStore#bootstrap()}
    */
   public void bootstrap()
   {
      userHome = SecurityActions.getSystemProperty("user.home");
      if (userHome == null)
         throw new RuntimeException(ErrorCodes.SYSTEM_PROPERTY_MISSING + "user.home");

      StringBuilder builder = new StringBuilder(userHome);
      builder.append(PicketLinkFederationConstants.FILE_STORE_DIRECTORY);
      baseDirectory = builder.toString();

      File plStore = new File(baseDirectory);
      if (plStore.exists() == false)
      {
         if (trace)
            log.trace(plStore.getPath() + " does not exist. Hence creating.");
         plStore.mkdir();
      }
   }

   /**
    * @see IMetadataConfigurationStore#getIdentityProviderID()
    */
   public Set<String> getIdentityProviderID()
   {
      Set<String> identityProviders = new HashSet<String>();

      Properties idp = new Properties();

      StringBuilder builder = new StringBuilder(baseDirectory);
      builder.append(PicketLinkFederationConstants.IDP_PROPERTIES);

      File identityProviderFile = new File(builder.toString());
      if (identityProviderFile.exists())
      {
         try
         {
            idp.load(new FileInputStream(identityProviderFile));
            String listOfIDP = (String) idp.get("IDP");
            if (StringUtil.isNotNull(listOfIDP))
            {
               identityProviders.addAll(StringUtil.tokenize(listOfIDP));
            }
         }
         catch (Exception e)
         {
            log.error("Exception loading the identity providers:", e);
         }
      }
      return identityProviders;
   }

   /**
    * @see IMetadataConfigurationStore#getServiceProviderID()
    */
   public Set<String> getServiceProviderID()
   {
      Set<String> serviceProviders = new HashSet<String>();

      Properties sp = new Properties();
      StringBuilder builder = new StringBuilder(baseDirectory);
      builder.append(PicketLinkFederationConstants.SP_PROPERTIES);

      File serviceProviderFile = new File(builder.toString());

      if (serviceProviderFile.exists())
      {
         try
         {
            sp.load(new FileInputStream(serviceProviderFile));
            String listOfSP = (String) sp.get("SP");

            //Comma separated list
            StringTokenizer st = new StringTokenizer(listOfSP, ",");
            while (st.hasMoreTokens())
            {
               String token = st.nextToken();
               serviceProviders.add(token);
            }
         }
         catch (Exception e)
         {
            log.error("Exception loading the service providers:", e);
         }
      }
      return serviceProviders;
   }

   /** 
    * @see IMetadataConfigurationStore#load(String)
    */
   public EntityDescriptorType load(String id) throws IOException
   {
      File persistedFile = validateIdAndReturnMDFile(id);

      SAMLEntityDescriptorParser parser = new SAMLEntityDescriptorParser();
      try
      {
         return (EntityDescriptorType) parser.parse(StaxParserUtil
               .getXMLEventReader(new FileInputStream(persistedFile)));
      }
      catch (ParsingException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**  
    * @see IMetadataConfigurationStore#persist(EntityDescriptorType, String)
    */
   public void persist(EntityDescriptorType entity, String id) throws IOException
   {
      File persistedFile = validateIdAndReturnMDFile(id);

      try
      {
         XMLStreamWriter streamWriter = StaxUtil.getXMLStreamWriter(new FileOutputStream(persistedFile));
         SAMLMetadataWriter writer = new SAMLMetadataWriter(streamWriter);

         writer.writeEntityDescriptor(entity);
      }
      catch (ProcessingException e)
      {
         throw new RuntimeException(e);
      }
      if (trace)
         log.trace("Persisted into " + persistedFile.getPath());

      //Process the EDT
      List<EDTChoiceType> edtChoiceTypeList = entity.getChoiceType();
      for (EDTChoiceType edtChoiceType : edtChoiceTypeList)
      {
         List<EDTDescriptorChoiceType> edtDescriptorChoiceTypeList = edtChoiceType.getDescriptors();
         for (EDTDescriptorChoiceType edtDesc : edtDescriptorChoiceTypeList)
         {
            IDPSSODescriptorType idpSSO = edtDesc.getIdpDescriptor();
            if (idpSSO != null)
            {
               addIdentityProvider(id);
            }
            SPSSODescriptorType spSSO = edtDesc.getSpDescriptor();
            if (spSSO != null)
            {
               addServiceProvider(id);
            }
         }
      }
   }

   /**
    * @see IMetadataConfigurationStore#delete(String)
    */
   public void delete(String id)
   {
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
   public Map<String, String> loadTrustedProviders(String id) throws IOException, ClassNotFoundException
   {
      File trustedFile = validateIdAndReturnTrustedProvidersFile(id);
      ObjectInputStream ois = null;
      try
      {
         ois = new ObjectInputStream(new FileInputStream(trustedFile));
         Map<String, String> trustedMap = (Map<String, String>) ois.readObject();
         return trustedMap;
      }
      finally
      {
         if (ois != null)
            ois.close();
      }
   }

   /**
    * @throws IOException   
    * @see IMetadataConfigurationStore#persistTrustedProviders(Map)
    */
   public void persistTrustedProviders(String id, Map<String, String> trusted) throws IOException
   {
      File trustedFile = validateIdAndReturnTrustedProvidersFile(id);
      ObjectOutputStream oos = null;

      try
      {
         oos = new ObjectOutputStream(new FileOutputStream(trustedFile));
         oos.writeObject(trusted);
      }
      finally
      {
         if (oos != null)
            oos.close();
      }
      if (trace)
         log.trace("Persisted trusted map into " + trustedFile.getPath());
   }

   /**
    * @see IMetadataConfigurationStore#deleteTrustedProviders(String)
    */
   public void deleteTrustedProviders(String id)
   {
      File persistedFile = validateIdAndReturnTrustedProvidersFile(id);

      if (persistedFile.exists())
         persistedFile.delete();
   }

   private File validateIdAndReturnMDFile(String id)
   {
      String serializationExtension = PicketLinkFederationConstants.SERIALIZATION_EXTENSION;

      if (id == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "id");
      if (!id.endsWith(serializationExtension))
         id += serializationExtension;

      StringBuilder builder = new StringBuilder(baseDirectory);
      builder.append("/").append(id);

      return new File(builder.toString());
   }

   private File validateIdAndReturnTrustedProvidersFile(String id)
   {
      if (id == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "id");

      id += "-trusted" + PicketLinkFederationConstants.SERIALIZATION_EXTENSION;

      StringBuilder builder = new StringBuilder(baseDirectory);
      builder.append("/").append(id);

      return new File(builder.toString());
   }

   private void addServiceProvider(String id)
   {
      Properties sp = new Properties();

      StringBuilder builder = new StringBuilder(baseDirectory);
      builder.append(PicketLinkFederationConstants.SP_PROPERTIES);

      File serviceProviderFile = new File(builder.toString());

      try
      {
         if (serviceProviderFile.exists() == false)
            serviceProviderFile.createNewFile();

         sp.load(new FileInputStream(serviceProviderFile));
         String listOfSP = (String) sp.get("SP");
         if (listOfSP == null)
         {
            listOfSP = id;
         }
         else
         {
            listOfSP += "," + id;
         }
         sp.put("SP", listOfSP);

         sp.store(new FileWriter(serviceProviderFile), "");
      }
      catch (Exception e)
      {
         log.error("Exception loading the service providers:", e);
      }
   }

   private void addIdentityProvider(String id)
   {
      Properties idp = new Properties();

      StringBuilder builder = new StringBuilder(baseDirectory);
      builder.append(PicketLinkFederationConstants.IDP_PROPERTIES);

      File idpProviderFile = new File(builder.toString());

      try
      {
         if (idpProviderFile.exists() == false)
            idpProviderFile.createNewFile();

         idp.load(new FileInputStream(idpProviderFile));
         String listOfIDP = (String) idp.get("IDP");
         if (listOfIDP == null)
         {
            listOfIDP = id;
         }
         else
         {
            listOfIDP += "," + id;
         }
         idp.put("IDP", listOfIDP);

         idp.store(new FileWriter(idpProviderFile), "");
      }
      catch (Exception e)
      {
         log.error("Exception loading the identity providers:", e);
      }
   }

   /**
    * @see {@code IMetadataConfigurationStore#cleanup()}
    */
   public void cleanup()
   {
   }
}
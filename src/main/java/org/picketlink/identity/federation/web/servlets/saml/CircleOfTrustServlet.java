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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.saml.v2.metadata.store.FileBasedMetadataConfigurationStore;
import org.picketlink.identity.federation.core.saml.v2.metadata.store.IMetadataConfigurationStore;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;

/**
 * Circle of trust establishing servlet that accesses
 * the metadata urls of the various sites and updates
 * the common store
 * @author Anil.Saldhana@redhat.com
 * @since Apr 23, 2009
 */
public class CircleOfTrustServlet extends HttpServlet
{
   private static final long serialVersionUID = 1L;

   private transient IMetadataConfigurationStore configProvider = new FileBasedMetadataConfigurationStore();

   @Override
   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);

      String cstr = config.getInitParameter("configProvider");
      if (isNotNull(cstr))
      {
         try
         {
            configProvider = (IMetadataConfigurationStore) SecurityActions.loadClass(getClass(), cstr).newInstance();
         }
         catch (Exception e)
         {
            throw new ServletException(e);
         }
      }
   }

   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      //Handle listing of providers for either idp or sp
      //Handle adding an IDP
      //Handle adding a SP
      String action = req.getParameter("action");
      String type = req.getParameter("type");
      if (action == null)
         throw new ServletException(ErrorCodes.NULL_VALUE + "action");
      if (type == null)
         throw new ServletException(ErrorCodes.NULL_VALUE + "type");

      //SP
      if ("sp".equalsIgnoreCase(type))
      {
         if ("add".equalsIgnoreCase(action))
         {
            try
            {
               addIDP(req, resp);
               req.getRequestDispatcher("/addedIDP.jsp").forward(req, resp);
            }
            catch (Exception e)
            {
               throw new ServletException(e);
            }
         }
         if ("display_trusted_providers".equalsIgnoreCase(action))
         {
            try
            {
               displayTrustedProvidersForSP(req, resp);
               req.getRequestDispatcher("/spTrustedProviders.jsp").forward(req, resp);
            }
            catch (Exception e)
            {
               throw new ServletException(e);
            }
         }
      }
      else
      //IDP
      if ("idp".equalsIgnoreCase(type))
      {
         if ("add".equalsIgnoreCase(action))
         {
            try
            {
               addSP(req, resp);
               req.getRequestDispatcher("/addedSP.jsp").forward(req, resp);
            }
            catch (Exception e)
            {
               throw new ServletException(e);
            }
         }
         if ("display_trusted_providers".equalsIgnoreCase(action))
         {
            try
            {
               displayTrustedProvidersForIDP(req, resp);
               req.getRequestDispatcher("/idpTrustedProviders.jsp").forward(req, resp);
            }
            catch (Exception e)
            {
               throw new ServletException(e);
            }
         }
      }
   }

   private void addIDP(HttpServletRequest request, HttpServletResponse response) throws IOException
   {
      String spName = request.getParameter("spname");
      String idpName = request.getParameter("idpname");
      String metadataURL = request.getParameter("metadataURL");

      EntityDescriptorType edt = getMetaData(metadataURL);

      configProvider.persist(edt, idpName);

      HttpSession httpSession = request.getSession();
      httpSession.setAttribute("idp", edt);

      //Let us add the trusted providers
      Map<String, String> trustedProviders = new HashMap<String, String>();
      try
      {
         trustedProviders = configProvider.loadTrustedProviders(spName);
      }
      catch (ClassNotFoundException e)
      {
         log("Error obtaining the trusted providers for " + spName);
         throw new RuntimeException(e);
      }
      finally
      {
         trustedProviders.put(idpName, metadataURL);
         configProvider.persistTrustedProviders(spName, trustedProviders);
      }
   }

   private void addSP(HttpServletRequest request, HttpServletResponse response) throws IOException
   {
      String idpName = request.getParameter("idpname");
      String spName = request.getParameter("spname");
      String metadataURL = request.getParameter("metadataURL");

      EntityDescriptorType edt = getMetaData(metadataURL);
      configProvider.persist(edt, spName);

      HttpSession httpSession = request.getSession();
      httpSession.setAttribute("sp", edt);

      //Let us add the trusted providers
      Map<String, String> trustedProviders = new HashMap<String, String>();
      try
      {
         trustedProviders = configProvider.loadTrustedProviders(spName);
      }
      catch (Exception e)
      {
         log("Error obtaining the trusted providers for " + spName);
      }
      finally
      {
         trustedProviders.put(spName, metadataURL);
         configProvider.persistTrustedProviders(idpName, trustedProviders);
      }
   }

   private EntityDescriptorType getMetaData(String metadataURL) throws IOException
   {
      throw new RuntimeException();

      /*InputStream is;
      URL md = new URL(metadataURL);
      HttpURLConnection http = (HttpURLConnection) md.openConnection();
      http.setInstanceFollowRedirects(true);
      is = http.getInputStream();

      Unmarshaller un = MetaDataBuilder.getUnmarshaller();
      JAXBElement<?> j = (JAXBElement<?>) un.unmarshal(is);
      Object obj = j.getValue();
      if(obj instanceof EntityDescriptorType == false)
         throw new RuntimeException("Unsupported type:"+ obj.getClass());
      EntityDescriptorType edt = (EntityDescriptorType) obj;
      return edt;*/
   }

   private void displayTrustedProvidersForIDP(HttpServletRequest request, HttpServletResponse response)
         throws IOException, ClassNotFoundException
   {
      String idpName = request.getParameter("name");

      Map<String, String> trustedProviders = configProvider.loadTrustedProviders(idpName);

      HttpSession httpSession = request.getSession();
      httpSession.setAttribute("idpName", idpName);
      httpSession.setAttribute("providers", trustedProviders);
   }

   private void displayTrustedProvidersForSP(HttpServletRequest request, HttpServletResponse response)
         throws IOException, ClassNotFoundException
   {
      String spName = request.getParameter("name");

      Map<String, String> trustedProviders = configProvider.loadTrustedProviders(spName);

      HttpSession httpSession = request.getSession();
      httpSession.setAttribute("spName", spName);
      httpSession.setAttribute("providers", trustedProviders);
   }
}
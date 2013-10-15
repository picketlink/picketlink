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

import org.picketlink.common.ErrorCodes;
import org.picketlink.identity.federation.core.saml.v2.metadata.store.FileBasedMetadataConfigurationStore;
import org.picketlink.identity.federation.core.saml.v2.metadata.store.IMetadataConfigurationStore;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.picketlink.common.util.StringUtil.isNotNull;

/**
 * Circle of trust establishing servlet that accesses the metadata urls of the various sites and updates the common
 * store
 *
 * @author Anil.Saldhana@redhat.com
 * @since Apr 23, 2009
 */
public class CircleOfTrustServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private transient IMetadataConfigurationStore configProvider = new FileBasedMetadataConfigurationStore();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        String cstr = config.getInitParameter("configProvider");
        if (isNotNull(cstr)) {
            try {
                configProvider = (IMetadataConfigurationStore) SecurityActions.loadClass(getClass(), cstr).newInstance();
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Handle listing of providers for either idp or sp
        // Handle adding an IDP
        // Handle adding a SP
        String action = req.getParameter("action");
        String type = req.getParameter("type");
        if (action == null)
            throw new ServletException(ErrorCodes.NULL_VALUE + "action");
        if (type == null)
            throw new ServletException(ErrorCodes.NULL_VALUE + "type");

        // SP
        if ("sp".equalsIgnoreCase(type)) {
            if ("add".equalsIgnoreCase(action)) {
                try {
                    addIDP(req, resp);
                    req.getRequestDispatcher("/addedIDP.jsp").forward(req, resp);
                } catch (Exception e) {
                    throw new ServletException(e);
                }
            }
            if ("display_trusted_providers".equalsIgnoreCase(action)) {
                try {
                    displayTrustedProvidersForSP(req, resp);
                    req.getRequestDispatcher("/spTrustedProviders.jsp").forward(req, resp);
                } catch (Exception e) {
                    throw new ServletException(e);
                }
            }
        } else
            // IDP
            if ("idp".equalsIgnoreCase(type)) {
                if ("add".equalsIgnoreCase(action)) {
                    try {
                        addSP(req, resp);
                        req.getRequestDispatcher("/addedSP.jsp").forward(req, resp);
                    } catch (Exception e) {
                        throw new ServletException(e);
                    }
                }
                if ("display_trusted_providers".equalsIgnoreCase(action)) {
                    try {
                        displayTrustedProvidersForIDP(req, resp);
                        req.getRequestDispatcher("/idpTrustedProviders.jsp").forward(req, resp);
                    } catch (Exception e) {
                        throw new ServletException(e);
                    }
                }
            }
    }

    private void addIDP(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String spName = request.getParameter("spname");
        String idpName = request.getParameter("idpname");
        String metadataURL = request.getParameter("metadataURL");

        EntityDescriptorType edt = getMetaData(metadataURL);

        configProvider.persist(edt, idpName);

        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("idp", edt);

        // Let us add the trusted providers
        Map<String, String> trustedProviders = new HashMap<String, String>();
        try {
            trustedProviders = configProvider.loadTrustedProviders(spName);
        } catch (ClassNotFoundException e) {
            log("Error obtaining the trusted providers for " + spName);
            throw new RuntimeException(e);
        } finally {
            trustedProviders.put(idpName, metadataURL);
            configProvider.persistTrustedProviders(spName, trustedProviders);
        }
    }

    private void addSP(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idpName = request.getParameter("idpname");
        String spName = request.getParameter("spname");
        String metadataURL = request.getParameter("metadataURL");

        EntityDescriptorType edt = getMetaData(metadataURL);
        configProvider.persist(edt, spName);

        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("sp", edt);

        // Let us add the trusted providers
        Map<String, String> trustedProviders = new HashMap<String, String>();
        try {
            trustedProviders = configProvider.loadTrustedProviders(spName);
        } catch (Exception e) {
            log("Error obtaining the trusted providers for " + spName);
        } finally {
            trustedProviders.put(spName, metadataURL);
            configProvider.persistTrustedProviders(idpName, trustedProviders);
        }
    }

    private EntityDescriptorType getMetaData(String metadataURL) throws IOException {
        throw new RuntimeException();

        /*
         * InputStream is; URL md = new URL(metadataURL); HttpURLConnection http = (HttpURLConnection) md.openConnection();
         * http.setInstanceFollowRedirects(true); is = http.getInputStream();
         *
         * Unmarshaller un = MetaDataBuilder.getUnmarshaller(); JAXBElement<?> j = (JAXBElement<?>) un.unmarshal(is); Object obj
         * = j.getValue(); if(obj instanceof EntityDescriptorType == false) throw new RuntimeException("Unsupported type:"+
         * obj.getClass()); EntityDescriptorType edt = (EntityDescriptorType) obj; return edt;
         */
    }

    private void displayTrustedProvidersForIDP(HttpServletRequest request, HttpServletResponse response) throws IOException,
            ClassNotFoundException {
        String idpName = request.getParameter("name");

        Map<String, String> trustedProviders = configProvider.loadTrustedProviders(idpName);

        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("idpName", idpName);
        httpSession.setAttribute("providers", trustedProviders);
    }

    private void displayTrustedProvidersForSP(HttpServletRequest request, HttpServletResponse response) throws IOException,
            ClassNotFoundException {
        String spName = request.getParameter("name");

        Map<String, String> trustedProviders = configProvider.loadTrustedProviders(spName);

        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("spName", spName);
        httpSession.setAttribute("providers", trustedProviders);
    }
}
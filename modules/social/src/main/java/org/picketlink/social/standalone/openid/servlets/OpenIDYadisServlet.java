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
package org.picketlink.social.standalone.openid.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet used for Yadis Discovery in OpenID
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jul 7, 2009
 */
public class OpenIDYadisServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private String yadisResourceFile = "/WEB-INF/openid-yadis.xml";
    private String yadisURL = null;

    private boolean supportHTTP_HEAD = false; // By default, we support GET

    private transient InputStream yadisResourceInputStream = null;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext context = config.getServletContext();

        String yadisResourceFileStr = config.getInitParameter("yadisResourceFile");
        if (yadisResourceFileStr != null && yadisResourceFileStr.length() > 0)
            yadisResourceFile = yadisResourceFileStr;
        log("yadisResourceFile Location=" + yadisResourceFile);

        yadisURL = config.getInitParameter("yadisResourceURL");

        if (yadisURL == null || yadisURL.length() == 0) {
            yadisResourceInputStream = context.getResourceAsStream(yadisResourceFile);
            if (yadisResourceInputStream == null)
                throw new RuntimeException("yadisResourceFile is missing");
        }

        String supportHead = config.getInitParameter("support_HTTP_HEAD");
        if (supportHead != null && supportHead.length() > 0)
            supportHTTP_HEAD = Boolean.parseBoolean(supportHead);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (this.supportHTTP_HEAD) {
            log("GET not supported as HTTP HEAD has been configured");
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        } else {
            if (yadisResourceInputStream == null) {
                log("ERROR::yadisResourceInputStream is null");
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            byte[] barr = new byte[1024];
            for (int i = 0; i < barr.length; i++) {
                int b = yadisResourceInputStream.read();
                if (b == -1)
                    break;
                barr[i] = (byte) b;
            }

            resp.setContentType("application/xrds+xml");
            resp.setStatus(HttpServletResponse.SC_OK);
            OutputStream os = resp.getOutputStream();
            os.write(barr);
            os.flush();
            os.close();
        }
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (this.supportHTTP_HEAD) {
            resp.addHeader("X-XRDS-Location", yadisURL);
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        return;
    }
}
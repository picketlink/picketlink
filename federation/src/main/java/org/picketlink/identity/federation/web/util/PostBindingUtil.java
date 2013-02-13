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
package org.picketlink.identity.federation.web.util;

import static org.picketlink.common.util.StringUtil.isNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.identity.federation.core.saml.v2.holders.DestinationInfoHolder;
import org.picketlink.common.util.Base64;
import org.picketlink.common.constants.GeneralConstants;

/**
 * Utility for the HTTP/Post binding
 *
 * @author Anil.Saldhana@redhat.com
 * @since May 22, 2009
 */
public class PostBindingUtil {
    
    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();
    
    /**
     * Apply base64 encoding on the message
     *
     * @param stringToEncode
     * @return
     */
    public static String base64Encode(String stringToEncode) throws IOException {
        return Base64.encodeBytes(stringToEncode.getBytes("UTF-8"), Base64.DONT_BREAK_LINES);
    }

    /**
     * Apply base64 decoding on the message and return the byte array
     *
     * @param encodedString
     * @return
     */
    public static byte[] base64Decode(String encodedString) {
        if (encodedString == null)
            throw logger.nullArgumentError("encodedString");

        return Base64.decode(encodedString);
    }

    /**
     * Apply base64 decoding on the message and return the stream
     *
     * @param encodedString
     * @return
     */
    public static InputStream base64DecodeAsStream(String encodedString) {
        if (encodedString == null)
            throw logger.nullArgumentError("encodedString");

        return new ByteArrayInputStream(base64Decode(encodedString));
    }

    /**
     * Send the response to the redirected destination while adding the character encoding of "UTF-8" as well as adding headers
     * for cache-control and Pragma
     *
     * @param destination Destination URI where the response needs to redirect
     * @param response HttpServletResponse
     * @throws IOException
     */
    public static void sendPost(DestinationInfoHolder holder, HttpServletResponse response, boolean request) throws IOException {
        String key = request ? GeneralConstants.SAML_REQUEST_KEY : GeneralConstants.SAML_RESPONSE_KEY;

        String relayState = holder.getRelayState();
        String destination = holder.getDestination();
        String samlMessage = holder.getSamlMessage();

        if (destination == null)
            throw logger.nullValueError("Destination is null");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        common(holder.getDestination(), response);
        StringBuilder builder = new StringBuilder();

        builder.append("<HTML>");
        builder.append("<HEAD>");
        if (request)
            builder.append("<TITLE>HTTP Post Binding (Request)</TITLE>");
        else
            builder.append("<TITLE>HTTP Post Binding Response (Response)</TITLE>");

        builder.append("</HEAD>");
        builder.append("<BODY Onload=\"document.forms[0].submit()\">");

        builder.append("<FORM METHOD=\"POST\" ACTION=\"" + destination + "\">");
        builder.append("<INPUT TYPE=\"HIDDEN\" NAME=\"" + key + "\"" + " VALUE=\"" + samlMessage + "\"/>");
        if (isNotNull(relayState)) {
            builder.append("<INPUT TYPE=\"HIDDEN\" NAME=\"RelayState\" " + "VALUE=\"" + relayState + "\"/>");
        }
        builder.append("</FORM></BODY></HTML>");

        String str = builder.toString();
        logger.trace(str);
        out.println(str);
        out.close();
    }

    private static void common(String destination, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, no-store");
    }
}
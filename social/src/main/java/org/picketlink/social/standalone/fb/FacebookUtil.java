/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.social.standalone.fb;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Utility for facebook
 *
 * @author Anil Saldhana
 * @since May 8, 2011
 */
public class FacebookUtil {
    protected String serviceURL = null;

    public FacebookUtil(String url) {
        this.serviceURL = url;
    }

    /**
     * Given a {@link Map} of params, construct a query string
     *
     * @param params
     * @return
     */
    public String createQueryString(Map<String, String> params) {
        StringBuilder queryString = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String paramName = entry.getKey();
            String paramValue = entry.getValue();
            if (first) {
                first = false;
            } else {
                queryString.append("&");
            }
            queryString.append(paramName).append("=");
            String encodedParamValue;
            try {
                if (paramValue == null)
                    throw new RuntimeException("paramValue is null for paramName=" + paramName);
                encodedParamValue = URLEncoder.encode(paramValue, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            queryString.append(encodedParamValue);
        }
        return queryString.toString();
    }
}
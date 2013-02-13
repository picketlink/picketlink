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
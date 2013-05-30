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
package org.picketlink.scim.client;

import org.picketlink.scim.codec.SCIMParser;
import org.picketlink.scim.codec.SCIMWriter;
import org.picketlink.scim.model.v11.SCIMGroups;
import org.picketlink.scim.model.v11.SCIMUser;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * SCIM client class
 *
 * @author anil saldhana
 * @since Apr 9, 2013
 */
public class SCIMClient {

    private String baseURL = null;

    public SCIMClient() {
    }

    public SCIMClient setBaseURL(String base) {
        this.baseURL = base;
        return this;
    }

    public SCIMUser createUser(SCIMUser user) throws Exception {
        String url = baseURL + "/Users";
        SCIMWriter writer = new SCIMWriter();
        String json = writer.json(user);

        InputStream is = executePost(url, json, true, "xyz");
        SCIMParser parser = new SCIMParser();
        return parser.parseUser(is);
    }

    public SCIMGroups createGroup(SCIMGroups group) throws Exception {
        String url = baseURL + "/Users";
        SCIMWriter writer = new SCIMWriter();
        String json = writer.json(group);

        InputStream is = executePost(url, json, true, "xyz");
        SCIMParser parser = new SCIMParser();
        return parser.parseGroup(is);
    }

    public SCIMUser getUser(String id) throws Exception {
        String url = baseURL + "/Users/" + id;
        InputStream is = executeGet(url, null, false, "xyz");

        SCIMParser parser = new SCIMParser();
        return parser.parseUser(is);
    }

    public SCIMGroups getGroup(String id) throws Exception {
        String url = baseURL + "/Groups/" + id;
        InputStream is = executeGet(url, null, false, "xyz");

        SCIMParser parser = new SCIMParser();
        return parser.parseGroup(is);
    }

    private InputStream executeGet(String endpointURL, String body, boolean isJSON, String authorizationHeader)
            throws Exception {
        InputStream inputStream = null;
        try {
            URL resUrl = new URL(endpointURL);
            URLConnection urlConnection = resUrl.openConnection();
            if (urlConnection instanceof HttpURLConnection) {
                HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setAllowUserInteraction(false);
                if (isJSON) {
                    httpURLConnection.setRequestProperty("Content-Type", "application/json");
                } else {
                    httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                }
                if (body != null) {
                    httpURLConnection.setRequestProperty("Content-Length", Integer.toString(body.length()));
                    OutputStream ost = httpURLConnection.getOutputStream();
                    PrintWriter pw = new PrintWriter(ost);
                    pw.print(body);
                    pw.flush();
                    pw.close();
                }

                if (httpURLConnection.getResponseCode() == 400) {
                    inputStream = httpURLConnection.getErrorStream();
                } else {
                    inputStream = httpURLConnection.getInputStream();
                }
            } else {
                throw new RuntimeException("Wrong url conn");
            }
        } catch (Exception e) {
            throw new Exception(e);
        }
        return inputStream;
    }

    private InputStream executePost(String endpointURL, String body, boolean isJSON, String authorizationHeader)
            throws Exception {
        InputStream inputStream = null;
        try {
            URL resUrl = new URL(endpointURL);
            URLConnection urlConnection = resUrl.openConnection();
            if (urlConnection instanceof HttpURLConnection) {
                HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setAllowUserInteraction(false);
                if (isJSON) {
                    httpURLConnection.setRequestProperty("Content-Type", "application/json");
                } else {
                    httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                }
                if (body != null) {
                    httpURLConnection.setRequestProperty("Content-Length", Integer.toString(body.length()));
                    OutputStream ost = httpURLConnection.getOutputStream();
                    PrintWriter pw = new PrintWriter(ost);
                    pw.print(body);
                    pw.flush();
                    pw.close();
                }

                if (httpURLConnection.getResponseCode() == 400) {
                    inputStream = httpURLConnection.getErrorStream();
                } else {
                    inputStream = httpURLConnection.getInputStream();
                }
            } else {
                throw new RuntimeException("Wrong url conn");
            }
        } catch (Exception e) {
            throw new Exception(e);
        }
        return inputStream;
    }
}
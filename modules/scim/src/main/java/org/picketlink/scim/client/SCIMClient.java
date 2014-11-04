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

import org.picketlink.scim.model.v11.parser.SCIMParser;
import org.picketlink.scim.model.v11.parser.SCIMWriter;
import org.picketlink.scim.model.v11.resource.SCIMGroup;
import org.picketlink.scim.model.v11.resource.SCIMUser;

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

    public static final String OAUTH2_BEARER_HEADER = "Authorization: Bearer ";

    private String baseURL = null;

    public SCIMClient() {
    }

    public SCIMClient setBaseURL(String base) {
        this.baseURL = base;
        return this;
    }

    public SCIMUser createUser(SCIMUser user, String authorizationHeader) throws Exception {
        String url = baseURL + "/Users";
        SCIMWriter writer = new SCIMWriter();
        String json = writer.toString(user);

        InputStream is = executePost(url, json, true, authorizationHeader);
        SCIMParser parser = new SCIMParser();
        return parser.parseResource(is, SCIMUser.class);
    }

    public SCIMGroup createGroup(SCIMGroup group, String authorizationHeader) throws Exception {
        String url = baseURL + "/Groups";
        SCIMWriter writer = new SCIMWriter();
        String json = writer.toString(group);

        InputStream is = executePost(url, json, true, authorizationHeader);
        SCIMParser parser = new SCIMParser();
        return parser.parseResource(is, SCIMGroup.class);
    }

    public boolean deleteUser(String id, String authorizationHeader) throws Exception{
        String url = baseURL + "/Users/" + id ;

        int code = executeDelete(url, authorizationHeader);
        if(code == 200){
            return true;
        }
        return false;
    }

    public boolean deleteGroup(String id, String authorizationHeader) throws Exception{
        String url = baseURL + "/Groups/" + id ;

        int code = executeDelete(url, authorizationHeader);
        if(code == 200){
            return true;
        }
        return false;
    }

    public SCIMUser getUser(String id, String authorizationHeader) throws Exception {
        String url = baseURL + "/Users/" + id;
        InputStream is = executeGet(url, null, false, authorizationHeader);

        SCIMParser parser = new SCIMParser();
        return parser.parseResource(is, SCIMUser.class);
    }

    public SCIMGroup getGroup(String id, String authorizationHeader) throws Exception {
        String url = baseURL + "/Groups/" + id;
        InputStream is = executeGet(url, null, false, authorizationHeader);

        SCIMParser parser = new SCIMParser();
        return parser.parseResource(is, SCIMGroup.class);
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
                httpURLConnection.setRequestProperty(OAUTH2_BEARER_HEADER, authorizationHeader);
                if (isJSON) {
                    httpURLConnection.setRequestProperty("Content-Type", "application/toString");
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

                httpURLConnection.setRequestProperty(OAUTH2_BEARER_HEADER, authorizationHeader);

                if (isJSON) {
                    httpURLConnection.setRequestProperty("Content-Type", "application/toString");
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

    private int executeDelete(String endpointURL, String authorizationHeader)
            throws Exception {
        try {
            URL resUrl = new URL(endpointURL);
            URLConnection urlConnection = resUrl.openConnection();
            if (urlConnection instanceof HttpURLConnection) {
                HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
                httpURLConnection.setRequestMethod("DELETE");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setAllowUserInteraction(false);
                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                httpURLConnection.setRequestProperty(OAUTH2_BEARER_HEADER, authorizationHeader);

                httpURLConnection.connect();

                return httpURLConnection.getResponseCode();
            } else {
                throw new RuntimeException("Wrong url conn");
            }
        } catch (Exception e) {
            throw new Exception(e);
        }
    }
}
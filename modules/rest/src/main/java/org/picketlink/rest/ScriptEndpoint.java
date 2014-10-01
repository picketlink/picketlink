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
package org.picketlink.rest;

import java.io.IOException;
import java.io.InputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Returns JavaScript resources
 *
 * @author Shane Bryzak
 *
 */
@Path("/script")
@ApplicationScoped
public class ScriptEndpoint {

    private String picketlinkScript;

    public ScriptEndpoint()
        throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("picketlink.js");

        byte[] buffer = new byte[4096];
        StringBuilder sb = new StringBuilder();

        int read = is.read(buffer);
        while (read != -1) {
            sb.append(new String(buffer, 0, read));
            read = is.read(buffer);
        }

        picketlinkScript = sb.toString();
    }

    @GET
    @Path("/picketlink.js")
    @Produces("application/javascript")
    public String getPicketLinkClientScript() {
        return picketlinkScript;
    }
}

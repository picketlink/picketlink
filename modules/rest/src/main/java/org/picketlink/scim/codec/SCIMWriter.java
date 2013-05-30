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
package org.picketlink.scim.codec;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.picketlink.scim.model.v11.SCIMGroups;
import org.picketlink.scim.model.v11.SCIMUser;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Writing SCIM Classes into JSON
 *
 * @author anil saldhana
 * @since Apr 9, 2013
 */
public class SCIMWriter {
    private ObjectMapper objectMapper = new ObjectMapper();
    private JsonFactory jsonFactory = new JsonFactory();

    public String json(SCIMUser user) throws SCIMWriterException {
        return jsonify(user);
    }

    public String json(SCIMGroups groups) throws SCIMWriterException {
        return jsonify(groups);
    }

    private String jsonify(Object object) throws SCIMWriterException {
        try {
            StringWriter stringWriter = new StringWriter();
            JsonGenerator jg = jsonFactory.createJsonGenerator(stringWriter);
            objectMapper.writeValue(jg, object);
            return stringWriter.toString();
        } catch (JsonGenerationException e) {
            throw new SCIMWriterException(e);
        } catch (JsonMappingException e) {
            throw new SCIMWriterException(e);
        } catch (IOException e) {
            throw new SCIMWriterException(e);
        }
    }
}
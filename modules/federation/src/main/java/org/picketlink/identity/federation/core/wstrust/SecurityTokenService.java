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
package org.picketlink.identity.federation.core.wstrust;

import javax.xml.transform.Source;
import javax.xml.ws.Provider;

/**
 * <p>
 * The {@code SecurityTokenService} (STS) interface. It extends the {@code Provider} interface so that it can be
 * dynamically
 * invoked (as opposed to having a service endpoint interface).
 * </p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public interface SecurityTokenService extends Provider<Source> {

    /*
     * (non-Javadoc)
     *
     * @see javax.xml.ws.Provider#invoke(java.lang.Object)
     */
    Source invoke(Source request);
}

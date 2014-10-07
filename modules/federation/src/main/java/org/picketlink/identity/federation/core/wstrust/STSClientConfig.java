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

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.constants.WSTrustConstants;
import org.picketlink.common.util.StringUtil;
import org.picketlink.identity.federation.core.constants.PicketLinkFederationConstants;

import javax.xml.ws.soap.SOAPBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * STSClientConfig has the ability to either programatically construct the configuration needed for {@link STSClient}
 * or
 * parse a
 * file containing the configuration parameters.
 * <p/>
 *
 * <h3>Configure programatically</h3>
 * Example:
 *
 * <pre>
 * {@code
 * Builder builder = new STSClientConfig.Builder();
 * builder.serviceName("PicketLinkSTS");
 * builder.portName("PicketLinkSTSPort");
 * ...
 * STSClientConfig config = builder.build();
 * }
 * </pre>
 *
 * <h3>Configure from file</h3>
 * Example:
 *
 * <pre>
 * {
 *     &#064;code
 *     STSClientConfig config = new STSClientConfig.Builder(configFile).build();
 * }
 * </pre>
 *
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 * @author Anil Saldhana
 */
public class STSClientConfig implements STSClientConfigKeyProvider {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    public static final String DEFAULT_CONFIG_FILE = "sts-client.properties";

    public static final String SERVICE_NAME = "serviceName";

    public static final String PORT_NAME = "portName";

    public static final String ENDPOINT_ADDRESS = "endpointAddress";

    public static final String USERNAME = "username";

    public static final String PASSWORD = "password";

    public static final String TOKEN_TYPE = "tokenType";

    public static final String WSA_ISSUER = "wsaIssuer";

    public static final String WSP_APPLIES_TO = "wspAppliesTo";

    public static final String IS_BATCH = "isBatch";

    public static final String REQUEST_TYPE = "requestType";

    public static final String SOAP_BINDING = "soapBinding";

    public static final String NO_MODULE = "NO_MODULE";

    public static final String SUBSTITUTE_MODULE = "${module}";

    private final String serviceName;

    private final String portName;

    private final String endpointAddress;

    private final String username;

    private final String password;

    private final String wsaIssuer;

    private final String wspAppliesTo;

    private boolean isBatch = false; // Is the RST a batch request?

    private final String requestType;

    private final String soapBinding;

    private STSClientConfig(final Builder builder) {
        serviceName = builder.serviceName;
        portName = builder.portName;
        endpointAddress = builder.endpointAddress;
        username = builder.username;
        password = builder.password;
        isBatch = builder.isBatch;
        wsaIssuer = builder.wsaIssuer;
        wspAppliesTo = builder.wspAppliesTo;
        requestType = builder.requestType;
        soapBinding = builder.soapBinding;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getPortName() {
        return portName;
    }

    public String getEndPointAddress() {
        return endpointAddress;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getWsaIssuer() {
        return wsaIssuer;
    }

    public String getWspAppliesTo() {
        return wspAppliesTo;
    }

    public boolean isBatch() {
        return isBatch;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getSoapBinding() {
        return soapBinding;
    }

    public String toString() {
        return getClass().getSimpleName() + "[serviceName=" + serviceName + ", portName=" + portName + ", endpointAddress="
                + endpointAddress + "]";
    }

    public static class Builder {

        private String serviceName;

        private String portName;

        private String endpointAddress;

        private String username;

        private String password;

        private String wsaIssuer;

        private String wspAppliesTo;

        private boolean isBatch;

        // default to Issue, but could be also Validate (including the base of the namespace URI)
        private String requestType = WSTrustConstants.ISSUE_REQUEST;

        private String soapBinding = SOAPBinding.SOAP11HTTP_BINDING;

        public Builder() {
        }

        public Builder(final String configFile) {
            populate(configFile);
        }

        public Builder serviceName(final String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder portName(final String portName) {
            this.portName = portName;
            return this;
        }

        public Builder endpointAddress(final String address) {
            this.endpointAddress = address;
            return this;
        }

        public Builder username(final String username) {
            this.username = username;
            return this;
        }

        public Builder password(final String password) {
            this.password = password;
            return this;
        }

        public Builder wsaIssuer(final String wsa) {
            this.wsaIssuer = wsa;
            return this;
        }

        public Builder wspAppliesTo(final String wsp) {
            this.wspAppliesTo = wsp;
            return this;
        }

        public Builder requestType(final String requestType) {
            this.requestType = requestType;
            return this;
        }

        public String getServiceName() {
            return serviceName;
        }

        public String getPortName() {
            return portName;
        }

        public String getEndpointAddress() {
            return endpointAddress;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public boolean isBatch() {
            return isBatch;
        }

        public void setBatch(boolean isBatch) {
            this.isBatch = isBatch;
        }

        public STSClientConfig build() {
            validate(this);
            return new STSClientConfig(this);
        }

        private void populate(final String configFile) {
            InputStream in = null;

            try {
                in = getResource(configFile);
                if (in == null) {
                    throw logger.nullValueError("properties file " + configFile);

                }
                final Properties properties = new Properties();
                properties.load(in);
                this.serviceName = properties.getProperty(SERVICE_NAME);
                this.portName = properties.getProperty(PORT_NAME);
                this.endpointAddress = properties.getProperty(ENDPOINT_ADDRESS);
                this.username = properties.getProperty(USERNAME);
                this.password = properties.getProperty(PASSWORD);
                this.wsaIssuer = properties.getProperty(WSA_ISSUER);
                this.wspAppliesTo = properties.getProperty(WSP_APPLIES_TO);
                String batchStr = properties.getProperty(IS_BATCH);
                this.isBatch = StringUtil.isNotNull(batchStr) ? Boolean.parseBoolean(batchStr) : false;
                this.requestType = properties.getProperty(REQUEST_TYPE);

                if (!StringUtil.isNullOrEmpty(properties.getProperty(SOAP_BINDING))) {
                    this.soapBinding = properties.getProperty(SOAP_BINDING);
                }

                if (this.password.startsWith(PicketLinkFederationConstants.PASS_MASK_PREFIX)) {
                    // password is masked
                    String salt = properties.getProperty(PicketLinkFederationConstants.SALT);
                    int iterationCount = Integer
                            .parseInt(properties.getProperty(PicketLinkFederationConstants.ITERATION_COUNT));
                    try {
                        this.password = StringUtil.decode(password, salt, iterationCount);
                    } catch (Exception e) {
                        throw logger.unableToDecodePasswordError(this.password);
                    }
                }
            } catch (IOException e) {
                throw logger.couldNotLoadProperties(configFile);
            } finally {
                try {
                    if (in != null)
                        in.close();
                } catch (final IOException ignored) {
                    ignored.printStackTrace();
                }
            }
        }

        private void validate(Builder builder) {
            logger.trace("Checkin ServiceName:");

            checkPropertyShowValue(SERVICE_NAME, serviceName);

            logger.trace("Checkin portName:");

            checkPropertyShowValue(PORT_NAME, portName);

            logger.trace("Checkin endpointAddress:");

            checkPropertyShowValue(ENDPOINT_ADDRESS, endpointAddress);

            logger.trace("Checkin username:");

            checkProperty(USERNAME, username);

            logger.trace("password portName:");

            checkProperty(PASSWORD, password);
        }

        private void checkPropertyShowValue(final String propertyName, final String propertyValue) {
            if (propertyValue == null || propertyValue.equals(""))
                throw logger.nullValueError(propertyName + " : was:" + propertyValue);
        }

        private void checkProperty(final String propertyName, final String propertyValue) {
            if (propertyValue == null || propertyValue.equals(""))
                throw logger.nullValueError(propertyValue);
        }
    }

    private static InputStream getResource(String resource) throws IOException {
        // Try it as a File resource...
        final File file = new File(resource);

        if (file.exists() && !file.isDirectory()) {
            return new FileInputStream(file);
        }
        // Try it as a classpath resource ...
        URL url = SecurityActions.loadResource(STSClientConfig.class, resource);
        if (url != null) {
            final InputStream is = url.openStream();
            if (is != null) {
                return is;
            }
        }

        return null;
    }

    @Override
    public String getSTSClientConfigKey() {
        return computeSTSClientConfigKey(SUBSTITUTE_MODULE, this.serviceName, this.portName, this.endpointAddress, this.getUsername());
    }

    public static String computeSTSClientConfigKey(String module, String serviceName, String portName, String endPointAddress, String userName) {
        if (module != null) {
            return module + "|" + serviceName + "|" + portName + "|" + endPointAddress + "|" + userName;
        }
        else {
            return NO_MODULE + "|" + serviceName + "|" + portName + "|" + endPointAddress + "|" + userName;
        }

    }

}
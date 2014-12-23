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
package org.picketlink.identity.federation.core.sts;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.sts.registry.DefaultRevocationRegistry;
import org.picketlink.identity.federation.core.sts.registry.DefaultTokenRegistry;
import org.picketlink.identity.federation.core.sts.registry.FileBasedRevocationRegistry;
import org.picketlink.identity.federation.core.sts.registry.FileBasedTokenRegistry;
import org.picketlink.identity.federation.core.sts.registry.JDBCRevocationRegistry;
import org.picketlink.identity.federation.core.sts.registry.JDBCTokenRegistry;
import org.picketlink.identity.federation.core.sts.registry.JPABasedRevocationRegistry;
import org.picketlink.identity.federation.core.sts.registry.JPABasedTokenRegistry;
import org.picketlink.identity.federation.core.sts.registry.OJDBCRevocationRegistry;
import org.picketlink.identity.federation.core.sts.registry.OJDBCTokenRegistry;
import org.picketlink.identity.federation.core.sts.registry.RevocationRegistry;
import org.picketlink.identity.federation.core.sts.registry.SecurityTokenRegistry;
import org.picketlink.identity.federation.core.wstrust.wrappers.Lifetime;

import java.util.Map;

/**
 * Base Class for instances of {@code SecurityTokenProvider}
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 * @author Anil.Saldhana@redhat.com
 * @since Jan 4, 2011
 */
public abstract class AbstractSecurityTokenProvider implements SecurityTokenProvider {

    protected static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    protected static final String TOKEN_REGISTRY = "TokenRegistry";

    protected static final String TOKEN_REGISTRY_FILE = "TokenRegistryFile";

    protected static final String TOKEN_REGISTRY_JPA = "TokenRegistryJPA";

    protected static final String TOKEN_REGISTRY_JDBC = "TokenRegistryJDBC";

    protected static final String TOKEN_REGISTRY_JDBC_NAME_SPACE = "TokenRegistryJDBCNameSpace";

    protected static final String TOKEN_REGISTRY_JDBC_DATASOURCE = "TokenRegistryJDBCDataSource";

    protected static final String REVOCATION_REGISTRY = "RevocationRegistry";

    protected static final String REVOCATION_REGISTRY_FILE = "RevocationRegistryFile";

    protected static final String REVOCATION_REGISTRY_JPA_CONFIG = "RevocationRegistryJPAConfig";

    protected static final String REVOCATION_REGISTRY_JDBC_CONFIG = "RevocationRegistryJDBCConfig";

    protected static final String REVOCATION_REGISTRY_JDBC_NAME_SPACE = "RevocationRegistryJDBCNameSpace";

    protected static final String REVOCATION_REGISTRY_JDBC_DATA_SOURCE = "RevocationRegistryJDBCDataSource";

    protected static final String ATTRIBUTE_PROVIDER = "AttributeProvider";

    protected static final String USE_ABSOLUTE_KEYIDENTIFIER = "USE_ABSOLUTE_KEYIDENTIFIER";

    protected SecurityTokenRegistry tokenRegistry = new DefaultTokenRegistry();

    protected RevocationRegistry revocationRegistry = new DefaultRevocationRegistry();

    protected Map<String, String> properties;

    public void initialize(Map<String, String> properties) {
        this.properties = properties;

        // Check for token registry
        String tokenRegistryOption = this.properties.get(TOKEN_REGISTRY);
        if (tokenRegistryOption == null) {
            logger.stsTokenRegistryNotSpecified();
        } else {
            // if a file is to be used as registry, check if the user has specified the file name.
            if ("FILE".equalsIgnoreCase(tokenRegistryOption)) {
                String tokenRegistryFile = this.properties.get(TOKEN_REGISTRY_FILE);
                if (tokenRegistryFile != null) {
                    this.tokenRegistry = new FileBasedTokenRegistry(tokenRegistryFile);
                } else {
                    this.tokenRegistry = new FileBasedTokenRegistry();
                }
            } else if ("JPA".equalsIgnoreCase(tokenRegistryOption)) {
                String tokenRegistryjpa = this.properties.get(TOKEN_REGISTRY_JPA);
                if (tokenRegistryjpa != null) {
                    this.tokenRegistry = new JPABasedTokenRegistry(tokenRegistryjpa);
                } else {
                    this.tokenRegistry = new JPABasedTokenRegistry();
                }
            } else if ("JDBC".equalsIgnoreCase(tokenRegistryOption)) {
                String tokenRegistryjdbc = this.properties.get(TOKEN_REGISTRY_JDBC);
                if (tokenRegistryjdbc != null) {
                    this.tokenRegistry = new JDBCTokenRegistry(tokenRegistryjdbc);
                } else {
                    this.tokenRegistry = new JDBCTokenRegistry();
                }
            } else if ("OJDBC".equalsIgnoreCase(tokenRegistryOption)) {
                String jndiNameSpace = this.properties.get(TOKEN_REGISTRY_JDBC_NAME_SPACE);
                String jndiDataSource = this.properties.get(TOKEN_REGISTRY_JDBC_DATASOURCE);
                if (jndiNameSpace != null && jndiDataSource != null) {
                    this.tokenRegistry = new OJDBCTokenRegistry(jndiNameSpace, jndiDataSource);
                } else if (jndiDataSource != null) {
                    this.tokenRegistry = new OJDBCTokenRegistry(jndiDataSource);
                } else {
                    this.tokenRegistry = new OJDBCTokenRegistry();
                }
            }

            // the user has specified its own registry implementation class.
            else {
                try {
                    Class<?> clazz = SecurityActions.loadClass(getClass(), tokenRegistryOption);
                    if (clazz != null) {
                        Object object = clazz.newInstance();
                        if (object instanceof SecurityTokenRegistry) {
                            this.tokenRegistry = (SecurityTokenRegistry) object;
                        } else {
                            logger.stsTokenRegistryInvalidType(tokenRegistryOption);
                        }
                    }
                } catch (Exception pae) {
                    logger.stsTokenRegistryInstantiationError();
                    pae.printStackTrace();
                }
            }
        }

        if (this.tokenRegistry == null) {
            tokenRegistry = new

                DefaultTokenRegistry();
        }

        // check if a revocation registry option has been set.
        String registryOption = this.properties.get(REVOCATION_REGISTRY);
        if (registryOption == null) {
            logger.stsRevocationRegistryNotSpecified();
        } else {
            // if a file is to be used as registry, check if the user has specified the file name.
            if ("FILE".equalsIgnoreCase(registryOption)) {
                String registryFile = this.properties.get(REVOCATION_REGISTRY_FILE);
                if (registryFile != null) {
                    this.revocationRegistry = new FileBasedRevocationRegistry(registryFile);
                } else {
                    this.revocationRegistry = new FileBasedRevocationRegistry();
                }
            }
            // another option is to use the default JPA registry to store the revoked ids.
            else if ("JPA".equalsIgnoreCase(registryOption)) {
                String configuration = this.properties.get(REVOCATION_REGISTRY_JPA_CONFIG);
                if (configuration != null) {
                    this.revocationRegistry = new JPABasedRevocationRegistry(configuration);
                } else {
                    this.revocationRegistry = new JPABasedRevocationRegistry();
                }
            } else if ("JDBC".equalsIgnoreCase(registryOption)) {
                String configuration = this.properties.get(REVOCATION_REGISTRY_JDBC_CONFIG);
                if (configuration != null) {
                    this.revocationRegistry = new JDBCRevocationRegistry(configuration);
                } else {
                    this.revocationRegistry = new JDBCRevocationRegistry();
                }
            } else if ("OJDBC".equalsIgnoreCase(registryOption)) {
                String jndiNameSpace = this.properties.get(REVOCATION_REGISTRY_JDBC_NAME_SPACE);
                String jndiDataSource = this.properties.get(REVOCATION_REGISTRY_JDBC_DATA_SOURCE);
                if (jndiNameSpace != null && jndiDataSource != null) {
                    this.revocationRegistry = new OJDBCRevocationRegistry(jndiNameSpace, jndiDataSource);
                } else if (jndiDataSource != null) {
                    this.revocationRegistry = new OJDBCRevocationRegistry(jndiDataSource);
                } else {
                    this.revocationRegistry = new OJDBCRevocationRegistry();
                }
            }
            // the user has specified its own registry implementation class.
            else {
                try {
                    Class<?> clazz = SecurityActions.loadClass(getClass(), registryOption);
                    if (clazz != null) {
                        Object object = clazz.newInstance();
                        if (object instanceof RevocationRegistry) {
                            this.revocationRegistry = (RevocationRegistry) object;
                        } else {
                            logger.stsRevocationRegistryInvalidType(registryOption);
                        }
                    }
                } catch (Exception pae) {
                    logger.stsRevocationRegistryInstantiationError();
                    pae.printStackTrace();
                }
            }
        }

        if (this.revocationRegistry == null) {
            this.revocationRegistry = new

                DefaultRevocationRegistry();
        }
    }

    protected long getClockSkewInMillis() {
        String clockSkew = this.properties.get("CLOCK_SKEW");

        if (clockSkew == null) {
            clockSkew = "0";
        }

        return Long.parseLong(clockSkew);
    }

    protected Lifetime adjustLifetimeForClockSkew(Lifetime lifetime) throws ProcessingException {
        try  {
            lifetime.setCreated( XMLTimeUtil.subtract(lifetime.getCreated(), getClockSkewInMillis()));
            lifetime.setExpires( XMLTimeUtil.add(lifetime.getExpires(), getClockSkewInMillis()));
            return lifetime;
        } catch( ConfigurationException ce ) {
            throw new ProcessingException(ce.getMessage());
        }
    }


}
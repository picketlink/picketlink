/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.picketlink.config.http;

import org.picketlink.config.AbstractSecurityConfigurationBuilder;
import org.picketlink.config.SecurityConfigurationBuilder;
import org.picketlink.idm.config.SecurityConfigurationException;

import java.util.LinkedList;
import java.util.List;

/**
 * <p>A configuration builder with covenience methods to configure http security features.</p>
 *
 * @author Pedro Igor
 */
public abstract class AbstractHttpSecurityBuilder extends AbstractSecurityConfigurationBuilder<HttpSecurityConfiguration> implements HttpSecurityConfigurationChildBuilder {

    private final List<PathConfigurationBuilder> uriConfigBuilder = new LinkedList<PathConfigurationBuilder>();
    private FilteringMode filteringMode;

    public AbstractHttpSecurityBuilder(SecurityConfigurationBuilder builder) {
        super(builder);
    }

    /**
     * <p>Creates a configuration for a specific path.</p>
     *
     * @return
     */
    public PathConfigurationBuilder allPaths() {
        return pathGroup(PathConfiguration.DEFAULT_GROUP_NAME, PathConfiguration.URI_ALL);
    }

    /**
     * <p>Creates a configuration for the given <code>path</code>.</p>
     *
     * @param path The path. It should always begin with a slash. Some examples of path are: /rest, /rest/*, /*.jsf.
     * @return
     */
    public PathConfigurationBuilder forPath(String path) {
        return forPath(path, PathConfiguration.DEFAULT_GROUP_NAME);
    }

    /**
     * <p>Creates a configuration for the given <code>path</code> based on a previously configured path group.</p>
     *
     * @param path The path. It should always begin with a slash. Some examples of path are: /rest, /rest/*, /*.jsf.
     * @param groupName The group name. It must be a valid name referencing a path group defined using the {@link org.picketlink.config.http.AbstractHttpSecurityBuilder#forGroup(String)}.
     * @return
     */
    public PathConfigurationBuilder forPath(String path, String groupName) {
        return pathGroup(groupName, path);
    }

    /**
     * <p>Creates a configuration for a group of paths.</p>
     *
     * @param groupName The name of the path group.
     * @return
     */
    public PathConfigurationBuilder forGroup(String groupName) {
        return pathGroup(groupName, null);
    }

    /**
     * <p>The same as {@link org.picketlink.config.http.AbstractHttpSecurityBuilder#forGroup(String)}, but with a default <code>path</code>.</p>
     *
     * @param groupName
     * @param path
     * @return
     */
    private PathConfigurationBuilder pathGroup(String groupName, String path) {
        PathConfigurationBuilder pathConfigurationBuilder = new PathConfigurationBuilder(groupName, path, null, this);

        this.uriConfigBuilder.add(pathConfigurationBuilder);

        return pathConfigurationBuilder;
    }

    /**
     * <p>Indicates if the default behavior is to enforce security to all paths regardless they have a respective path configuration
     * or not.
     *
     * <p>If the protection mode is not specified (eg.: {@link AbstractHttpSecurityBuilder#restrictive()}, default is to be permissive.</p>
     *
     * @return
     */
    public HttpSecurityConfigurationChildBuilder restrictive() {
        this.filteringMode = FilteringMode.RESTRICTIVE;
        return this;
    }

    @Override
    protected HttpSecurityConfiguration create() throws SecurityConfigurationException {
        List<PathConfiguration> uriConfigs = new LinkedList<PathConfiguration>();

        for (PathConfigurationBuilder uriConfigBuilder : this.uriConfigBuilder) {
            uriConfigs.add(uriConfigBuilder.create());
        }

        return new HttpSecurityConfiguration(uriConfigs, this.filteringMode);
    }
}

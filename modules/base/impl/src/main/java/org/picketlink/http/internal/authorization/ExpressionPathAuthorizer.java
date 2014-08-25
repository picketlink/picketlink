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
package org.picketlink.http.internal.authorization;

import org.picketlink.config.http.AuthorizationConfiguration;
import org.picketlink.config.http.PathConfiguration;
import org.picketlink.internal.el.ELProcessor;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>A default implementation of {@link org.picketlink.http.authorization.PathAuthorizer}.</p>
 *
 * @author Pedro Igor
 */
public class ExpressionPathAuthorizer extends AbstractPathAuthorizer {

    @Inject
    private ELProcessor elProcessor;

    @Override
    protected boolean doAuthorize(PathConfiguration pathConfiguration, HttpServletRequest request, HttpServletResponse response) {
        AuthorizationConfiguration authorizationConfiguration = pathConfiguration.getAuthorizationConfiguration();

        if (authorizationConfiguration.getExpressions() == null) {
            return true;
        }

        String protectedUri = request.getContextPath() + pathConfiguration.getUri();
        int startRegex = protectedUri.indexOf("{");

        if (startRegex == -1) {
            String[] expressions = authorizationConfiguration.getExpressions();

            if (expressions != null) {
                for (String expression : expressions) {
                    try {
                        Object eval = this.elProcessor.eval(expression);

                        if (eval == null || !Boolean.class.isInstance(eval)) {
                            throw new RuntimeException("Authorization expressions [" + expression + "] must evaluate to a boolean.");
                        }

                        if (!Boolean.valueOf(eval.toString())) {
                            return false;
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to process authorization expression [" + expression + "] for path [" + protectedUri + "].", e);
                    }
                }
            }
        } else {
            String[] expressions = authorizationConfiguration.getExpressions();
            String formattedProtectedUri = protectedUri;

            if (expressions != null) {
                for (String expression : expressions) {
                    try {
                        Object eval = this.elProcessor.eval(expression);

                        if (eval == null) {
                            throw new RuntimeException("Authorization expressions [" + expression + "] must evaluate to a not null value.");
                        }

                        String expressionPattern = expression.substring(1);

                        if (formattedProtectedUri.indexOf(expressionPattern) == -1) {
                            return false;
                        }

                        formattedProtectedUri = formattedProtectedUri.replace(expressionPattern, eval.toString());
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to process authorization expression [" + expression + "] for path [" + protectedUri + "].", e);
                    }
                }

                if (!request.getRequestURI().equals(formattedProtectedUri)) {
                    int prefixEnd = formattedProtectedUri.lastIndexOf('/');

                    if (prefixEnd != -1) {
                        String prefix = formattedProtectedUri.substring(0, prefixEnd);

                        if (!request.getRequestURI().startsWith(prefix)) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

}

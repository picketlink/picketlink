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
package org.picketlink.http.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.picketlink.config.SecurityConfigurationBuilder;
import org.picketlink.config.http.AuthenticationConfigurationBuilder;
import org.picketlink.config.http.AuthorizationConfigurationBuilder;
import org.picketlink.config.http.BasicAuthenticationConfigurationBuilder;
import org.picketlink.config.http.CORSConfigurationBuilder;
import org.picketlink.config.http.DigestAuthenticationConfigurationBuilder;
import org.picketlink.config.http.FormAuthenticationConfigurationBuilder;
import org.picketlink.config.http.HttpSecurityConfigurationChildBuilder;
import org.picketlink.config.http.PathConfigurationBuilder;
import org.picketlink.config.http.TokenAuthenticationConfigurationBuilder;
import org.picketlink.config.http.X509AuthenticationConfigurationBuilder;
import org.picketlink.config.http.annotations.AllPaths;
import org.picketlink.config.http.annotations.AllowAnyOrigin;
import org.picketlink.config.http.annotations.AllowedGroups;
import org.picketlink.config.http.annotations.AllowedOrigins;
import org.picketlink.config.http.annotations.AllowedRealms;
import org.picketlink.config.http.annotations.AllowedRoles;
import org.picketlink.config.http.annotations.Authc;
import org.picketlink.config.http.annotations.Authz;
import org.picketlink.config.http.annotations.Basic;
import org.picketlink.config.http.annotations.Cors;
import org.picketlink.config.http.annotations.Digest;
import org.picketlink.config.http.annotations.ExposedHeaders;
import org.picketlink.config.http.annotations.Expressions;
import org.picketlink.config.http.annotations.Form;
import org.picketlink.config.http.annotations.MaxAge;
import org.picketlink.config.http.annotations.Path;
import org.picketlink.config.http.annotations.PathGroup;
import org.picketlink.config.http.annotations.Restrictive;
import org.picketlink.config.http.annotations.SupportAnyHeader;
import org.picketlink.config.http.annotations.SupportedHeaders;
import org.picketlink.config.http.annotations.SupportedMethods;
import org.picketlink.config.http.annotations.SupportsCredentials;
import org.picketlink.config.http.annotations.Token;
import org.picketlink.config.http.annotations.X509;

/**
 * @author Giriraj Sharma
 */
public class HttpSecurityAnnotationsParser {

    private SecurityConfigurationBuilder builder = new SecurityConfigurationBuilder();
    private HttpSecurityConfigurationChildBuilder httpSecurityBuilder = builder.http();
    private PathConfigurationBuilder pathConfigurationBuilder;
    private CORSConfigurationBuilder corsConfigurationBuilder;
    private AuthenticationConfigurationBuilder authenticationConfigurationBuilder;
    private FormAuthenticationConfigurationBuilder formAuthenticationConfigurationBuilder;
    private BasicAuthenticationConfigurationBuilder basicAuthenticationConfigurationBuilder;
    private DigestAuthenticationConfigurationBuilder digestAuthenticationConfigurationBuilder;
    private TokenAuthenticationConfigurationBuilder tokenAuthenticationConfigurationBuilder;
    private X509AuthenticationConfigurationBuilder x509AuthenticationConfigurationBuilder;
    private AuthorizationConfigurationBuilder authorizationConfigurationBuilder;

    public HttpSecurityConfigurationChildBuilder processAnnotatedType(Class<?> configurationClass) {

        Class<?> clazz = configurationClass;

        Class<? extends Annotation> previousAnnotation = null;
        Class<? extends Annotation> newAnnotation = null;

        for (Field enumm : clazz.getFields()) {

            for (Annotation a : enumm.getAnnotations()) {

                previousAnnotation = newAnnotation;
                newAnnotation = a.annotationType();

                if (a.annotationType() == Restrictive.class) {
                    this.httpSecurityBuilder = this.httpSecurityBuilder.restrictive();

                } else if (a.annotationType() == AllPaths.class) {
                    this.pathConfigurationBuilder = this.httpSecurityBuilder.allPaths();

                } else if (a.annotationType() == Path.class) {
                    Path path = (Path) a;
                    String pathName = path.pathName();
                    String pathGroup = path.pathGroup();

                    if (pathName != null && !pathName.isEmpty() && pathGroup != null && !pathGroup.isEmpty()) {
                        if (previousAnnotation == newAnnotation) {
                            this.pathConfigurationBuilder = this.pathConfigurationBuilder.forPath(pathName, pathGroup);
                        } else if (this.authorizationConfigurationBuilder != null) {
                            this.pathConfigurationBuilder = this.authorizationConfigurationBuilder.forPath(pathName, pathGroup);
                        } else if (previousAnnotation == Form.class || previousAnnotation == Basic.class
                                || previousAnnotation == Digest.class || previousAnnotation == Token.class
                                || previousAnnotation == X509.class) {
                            this.pathConfigurationBuilder = SetAuthenticationPathNameAndGroup(previousAnnotation, pathName,
                                    pathGroup);
                        } else if (this.corsConfigurationBuilder != null) {
                            this.pathConfigurationBuilder = this.corsConfigurationBuilder.forPath(pathName, pathGroup);
                        } else {
                            this.pathConfigurationBuilder = this.httpSecurityBuilder.forPath(pathName, pathGroup);
                        }
                    } else if (pathName != null && !pathName.isEmpty()) {
                        if (previousAnnotation == newAnnotation) {
                            this.pathConfigurationBuilder = this.pathConfigurationBuilder.forPath(pathName);
                        } else if (this.authorizationConfigurationBuilder != null) {
                            this.pathConfigurationBuilder = this.authorizationConfigurationBuilder.forPath(pathName);
                        } else if (previousAnnotation == Form.class || previousAnnotation == Basic.class
                                || previousAnnotation == Digest.class || previousAnnotation == Token.class
                                || previousAnnotation == X509.class) {
                            this.pathConfigurationBuilder = SetAuthenticationPathName(previousAnnotation, pathName);
                        } else if (this.corsConfigurationBuilder != null) {
                            this.pathConfigurationBuilder = this.corsConfigurationBuilder.forPath(pathName, pathGroup);
                        } else {
                            this.pathConfigurationBuilder = this.httpSecurityBuilder.forPath(pathName);
                        }
                    }

                } else if (a.annotationType() == PathGroup.class) {
                    PathGroup pathGroup = (PathGroup) a;
                    String groupName = pathGroup.pathGroupName();

                    if (groupName != null && !groupName.isEmpty()) {
                        this.pathConfigurationBuilder = this.httpSecurityBuilder.forGroup(groupName);
                    }

                } else if (a.annotationType() == Cors.class) {
                    this.corsConfigurationBuilder = this.pathConfigurationBuilder.cors();

                } else if (a.annotationType() == AllowedOrigins.class) {
                    AllowedOrigins allowedOrigins = (AllowedOrigins) a;
                    String[] origins = allowedOrigins.origins();
                    if (origins != null && origins.length > 0) {
                        this.corsConfigurationBuilder = this.corsConfigurationBuilder.allowedOrigins(origins);
                    }

                } else if (a.annotationType() == SupportedMethods.class) {
                    SupportedMethods supportedMethods = (SupportedMethods) a;
                    String[] methods = supportedMethods.methods();
                    if (methods != null && methods.length > 0) {
                        this.corsConfigurationBuilder = this.corsConfigurationBuilder.supportedMethods(methods);
                    }

                } else if (a.annotationType() == SupportedHeaders.class) {
                    SupportedHeaders supportedHeaders = (SupportedHeaders) a;
                    String[] headers = supportedHeaders.headers();
                    if (headers != null && headers.length > 0) {
                        this.corsConfigurationBuilder = this.corsConfigurationBuilder.supportedHeaders(headers);
                    }

                } else if (a.annotationType() == ExposedHeaders.class) {
                    ExposedHeaders exposedHeaders = (ExposedHeaders) a;
                    String[] headers = exposedHeaders.headers();
                    if (headers != null && headers.length > 0) {
                        this.corsConfigurationBuilder = this.corsConfigurationBuilder.exposedHeaders(headers);
                    }

                } else if (a.annotationType() == SupportsCredentials.class) {
                    this.corsConfigurationBuilder = this.corsConfigurationBuilder.supportsCredentials(true);

                } else if (a.annotationType() == AllowAnyOrigin.class) {
                    this.corsConfigurationBuilder = this.corsConfigurationBuilder.allowAnyOrigin(true);

                } else if (a.annotationType() == SupportAnyHeader.class) {
                    this.corsConfigurationBuilder = this.corsConfigurationBuilder.supportAnyHeader(true);

                } else if (a.annotationType() == MaxAge.class) {
                    MaxAge maxAge = (MaxAge) a;
                    long age = maxAge.age();
                    this.corsConfigurationBuilder = this.corsConfigurationBuilder.maxAge(age);

                } else if (a.annotationType() == Authc.class) {
                    if (this.corsConfigurationBuilder != null) {
                        this.authenticationConfigurationBuilder = this.corsConfigurationBuilder.authenticateWith();
                    } else {
                        this.authenticationConfigurationBuilder = this.pathConfigurationBuilder.authenticateWith();
                    }

                } else if (a.annotationType() == Form.class) {
                    this.formAuthenticationConfigurationBuilder = this.authenticationConfigurationBuilder.form();

                    Form form = (Form) a;
                    String restoreOriginalRequest = form.restoreOriginalRequest();
                    String loginPage = form.loginPage();
                    String errorPage = form.errorPage();

                    if (loginPage != null && !loginPage.isEmpty()) {
                        this.formAuthenticationConfigurationBuilder = this.formAuthenticationConfigurationBuilder
                                .loginPage(loginPage);
                    }
                    if (errorPage != null && !errorPage.isEmpty()) {
                        this.formAuthenticationConfigurationBuilder = this.formAuthenticationConfigurationBuilder
                                .errorPage(errorPage);
                    }
                    if (restoreOriginalRequest != null && restoreOriginalRequest.equals("yes")) {
                        this.formAuthenticationConfigurationBuilder = this.formAuthenticationConfigurationBuilder
                                .restoreOriginalRequest();
                    }

                } else if (a.annotationType() == Basic.class) {
                    this.basicAuthenticationConfigurationBuilder = this.authenticationConfigurationBuilder.basic();
                    Basic basic = (Basic) a;
                    String realmName = basic.realmName();
                    if (realmName != null && !realmName.isEmpty()) {
                        this.basicAuthenticationConfigurationBuilder = this.basicAuthenticationConfigurationBuilder
                                .realmName(realmName);
                    }

                } else if (a.annotationType() == Digest.class) {
                    this.digestAuthenticationConfigurationBuilder = this.authenticationConfigurationBuilder.digest();
                    Digest digest = (Digest) a;
                    String realmName = digest.realmName();
                    if (realmName != null && !realmName.isEmpty()) {
                        this.digestAuthenticationConfigurationBuilder = this.digestAuthenticationConfigurationBuilder
                                .realmName(realmName);
                    }

                } else if (a.annotationType() == X509.class) {
                    this.x509AuthenticationConfigurationBuilder = this.authenticationConfigurationBuilder.x509();
                    X509 x509 = (X509) a;
                    String subjectRegex = x509.subjectRegex();
                    if (subjectRegex != null && !subjectRegex.isEmpty()) {
                        this.x509AuthenticationConfigurationBuilder = this.x509AuthenticationConfigurationBuilder
                                .subjectRegex(subjectRegex);
                    }

                } else if (a.annotationType() == Token.class) {
                    this.tokenAuthenticationConfigurationBuilder = this.authenticationConfigurationBuilder.token();

                } else if (a.annotationType() == Authz.class) {
                    if (this.formAuthenticationConfigurationBuilder != null) {
                        this.authorizationConfigurationBuilder = this.formAuthenticationConfigurationBuilder.authorizeWith();

                    } else if (this.digestAuthenticationConfigurationBuilder != null) {
                        this.authorizationConfigurationBuilder = this.digestAuthenticationConfigurationBuilder.authorizeWith();

                    } else if (this.basicAuthenticationConfigurationBuilder != null) {
                        this.authorizationConfigurationBuilder = this.basicAuthenticationConfigurationBuilder.authorizeWith();

                    } else if (this.tokenAuthenticationConfigurationBuilder != null) {
                        this.authorizationConfigurationBuilder = this.tokenAuthenticationConfigurationBuilder.authorizeWith();

                    } else if (this.x509AuthenticationConfigurationBuilder != null) {
                        this.authorizationConfigurationBuilder = this.x509AuthenticationConfigurationBuilder.authorizeWith();

                    } else if (this.corsConfigurationBuilder != null) {
                        this.authorizationConfigurationBuilder = this.corsConfigurationBuilder.authorizeWith();

                    } else {
                        this.authorizationConfigurationBuilder = this.pathConfigurationBuilder.authorizeWith();
                    }

                } else if (a.annotationType() == AllowedRoles.class) {
                    AllowedRoles role = (AllowedRoles) a;
                    String[] roles = role.roles();
                    if (roles != null && roles.length > 0) {
                        this.authorizationConfigurationBuilder = this.authorizationConfigurationBuilder.role(roles);
                    }

                } else if (a.annotationType() == AllowedGroups.class) {
                    AllowedGroups group = (AllowedGroups) a;
                    String[] groups = group.groups();
                    if (groups != null && groups.length > 0) {
                        this.authorizationConfigurationBuilder = this.authorizationConfigurationBuilder.group(groups);
                    }

                } else if (a.annotationType() == AllowedRealms.class) {
                    AllowedRealms realm = (AllowedRealms) a;
                    String[] realms = realm.realms();
                    if (realms != null && realms.length > 0) {
                        this.authorizationConfigurationBuilder = this.authorizationConfigurationBuilder.realm(realms);
                    }

                } else if (a.annotationType() == Expressions.class) {
                    Expressions exp = (Expressions) a;
                    String[] expressions = exp.expressions();
                    if (expressions != null && expressions.length > 0) {
                        this.authorizationConfigurationBuilder = this.authorizationConfigurationBuilder.expression(expressions);
                    }
                }
            }
        }
        return this.httpSecurityBuilder;
    }

    private PathConfigurationBuilder SetAuthenticationPathName(Class<? extends Annotation> previousAnnotation, String pathName) {
        if (previousAnnotation == Form.class) {
            this.pathConfigurationBuilder = this.formAuthenticationConfigurationBuilder.forPath(pathName);
        } else if (previousAnnotation == Digest.class) {
            this.pathConfigurationBuilder = this.digestAuthenticationConfigurationBuilder.forPath(pathName);
        } else if (previousAnnotation == Basic.class) {
            this.pathConfigurationBuilder = this.basicAuthenticationConfigurationBuilder.forPath(pathName);
        } else if (previousAnnotation == Token.class) {
            this.pathConfigurationBuilder = this.tokenAuthenticationConfigurationBuilder.forPath(pathName);
        } else if (previousAnnotation == X509.class) {
            this.pathConfigurationBuilder = this.x509AuthenticationConfigurationBuilder.forPath(pathName);
        }
        return this.pathConfigurationBuilder;
    }

    private PathConfigurationBuilder SetAuthenticationPathNameAndGroup(Class<? extends Annotation> previousAnnotation,
            String pathName, String pathGroup) {
        if (previousAnnotation == Form.class) {
            this.pathConfigurationBuilder = this.formAuthenticationConfigurationBuilder.forPath(pathName, pathGroup);
        } else if (previousAnnotation == Digest.class) {
            this.pathConfigurationBuilder = this.digestAuthenticationConfigurationBuilder.forPath(pathName, pathGroup);
        } else if (previousAnnotation == Basic.class) {
            this.pathConfigurationBuilder = this.basicAuthenticationConfigurationBuilder.forPath(pathName, pathGroup);
        } else if (previousAnnotation == Token.class) {
            this.pathConfigurationBuilder = this.tokenAuthenticationConfigurationBuilder.forPath(pathName, pathGroup);
        } else if (previousAnnotation == X509.class) {
            this.pathConfigurationBuilder = this.x509AuthenticationConfigurationBuilder.forPath(pathName, pathGroup);
        }
        return this.pathConfigurationBuilder;
    }
}
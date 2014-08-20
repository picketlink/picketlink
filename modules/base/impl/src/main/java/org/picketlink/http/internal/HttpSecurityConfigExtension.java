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

import javax.enterprise.inject.spi.Extension;

import org.picketlink.config.SecurityConfigurationBuilder;
import org.picketlink.config.http.AuthenticationConfigurationBuilder;
import org.picketlink.config.http.AuthorizationConfigurationBuilder;
import org.picketlink.config.http.BasicAuthenticationConfigurationBuilder;
import org.picketlink.config.http.DigestAuthenticationConfigurationBuilder;
import org.picketlink.config.http.FormAuthenticationConfigurationBuilder;
import org.picketlink.config.http.HttpSecurityBuilder;
import org.picketlink.config.http.PathConfigurationBuilder;
import org.picketlink.config.http.TokenAuthenticationConfigurationBuilder;
import org.picketlink.config.http.X509AuthenticationConfigurationBuilder;
import org.picketlink.config.http.annotations.AllPaths;
import org.picketlink.config.http.annotations.AllowedGroups;
import org.picketlink.config.http.annotations.AllowedRealms;
import org.picketlink.config.http.annotations.AllowedRoles;
import org.picketlink.config.http.annotations.Authc;
import org.picketlink.config.http.annotations.Authz;
import org.picketlink.config.http.annotations.Basic;
import org.picketlink.config.http.annotations.Digest;
import org.picketlink.config.http.annotations.Expressions;
import org.picketlink.config.http.annotations.Form;
import org.picketlink.config.http.annotations.Path;
import org.picketlink.config.http.annotations.PathGroup;
import org.picketlink.config.http.annotations.Permissive;
import org.picketlink.config.http.annotations.Restrictive;
import org.picketlink.config.http.annotations.Token;
import org.picketlink.config.http.annotations.X509;

public class HttpSecurityConfigExtension implements Extension {

    public static SecurityConfigurationBuilder builder = new SecurityConfigurationBuilder();
    public static HttpSecurityBuilder httpSecurityBuilder = builder.http();
    private static PathConfigurationBuilder pathConfigurationBuilder;
    private static AuthenticationConfigurationBuilder authenticationConfigurationBuilder;
    private static FormAuthenticationConfigurationBuilder formAuthenticationConfigurationBuilder;
    private static BasicAuthenticationConfigurationBuilder basicAuthenticationConfigurationBuilder;
    private static DigestAuthenticationConfigurationBuilder digestAuthenticationConfigurationBuilder;
    private static TokenAuthenticationConfigurationBuilder tokenAuthenticationConfigurationBuilder;
    private static X509AuthenticationConfigurationBuilder x509AuthenticationConfigurationBuilder;
    private static AuthorizationConfigurationBuilder authorizationConfigurationBuilder;

    public static HttpSecurityBuilder processAnnotatedType(Class<?> configurationClass) {

        Class<?> clazz = configurationClass;

        Class<? extends Annotation> previousAnnotation = null;
        Class<? extends Annotation> newAnnotation = null;

        for (Field enumm : clazz.getFields()) {

            for (Annotation a : enumm.getAnnotations()) {

                previousAnnotation = newAnnotation;
                newAnnotation = a.annotationType();

                if (a.annotationType() == Permissive.class) {
                    HttpSecurityConfigExtension.httpSecurityBuilder = HttpSecurityConfigExtension.httpSecurityBuilder.permissive();

                } else if (a.annotationType() == Restrictive.class) {
                    HttpSecurityConfigExtension.httpSecurityBuilder = HttpSecurityConfigExtension.httpSecurityBuilder.restrictive();

                } else if (a.annotationType() == AllPaths.class) {
                    if (HttpSecurityConfigExtension.authorizationConfigurationBuilder != null) {
                        HttpSecurityConfigExtension.pathConfigurationBuilder = HttpSecurityConfigExtension.authorizationConfigurationBuilder.allPaths();
                    } else {
                        HttpSecurityConfigExtension.pathConfigurationBuilder = HttpSecurityConfigExtension.httpSecurityBuilder.allPaths();
                    }

                } else if (a.annotationType() == Path.class) {
                    Path path = (Path) a;
                    String pathName = path.pathName();
                    String pathGroup = path.pathGroup();

                    if (pathName != null && !pathName.isEmpty() && pathGroup != null && !pathGroup.isEmpty()) {
                        if (previousAnnotation == newAnnotation) {
                            HttpSecurityConfigExtension.pathConfigurationBuilder = HttpSecurityConfigExtension.pathConfigurationBuilder.path(pathName, pathGroup);
                        } else if (HttpSecurityConfigExtension.authorizationConfigurationBuilder != null) {
                            HttpSecurityConfigExtension.pathConfigurationBuilder = HttpSecurityConfigExtension.authorizationConfigurationBuilder.path(pathName, pathGroup);
                        } else if (previousAnnotation == Form.class || previousAnnotation == Basic.class || previousAnnotation == Digest.class
                            || previousAnnotation == Token.class || previousAnnotation == X509.class) {
                            HttpSecurityConfigExtension.pathConfigurationBuilder = SetAuthenticationPathNameAndGroup(previousAnnotation, pathName, pathGroup);
                        } else {
                            HttpSecurityConfigExtension.pathConfigurationBuilder = HttpSecurityConfigExtension.httpSecurityBuilder.path(pathName, pathGroup);
                        }
                    } else if (pathName != null && !pathName.isEmpty()) {
                        if (previousAnnotation == newAnnotation) {
                            HttpSecurityConfigExtension.pathConfigurationBuilder = HttpSecurityConfigExtension.pathConfigurationBuilder.path(pathName);
                        } else if (HttpSecurityConfigExtension.authorizationConfigurationBuilder != null) {
                            HttpSecurityConfigExtension.pathConfigurationBuilder = HttpSecurityConfigExtension.authorizationConfigurationBuilder.path(pathName);
                        } else if (previousAnnotation == Form.class || previousAnnotation == Basic.class || previousAnnotation == Digest.class
                            || previousAnnotation == Token.class || previousAnnotation == X509.class) {
                            HttpSecurityConfigExtension.pathConfigurationBuilder = SetAuthenticationPathName(previousAnnotation, pathName);
                        } else {
                            HttpSecurityConfigExtension.pathConfigurationBuilder = HttpSecurityConfigExtension.httpSecurityBuilder.path(pathName);
                        }
                    }

                } else if (a.annotationType() == PathGroup.class) {
                    PathGroup pathGroup = (PathGroup) a;
                    String groupName = pathGroup.pathGroupName();

                    if (groupName != null && !groupName.isEmpty()) {
                        if (previousAnnotation == newAnnotation) {
                            HttpSecurityConfigExtension.pathConfigurationBuilder = HttpSecurityConfigExtension.pathConfigurationBuilder.path(groupName);
                        } else if (HttpSecurityConfigExtension.authorizationConfigurationBuilder != null) {
                            HttpSecurityConfigExtension.pathConfigurationBuilder = HttpSecurityConfigExtension.authorizationConfigurationBuilder.pathGroup(groupName);
                        } else {
                            HttpSecurityConfigExtension.pathConfigurationBuilder = HttpSecurityConfigExtension.httpSecurityBuilder.pathGroup(groupName);
                        }
                    }
                }
                else if (a.annotationType() == Authc.class) {
                    HttpSecurityConfigExtension.authenticationConfigurationBuilder = HttpSecurityConfigExtension.pathConfigurationBuilder.authc();

                } else if (a.annotationType() == Form.class) {
                    HttpSecurityConfigExtension.formAuthenticationConfigurationBuilder = HttpSecurityConfigExtension.authenticationConfigurationBuilder.form();

                    Form form = (Form) a;
                    String restoreOriginalRequest = form.restoreOriginalRequest();
                    String loginPage = form.loginPage();
                    String errorPage = form.errorPage();

                    if (loginPage != null && !loginPage.isEmpty()) {
                        HttpSecurityConfigExtension.formAuthenticationConfigurationBuilder =
                            HttpSecurityConfigExtension.formAuthenticationConfigurationBuilder.loginPage(loginPage);
                    }
                    if (errorPage != null && !errorPage.isEmpty()) {
                        HttpSecurityConfigExtension.formAuthenticationConfigurationBuilder =
                            HttpSecurityConfigExtension.formAuthenticationConfigurationBuilder.errorPage(errorPage);
                    }
                    if (restoreOriginalRequest != null && restoreOriginalRequest.equals("yes")) {
                        HttpSecurityConfigExtension.formAuthenticationConfigurationBuilder =
                            HttpSecurityConfigExtension.formAuthenticationConfigurationBuilder.restoreOriginalRequest();
                    }

                } else if (a.annotationType() == Basic.class) {
                    HttpSecurityConfigExtension.basicAuthenticationConfigurationBuilder = HttpSecurityConfigExtension.authenticationConfigurationBuilder.basic();
                    Basic basic = (Basic) a;
                    String realmName = basic.realmName();
                    if (realmName != null && !realmName.isEmpty()) {
                        HttpSecurityConfigExtension.basicAuthenticationConfigurationBuilder =
                            HttpSecurityConfigExtension.basicAuthenticationConfigurationBuilder.realmName(realmName);
                    }

                } else if (a.annotationType() == Digest.class) {
                    HttpSecurityConfigExtension.digestAuthenticationConfigurationBuilder = HttpSecurityConfigExtension.authenticationConfigurationBuilder.digest();
                    Digest digest = (Digest) a;
                    String realmName = digest.realmName();
                    if (realmName != null && !realmName.isEmpty()) {
                        HttpSecurityConfigExtension.digestAuthenticationConfigurationBuilder =
                            HttpSecurityConfigExtension.digestAuthenticationConfigurationBuilder.realmName(realmName);
                    }

                } else if (a.annotationType() == X509.class) {
                    HttpSecurityConfigExtension.x509AuthenticationConfigurationBuilder = HttpSecurityConfigExtension.authenticationConfigurationBuilder.x509();
                    X509 x509 = (X509) a;
                    String subjectRegex = x509.subjectRegex();
                    if (subjectRegex != null && !subjectRegex.isEmpty()) {
                        HttpSecurityConfigExtension.x509AuthenticationConfigurationBuilder =
                            HttpSecurityConfigExtension.x509AuthenticationConfigurationBuilder.subjectRegex(subjectRegex);
                    }

                } else if (a.annotationType() == Token.class) {
                    HttpSecurityConfigExtension.tokenAuthenticationConfigurationBuilder = HttpSecurityConfigExtension.authenticationConfigurationBuilder.token();

                } else if (a.annotationType() == Authz.class) {
                    if (HttpSecurityConfigExtension.authenticationConfigurationBuilder == null) {
                        HttpSecurityConfigExtension.authorizationConfigurationBuilder = HttpSecurityConfigExtension.pathConfigurationBuilder.authz();

                    } else if (HttpSecurityConfigExtension.formAuthenticationConfigurationBuilder != null) {
                        HttpSecurityConfigExtension.authorizationConfigurationBuilder = HttpSecurityConfigExtension.formAuthenticationConfigurationBuilder.authz();

                    } else if (HttpSecurityConfigExtension.digestAuthenticationConfigurationBuilder != null) {
                        HttpSecurityConfigExtension.authorizationConfigurationBuilder = HttpSecurityConfigExtension.digestAuthenticationConfigurationBuilder.authz();

                    } else if (HttpSecurityConfigExtension.basicAuthenticationConfigurationBuilder != null) {
                        HttpSecurityConfigExtension.authorizationConfigurationBuilder = HttpSecurityConfigExtension.basicAuthenticationConfigurationBuilder.authz();

                    } else if (HttpSecurityConfigExtension.tokenAuthenticationConfigurationBuilder != null) {
                        HttpSecurityConfigExtension.authorizationConfigurationBuilder = HttpSecurityConfigExtension.tokenAuthenticationConfigurationBuilder.authz();

                    } else if (HttpSecurityConfigExtension.x509AuthenticationConfigurationBuilder != null) {
                        HttpSecurityConfigExtension.authorizationConfigurationBuilder = HttpSecurityConfigExtension.x509AuthenticationConfigurationBuilder.authz();
                    }

                } else if (a.annotationType() == AllowedRoles.class) {
                    AllowedRoles role = (AllowedRoles) a;
                    String[] roles = role.roles();
                    if (roles != null && roles.length > 0) {
                        HttpSecurityConfigExtension.authorizationConfigurationBuilder = HttpSecurityConfigExtension.authorizationConfigurationBuilder.role(roles);
                    }

                } else if (a.annotationType() == AllowedGroups.class) {
                    AllowedGroups group = (AllowedGroups) a;
                    String[] groups = group.groups();
                    if (groups != null && groups.length > 0) {
                        HttpSecurityConfigExtension.authorizationConfigurationBuilder = HttpSecurityConfigExtension.authorizationConfigurationBuilder.group(groups);
                    }

                } else if (a.annotationType() == AllowedRealms.class) {
                    AllowedRealms realm = (AllowedRealms) a;
                    String[] realms = realm.realms();
                    if (realms != null && realms.length > 0) {
                        HttpSecurityConfigExtension.authorizationConfigurationBuilder = HttpSecurityConfigExtension.authorizationConfigurationBuilder.realm(realms);
                    }

                } else if (a.annotationType() == Expressions.class) {
                    Expressions exp = (Expressions) a;
                    String[] expressions = exp.expressions();
                    if (expressions != null && expressions.length > 0) {
                        HttpSecurityConfigExtension.authorizationConfigurationBuilder =
                            HttpSecurityConfigExtension.authorizationConfigurationBuilder.expression(expressions);
                    }
                }
            }
        }
        return HttpSecurityConfigExtension.httpSecurityBuilder;
    }

    private static PathConfigurationBuilder SetAuthenticationPathName(Class<? extends Annotation> previousAnnotation, String pathName) {
        if (previousAnnotation == Form.class) {
            HttpSecurityConfigExtension.pathConfigurationBuilder = HttpSecurityConfigExtension.formAuthenticationConfigurationBuilder.path(pathName);
        } else if (previousAnnotation == Digest.class) {
            HttpSecurityConfigExtension.pathConfigurationBuilder = HttpSecurityConfigExtension.digestAuthenticationConfigurationBuilder.path(pathName);
        } else if (previousAnnotation == Basic.class) {
            HttpSecurityConfigExtension.pathConfigurationBuilder = HttpSecurityConfigExtension.basicAuthenticationConfigurationBuilder.path(pathName);
        } else if (previousAnnotation == Token.class) {
            HttpSecurityConfigExtension.pathConfigurationBuilder = HttpSecurityConfigExtension.tokenAuthenticationConfigurationBuilder.path(pathName);
        } else if (previousAnnotation == X509.class) {
            HttpSecurityConfigExtension.pathConfigurationBuilder = HttpSecurityConfigExtension.x509AuthenticationConfigurationBuilder.path(pathName);
        }
        return HttpSecurityConfigExtension.pathConfigurationBuilder;
    }

    private static PathConfigurationBuilder SetAuthenticationPathNameAndGroup(Class<? extends Annotation> previousAnnotation, String pathName, String pathGroup) {
        if (previousAnnotation == Form.class) {
            HttpSecurityConfigExtension.pathConfigurationBuilder = HttpSecurityConfigExtension.formAuthenticationConfigurationBuilder.path(pathName, pathGroup);
        } else if (previousAnnotation == Digest.class) {
            HttpSecurityConfigExtension.pathConfigurationBuilder = HttpSecurityConfigExtension.digestAuthenticationConfigurationBuilder.path(pathName, pathGroup);
        } else if (previousAnnotation == Basic.class) {
            HttpSecurityConfigExtension.pathConfigurationBuilder = HttpSecurityConfigExtension.basicAuthenticationConfigurationBuilder.path(pathName, pathGroup);
        } else if (previousAnnotation == Token.class) {
            HttpSecurityConfigExtension.pathConfigurationBuilder = HttpSecurityConfigExtension.tokenAuthenticationConfigurationBuilder.path(pathName, pathGroup);
        } else if (previousAnnotation == X509.class) {
            HttpSecurityConfigExtension.pathConfigurationBuilder = HttpSecurityConfigExtension.x509AuthenticationConfigurationBuilder.path(pathName, pathGroup);
        }
        return HttpSecurityConfigExtension.pathConfigurationBuilder;
    }
}
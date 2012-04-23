/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.identity.federation.bindings.jboss.auth;

import java.io.Serializable;
import java.security.Principal;

import javax.security.auth.Subject;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.logging.Logger;
import org.jboss.security.SecurityContext;
import org.picketlink.identity.federation.api.wstrust.WSTrustClient;
import org.picketlink.identity.federation.api.wstrust.WSTrustClient.SecurityInfo;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.wstrust.STSClientConfig.Builder;
import org.picketlink.identity.federation.core.wstrust.SamlCredential;
import org.picketlink.identity.federation.core.wstrust.WSTrustException;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.w3c.dom.Element;

/**
 * <p>
 * A client side EJB3 interceptor to automatically create a STS token and use it as the credential to invoke an EJB.
 * This interceptor must be included after <code>org.jboss.ejb3.security.client.SecurityClientInterceptor</code>
 * in the client interceptor stack in deploy/ejb3-interceptors-aop.xml
 * This interceptor requires an attribute named propertiesFile which is a resource in the classpath where the configuration
 * necessary to connect to the STS application can be read. E.g.
 * <pre>
 * <interceptor class="org.picketlink.identity.federation.bindings.jboss.auth.STSClientInterceptor" scope="PER_VM">
 *    <attribute name="propertiesFile">sts.properties</attribute>
 * </interceptor>
 * </pre>
 * The properties file must contain the following parameters:
 * <pre>
 * serviceName=[service name]
 * portName=[port name]
 * endpointAddress=[endpoint URI]
 * </pre>
 * </p>  
 * 
 * @author <a href="mailto:mmoyses@redhat.com">Marcus Moyses</a>
 * @version $Revision: 1 $
 */
public class STSClientInterceptor implements Interceptor, Serializable
{
   private static final long serialVersionUID = -4351623612864518960L;

   private static final Logger log = Logger.getLogger(STSClientInterceptor.class);

   private static boolean trace = log.isTraceEnabled();

   private String propertiesFile;

   private Builder builder;

   public String getName()
   {
      return getClass().getName();
   }

   public void setPropertiesFile(String propertiesFile)
   {
      this.propertiesFile = propertiesFile;
      if (trace)
         log.trace("Constructing STSClientInterceptor using " + propertiesFile + " as the configuration file");
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      SecurityContext sc = (SecurityContext) invocation.getMetaData("security", "context");
      if (trace)
         log.trace("Retrieved SecurityContext from invocation: " + sc);
      if (sc != null)
      {
         // retrieve username and credential from invocation
         Principal principal = sc.getUtil().getUserPrincipal();
         String credential = (String) sc.getUtil().getCredential();
         // look for the properties file in the classpath
         if (builder == null)
         {
            if (propertiesFile != null)
            {
               builder = new Builder(propertiesFile);
            }
            else
               throw new IllegalStateException(ErrorCodes.OPTION_NOT_SET + "Attribute propertiesFile must be set");
         }
         WSTrustClient client = new WSTrustClient(builder.getServiceName(), builder.getPortName(),
               builder.getEndpointAddress(), new SecurityInfo(principal.getName(), credential));
         Element assertion = null;
         try
         {
            if (trace)
               log.trace("Invoking token service to get SAML assertion for " + principal.getName());
            // create the token
            assertion = client.issueToken(SAMLUtil.SAML2_TOKEN_TYPE);
            if (trace)
               log.trace("SAML assertion for " + principal.getName() + " successfully obtained");
         }
         catch (WSTrustException wse)
         {
            log.error("Unable to issue assertion", wse);
         }

         if (assertion != null)
         {
            Subject subject = sc.getUtil().getSubject();
            // create new SecurityContext with token credential
            SecurityContext newSC = SecurityActions.createSecurityContext();
            newSC.getUtil().createSubjectInfo(principal, new SamlCredential(assertion), subject);
            // replace SecurityContext in the invocation
            invocation.getMetaData().addMetaData("security", "context", newSC);
         }
      }

      return invocation.invokeNext();
   }
}
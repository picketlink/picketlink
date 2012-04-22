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
package org.picketlink.identity.federation.core.factories;

import org.jboss.security.xacml.core.model.context.RequestType;
import org.jboss.security.xacml.core.model.context.ResponseType;
import org.picketlink.identity.federation.saml.v2.profiles.xacml.assertion.XACMLAuthzDecisionStatementType;
 

/**
 * Provides handle to XACML Object Factory
 * @author Anil.Saldhana@redhat.com
 * @since Jul 30, 2009
 */
public class XACMLContextFactory
{   
   /**
    * Create an XACML Authorization Decision Statement Type
    * @param request
    * @param response
    * @return
    */
   public static XACMLAuthzDecisionStatementType createXACMLAuthzDecisionStatementType(RequestType request,
         ResponseType response)
   {
      XACMLAuthzDecisionStatementType xacmlStatement =  new XACMLAuthzDecisionStatementType();
      xacmlStatement.setRequest(request);
      xacmlStatement.setResponse(response);
      return xacmlStatement;
   }
}
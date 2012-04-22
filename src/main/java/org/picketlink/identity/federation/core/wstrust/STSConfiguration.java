/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.identity.federation.core.wstrust;

import org.picketlink.identity.federation.core.sts.STSCoreConfig;

/**
 * <p>
 * The {@code STSConfiguration} interface allows access to the security token service (STS) configuration attributes.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 * @author Anil.Saldhana@redhat.com
 */
public interface STSConfiguration extends STSCoreConfig
{
   /**
    * <p>
    * Obtains the WS-Trust request handler class.
    * </p>
    * 
    * @return a reference to the configured {@code WSTrustRequestHandler}.
    */
   public WSTrustRequestHandler getRequestHandler();

   /**
    * <p>
    * Obtains the {@code ClaimsProcessor} that must be used to handle claims of the specified dialect.
    * </p>
    * 
    * @param claimsDialect a {@code String} representing the claims dialect (usually a URL).
    * @return the {@code ClaimsProcessor} to be used, or {@code null} if no processor could be found for the dialect.
    */
   public ClaimsProcessor getClaimsProcessor(String claimsDialect);
   
   
   /**
    * <p>
    * Returns the configured canonicalization method.
    * </p>
    * <p>
    * <b>NOTE:</b> Defaults to javax.xml.crypto.dsig.CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS
    * </p>
    * @return
    */
   public String getXMLDSigCanonicalizationMethod();
}
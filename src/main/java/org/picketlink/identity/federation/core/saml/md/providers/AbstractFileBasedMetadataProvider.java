/*
 * JBoss, a division of Red Hat
 * Copyright 2012, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.picketlink.identity.federation.core.saml.md.providers;

import org.picketlink.identity.federation.PicketLinkLogger;
import org.picketlink.identity.federation.PicketLinkLoggerFactory;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.interfaces.IMetadataProvider;

import java.io.InputStream;
import java.security.PublicKey;
import java.util.Map;

/**
 * <p>Base implementation of {@link IMetadataProvider}, which provides common functionality for other implementations.</p>
 * 
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class AbstractFileBasedMetadataProvider<T> extends AbstractMetadataProvider
      implements IMetadataProvider<T> {

    protected static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();
    
    public static final String FILENAME_KEY = "FileName";

    private String fileName;

    protected InputStream metadataFileStream;

    @SuppressWarnings("unused")
    private PublicKey encryptionKey;

    @SuppressWarnings("unused")
    private PublicKey signingKey;

    @Override
    public void init(Map<String, String> options) {
        super.init(options);
        fileName = options.get(FILENAME_KEY);
        if (fileName == null)
            throw logger.optionNotSet("FileName");
    }

   public void injectEncryptionKey(PublicKey publicKey) {
       this.encryptionKey = publicKey;
   }

   public void injectFileStream(InputStream fileStream) {
       this.metadataFileStream = fileStream;
   }

   public void injectSigningKey(PublicKey publicKey) {
       this.signingKey = publicKey;
   }

   public String requireFileInjection() {
       return this.fileName;
   }
}

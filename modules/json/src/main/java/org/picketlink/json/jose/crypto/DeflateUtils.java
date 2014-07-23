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
package org.picketlink.json.jose.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.Inflater;

/**
 * Deflate (RFC 1951) utilities to compress and decompress bytes.
 *
 * @author Giriraj Sharma
 */
public class DeflateUtils {

    private static final boolean NOWRAP = true;

    /**
     * Compresses the specified byte array according to the DEFLATE specification (RFC 1951).
     *
     * @param bytes The byte array to compress. Must not be {@code null}.
     *
     * @return The compressed bytes.
     *
     * @throws IOException If compression failed.
     */
    public static byte[] compress(final byte[] bytes)
        throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DeflaterOutputStream def = new DeflaterOutputStream(out, new Deflater(Deflater.DEFLATED, NOWRAP));
        def.write(bytes);
        def.close();
        return out.toByteArray();
    }

    /**
     * Decompresses the specified byte array according to the DEFLATE specification (RFC 1951).
     *
     * @param bytes The byte array to decompress. Must not be {@code null}.
     *
     * @return The decompressed bytes.
     *
     * @throws IOException If decompression failed.
     */
    public static byte[] decompress(final byte[] bytes)
        throws IOException {

        InflaterInputStream inf = new InflaterInputStream(new ByteArrayInputStream(bytes), new Inflater(NOWRAP));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // Transfer bytes from the compressed array to the output
        byte[] buf = new byte[1024];
        int len;
        while ((len = inf.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        inf.close();
        out.close();

        return out.toByteArray();
    }

    /**
     * Prevents public instantiation.
     */
    private DeflateUtils() {

    }
}

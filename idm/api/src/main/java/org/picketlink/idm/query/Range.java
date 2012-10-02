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
package org.picketlink.idm.query;

/**
 * Represent range in paginated query
 */
public class Range {
    // TODO: Just a quick impl

    private int offset;

    private int limit = -1;

    private Range() {
    }

    private Range(int offset, int limit) {
        this.offset = offset;
        this.limit = limit;
    }

    int getPage() {
        // TODO: Calculate based on limit/offset.
        // TODO: Should it start from 0 or 1? Rather 1....
        return 1;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    public Range of(int offset, int limit) {
        return new Range(offset, limit);
    }

    public Range next() {
        offset += limit;
        return this;
    }

}

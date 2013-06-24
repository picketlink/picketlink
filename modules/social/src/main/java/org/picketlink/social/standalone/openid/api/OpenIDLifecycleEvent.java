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
package org.picketlink.social.standalone.openid.api;

/**
 * Events in the lifecycle
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jul 10, 2009
 */
public class OpenIDLifecycleEvent {
    public enum TYPE {
        SUCCESS("lifecycle"), SESSION("session");
        private String type;

        TYPE(String type) {
            this.type = type;
        }

        public String type() {
            return type;
        }
    }

    public enum OP {
        ADD, REMOVE;
    }

    private TYPE eventType;
    private OP operation;
    private String attributeName;
    private Object attributeValue;

    public OpenIDLifecycleEvent(TYPE type, OP operation, String attr, Object val) {
        this.eventType = type;
        this.operation = operation;
        this.attributeName = attr;
        this.attributeValue = val;
    }

    /**
     * Get the type of the event (session, lifecycle etc)
     *
     * @return
     */
    public TYPE getEventType() {
        return eventType;
    }

    /**
     * Get the operation we are dealing with (add,remove)
     *
     * @return
     */
    public OP getOperation() {
        return this.operation;
    }

    /**
     * Return the attribute name that needs to be dealt at the session level
     *
     * @return
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * Get the attribute value
     *
     * @return
     */
    public Object getAttributeValue() {
        return attributeValue;
    }
}
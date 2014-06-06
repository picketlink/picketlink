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
package org.picketlink.internal.el;

import org.picketlink.Identity;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.Partition;

import javax.el.ELContext;
import javax.el.ELResolver;
import java.beans.FeatureDescriptor;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>{@link javax.el.ELResolver} that enchances EL with some PicketLink specific functionality.</p>
 *
 * @author Pedro Igor
 */
class PicketLinkELResolver extends ELResolver {

    private final ELResolver target;

    PicketLinkELResolver(ELResolver target) {
        this.target = target;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        Object value = target.getValue(context, base, property);

        if (value == null) {
            if (base != null) {
                if (Identity.class.isInstance(base)) {
                    value = resolveInIdentity(context, (Identity) base, property.toString());
                } else if (Account.class.isInstance(base)) {
                    value = resolveInAccount(context, (Account) base, property.toString());
                } else if (Partition.class.isInstance(base)) {
                    value = resolveInPartition(context, (Partition) base, property.toString());
                }
            }
        }

        return value;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        return target.getType(context, base, property);
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
        target.setValue(context, base, property, value);
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        return target.isReadOnly(context, base, property);
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        return target.getFeatureDescriptors(context, base);
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        return target.getCommonPropertyType(context, base);
    }

    private Object resolveInIdentity(ELContext context, Identity identity, String property) {
        Object value = null;

        if ("loggedIn".equals(property)) {
            context.setPropertyResolved(true);
            value = identity.isLoggedIn();
        } else if ("account".equals(property)) {
            context.setPropertyResolved(true);
            value = identity.getAccount();
        }

        return value;
    }

    private Object resolveInAccount(ELContext context, Account account, String property) {
        Object value = null;

        if ("id".equals(property)) {
            context.setPropertyResolved(true);
            value = account.getId();
        } else if ("partition".equals(property)) {
            context.setPropertyResolved(true);
            value = account.getPartition();
        } else if ("attributes".equals(property)) {
            context.setPropertyResolved(true);
            value = getAttributes(account);
        }

        return value;
    }

    private Object resolveInPartition(ELContext context, Partition partition, String property) {
        Object value = null;

        if ("id".equals(property)) {
            context.setPropertyResolved(true);
            value = partition.getId();
        } else if ("name".equals(property)) {
            context.setPropertyResolved(true);
            value = partition.getName();
        } else if ("attributes".equals(property)) {
            context.setPropertyResolved(true);
            value = getAttributes(partition);
        }

        return value;
    }

    private Map<String, Serializable> getAttributes(AttributedType attributedType) {
        HashMap<String, Serializable> attributes = new HashMap<String, Serializable>();

        for (Attribute attribute : attributedType.getAttributes()) {
            attributes.put(attribute.getName(), attribute.getValue());
        }

        return attributes;
    }
}
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
package org.picketlink.identity.federation.saml.v1.assertion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.picketlink.identity.federation.saml.common.CommonConditionsType;

/**
 * <complexType name="ConditionsType">
        <choice minOccurs="0" maxOccurs="unbounded">
            <element ref="saml:AudienceRestrictionCondition"/>
            <element ref="saml:DoNotCacheCondition"/>
            <element ref="saml:Condition"/>
        </choice>
        <attribute name="NotBefore" type="dateTime" use="optional"/>
        <attribute name="NotOnOrAfter" type="dateTime" use="optional"/>
    </complexType>
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11ConditionsType extends CommonConditionsType
{
   private static final long serialVersionUID = 1L;

   public List<SAML11ConditionAbstractType> conditions = new ArrayList<SAML11ConditionAbstractType>();

   public void add(SAML11ConditionAbstractType condition)
   {
      this.conditions.add(condition);
   }

   public void addAll(List<SAML11ConditionAbstractType> theConditions)
   {
      this.conditions.addAll(theConditions);
   }

   public boolean remove(SAML11ConditionsAbstractType condition)
   {
      return this.conditions.remove(condition);
   }

   public List<SAML11ConditionAbstractType> get()
   {
      return Collections.unmodifiableList(conditions);
   }
}
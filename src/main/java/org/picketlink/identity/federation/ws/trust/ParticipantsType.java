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
package org.picketlink.identity.federation.ws.trust;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>Java class for ParticipantsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ParticipantsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Primary" type="{http://docs.oasis-open.org/ws-sx/ws-trust/200512/}ParticipantType" minOccurs="0"/>
 *         &lt;element name="Participant" type="{http://docs.oasis-open.org/ws-sx/ws-trust/200512/}ParticipantType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;any/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class ParticipantsType extends SimpleAnyType
{
   protected ParticipantType primary;

   protected List<ParticipantType> participant = new ArrayList<ParticipantType>();

   /**
    * Gets the value of the primary property.
    * 
    * @return
    *     possible object is
    *     {@link ParticipantType }
    *     
    */
   public ParticipantType getPrimary()
   {
      return primary;
   }

   /**
    * Sets the value of the primary property.
    * 
    * @param value
    *     allowed object is
    *     {@link ParticipantType }
    *     
    */
   public void setPrimary(ParticipantType value)
   {
      this.primary = value;
   }

   public void add(ParticipantType p)
   {
      this.participant.add(p);
   }

   public boolean remove(ParticipantType p)
   {
      return this.participant.remove(p);
   }

   /**
    * Gets the value of the participant property.
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link ParticipantType }
    * 
    * 
    */
   public List<ParticipantType> getParticipant()
   {
      return Collections.unmodifiableList(this.participant);
   }
}
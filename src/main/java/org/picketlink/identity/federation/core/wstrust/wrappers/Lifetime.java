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
package org.picketlink.identity.federation.core.wstrust.wrappers;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.ws.trust.LifetimeType;
import org.picketlink.identity.federation.ws.wss.utility.AttributedDateTime;

/**
 * <p>
 * This class represents a WS-Trust {@code Lifetime}. It wraps the JAXB {@code LifetimeType} and offer methods that
 * allows for easy retrieval of the creation and expiration times as {@code XMLGregorianCalendar} and
 * {@code GregorianCalendar} objects.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class Lifetime
{

   private final LifetimeType delegate;

   private XMLGregorianCalendar created;

   private XMLGregorianCalendar expires;

   private DatatypeFactory factory;

   /**
    * <p>
    * Creates an instance of {@code Lifetime} with the specified parameters.
    * </p>
    * 
    * @param created a {@code GregorianCalendar} representing the token creation time.
    * @param expires a {@code GregorianCalendar} representing the token expiration time.
    */
   public Lifetime(GregorianCalendar created, GregorianCalendar expires)
   {
      try
      {
         this.factory = DatatypeFactory.newInstance();
      }
      catch (DatatypeConfigurationException dce)
      {
         throw new RuntimeException(ErrorCodes.PROCESSING_EXCEPTION + "Unable to get DatatypeFactory instance", dce);
      }

      // normalize the parameters (convert to UTC).
      this.created = factory.newXMLGregorianCalendar(created).normalize();
      this.expires = factory.newXMLGregorianCalendar(expires).normalize();

      // set the delegate fields.
      this.delegate = new LifetimeType();
      AttributedDateTime dateTime = new AttributedDateTime();
      dateTime.setValue(this.created.toXMLFormat());
      this.delegate.setCreated(dateTime);
      dateTime = new AttributedDateTime();
      dateTime.setValue(this.expires.toXMLFormat());
      this.delegate.setExpires(dateTime);

   }

   /**
    * <p>
    * Creates a {@code Lifetime} instance using the specified {@code LifetimeType}.
    * </p>
    * 
    * @param lifetime a reference to the {@code LifetimeType} instance that contains the information used in the
    *            {@code Lifetime} construction.
    */
   public Lifetime(LifetimeType lifetime)
   {
      if (lifetime == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "LifetimeType");

      try
      {
         this.factory = DatatypeFactory.newInstance();
      }
      catch (DatatypeConfigurationException dce)
      {
         throw new RuntimeException(ErrorCodes.PROCESSING_EXCEPTION + "Unable to get DatatypeFactory instance", dce);
      }
      this.delegate = lifetime;

      // construct the created and expires instances from the lifetime object.
      this.created = factory.newXMLGregorianCalendar(lifetime.getCreated().getValue());
      this.expires = factory.newXMLGregorianCalendar(lifetime.getExpires().getValue());

      // check if the supplied lifetime needs to be normalized.
      if (this.created.getTimezone() != 0)
      {
         this.created = this.created.normalize();
         this.delegate.getCreated().setValue(this.created.toXMLFormat());
      }
      if (this.expires.getTimezone() != 0)
      {
         this.expires = this.expires.normalize();
         this.delegate.getExpires().setValue(this.expires.toXMLFormat());
      }
   }

   /**
    * <p>
    * Obtains the creation time as a {@code XMLGregorianCalendar}.
    * </p>
    * 
    * @return a reference to the {@code XMLGregorianCalendar} that represents the creation time.
    */
   public XMLGregorianCalendar getCreated()
   {
      return this.created;
   }

   /**
    * <p>
    * Sets the creation time.
    * </p>
    * 
    * @param created a reference to the {@code XMLGregorianCalendar} that represents the creation time to be set.
    */
   public void setCreated(XMLGregorianCalendar created)
   {
      this.created = created.normalize();
      this.delegate.getCreated().setValue(this.created.toXMLFormat());
   }

   /**
    * <p>
    * Obtains the creation time as a {@code GregorianCalendar}.
    * </p>
    * 
    * @return a reference to the {@code GregorianCalendar} that represents the creation time.
    */
   public GregorianCalendar getCreatedCalendar()
   {
      return this.created.toGregorianCalendar();
   }

   /**
    * <p>
    * Sets the creation time.
    * </p>
    * 
    * @param created a reference to the {@code GregorianCalendar} that represents the creation time to be set.
    */
   public void setCreatedCalendar(GregorianCalendar created)
   {
      this.setCreated(this.factory.newXMLGregorianCalendar(created));
   }

   /**
    * <p>
    * Obtains the expiration time as a {@code XMLGregorianCalendar}.
    * </p>
    * 
    * @return a reference to the {@code XMLGregorianCalendar} that represents the expiration time.
    */
   public XMLGregorianCalendar getExpires()
   {
      return this.expires;
   }

   /**
    * <p>
    * Sets the expiration time.
    * </p>
    * 
    * @param expires a reference to the {@code XMLGregorianCalendar} that represents the expiration time.
    */
   public void setExpires(XMLGregorianCalendar expires)
   {
      this.expires = expires.normalize();
      this.delegate.getExpires().setValue(this.expires.toXMLFormat());
   }

   /**
    * <p>
    * Obtains the expiration time as a {@code GregorianCalendar}.
    * </p>
    * 
    * @return a reference to the {@code GregorianCalendar} that represents the expiration time.
    */
   public GregorianCalendar getExpiresCalendar()
   {
      return this.expires.toGregorianCalendar();
   }

   /**
    * <p>
    * Sets the expiration time.
    * </p>
    * 
    * @param expires a reference to the {@code GregorianCalendar} that represents the expiration time.
    */
   public void setExpiresCalendar(GregorianCalendar expires)
   {
      this.setExpires(this.factory.newXMLGregorianCalendar(expires));
   }

   /**
    * <p>
    * Obtains a reference to the {@code LifetimeType} delegate.
    * </p>
    * 
    * @return a reference to the delegate instance.
    */
   public LifetimeType getDelegate()
   {
      return this.delegate;
   }
}

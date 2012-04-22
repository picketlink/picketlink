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
package org.picketlink.identity.federation.ws.policy;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.w3c.dom.Element;

/**
 * <p>Java class for OperatorContentType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OperatorContentType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://schemas.xmlsoap.org/ws/2004/09/policy}Policy"/>
 *           &lt;element ref="{http://schemas.xmlsoap.org/ws/2004/09/policy}All"/>
 *           &lt;element ref="{http://schemas.xmlsoap.org/ws/2004/09/policy}ExactlyOne"/>
 *           &lt;element ref="{http://schemas.xmlsoap.org/ws/2004/09/policy}PolicyReference"/>
 *           &lt;any/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OperatorContentType", propOrder =
{"policyOrAllOrExactlyOne"})
@XmlSeeAlso(
{Policy.class})
public class OperatorContentType
{

   @XmlElementRefs(
   {
         @XmlElementRef(name = "Policy", namespace = "http://schemas.xmlsoap.org/ws/2004/09/policy", type = Policy.class),
         @XmlElementRef(name = "PolicyReference", namespace = "http://schemas.xmlsoap.org/ws/2004/09/policy", type = PolicyReference.class),
         @XmlElementRef(name = "ExactlyOne", namespace = "http://schemas.xmlsoap.org/ws/2004/09/policy", type = JAXBElement.class),
         @XmlElementRef(name = "All", namespace = "http://schemas.xmlsoap.org/ws/2004/09/policy", type = JAXBElement.class)})
   @XmlAnyElement(lax = true)
   protected List<Object> policyOrAllOrExactlyOne;

   /**
    * Gets the value of the policyOrAllOrExactlyOne property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the policyOrAllOrExactlyOne property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getPolicyOrAllOrExactlyOne().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link PolicyReference }
    * {@link Element }
    * {@link JAXBElement }{@code <}{@link OperatorContentType }{@code >}
    * {@link Policy }
    * {@link Object }
    * {@link JAXBElement }{@code <}{@link OperatorContentType }{@code >}
    * 
    * 
    */
   public List<Object> getPolicyOrAllOrExactlyOne()
   {
      if (policyOrAllOrExactlyOne == null)
      {
         policyOrAllOrExactlyOne = new ArrayList<Object>();
      }
      return this.policyOrAllOrExactlyOne;
   }

}

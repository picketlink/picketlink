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
package org.picketlink.identity.xmlsec.w3.xmlenc;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;


/**
 * <p>Java class for EncryptionPropertyType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EncryptionPropertyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;any/>
 *       &lt;/choice>
 *       &lt;attribute name="Target" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="Id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class EncryptionPropertyType {
 
    protected URI target;
    protected String id;
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

     

    /**
     * Gets the value of the target property.
     * 
     * @return
     *     possible object is
     *     {@link URI }
     *     
     */
    public URI getTarget() {
        return target;
    }

    /**
     * Sets the value of the target property.
     * 
     * @param value
     *     allowed object is
     *     {@link URI }
     *     
     */
    public void setTarget( URI value) {
        this.target = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    public void addOtherAttribute( QName key, String val )
    {
       this.otherAttributes.put(key, val);
    }
    
    public void addOtherAttributes( Map< QName, String> otherMap )
    {
       this.otherAttributes.putAll(otherMap);
    }
    
    public void removeOtherAttribute( QName key )
    {
       this.otherAttributes.remove(key);
    }
    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class. 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return Collections.unmodifiableMap( otherAttributes );
    }
}
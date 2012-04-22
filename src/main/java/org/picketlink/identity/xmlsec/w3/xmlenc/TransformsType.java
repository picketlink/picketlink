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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.picketlink.identity.xmlsec.w3.xmldsig.TransformType;
 


/**
 * <p>Java class for TransformsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TransformsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}Transform" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class TransformsType 
{
    protected List<TransformType> transform = new ArrayList<TransformType>();

    public void add( TransformType tt )
    {
       this.transform.add( tt );
    }
    
    public void addAll( List<TransformType> ttlist )
    {
       this.transform.addAll( ttlist );
    }
    
    public void remove( TransformType tt )
    {
       this.transform.remove( tt );
    }
    
    /**
     * Gets the value of the transform property.
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TransformType } 
     */
    public List<TransformType> getTransform() {
        return Collections.unmodifiableList( this.transform );
    }
}
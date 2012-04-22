package org.picketlink.identity.federation.core.handler.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * <p>Java class for Handlers complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Handlers">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Handler" type="{urn:picketlink:identity-federation:handler:config:1.0}Handler" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class Handlers {
 
    protected List<Handler> handler = new ArrayList<Handler>();

    public void add( Handler h )
    {
       this.handler.add( h );
    }
    
    public void remove( Handler h )
    {
       this.handler.remove( h );
    }
    
    /**
     * Gets the value of the handler property.
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Handler }
     * 
     * 
     */
    public List<Handler> getHandler() { 
        return Collections.unmodifiableList( this.handler );
    }

}
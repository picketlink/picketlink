package org.jboss.picketlink.example.securityconsole.util;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import org.jboss.picketlink.example.securityconsole.model.Customer;
import org.omnifaces.converter.SelectItemsConverter;

@FacesConverter("resourceConverter")
public class ResourceConverter extends SelectItemsConverter
{
    //@Override
    public String getAsString(FacesContext context, UIComponent component, Object value) 
    {
        Long id = null;
        if (value instanceof Customer)
        {
            id = ((Customer) value).getId();
        }
        
        return (id != null) ? String.valueOf(id) : null;
    }
}

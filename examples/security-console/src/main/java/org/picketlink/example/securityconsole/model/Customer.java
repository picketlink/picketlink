package org.picketlink.example.securityconsole.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Customer implements Serializable 
{
    private static final long serialVersionUID = 8279444660789483143L;
    
    @Id @GeneratedValue
    private Long id;
        
    private String firstName;
    private String lastName;
    
    public Long getId()
    {
        return id;
    }
    
    public void setId(Long id)
    {
        this.id = id;
    }
    
    public String getFirstName()
    {
        return firstName;
    }
    
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }
    
    public String getLastName()
    {
        return lastName;
    }
    
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }
    
    public String toString()
    {
        return String.format("%d: %s %s", id, firstName, lastName);
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Customer))
        {
            return false;
        }
        
        Customer other = (Customer) obj;
        
        return this.id != null && this.id.equals(other.id);
    }
}

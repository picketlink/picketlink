package org.picketlink.idm.password.internal;

import java.util.Date;

import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.credential.spi.annotations.Stored;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class PlainTextPasswordStorage implements CredentialStorage {

    private Date expiryDate;
    private String password;

    public PlainTextPasswordStorage() {
        
    }
    
    public PlainTextPasswordStorage(String password) {
        setPassword(password);
    }

    @Override
    public Date getExpiryDate() {
        return this.expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    @Stored
    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}

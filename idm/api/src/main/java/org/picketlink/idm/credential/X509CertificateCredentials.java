package org.picketlink.idm.credential;

import java.io.IOException;
import java.io.StringReader;
import java.security.Principal;
import java.util.Properties;

/**
 * A credential representing an X509 certificate for certificate-based authentication
 * 
 * @author Shane Bryzak
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class X509CertificateCredentials extends AbstractBaseCredentials implements Credentials {
    private X509Cert certificate;
    private String userName;

    public X509CertificateCredentials(X509Cert certificate) {
        this.certificate = certificate;
    }

    public X509Cert getCertificate() {
        return certificate;
    }
    
    public String getUsername() {
        if (this.userName == null) {
            Properties prop = new Properties();
            
            this.userName = getCertificatePrincipal().getName();
            
            try {
                prop.load(new StringReader(userName.replaceAll(",", "\n")));
            } catch (IOException e) {
                e.printStackTrace();
            }

            userName = prop.getProperty("CN");
        }

        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    @Override
    public void invalidate() {
        certificate = null;
    }
    
    private Principal getCertificatePrincipal() {
        Principal certprincipal = this.certificate.getValue().getSubjectDN();

        if (certprincipal == null) {
            certprincipal = this.certificate.getValue().getIssuerDN();
        }
        
        return certprincipal;
    }

}

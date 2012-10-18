package org.picketlink.idm.credential;

import java.security.cert.X509Certificate;

/**
 * A credential representing an X509 certificate for certificate-based authentication
 * 
 * @author Shane Bryzak
 */
public class X509CertificateCredential implements Credential {
    private X509Certificate certificate;
    
    public X509CertificateCredential(X509Certificate certificate) {
        this.certificate = certificate;
    }
    
    public X509Certificate getCertificate() {
        return certificate;
    }
}

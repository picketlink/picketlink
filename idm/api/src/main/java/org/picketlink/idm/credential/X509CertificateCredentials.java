package org.picketlink.idm.credential;

import java.security.cert.X509Certificate;

/**
 * A credential representing an X509 certificate for certificate-based authentication
 * 
 * @author Shane Bryzak
 */
public class X509CertificateCredentials extends AbstractBaseCredentials implements Credentials {
    private X509Certificate certificate;

    public X509CertificateCredentials(X509Certificate certificate) {
        this.certificate = certificate;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    @Override
    public void invalidate() {
        certificate = null;
    }
}

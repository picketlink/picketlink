package org.picketlink.idm.credential;

import java.security.cert.X509Certificate;

/**
 * Represents a {@link X509Certificate} credential.
 *
 * @author Shane Bryzak
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class X509Cert {

    private X509Certificate value;

    public X509Cert(X509Certificate value) {
        this.value = value;
    }

    public X509Certificate getValue() {
        return this.value;
    }

    public void clear() {
        this.value = null;
    }
}

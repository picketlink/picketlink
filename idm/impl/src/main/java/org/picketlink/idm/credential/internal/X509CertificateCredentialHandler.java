package org.picketlink.idm.credential.internal;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.X509Cert;
import org.picketlink.idm.credential.X509CertificateCredentials;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.annotations.SupportsCredentials;
import org.picketlink.idm.internal.util.Base64;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.spi.IdentityStore;

/**
 * This particular implementation supports the validation of {@link X509CertificateCredentials}, and updating {@link X509Cert} credentials.
 *
 * @author Shane Bryzak
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@SupportsCredentials({X509CertificateCredentials.class, X509Cert.class})
public class X509CertificateCredentialHandler implements CredentialHandler {

    @Override
    public void validate(Credentials credentials, IdentityStore<?> identityStore) {
        if (!X509CertificateCredentials.class.isInstance(credentials)) {
            throw new IllegalArgumentException("Credentials class [" + 
                    credentials.getClass().getName() + "] not supported by this handler.");
        }

        X509CertificateCredentials certCredentials = (X509CertificateCredentials) credentials;

        Agent agent = identityStore.getAgent(certCredentials.getUsername());
        
        certCredentials.setStatus(Status.INVALID);
        
        // If the user for the provided username cannot be found we fail validation
        if (agent != null) {
            X509CertificateStorage storage = null; // FIXME identityStore.retrieveCredential(agent, X509CertificateStorage.class);

            if (storage != null) {
                String base64Cert = storage.getBase64Cert();
                
                byte[] certBytes = Base64.decode(base64Cert);

                try {
                    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                    X509Certificate storedCert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(
                            certBytes));
                    X509Cert providedCert = certCredentials.getCertificate();

                    if (storedCert.equals(providedCert.getValue())) {
                        certCredentials.setStatus(Status.VALID); 
                    }
                } catch (Exception e) {
                    throw new IdentityManagementException("Error while checking user's certificate.", e);
                }
            }
        }
    }

    @Override
    public void update(Agent agent, Object credential, IdentityStore<?> store) {
        if (!X509Cert.class.isInstance(credential)) {
            throw new IllegalArgumentException("Credential class [" + 
                    credential.getClass().getName() + "] not supported by this handler.");
        }

        X509Cert certificate = (X509Cert) credential;
        X509CertificateStorage storage = new X509CertificateStorage((X509Cert) certificate);
        
        // FIXME store.<X509CertificateStorage>storeCredential(agent, storage);
    }

}

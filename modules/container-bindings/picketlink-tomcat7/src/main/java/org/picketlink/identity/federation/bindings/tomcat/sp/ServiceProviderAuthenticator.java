package org.picketlink.identity.federation.bindings.tomcat.sp;

import java.security.Principal;
import java.util.List;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.realm.GenericPrincipal;


/**
 * Unified Service Provider Authenticator
 *
 * @author anil saldhana
 */
public class ServiceProviderAuthenticator extends AbstractSPFormAuthenticator {
    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.bindings.tomcat.sp.BaseFormAuthenticator#start()
     */
    @Override
    protected void startInternal() throws LifecycleException {
        super.startInternal();
        startPicketLink(); 
    }
    
    @Override
    protected String getContextPath() { 
        return getContext().getServletContext().getContextPath();
    }
    

    @Override
    protected Principal getGenericPrincipal(Request request, String username, List<String> roles){
        return new GenericPrincipal(username, "", roles);
    }
}
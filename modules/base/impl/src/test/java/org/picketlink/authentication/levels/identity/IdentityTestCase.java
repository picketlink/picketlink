package org.picketlink.authentication.levels.identity;

import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.permission.spi.PermissionResolver;
import org.picketlink.internal.CDIEventBridge;
import org.picketlink.internal.DefaultIdentity;
import org.picketlink.authentication.Authenticator;
import org.picketlink.authentication.internal.IdmAuthenticator;
import org.picketlink.authentication.levels.SecurityLevelManager;
import org.picketlink.credential.DefaultLoginCredentials;

import static org.mockito.Mockito.*;

public abstract class IdentityTestCase {

    @Mock
    protected Instance<Authenticator> authenticator;

    @Mock
    protected CDIEventBridge eventBridge;

    @Mock
    protected DefaultLoginCredentials loginCredential;

    @Mock
    protected Instance<IdmAuthenticator> idmAuthenticatorInstance;

    @Mock
    protected PermissionResolver permissionResolver;

    @Mock
    protected SecurityLevelManager securityLevelManager;

    @InjectMocks
    DefaultIdentity identity;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
    }

    protected void setAuthenticator(){
        setAuthenticator(true);
    }

    protected void setAuthenticator(boolean success){
        setAuthenticator(success, null);
    }

    protected void setAuthenticator(boolean success, Account acc){
        when(authenticator.isUnsatisfied()).thenReturn(false);
        Authenticator au = getAuthenticatorMock(success, acc);
        when(authenticator.get()).thenReturn(au);
    }

    protected Authenticator getAuthenticatorMock(boolean status, Account acc){
        Authenticator auth = mock(Authenticator.class);
        if(status){
            when(auth.getStatus()).thenReturn(Authenticator.AuthenticationStatus.SUCCESS);
        }else{
            when(auth.getStatus()).thenReturn(Authenticator.AuthenticationStatus.FAILURE);
        }
        if(acc == null){
            acc = mock(Account.class);
            when(acc.isEnabled()).thenReturn(true);
        }
        when(auth.getAccount()).thenReturn(acc);
        return auth;
    }
}

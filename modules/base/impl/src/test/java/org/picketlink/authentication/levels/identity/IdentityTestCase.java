package org.picketlink.authentication.levels.identity;

import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.picketlink.authentication.Authenticator;
import org.picketlink.authentication.internal.IdmAuthenticator;
import org.picketlink.authentication.levels.Level;
import org.picketlink.authentication.levels.SecurityLevelManager;
import org.picketlink.authentication.levels.SecurityLevelResolver;
import org.picketlink.authentication.levels.internal.DefaultSecurityLevelManager;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.permission.spi.PermissionResolver;
import org.picketlink.internal.CDIEventBridge;
import org.picketlink.internal.DefaultIdentity;
import org.picketlink.producer.LevelFactoryResolver;

import javax.enterprise.inject.Instance;
import java.util.Iterator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    protected Instance<Authenticator> authenticatorInstance;

    @Mock
    protected Instance<PermissionResolver> permissionResolver;

    @Mock
    protected Instance<SecurityLevelManager> securityLevelManager;

    @Mock
    private Instance<Level> levelInstance;

    @Mock
    private LevelFactoryResolver lfr;

    @Mock
    private Instance<SecurityLevelResolver> resolverInstances;

    @Mock
    protected DefaultSecurityLevelManager levelManager;

    @Mock
    protected IdmAuthenticator idmAuthenticator;

    @Mock
    Iterator<SecurityLevelResolver> iterator;

    @InjectMocks
    DefaultIdentity identity;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        when(this.authenticatorInstance.isUnsatisfied()).thenReturn(true);
        when(this.idmAuthenticatorInstance.get()).thenReturn(this.idmAuthenticator);
        when(this.idmAuthenticator.getStatus()).thenReturn(Authenticator.AuthenticationStatus.SUCCESS);
        when(this.idmAuthenticator.getAccount()).thenReturn(new User("test"));
        when(this.securityLevelManager.get()).thenReturn(levelManager);
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

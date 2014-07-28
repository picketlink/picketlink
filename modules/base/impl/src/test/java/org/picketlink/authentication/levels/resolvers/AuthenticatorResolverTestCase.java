package org.picketlink.authentication.levels.resolvers;

import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.picketlink.Identity;
import org.picketlink.authentication.levels.LevelFactory;
import org.picketlink.authentication.levels.annotations.SecurityLevel;
import org.picketlink.authentication.levels.internal.AuthenticatorLevelResolver;
import org.picketlink.idm.model.Account;
import org.picketlink.producer.AbstractLevelFactory;
import org.picketlink.authentication.Authenticator;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class AuthenticatorResolverTestCase {

    @Mock
    Identity identity;

    @Mock
    AbstractLevelFactory abstractFactory;

    @Mock
    private Instance<Authenticator> authenticatorInstance;

    @InjectMocks
    AuthenticatorLevelResolver resolver;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void successTest(){
        when(identity.isLoggedIn()).thenReturn(true);
        when(authenticatorInstance.isUnsatisfied()).thenReturn(false);
        when(authenticatorInstance.get()).thenReturn(new testAuthenticator());
        LevelFactory factory = mock(LevelFactory.class);
        when(abstractFactory.getFactory()).thenReturn(factory);
        resolver.resolve();
        verify(factory,times(1)).createLevel("2");
    }

    @Test
    public void userNotLoggedInTest(){
        when(identity.isLoggedIn()).thenReturn(false);
        //just to be sure, normally it should fail because there is not defined LevelFactory in getMethod of abstractFactory
        assertTrue(resolver.resolve()==null);
    }

    @Test
    public void unsatisfiedAuthenticator(){
        when(identity.isLoggedIn()).thenReturn(true);
        when(authenticatorInstance.isUnsatisfied()).thenReturn(true);
        //just to be sure, normally it should fail because there is not defined LevelFactory in getMethod of abstractFactory
        assertTrue(resolver.resolve()==null);
    }

    @Test
    public void missingAnnotationTest(){
        when(identity.isLoggedIn()).thenReturn(true);
        when(authenticatorInstance.isUnsatisfied()).thenReturn(false);
        when(authenticatorInstance.get()).thenReturn(mock(Authenticator.class));
        //just to be sure, normally it should fail because there is not defined LevelFactory in getMethod of abstractFactory
        assertTrue(resolver.resolve()==null);
    }

    @SecurityLevel("2")
    class testAuthenticator implements Authenticator{
        @Override
        public void authenticate() {
        }
        @Override
        public void postAuthenticate() {
        }
        @Override
        public AuthenticationStatus getStatus() {
            return null;
        }
        @Override
        public Account getAccount() {
            return null;
        }
    }
}

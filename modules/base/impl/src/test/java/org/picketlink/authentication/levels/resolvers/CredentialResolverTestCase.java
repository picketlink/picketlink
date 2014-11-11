package org.picketlink.authentication.levels.resolvers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.picketlink.Identity;
import org.picketlink.authentication.levels.LevelFactory;
import org.picketlink.authentication.levels.annotations.SecurityLevel;
import org.picketlink.authentication.levels.internal.CredentialLevelResolver;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.model.Account;
import org.picketlink.producer.LevelFactoryResolver;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CredentialResolverTestCase {
    @Mock
    Identity identity;

    @Mock
    LevelFactoryResolver levelFactoryResolver;

    @Mock
    private DefaultLoginCredentials authenticatorInstance;

    @InjectMocks
    CredentialLevelResolver resolver;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void successTest(){
        when(identity.isLoggedIn()).thenReturn(true);
        when(authenticatorInstance.getCredential()).thenReturn(new testCredential());
        LevelFactory factory = mock(LevelFactory.class);
        when(levelFactoryResolver.resolve()).thenReturn(factory);
        resolver.resolve();
        verify(factory,times(1)).createLevel("2");
    }

    @Test
    public void userNotLoggedInTest(){
        when(identity.isLoggedIn()).thenReturn(false);
        //just to be sure, normally it should fail because there is not defined LevelFactory in getMethod of levelFactoryResolver
        assertTrue(resolver.resolve()==null);
    }

    @Test
    public void unsatisfiedAuthenticator(){
        when(identity.isLoggedIn()).thenReturn(true);
        when(authenticatorInstance.getCredential()).thenReturn(null);
        //just to be sure, normally it should fail because there is not defined LevelFactory in getMethod of levelFactoryResolver
        assertTrue(resolver.resolve()==null);
    }

    @Test
    public void missingAnnotationTest(){
        when(identity.isLoggedIn()).thenReturn(true);
        when(authenticatorInstance.getCredential()).thenReturn(mock(Credentials.class));
        //just to be sure, normally it should fail because there is not defined LevelFactory in getMethod of levelFactoryResolver
        assertTrue(resolver.resolve()==null);
    }

    @SecurityLevel("2")
    class testCredential implements Credentials{
        @Override
        public Account getValidatedAccount() {
            return null;
        }
        @Override
        public Status getStatus() {
            return null;
        }
        @Override
        public void invalidate() {
        }
    }
}

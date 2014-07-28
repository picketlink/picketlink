package org.picketlink.authentication.levels.identity;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.picketlink.authentication.levels.DifferentUserLoggedInExcpetion;
import org.picketlink.authentication.levels.internal.DefaultLevel;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.basic.User;

public class BasicTestCase extends IdentityTestCase{

    @Test
    public void basicLoginTest(){
        when(securityLevelManager.resolveSecurityLevel()).thenReturn(new DefaultLevel(1));
        setAuthenticator();

        identity.login();
        assertTrue(identity.getLevel().compareTo(new DefaultLevel(1))==0);
    }

    @Test(expected = DifferentUserLoggedInExcpetion.class)
    public void differentUserLogsInTest(){
        when(securityLevelManager.resolveSecurityLevel()).thenReturn(new DefaultLevel(1),new DefaultLevel(2));
        Account acc = new User("Joe");
        setAuthenticator(true,acc);
        identity.login();
        assertTrue(identity.getLevel().compareTo(new DefaultLevel(1))==0);

        acc = new User("Roe");
        setAuthenticator(true,acc);
        identity.login();
    }

    @Test
    public void logoutTest(){
        when(securityLevelManager.resolveSecurityLevel()).thenReturn(new DefaultLevel(2),new DefaultLevel(1));
        setAuthenticator();
        identity.login();
        assertTrue(identity.getLevel().compareTo(new DefaultLevel(2))==0);

        identity.logout();
        assertTrue(identity.getLevel().compareTo(new DefaultLevel(1))==0);
    }
}

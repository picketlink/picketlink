package org.picketlink.authentication.levels.identity;

import org.junit.Test;
import org.picketlink.authentication.levels.DifferentUserLoggedInExcpetion;
import org.picketlink.authentication.levels.Level;
import org.picketlink.authentication.levels.internal.DefaultLevel;
import org.picketlink.idm.model.basic.User;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class BasicTestCase extends IdentityTestCase{

    @Test
    public void basicLoginTest(){
        when(resolveSecurityLevel()).thenReturn(new DefaultLevel(1));
        setAuthenticator();

        identity.login();
        assertTrue(identity.getLevel().compareTo(new DefaultLevel(1))==0);
    }

    @Test(expected = DifferentUserLoggedInExcpetion.class)
    public void differentUserLogsInTest(){
        when(resolveSecurityLevel()).thenReturn(new DefaultLevel(1),new DefaultLevel(2));
        identity.login();
        assertTrue(identity.getLevel().compareTo(new DefaultLevel(1))==0);

        User acc = new User("Roe");

        when(this.idmAuthenticator.getAccount()).thenReturn(acc);

        identity.login();
    }

    @Test
    public void logoutTest(){
        when(resolveSecurityLevel()).thenReturn(new DefaultLevel(2),new DefaultLevel(1));
        setAuthenticator();
        identity.login();
        assertTrue(identity.getLevel().compareTo(new DefaultLevel(2))==0);

        identity.logout();
        assertTrue(identity.getLevel().compareTo(new DefaultLevel(1))==0);
    }

    private Level resolveSecurityLevel() {
        return levelManager.resolveSecurityLevel();
    }
}

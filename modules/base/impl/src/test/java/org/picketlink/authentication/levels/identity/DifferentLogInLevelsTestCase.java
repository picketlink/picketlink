package org.picketlink.authentication.levels.identity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.picketlink.authentication.UserAlreadyLoggedInException;
import org.picketlink.authentication.levels.internal.DefaultLevel;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.basic.User;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class DifferentLogInLevelsTestCase extends IdentityTestCase{

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                 { 1 , 1 , false}, { 2, 1 , false}, { 1, 2 , true},
           });
    }

    private int firstLevel;

    private int secondLevel;

    private boolean result;

    public DifferentLogInLevelsTestCase(int first, int second, boolean res) {
        firstLevel = first;
        secondLevel = second;
        result = res;
    }

    @Test
    public void userAlreadyLoggedInTest(){
        when(securityLevelManager.get().resolveSecurityLevel()).thenReturn(new DefaultLevel(firstLevel),new DefaultLevel(secondLevel));
        Account acc = new User("Joe");
        setAuthenticator(true,acc);
        identity.login();
        assertTrue(identity.getLevel().compareTo(new DefaultLevel(firstLevel))==0);

        if(result){
            identity.login();
            assertTrue(identity.getLevel().compareTo(new DefaultLevel(secondLevel))==0);
        }else{
            try{
                identity.login();
                fail("There were expected expection which did not happen");
            }catch(UserAlreadyLoggedInException e){

            }
        }
    }
}

package org.picketlink.authentication.levels.core;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.picketlink.authentication.levels.Level;
import org.picketlink.authentication.levels.SecurityLevelsMismatchException;
import org.picketlink.authentication.levels.internal.DefaultLevel;

import static org.mockito.Mockito.mock;

@RunWith(Parameterized.class)
public class DefaultLevelTestCase {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                 { 1 , 1 , 0}, { 2, 1 , 1}, { 1, 2 , -1},
           });
    }

    private int firstLevel;

    private int secondLevel;

    private int result;

    public DefaultLevelTestCase(int first, int second, int res) {
        firstLevel = first;
        secondLevel = second;
        result = res;
    }

    @Test
    public void testCompare(){
        Level one = new DefaultLevel(firstLevel);
        Level two = new DefaultLevel(secondLevel);
        assertTrue(one.compareTo(two)==result);
    }

    @Test(expected = SecurityLevelsMismatchException.class)
    public void testMismatch(){
        Level one = new DefaultLevel(firstLevel);
        Level two = mock(Level.class);
        one.compareTo(two);
    }
}

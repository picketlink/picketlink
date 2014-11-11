package org.picketlink.authentication.levels.core;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.picketlink.authentication.levels.Level;
import org.picketlink.authentication.levels.SecurityLevelResolver;
import org.picketlink.authentication.levels.internal.DefaultLevel;
import org.picketlink.authentication.levels.internal.DefaultSecurityLevelManager;

import javax.enterprise.inject.Instance;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultSecurityLevelManagerTestCase {

    @Mock
    private Instance<Level> levelInstance;

    @Mock
    private Instance<SecurityLevelResolver> resolverInstances;

    @InjectMocks
    private DefaultSecurityLevelManager levelManager;

    @Mock
    Iterator<SecurityLevelResolver> iterator;

    @Before
    public void startUp(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void synchronousLevelResolveTest() throws InterruptedException, ExecutionException{
        when(levelInstance.isUnsatisfied()).thenReturn(true);
        SecurityLevelResolver one = mock(SecurityLevelResolver.class);
        when(one.resolve()).thenReturn(new DefaultLevel(2));
        SecurityLevelResolver two = mock(SecurityLevelResolver.class);
        when(two.resolve()).thenReturn(new DefaultLevel(3));
        SecurityLevelResolver three = mock(SecurityLevelResolver.class);
        when(three.resolve()).thenReturn(new DefaultLevel(1));

        when(iterator.hasNext()).thenReturn(true,true,true,false,true,true,true,false);
        when(iterator.next()).thenReturn(one,two,three,one,two,three);
        when(resolverInstances.iterator()).thenReturn(iterator);

        levelManager.init();
        assertTrue(levelManager.resolveSecurityLevel().compareTo(new DefaultLevel(3))==0);
    }
}

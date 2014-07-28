package org.picketlink.authentication.levels.core;

import javax.enterprise.inject.Instance;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.picketlink.authentication.levels.Level;
import org.picketlink.authentication.levels.SecurityLevelResolver;
import org.picketlink.authentication.levels.internal.AsynchronousResolverProcessor;
import org.picketlink.authentication.levels.internal.DefaultLevel;
import org.picketlink.authentication.levels.internal.DefaultSecurityLevelManager;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertTrue;

public class DefaultSecurityLevelManagerTestCase {

    @Mock
    private Instance<Level> levelInstance;

    @Mock
    private Instance<SecurityLevelResolver> resolverInstances;

    @Mock
    private AsynchronousResolverProcessor asynchronousResolver;

    @InjectMocks
    private DefaultSecurityLevelManager levelManager;

    @Mock
    Iterator<SecurityLevelResolver> iterator;

    @Before
    public void startUp(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void asynchronousLevelResolveTest() throws InterruptedException, ExecutionException{
        when(levelInstance.isUnsatisfied()).thenReturn(true);
        SecurityLevelResolver one = mock(SecurityLevelResolver.class);
        SecurityLevelResolver two = mock(SecurityLevelResolver.class);
        SecurityLevelResolver three = mock(SecurityLevelResolver.class);

        Future<Level> futureOne = (Future<Level>) mock(Future.class);
        when(futureOne.get()).thenReturn(new DefaultLevel(2));
        when(asynchronousResolver.processResolver(one)).thenReturn(futureOne);

        Future<Level> futureTwo = (Future<Level>) mock(Future.class);
        when(futureTwo.get()).thenReturn(new DefaultLevel(3));
        when(asynchronousResolver.processResolver(two)).thenReturn(futureTwo);

        Future<Level> futureThree = (Future<Level>) mock(Future.class);
        when(futureThree.get()).thenReturn(new DefaultLevel(1));
        when(asynchronousResolver.processResolver(three)).thenReturn(futureThree);

        when(iterator.hasNext()).thenReturn(true,true,true,false);
        when(iterator.next()).thenReturn(one,two,three);
        when(resolverInstances.iterator()).thenReturn(iterator);

        levelManager.init();
        assertTrue(levelManager.resolveSecurityLevel().compareTo(new DefaultLevel(3))==0);
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

        Future<Level> fut = (Future<Level>)mock(Future.class);
        when(fut.get()).thenThrow(ExecutionException.class);
        when(asynchronousResolver.processResolver(one)).thenReturn(fut);
        when(asynchronousResolver.processResolver(two)).thenReturn(mock(Future.class));
        when(asynchronousResolver.processResolver(three)).thenReturn(mock(Future.class));

        when(iterator.hasNext()).thenReturn(true,true,true,false,true,true,true,false);
        when(iterator.next()).thenReturn(one,two,three,one,two,three);
        when(resolverInstances.iterator()).thenReturn(iterator);

        levelManager.init();
        assertTrue(levelManager.resolveSecurityLevel().compareTo(new DefaultLevel(3))==0);
    }
}

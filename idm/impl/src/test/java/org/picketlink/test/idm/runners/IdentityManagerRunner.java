/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.picketlink.test.idm.runners;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.picketlink.idm.IdentityManager;

/**
 * <p>
 * Custom {@link Suite} JUnit4 Runner that allows to create test suites where the configured test cases share the same runtime
 * environment.
 * </p>
 * <p>
 * This runner expects that the test suite has a public static method that returns a {@link TestLifecycle}. The method name
 * should me init. Eg.: public static TestLifecycle init() {...}
 * </p>
 * <p>
 * Before each test method (for each test case) the {@link TestLifecycle} onInit method is called to perform some initialization
 * before the method is executed. The same goes when the test method is finished, the onDestroy method is called.
 * </p>
 * <p>
 * After the onInit method is called, this runner injects a {@link IdentityManager} into the current test case. Test cases
 * should have a setter that expects a {@link IdentityManager} instance.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class IdentityManagerRunner extends Suite {

    public IdentityManagerRunner(Class<?> klass, List<Runner> runners) throws InitializationError {
        super(klass, runners);
    }

    public IdentityManagerRunner(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(klass, builder);
    }

    public IdentityManagerRunner(RunnerBuilder builder, Class<?> klass, Class<?>[] suiteClasses) throws InitializationError {
        super(builder, klass, suiteClasses);
    }

    protected IdentityManagerRunner(RunnerBuilder builder, Class<?>[] classes) throws InitializationError {
        super(builder, classes);
    }

    protected IdentityManagerRunner(Class<?> klass, Class<?>[] suiteClasses) throws InitializationError {
        super(klass, suiteClasses);
    }

    protected void runChild(Runner runner, RunNotifier notifier) {
        try {
            final Class<?> suiteClass = getTestClass().getJavaClass();

            Method initFactoryMethod = suiteClass.getMethod("init", (Class<?>[]) null);
            final TestLifecycle lifecycle = (TestLifecycle) initFactoryMethod.invoke(null, (Object[]) null);

            super.runChild(new BlockJUnit4ClassRunner(runner.getDescription().getTestClass()) {

                @Override
                protected void runChild(FrameworkMethod method, RunNotifier notifier) {
                    lifecycle.onInit();
                    super.runChild(method, notifier);
                    lifecycle.onDestroy();
                }

                @Override
                protected Object createTest() throws Exception {
                    Object createTest = super.createTest();

                    try {
                        Method identityManagerSetter = createTest.getClass().getMethod("setIdentityManager",
                                new Class[] { IdentityManager.class });

                        identityManagerSetter.invoke(createTest, lifecycle.createIdentityManager());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return createTest;
                }
            }, notifier);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

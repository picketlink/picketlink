/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.picketlink.test.idm;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.IdentityManagerFactory;

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

                    Description description= describeChild(method);
                    EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);

                    if (!isExcludedSuite(suiteClass, method)) {
                        if (method.getAnnotation(Ignore.class) != null) {
                            eachNotifier.fireTestIgnored();
                            return;
                        }

                        eachNotifier.fireTestStarted();
                        
                        try {
                            methodBlock(method).evaluate();
                        } catch (AssumptionViolatedException e) {
                            eachNotifier.addFailedAssumption(e);
                        } catch (Throwable e) {
                            eachNotifier.addFailure(e);
                        } finally {
                            eachNotifier.fireTestFinished();
                        }
                    } else {
                        eachNotifier.fireTestIgnored();
                    }
                    
                    lifecycle.onDestroy();
                }

                private boolean isExcludedSuite(Class<?> suiteClass, FrameworkMethod method) {
                    ExcludeTestSuite annotation = method.getAnnotation(ExcludeTestSuite.class);
                    
                    if (annotation != null) {
                        List<Class<?>> excludedSuites = Arrays.asList(annotation.value());
                        
                        return excludedSuites.contains(suiteClass);
                    }
                    
                    return false;
                }

                @Override
                protected Object createTest() throws Exception {
                    Object createTest = super.createTest();

                    try {
                        Method identityManagerFactorySetter = createTest.getClass().getMethod("setIdentityManagerFactory", 
                                new Class[] { IdentityManagerFactory.class });

                        identityManagerFactorySetter.invoke(createTest, lifecycle.createIdentityManagerFactory());
                    } catch (Exception e) {
                        throw e;
                    }

                    return createTest;
                }
            }, notifier);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

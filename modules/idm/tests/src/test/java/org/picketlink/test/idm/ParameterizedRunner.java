package org.picketlink.test.idm;

import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.runners.Parameterized.Parameters;

/**
 * <p> The custom runner <code>Parameterized</code> implements parameterized tests. When running a parameterized test
 * class, instances are created for the cross-product of the test methods and the test data elements. </p> <p/> For
 * example, to test a Fibonacci function, write: <p/>
 * <pre>
 * &#064;RunWith(Parameterized.class)
 * public class FibonacciTest {
 * 	&#064;Parameters
 * 	public static List&lt;Object[]&gt; data() {
 * 		return Arrays.asList(new Object[][] {
 *            { 0, 0 }, { 1, 1 }, { 2, 1 }, { 3, 2 }, { 4, 3 }, { 5, 5 }, { 6, 8 }
 *        });
 *    }
 *
 * 	private int fInput;
 *
 * 	private int fExpected;
 *
 * 	public FibonacciTest(int input, int expected) {
 * 		fInput= input;
 * 		fExpected= expected;
 *    }
 *
 * 	&#064;Test
 * 	public void test() {
 * 		assertEquals(fExpected, Fibonacci.compute(fInput));
 *    }
 * }
 * </pre>
 * <p/> <p> Each instance of <code>FibonacciTest</code> will be constructed using the two-argument constructor and the
 * data values in the <code>&#064;Parameters</code> method. </p>
 */
public class ParameterizedRunner extends Suite {

    private class TestClassRunnerForParameters extends
            BlockJUnit4ClassRunner {
        private final int fParameterSetNumber;

        private final List<Object[]> fParameterList;

        TestClassRunnerForParameters(Class<?> type,
                                     List<Object[]> parameterList, int i) throws InitializationError {
            super(type);
            fParameterList = parameterList;
            fParameterSetNumber = i;
        }

        @Override
        protected void runChild(final FrameworkMethod method, final RunNotifier notifier) {
            Object currentConfig = fParameterList.get(fParameterSetNumber)[0];
            List<Class<?>> includes = getSupportedParameterTypes(method);

            if (!includes.contains(currentConfig.getClass())) {
                notifier.fireTestIgnored(describeChild(method));
                return;
            }

            super.runChild(method, notifier);
        }

        private List<Class<?>> getSupportedParameterTypes(FrameworkMethod method) {
            Configuration typeConfiguration = getTestClass().getJavaClass().getAnnotation(Configuration.class);
            Configuration methodConfiguration = method.getAnnotation(Configuration.class);
            List<Class<?>> includes = new ArrayList<Class<?>>();

            if (typeConfiguration != null) {
                includes.addAll(Arrays.asList(typeConfiguration.include()));
                includes.removeAll(Arrays.asList(typeConfiguration.exclude()));
            }

            if (methodConfiguration != null) {
                List<Class<? extends IdentityConfigurationTester>> methodIncludes = Arrays.asList(methodConfiguration.include());

                if (!methodIncludes.isEmpty()) {
                    includes.retainAll(methodIncludes);
                    includes.addAll(methodIncludes);
                }

                includes.removeAll(Arrays.asList(methodConfiguration.exclude()));
            }

            return includes;
        }

        @Override
        public Object createTest() throws Exception {
            return getTestClass().getOnlyConstructor().newInstance(
                    computeParams());
        }

        private Object[] computeParams() throws Exception {
            try {
                return fParameterList.get(fParameterSetNumber);
            } catch (ClassCastException e) {
                throw new Exception(String.format(
                        "%s.%s() must return a Collection of arrays.",
                        getTestClass().getName(), getParametersMethod(
                        getTestClass()).getName()));
            }
        }

        @Override
        protected String getName() {
            return String.format("[%s]", fParameterSetNumber);
        }

        @Override
        protected String testName(final FrameworkMethod method) {
            return String.format("%s[%s]", method.getName(),
                    fParameterSetNumber);
        }

        @Override
        protected void validateConstructor(List<Throwable> errors) {
            validateOnlyOneConstructor(errors);
        }

        @Override
        protected Statement classBlock(RunNotifier notifier) {
            return childrenInvoker(notifier);
        }

        @Override
        protected Annotation[] getRunnerAnnotations() {
            return new Annotation[0];
        }
    }

    private final ArrayList<Runner> runners = new ArrayList<Runner>();

    /**
     * Only called reflectively. Do not use programmatically.
     */
    public ParameterizedRunner(Class<?> klass) throws Throwable {
        super(klass, Collections.<Runner>emptyList());
        List<Object[]> parametersList = getParametersList(getTestClass());
        for (int i = 0; i < parametersList.size(); i++)
            runners.add(new TestClassRunnerForParameters(getTestClass().getJavaClass(),
                    parametersList, i));
    }

    @Override
    protected List<Runner> getChildren() {
        return runners;
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> getParametersList(TestClass klass)
            throws Throwable {
        return (List<Object[]>) getParametersMethod(klass).invokeExplosively(
                null);
    }

    private FrameworkMethod getParametersMethod(TestClass testClass)
            throws Exception {
        List<FrameworkMethod> methods = testClass
                .getAnnotatedMethods(Parameters.class);
        for (FrameworkMethod each : methods) {
            int modifiers = each.getMethod().getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers))
                return each;
        }

        throw new Exception("No public static parameters method on class "
                + testClass.getName());
    }

}

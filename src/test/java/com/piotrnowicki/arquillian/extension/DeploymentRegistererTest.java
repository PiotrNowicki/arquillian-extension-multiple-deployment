package com.piotrnowicki.arquillian.extension;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.text.MessageFormat;

import org.jboss.arquillian.test.spi.TestClass;
import org.junit.Before;
import org.junit.Test;

import com.piotrnowicki.arquillian.extension.TargetContainerName;
import com.piotrnowicki.arquillian.extension.DeploymentRegisterer.FlexibleRegisterDeployment;
import com.piotrnowicki.arquillian.extension.examples.MultipleConcreteDeployment;
import com.piotrnowicki.arquillian.extension.examples.MultipleDuplicatedConcreteDeployment;
import com.piotrnowicki.arquillian.extension.examples.MultiplePlainDeployment;
import com.piotrnowicki.arquillian.extension.examples.NoDeployment;
import com.piotrnowicki.arquillian.extension.examples.SingleConcreteDeployment;
import com.piotrnowicki.arquillian.extension.examples.SinglePlainDeployment;

public class DeploymentRegistererTest {

	private FlexibleRegisterDeployment cut;

	@Before
	public void before() {
		// Default. Fake Glassfish container on the classpath.
		cut = getCUTWithContainer(TargetContainerName.GLASSFISH);
	}

	@Test(expected = IllegalStateException.class)
	public void testNoContainerInClasspath() {

		// Default implementation checks the CP for container. No container in CP during testing.
		cut = new FlexibleRegisterDeployment();

		TestClass testClass = new TestClass(SinglePlainDeployment.class);
		cut.generate(testClass);
	}

	/*
	 * No @Deployment method in test class is not allowed.
	 */
	@Test(expected = IllegalStateException.class)
	public void testNoDeployment() {
		TestClass testClass = new TestClass(NoDeployment.class);
		cut.generate(testClass);
	}

	@Test
	public void testSinglePlainDeployment() {
		Method m = getDeploymentMethod(SinglePlainDeployment.class, "deploy");

		TestClass testClass = new TestClass(SinglePlainDeployment.class);
		cut.generate(testClass);

		assertEquals(m, cut.deployment);
	}

	@Test
	public void testSingleConcreteDeployment() {
		Method m = getDeploymentMethod(SingleConcreteDeployment.class, "deploy");

		TestClass testClass = new TestClass(SingleConcreteDeployment.class);

		// We want glassfish, and there is glassfish deployment, so it's OK.
		cut.generate(testClass);

		assertEquals(m, cut.deployment);
	}

	@Test(expected = IllegalStateException.class)
	public void testSingleConcreteDeploymentNotRequested() {
		// We have requested AS7 specific deployment.
		cut = getCUTWithContainer(TargetContainerName.JBOSSAS7);

		TestClass testClass = new TestClass(SingleConcreteDeployment.class);

		// We want AS7, but there is only a glassfish concrete deployment = exception.
		cut.generate(testClass);
	}

	@Test(expected = IllegalStateException.class)
	public void testMultiplePlainDeployment() {
		// TODO: Add support for recognizing such situations and choose one deployment.
		TestClass testClass = new TestClass(MultiplePlainDeployment.class);

		// We don't know which deployment to choose = exception.
		cut.generate(testClass);
	}

	@Test
	public void testMultipleConcreteDeployment() {
		TestClass testClass = new TestClass(MultipleConcreteDeployment.class);

		Method m = getDeploymentMethod(MultipleConcreteDeployment.class, "deploy2");

		cut.generate(testClass);

		assertEquals(m, cut.deployment);
	}

	@Test(expected = IllegalStateException.class)
	public void testMultipleDuplicatedConcreteDeployment() {
		TestClass testClass = new TestClass(MultipleDuplicatedConcreteDeployment.class);

		// We have two container-specific deployments and don't know which to choose = exception.
		cut.generate(testClass);
	}

	/*
	 * Get the deployment method for given Arquillian test case class.
	 */
	private final Method getDeploymentMethod(Class<?> clazz, String method) {
		MessageFormat msg = new MessageFormat("Couldn''t find deployment method \"{0}\" in \"{1}\"");
		String errorMsg = msg.format(new Object[] { method, clazz });

		try {
			Method result = clazz.getMethod(method, (Class<?>[]) null);

			return result;
		} catch (SecurityException e) {
			throw new RuntimeException(errorMsg);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(errorMsg);
		}

	}

	/*
	 * Nasty CUT mocking... Just shut your eyes the move along. Fakes the given container returned
	 * from the user classpath.
	 */
	private final FlexibleRegisterDeployment getCUTWithContainer(final TargetContainerName tcn) {
		FlexibleRegisterDeployment result = new FlexibleRegisterDeployment() {
			@Override
			TargetContainerName getTargetContainer() {
				return tcn;
			}
		};

		return result;
	}
}